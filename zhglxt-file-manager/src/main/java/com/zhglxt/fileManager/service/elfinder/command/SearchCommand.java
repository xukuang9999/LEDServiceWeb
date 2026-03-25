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
 * elFinder Search Command
 * Searches for files by name pattern
 * 
 * @author zhglxt
 */
@Component
public class SearchCommand extends AbstractElFinderCommand {

    @Override
    public String getCommandName() {
        return "search";
    }

    @Override
    public boolean isValidRequest(ElFinderRequest request) {
        return StringUtils.hasText(request.getQ());
    }

    @Override
    public ElFinderResponse execute(ElFinderRequest request, String userId, HttpServletRequest httpRequest) {
        try {
            logger.info("Executing search command for user: {}", userId);

            String query = request.getQ();
            String targetHash = request.getTarget();
            String searchPath = StringUtils.hasText(targetHash) ? decodePath(targetHash) : "";

            logger.debug("Searching for: '{}' in path: '{}'", query, searchPath);

            List<FileInfo> searchResults = fileManagerService.searchFiles(searchPath, query, userId, true);
            
            // Convert to elFinder files
            List<ElFinderFile> elFinderFiles = searchResults.stream()
                .map(fileInfo -> {
                    String parentPath = getParentPath(fileInfo.getPath());
                    String parentHash = StringUtils.hasText(parentPath) ? 
                        encodePath(parentPath) : hashUtil.getRootHash();
                    return convertToElFinderFile(fileInfo, parentHash);
                })
                .toList();

            ElFinderResponse response = createSuccessResponse();
            response.setFiles(elFinderFiles);

            logger.info("Search command completed. Found: {} files", elFinderFiles.size());
            return response;

        } catch (Exception e) {
            logger.error("Error executing search command: {}", e.getMessage(), e);
            return createErrorResponse("error.search.failed", httpRequest, e.getMessage());
        }
    }
}