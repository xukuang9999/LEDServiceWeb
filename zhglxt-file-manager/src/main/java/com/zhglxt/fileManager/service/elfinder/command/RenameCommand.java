package com.zhglxt.fileManager.service.elfinder.command;

import com.zhglxt.fileManager.domain.FileInfo;
import com.zhglxt.fileManager.domain.elfinder.ElFinderFile;
import com.zhglxt.fileManager.domain.elfinder.ElFinderRequest;
import com.zhglxt.fileManager.domain.elfinder.ElFinderResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * elFinder Rename Command
 * Renames files and directories
 * 
 * @author zhglxt
 */
@Component
public class RenameCommand extends AbstractElFinderCommand {

    @Override
    public String getCommandName() {
        return "rename";
    }

    @Override
    public boolean isValidRequest(ElFinderRequest request) {
        return StringUtils.hasText(request.getTarget()) && StringUtils.hasText(request.getName());
    }

    @Override
    public ElFinderResponse execute(ElFinderRequest request, String userId, HttpServletRequest httpRequest) {
        try {
            logger.info("Executing rename command for user: {}", userId);

            String targetHash = request.getTarget();
            String targetPath = decodePath(targetHash);
            String newName = request.getName();

            logger.debug("Renaming: '{}' to '{}'", targetPath, newName);

            boolean renamed = fileManagerService.rename(targetPath, newName, userId);
            
            if (!renamed) {
                return createErrorResponse("error.rename.failed", httpRequest, targetPath, newName);
            }

            ElFinderResponse response = createSuccessResponse();

            // Get the renamed file info
            String parentPath = getParentPath(targetPath);
            String newPath = StringUtils.hasText(parentPath) ? parentPath + "/" + newName : newName;
            
            FileInfo fileInfo = fileManagerService.getFileInfo(newPath, userId);
            if (fileInfo != null) {
                String parentHash = StringUtils.hasText(parentPath) ? encodePath(parentPath) : hashUtil.getRootHash();
                ElFinderFile elFile = convertToElFinderFile(fileInfo, parentHash);
                
                List<ElFinderFile> added = new ArrayList<>();
                added.add(elFile);
                response.setAdded(added);
                
                List<String> removed = new ArrayList<>();
                removed.add(targetHash);
                response.setRemoved(removed);
            }

            logger.info("Rename command completed successfully: {} -> {}", targetPath, newName);
            return response;

        } catch (Exception e) {
            logger.error("Error executing rename command: {}", e.getMessage(), e);
            return createErrorResponse("error.rename.failed", httpRequest, e.getMessage());
        }
    }
}