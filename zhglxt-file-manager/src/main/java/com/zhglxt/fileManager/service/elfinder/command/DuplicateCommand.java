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
 * elFinder Duplicate Command
 * Duplicates files and directories
 * 
 * @author zhglxt
 */
@Component
public class DuplicateCommand extends AbstractElFinderCommand {

    @Override
    public String getCommandName() {
        return "duplicate";
    }

    @Override
    public boolean isValidRequest(ElFinderRequest request) {
        return request.getTargets() != null && !request.getTargets().isEmpty();
    }

    @Override
    public ElFinderResponse execute(ElFinderRequest request, String userId, HttpServletRequest httpRequest) {
        try {
            logger.info("Executing duplicate command for user: {}", userId);

            ElFinderResponse response = createSuccessResponse();
            List<ElFinderFile> added = new ArrayList<>();
            List<String> errors = new ArrayList<>();

            for (String targetHash : request.getTargets()) {
                String targetPath = decodePath(targetHash);
                String parentPath = getParentPath(targetPath);
                String fileName = getFileName(targetPath);
                
                // Generate duplicate name
                String duplicateName = generateDuplicateName(fileName, parentPath, userId);
                String duplicatePath = StringUtils.hasText(parentPath) ? 
                    parentPath + "/" + duplicateName : duplicateName;

                boolean copied = fileManagerService.copyFile(targetPath, duplicatePath, userId);
                if (copied) {
                    FileInfo fileInfo = fileManagerService.getFileInfo(duplicatePath, userId);
                    if (fileInfo != null) {
                        String parentHash = StringUtils.hasText(parentPath) ? 
                            encodePath(parentPath) : hashUtil.getRootHash();
                        ElFinderFile elFile = convertToElFinderFile(fileInfo, parentHash);
                        added.add(elFile);
                    }
                } else {
                    errors.add("Failed to duplicate: " + targetPath);
                }
            }

            response.setAdded(added);
            if (!errors.isEmpty()) {
                response.setError(errors);
            }

            logger.info("Duplicate command completed. Added: {}, Errors: {}", added.size(), errors.size());
            return response;

        } catch (Exception e) {
            logger.error("Error executing duplicate command: {}", e.getMessage(), e);
            return createErrorResponse("error.duplicate.failed", httpRequest, e.getMessage());
        }
    }

    private String generateDuplicateName(String fileName, String parentPath, String userId) {
        String baseName = fileName;
        String extension = "";
        
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            baseName = fileName.substring(0, lastDot);
            extension = fileName.substring(lastDot);
        }
        
        int counter = 1;
        String duplicateName;
        do {
            duplicateName = baseName + " copy" + (counter > 1 ? " " + counter : "") + extension;
            String duplicatePath = StringUtils.hasText(parentPath) ? 
                parentPath + "/" + duplicateName : duplicateName;
            
            if (!fileManagerService.fileExists(duplicatePath, userId)) {
                break;
            }
            counter++;
        } while (counter < 100); // Prevent infinite loop
        
        return duplicateName;
    }
}