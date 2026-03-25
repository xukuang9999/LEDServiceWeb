package com.zhglxt.fileManager.service.elfinder.command;

import com.zhglxt.fileManager.domain.FileInfo;
import com.zhglxt.fileManager.domain.elfinder.ElFinderFile;
import com.zhglxt.fileManager.domain.elfinder.ElFinderRequest;
import com.zhglxt.fileManager.domain.elfinder.ElFinderResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * elFinder Tree Command
 * Returns directory tree structure
 * 
 * @author zhglxt
 */
@Component
public class TreeCommand extends AbstractElFinderCommand {

    @Override
    public String getCommandName() {
        return "tree";
    }

    @Override
    public boolean isValidRequest(ElFinderRequest request) {
        // Tree command requires target directory
        return StringUtils.hasText(request.getTarget());
    }

    @Override
    public ElFinderResponse execute(ElFinderRequest request, String userId, HttpServletRequest httpRequest) {
        try {
            logger.info("Executing tree command for user: {}", userId);

            String targetHash = request.getTarget();
            String targetPath = decodePath(targetHash);

            logger.debug("Getting tree for directory: '{}' (hash: '{}')", targetPath, targetHash);

            ElFinderResponse response = createSuccessResponse();

            // Get directory contents
            List<FileInfo> fileInfos = fileManagerService.listFiles(targetPath, userId);
            
            // Filter only directories for tree view
            List<FileInfo> directories = fileInfos.stream()
                .filter(FileInfo::isDirectory)
                .toList();

            // Convert to elFinder files
            List<ElFinderFile> elFinderDirs = convertToElFinderFiles(directories, targetHash);
            
            // For each directory, check if it has subdirectories
            for (ElFinderFile dir : elFinderDirs) {
                String dirPath = decodePath(dir.getHash());
                List<FileInfo> subFiles = fileManagerService.listFiles(dirPath, userId);
                
                // Check if directory has subdirectories
                boolean hasSubdirs = subFiles.stream().anyMatch(FileInfo::isDirectory);
                dir.setHasSubdirectories(hasSubdirs ? 1 : 0);
            }

            // Set tree data
            response.setFiles(elFinderDirs);

            logger.info("Tree command completed successfully. Directories: {}", elFinderDirs.size());

            return response;

        } catch (Exception e) {
            logger.error("Error executing tree command: {}", e.getMessage(), e);
            return createErrorResponse("error.tree.failed", httpRequest, e.getMessage());
        }
    }
}