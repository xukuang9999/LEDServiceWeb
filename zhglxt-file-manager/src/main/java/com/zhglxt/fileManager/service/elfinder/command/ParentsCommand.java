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
 * elFinder Parents Command
 * Returns parent directories for tree navigation
 * 
 * @author zhglxt
 */
@Component
public class ParentsCommand extends AbstractElFinderCommand {

    @Override
    public String getCommandName() {
        return "parents";
    }

    @Override
    public boolean isValidRequest(ElFinderRequest request) {
        return StringUtils.hasText(request.getTarget());
    }

    @Override
    public ElFinderResponse execute(ElFinderRequest request, String userId, HttpServletRequest httpRequest) {
        try {
            logger.info("Executing parents command for user: {}", userId);

            String targetHash = request.getTarget();
            String targetPath = decodePath(targetHash);

            logger.debug("Getting parents for: '{}' (hash: '{}')", targetPath, targetHash);

            ElFinderResponse response = createSuccessResponse();
            List<ElFinderFile> parents = new ArrayList<>();

            // Build parent chain
            String currentPath = targetPath;
            while (StringUtils.hasText(currentPath)) {
                String parentPath = getParentPath(currentPath);
                
                FileInfo parentInfo = StringUtils.hasText(parentPath) ? 
                    fileManagerService.getFileInfo(parentPath, userId) : 
                    createRootDirectoryInfo();
                
                if (parentInfo != null) {
                    String parentParentHash = StringUtils.hasText(parentPath) ? 
                        encodePath(getParentPath(parentPath)) : null;
                    ElFinderFile parentFile = convertToElFinderFile(parentInfo, parentParentHash);
                    parents.add(0, parentFile); // Add to beginning
                }
                
                currentPath = parentPath;
            }

            response.setFiles(parents);

            logger.info("Parents command completed. Parents: {}", parents.size());
            return response;

        } catch (Exception e) {
            logger.error("Error executing parents command: {}", e.getMessage(), e);
            return createErrorResponse("error.parents.failed", httpRequest, e.getMessage());
        }
    }

    private FileInfo createRootDirectoryInfo() {
        FileInfo rootInfo = new FileInfo();
        rootInfo.setName("Root");
        rootInfo.setPath("");
        rootInfo.setDirectory(true);
        rootInfo.setSize(0);
        rootInfo.setLastModified(java.time.LocalDateTime.now());
        rootInfo.setMimeType("directory");
        rootInfo.setReadable(true);
        rootInfo.setWritable(true);
        return rootInfo;
    }
}