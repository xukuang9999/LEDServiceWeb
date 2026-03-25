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
 * elFinder Mkdir Command
 * Creates new directories
 * 
 * @author zhglxt
 */
@Component
public class MkdirCommand extends AbstractElFinderCommand {

    @Override
    public String getCommandName() {
        return "mkdir";
    }

    @Override
    public boolean isValidRequest(ElFinderRequest request) {
        // Mkdir requires parent target and directory name
        return StringUtils.hasText(request.getTarget()) && StringUtils.hasText(request.getName());
    }

    @Override
    public ElFinderResponse execute(ElFinderRequest request, String userId, HttpServletRequest httpRequest) {
        try {
            logger.info("Executing mkdir command for user: {}", userId);

            String parentHash = request.getTarget();
            String parentPath = decodePath(parentHash);
            String dirName = request.getName();

            logger.debug("Creating directory: '{}' in parent: '{}' (hash: '{}')", 
                        dirName, parentPath, parentHash);

            // Construct new directory path
            String newDirPath = StringUtils.hasText(parentPath) ? 
                parentPath + "/" + dirName : dirName;

            // Create the directory
            boolean created = fileManagerService.createDirectory(newDirPath, userId);
            
            if (!created) {
                return createErrorResponse("error.mkdir.failed", httpRequest, dirName);
            }

            ElFinderResponse response = createSuccessResponse();

            // Get the created directory info
            FileInfo dirInfo = fileManagerService.getFileInfo(newDirPath, userId);
            if (dirInfo != null) {
                ElFinderFile elFile = convertToElFinderFile(dirInfo, parentHash);
                
                List<ElFinderFile> addedFiles = new ArrayList<>();
                addedFiles.add(elFile);
                response.setAdded(addedFiles);
                
                logger.info("Directory created successfully: {}", newDirPath);
            } else {
                logger.warn("Directory created but could not retrieve info: {}", newDirPath);
            }

            return response;

        } catch (Exception e) {
            logger.error("Error executing mkdir command: {}", e.getMessage(), e);
            return createErrorResponse("error.mkdir.failed", httpRequest, e.getMessage());
        }
    }
}