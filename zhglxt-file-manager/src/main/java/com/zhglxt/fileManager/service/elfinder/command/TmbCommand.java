package com.zhglxt.fileManager.service.elfinder.command;

import com.zhglxt.fileManager.domain.ThumbnailInfo;
import com.zhglxt.fileManager.domain.elfinder.ElFinderRequest;
import com.zhglxt.fileManager.domain.elfinder.ElFinderResponse;
import com.zhglxt.fileManager.service.ThumbnailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * elFinder Thumbnail Command
 * Generates and returns thumbnails for images
 * 
 * @author zhglxt
 */
@Component
public class TmbCommand extends AbstractElFinderCommand {

    @Autowired
    private ThumbnailService thumbnailService;

    @Override
    public String getCommandName() {
        return "tmb";
    }

    @Override
    public boolean isValidRequest(ElFinderRequest request) {
        return request.getTargets() != null && !request.getTargets().isEmpty();
    }

    @Override
    public ElFinderResponse execute(ElFinderRequest request, String userId, HttpServletRequest httpRequest) {
        try {
            logger.info("Executing tmb command for user: {}", userId);

            ElFinderResponse response = createSuccessResponse();
            Map<String, String> thumbnails = new HashMap<>();

            for (String targetHash : request.getTargets()) {
                String targetPath = decodePath(targetHash);
                
                try {
                    String thumbnailPath = thumbnailService.generateThumbnail(targetPath, 
                        com.zhglxt.fileManager.domain.config.ThumbnailConfig.ThumbnailSize.SMALL);
                    if (thumbnailPath != null) {
                        thumbnails.put(targetHash, thumbnailPath);
                        logger.debug("Generated thumbnail for: {}", targetPath);
                    } else {
                        logger.debug("Could not generate thumbnail for: {}", targetPath);
                    }
                } catch (Exception e) {
                    logger.warn("Error generating thumbnail for {}: {}", targetPath, e.getMessage());
                }
            }

            response.addData("images", thumbnails);

            logger.info("Tmb command completed. Thumbnails: {}", thumbnails.size());
            return response;

        } catch (Exception e) {
            logger.error("Error executing tmb command: {}", e.getMessage(), e);
            return createErrorResponse("error.tmb.failed", httpRequest, e.getMessage());
        }
    }
}