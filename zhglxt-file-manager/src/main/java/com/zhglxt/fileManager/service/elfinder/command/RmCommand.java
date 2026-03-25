package com.zhglxt.fileManager.service.elfinder.command;

import com.zhglxt.fileManager.domain.elfinder.ElFinderRequest;
import com.zhglxt.fileManager.domain.elfinder.ElFinderResponse;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * elFinder Remove Command
 * Deletes files and directories
 * 
 * @author zhglxt
 */
@Component
public class RmCommand extends AbstractElFinderCommand {

    @Override
    public String getCommandName() {
        return "rm";
    }

    @Override
    public boolean isValidRequest(ElFinderRequest request) {
        return request.getTargets() != null && !request.getTargets().isEmpty();
    }

    @Override
    public ElFinderResponse execute(ElFinderRequest request, String userId, HttpServletRequest httpRequest) {
        try {
            logger.info("Executing rm command for user: {}", userId);

            ElFinderResponse response = createSuccessResponse();
            List<String> removed = new ArrayList<>();
            List<String> errors = new ArrayList<>();

            for (String targetHash : request.getTargets()) {
                String targetPath = decodePath(targetHash);
                
                boolean deleted = fileManagerService.deleteFile(targetPath, userId);
                if (deleted) {
                    removed.add(targetHash);
                } else {
                    errors.add("Failed to delete: " + targetPath);
                }
            }

            response.setRemoved(removed);
            if (!errors.isEmpty()) {
                response.setError(errors);
            }

            logger.info("Rm command completed. Removed: {}, Errors: {}", removed.size(), errors.size());
            return response;

        } catch (Exception e) {
            logger.error("Error executing rm command: {}", e.getMessage(), e);
            return createErrorResponse("error.rm.failed", httpRequest, e.getMessage());
        }
    }
}