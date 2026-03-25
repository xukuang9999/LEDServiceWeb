package com.zhglxt.fileManager.service.elfinder.command;

import com.zhglxt.fileManager.domain.FileInfo;
import com.zhglxt.fileManager.domain.elfinder.ElFinderRequest;
import com.zhglxt.fileManager.domain.elfinder.ElFinderResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * elFinder Info Command
 * Returns detailed information about files
 * 
 * @author zhglxt
 */
@Component
public class InfoCommand extends AbstractElFinderCommand {

    @Override
    public String getCommandName() {
        return "info";
    }

    @Override
    public boolean isValidRequest(ElFinderRequest request) {
        return request.getTargets() != null && !request.getTargets().isEmpty();
    }

    @Override
    public ElFinderResponse execute(ElFinderRequest request, String userId, HttpServletRequest httpRequest) {
        try {
            logger.info("Executing info command for user: {}", userId);

            ElFinderResponse response = createSuccessResponse();
            List<Map<String, Object>> infoList = new ArrayList<>();

            for (String targetHash : request.getTargets()) {
                String targetPath = decodePath(targetHash);
                
                FileInfo fileInfo = fileManagerService.getFileInfo(targetPath, userId);
                if (fileInfo != null) {
                    Map<String, Object> info = new HashMap<>();
                    info.put("name", fileInfo.getName());
                    info.put("path", fileInfo.getPath());
                    info.put("size", fileInfo.getSize());
                    info.put("mime", fileInfo.getMimeType());
                    info.put("ts", fileInfo.getLastModified().toEpochSecond(java.time.ZoneOffset.UTC));
                    info.put("read", fileInfo.isReadable());
                    info.put("write", fileInfo.isWritable());
                    info.put("locked", false); // Default to not locked
                    
                    if (fileInfo.getMetadata() != null) {
                        info.putAll(fileInfo.getMetadata());
                    }
                    
                    infoList.add(info);
                }
            }

            response.addData("info", infoList);

            logger.info("Info command completed. Files: {}", infoList.size());
            return response;

        } catch (Exception e) {
            logger.error("Error executing info command: {}", e.getMessage(), e);
            return createErrorResponse("error.info.failed", httpRequest, e.getMessage());
        }
    }
}