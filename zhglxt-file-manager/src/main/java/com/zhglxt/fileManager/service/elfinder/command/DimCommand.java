package com.zhglxt.fileManager.service.elfinder.command;

import com.zhglxt.fileManager.domain.FileInfo;
import com.zhglxt.fileManager.domain.elfinder.ElFinderRequest;
import com.zhglxt.fileManager.domain.elfinder.ElFinderResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;

/**
 * elFinder Dim Command
 * Returns image dimensions
 * 
 * @author zhglxt
 */
@Component
public class DimCommand extends AbstractElFinderCommand {

    @Override
    public String getCommandName() {
        return "dim";
    }

    @Override
    public boolean isValidRequest(ElFinderRequest request) {
        return StringUtils.hasText(request.getTarget());
    }

    @Override
    public ElFinderResponse execute(ElFinderRequest request, String userId, HttpServletRequest httpRequest) {
        try {
            logger.info("Executing dim command for user: {}", userId);

            String targetHash = request.getTarget();
            String targetPath = decodePath(targetHash);

            logger.debug("Getting dimensions for: '{}'", targetPath);

            FileInfo fileInfo = fileManagerService.getFileInfo(targetPath, userId);
            if (fileInfo == null || !fileInfo.isImage()) {
                return createErrorResponse("error.dim.not.image", httpRequest);
            }

            ElFinderResponse response = createSuccessResponse();
            
            // Get dimensions from metadata
            if (fileInfo.getMetadata() != null) {
                Object width = fileInfo.getMetadata().get("width");
                Object height = fileInfo.getMetadata().get("height");
                
                if (width != null && height != null) {
                    response.addData("dim", width + "x" + height);
                } else {
                    response.addData("dim", "0x0");
                }
            } else {
                response.addData("dim", "0x0");
            }

            logger.info("Dim command completed for: {}", targetPath);
            return response;

        } catch (Exception e) {
            logger.error("Error executing dim command: {}", e.getMessage(), e);
            return createErrorResponse("error.dim.failed", httpRequest, e.getMessage());
        }
    }
}