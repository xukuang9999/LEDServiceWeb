package com.zhglxt.fileManager.service.streaming.impl;

import com.zhglxt.fileManager.service.streaming.FileStreamingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Default implementation of file streaming service
 * 
 * @author zhglxt
 */
@Service
public class DefaultFileStreamingService implements FileStreamingService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultFileStreamingService.class);

    private static final int DEFAULT_BUFFER_SIZE = 8192; // 8KB
    private static final int LARGE_FILE_BUFFER_SIZE = 65536; // 64KB
    private static final long LARGE_FILE_THRESHOLD = 10 * 1024 * 1024; // 10MB

    @Override
    public void streamFile(String filePath, HttpServletRequest request, HttpServletResponse response) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        File file = path.toFile();
        long fileSize = file.length();
        String fileName = file.getName();

        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis)) {
            
            streamFile(bis, fileName, fileSize, request, response);
        }
    }

    @Override
    public void streamFile(InputStream inputStream, String fileName, long fileSize, 
                          HttpServletRequest request, HttpServletResponse response) throws IOException {
        
        String rangeHeader = request.getHeader("Range");
        
        // Set basic headers
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        
        // Determine content type
        String contentType = getContentType(fileName);
        response.setContentType(contentType);

        if (rangeHeader != null && isValidRange(rangeHeader, fileSize)) {
            // Handle range request
            handleRangeRequest(inputStream, rangeHeader, fileSize, response);
        } else {
            // Handle full file request
            handleFullFileRequest(inputStream, fileSize, response);
        }
    }

    @Override
    public ResponseEntity<Resource> createStreamingResponse(Resource resource, HttpServletRequest request) {
        try {
            long fileSize = resource.contentLength();
            String rangeHeader = request.getHeader("Range");

            HttpHeaders headers = new HttpHeaders();
            headers.add("Accept-Ranges", "bytes");
            
            String filename = resource.getFilename();
            if (filename != null) {
                headers.add("Content-Disposition", "attachment; filename=\"" + filename + "\"");
            }

            if (rangeHeader != null && isValidRange(rangeHeader, fileSize)) {
                RangeInfo range = parseRange(rangeHeader, fileSize);
                
                headers.add("Content-Range", range.getContentRange());
                headers.add("Content-Length", String.valueOf(range.getLength()));
                
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .headers(headers)
                    .body(resource);
            } else {
                headers.add("Content-Length", String.valueOf(fileSize));
                
                return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
            }
        } catch (IOException e) {
            logger.error("Error creating streaming response for resource: {}", resource.getDescription(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public int getOptimalBufferSize(long fileSize) {
        return fileSize > LARGE_FILE_THRESHOLD ? LARGE_FILE_BUFFER_SIZE : DEFAULT_BUFFER_SIZE;
    }

    @Override
    public boolean isValidRange(String rangeHeader, long fileSize) {
        if (!StringUtils.hasText(rangeHeader) || !rangeHeader.startsWith("bytes=")) {
            return false;
        }

        try {
            String range = rangeHeader.substring(6);
            String[] parts = range.split("-");
            
            if (parts.length == 1) {
                // Range like "bytes=100-" or "bytes=-100"
                if (range.startsWith("-")) {
                    long suffix = Long.parseLong(parts[0].substring(1));
                    return suffix > 0 && suffix <= fileSize;
                } else {
                    long start = Long.parseLong(parts[0]);
                    return start >= 0 && start < fileSize;
                }
            } else if (parts.length == 2) {
                // Range like "bytes=100-200"
                long start = Long.parseLong(parts[0]);
                long end = Long.parseLong(parts[1]);
                return start >= 0 && end >= start && end < fileSize;
            }
            
            return false;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public RangeInfo parseRange(String rangeHeader, long fileSize) {
        String range = rangeHeader.substring(6);
        String[] parts = range.split("-");
        
        long start, end;
        
        if (parts.length == 1) {
            if (range.startsWith("-")) {
                // Suffix range like "bytes=-100"
                long suffix = Long.parseLong(parts[0].substring(1));
                start = Math.max(0, fileSize - suffix);
                end = fileSize - 1;
            } else {
                // Start range like "bytes=100-"
                start = Long.parseLong(parts[0]);
                end = fileSize - 1;
            }
        } else {
            // Full range like "bytes=100-200"
            start = Long.parseLong(parts[0]);
            end = Long.parseLong(parts[1]);
        }
        
        // Ensure end doesn't exceed file size
        end = Math.min(end, fileSize - 1);
        
        return new RangeInfo(start, end, fileSize);
    }

    private void handleRangeRequest(InputStream inputStream, String rangeHeader, long fileSize, 
                                   HttpServletResponse response) throws IOException {
        
        RangeInfo range = parseRange(rangeHeader, fileSize);
        
        response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        response.setHeader("Content-Range", range.getContentRange());
        response.setHeader("Content-Length", String.valueOf(range.getLength()));
        
        // Skip to start position
        long skipped = 0;
        while (skipped < range.getStart()) {
            long toSkip = Math.min(range.getStart() - skipped, DEFAULT_BUFFER_SIZE);
            long actuallySkipped = inputStream.skip(toSkip);
            if (actuallySkipped <= 0) {
                break;
            }
            skipped += actuallySkipped;
        }
        
        // Stream the requested range
        streamBytes(inputStream, response.getOutputStream(), range.getLength(), 
                   getOptimalBufferSize(range.getLength()));
    }

    private void handleFullFileRequest(InputStream inputStream, long fileSize, 
                                     HttpServletResponse response) throws IOException {
        
        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader("Content-Length", String.valueOf(fileSize));
        
        streamBytes(inputStream, response.getOutputStream(), fileSize, 
                   getOptimalBufferSize(fileSize));
    }

    private void streamBytes(InputStream inputStream, OutputStream outputStream, 
                           long totalBytes, int bufferSize) throws IOException {
        
        byte[] buffer = new byte[bufferSize];
        long bytesStreamed = 0;
        
        while (bytesStreamed < totalBytes) {
            int bytesToRead = (int) Math.min(buffer.length, totalBytes - bytesStreamed);
            int bytesRead = inputStream.read(buffer, 0, bytesToRead);
            
            if (bytesRead == -1) {
                break;
            }
            
            outputStream.write(buffer, 0, bytesRead);
            bytesStreamed += bytesRead;
            
            // Flush periodically for large files
            if (bytesStreamed % (bufferSize * 10) == 0) {
                outputStream.flush();
            }
        }
        
        outputStream.flush();
    }

    private String getContentType(String fileName) {
        try {
            Path path = Paths.get(fileName);
            String contentType = Files.probeContentType(path);
            return contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE;
        } catch (Exception e) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
    }
}