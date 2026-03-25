package com.zhglxt.fileManager.service.elfinder.command;

import com.zhglxt.fileManager.domain.FileInfo;
import com.zhglxt.fileManager.domain.FileUploadResult;
import com.zhglxt.fileManager.domain.elfinder.ElFinderFile;
import com.zhglxt.fileManager.domain.elfinder.ElFinderRequest;
import com.zhglxt.fileManager.domain.elfinder.ElFinderResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * elFinder Put Command
 * Saves file content after editing
 * 
 * @author zhglxt
 */
@Component
public class PutCommand extends AbstractElFinderCommand {

    @Override
    public String getCommandName() {
        return "put";
    }

    @Override
    public boolean isValidRequest(ElFinderRequest request) {
        return StringUtils.hasText(request.getTarget()) && 
               StringUtils.hasText(request.getContent());
    }

    @Override
    public ElFinderResponse execute(ElFinderRequest request, String userId, HttpServletRequest httpRequest) {
        try {
            logger.info("Executing put command for user: {}", userId);

            String targetHash = request.getTarget();
            String targetPath = decodePath(targetHash);
            String content = request.getContent();
            String encoding = StringUtils.hasText(request.getEncoding()) ? 
                request.getEncoding() : "UTF-8";

            logger.debug("Saving content to file: '{}' (encoding: {})", targetPath, encoding);

            // Delete existing file first
            fileManagerService.deleteFile(targetPath, userId);

            // Create new file with content
            String fileName = getFileName(targetPath);
            String parentPath = getParentPath(targetPath);
            
            byte[] contentBytes = content.getBytes(encoding);
            MultipartFile file = new SimpleMultipartFile(fileName, "text/plain", contentBytes);

            FileUploadResult uploadResult = fileManagerService.uploadFile(file, parentPath, userId);
            
            if (!uploadResult.isSuccess()) {
                return createErrorResponse("error.put.failed", httpRequest, 
                                         String.join(", ", uploadResult.getErrors()));
            }

            ElFinderResponse response = createSuccessResponse();

            // Get updated file info
            FileInfo fileInfo = fileManagerService.getFileInfo(uploadResult.getFilePath(), userId);
            if (fileInfo != null) {
                String parentHash = StringUtils.hasText(parentPath) ? 
                    encodePath(parentPath) : hashUtil.getRootHash();
                ElFinderFile elFile = convertToElFinderFile(fileInfo, parentHash);
                
                List<ElFinderFile> changed = new ArrayList<>();
                changed.add(elFile);
                response.setChanged(changed);
            }

            logger.info("Put command completed successfully for file: {}", targetPath);
            return response;

        } catch (Exception e) {
            logger.error("Error executing put command: {}", e.getMessage(), e);
            return createErrorResponse("error.put.failed", httpRequest, e.getMessage());
        }
    }

    /**
     * Simple MultipartFile implementation for text content
     */
    private static class SimpleMultipartFile implements MultipartFile {
        private final String name;
        private final String contentType;
        private final byte[] content;

        public SimpleMultipartFile(String name, String contentType, byte[] content) {
            this.name = name;
            this.contentType = contentType;
            this.content = content;
        }

        @Override
        public String getName() {
            return "file";
        }

        @Override
        public String getOriginalFilename() {
            return name;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return content.length == 0;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @Override
        public byte[] getBytes() throws IOException {
            return content;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
            throw new UnsupportedOperationException("transferTo not supported");
        }
    }}
