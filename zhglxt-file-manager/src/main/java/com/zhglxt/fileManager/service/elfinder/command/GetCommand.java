package com.zhglxt.fileManager.service.elfinder.command;

import com.zhglxt.fileManager.domain.FileDownloadResult;
import com.zhglxt.fileManager.domain.elfinder.ElFinderRequest;
import com.zhglxt.fileManager.domain.elfinder.ElFinderResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.io.InputStream;

/**
 * elFinder Get Command
 * Gets file content for editing
 * 
 * @author zhglxt
 */
@Component
public class GetCommand extends AbstractElFinderCommand {

    @Override
    public String getCommandName() {
        return "get";
    }

    @Override
    public boolean isValidRequest(ElFinderRequest request) {
        return StringUtils.hasText(request.getTarget());
    }

    @Override
    public ElFinderResponse execute(ElFinderRequest request, String userId, HttpServletRequest httpRequest) {
        try {
            logger.info("Executing get command for user: {}", userId);

            String targetHash = request.getTarget();
            String targetPath = decodePath(targetHash);

            logger.debug("Getting content for file: '{}'", targetPath);

            FileDownloadResult downloadResult = fileManagerService.downloadFile(targetPath, userId);
            
            if (!downloadResult.isSuccess()) {
                return createErrorResponse("error.get.failed", httpRequest, 
                                         downloadResult.getErrorMessage());
            }

            // Read file content (limit to reasonable size for editing)
            String content;
            try (InputStream inputStream = downloadResult.getInputStream()) {
                if (downloadResult.getFileSize() > 1024 * 1024) { // 1MB limit
                    return createErrorResponse("error.get.file.too.large", httpRequest);
                }
                
                byte[] bytes = inputStream.readAllBytes();
                content = new String(bytes, "UTF-8");
            }

            ElFinderResponse response = createSuccessResponse();
            response.addData("content", content);

            logger.info("Get command completed successfully for file: {}", targetPath);
            return response;

        } catch (Exception e) {
            logger.error("Error executing get command: {}", e.getMessage(), e);
            return createErrorResponse("error.get.failed", httpRequest, e.getMessage());
        }
    }
}