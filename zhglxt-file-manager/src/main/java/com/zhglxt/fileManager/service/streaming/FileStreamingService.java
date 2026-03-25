package com.zhglxt.fileManager.service.streaming;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

/**
 * File Streaming Service Interface
 * Optimizes file streaming for large file downloads
 * 
 * @author zhglxt
 */
public interface FileStreamingService {

    /**
     * Stream file with range support for large files
     * 
     * @param filePath path to the file
     * @param request HTTP request
     * @param response HTTP response
     * @throws IOException if streaming fails
     */
    void streamFile(String filePath, HttpServletRequest request, HttpServletResponse response) throws IOException;

    /**
     * Stream file from input stream with range support
     * 
     * @param inputStream file input stream
     * @param fileName file name for content disposition
     * @param fileSize total file size
     * @param request HTTP request
     * @param response HTTP response
     * @throws IOException if streaming fails
     */
    void streamFile(InputStream inputStream, String fileName, long fileSize, 
                   HttpServletRequest request, HttpServletResponse response) throws IOException;

    /**
     * Create streaming response entity with range support
     * 
     * @param resource file resource
     * @param request HTTP request
     * @return response entity with appropriate headers
     */
    ResponseEntity<Resource> createStreamingResponse(Resource resource, HttpServletRequest request);

    /**
     * Get optimal buffer size for streaming
     * 
     * @param fileSize total file size
     * @return optimal buffer size in bytes
     */
    int getOptimalBufferSize(long fileSize);

    /**
     * Check if range request is valid
     * 
     * @param rangeHeader range header value
     * @param fileSize total file size
     * @return true if range is valid
     */
    boolean isValidRange(String rangeHeader, long fileSize);

    /**
     * Parse range header
     * 
     * @param rangeHeader range header value
     * @param fileSize total file size
     * @return range information
     */
    RangeInfo parseRange(String rangeHeader, long fileSize);

    /**
     * Range information for partial content requests
     */
    class RangeInfo {
        private final long start;
        private final long end;
        private final long length;
        private final long totalSize;

        public RangeInfo(long start, long end, long totalSize) {
            this.start = start;
            this.end = end;
            this.totalSize = totalSize;
            this.length = end - start + 1;
        }

        public long getStart() {
            return start;
        }

        public long getEnd() {
            return end;
        }

        public long getLength() {
            return length;
        }

        public long getTotalSize() {
            return totalSize;
        }

        public String getContentRange() {
            return String.format("bytes %d-%d/%d", start, end, totalSize);
        }
    }
}