package com.zhglxt.fileManager.service.elfinder.command;

import com.zhglxt.fileManager.domain.FileDownloadResult;
import com.zhglxt.fileManager.domain.elfinder.ElFinderRequest;
import com.zhglxt.fileManager.domain.elfinder.ElFinderResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * elFinder File Command
 * Serves file content for download or preview
 * 
 * @author zhglxt
 */
@Component
public class FileCommand extends AbstractElFinderCommand {

    @Override
    public String getCommandName() {
        return "file";
    }

    @Override
    public boolean isValidRequest(ElFinderRequest request) {
        // File command requires target file
        return StringUtils.hasText(request.getTarget());
    }

    @Override
    public ElFinderResponse execute(ElFinderRequest request, String userId, HttpServletRequest httpRequest) {
        try {
            logger.info("Executing file command for user: {}", userId);

            String targetHash = request.getTarget();
            String targetPath = decodePath(targetHash);

            logger.debug("Serving file: '{}' (hash: '{}')", targetPath, targetHash);

            // Get file download result
            FileDownloadResult downloadResult = fileManagerService.downloadFile(targetPath, userId);
            
            if (!downloadResult.isSuccess()) {
                return createErrorResponse("error.file.download.failed", httpRequest, 
                                         downloadResult.getErrorMessage());
            }

            // Get HTTP response from request attributes (set by controller)
            HttpServletResponse httpResponse = (HttpServletResponse) httpRequest.getAttribute("httpResponse");
            if (httpResponse == null) {
                return createErrorResponse("error.file.response.unavailable", httpRequest);
            }

            // Set response headers
            httpResponse.setContentType(downloadResult.getMimeType());
            httpResponse.setContentLengthLong(downloadResult.getFileSize());
            
            // Set content disposition
            String disposition = downloadResult.getContentDisposition();
            if (StringUtils.hasText(disposition)) {
                httpResponse.setHeader("Content-Disposition", 
                    disposition + "; filename=\"" + downloadResult.getFileName() + "\"");
            }

            // Set cache headers for images and static content
            if (downloadResult.getMimeType().startsWith("image/")) {
                httpResponse.setHeader("Cache-Control", "public, max-age=3600");
                httpResponse.setDateHeader("Expires", System.currentTimeMillis() + 3600000);
            }

            // Stream file content
            try (InputStream inputStream = downloadResult.getInputStream();
                 OutputStream outputStream = httpResponse.getOutputStream()) {
                
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
            }

            logger.info("File served successfully: {}", targetPath);

            // Return null to indicate that response has been handled directly
            return null;

        } catch (IOException e) {
            logger.error("IO error serving file: {}", e.getMessage(), e);
            return createErrorResponse("error.file.io.failed", httpRequest, e.getMessage());
        } catch (Exception e) {
            logger.error("Error executing file command: {}", e.getMessage(), e);
            return createErrorResponse("error.file.failed", httpRequest, e.getMessage());
        }
    }
}