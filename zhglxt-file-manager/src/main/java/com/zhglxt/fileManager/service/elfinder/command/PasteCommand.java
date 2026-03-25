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
 * elFinder Paste Command
 * Moves or copies files to a destination
 * 
 * @author zhglxt
 */
@Component
public class PasteCommand extends AbstractElFinderCommand {

    @Override
    public String getCommandName() {
        return "paste";
    }

    @Override
    public boolean isValidRequest(ElFinderRequest request) {
        return request.getTargets() != null && !request.getTargets().isEmpty() &&
               StringUtils.hasText(request.getDst());
    }

    @Override
    public ElFinderResponse execute(ElFinderRequest request, String userId, HttpServletRequest httpRequest) {
        try {
            logger.info("Executing paste command for user: {}", userId);

            String dstHash = request.getDst();
            String dstPath = decodePath(dstHash);
            boolean cut = "1".equals(request.getParameter("cut"));

            logger.debug("Pasting files to: '{}' (cut: {})", dstPath, cut);

            ElFinderResponse response = createSuccessResponse();
            List<ElFinderFile> added = new ArrayList<>();
            List<String> removed = new ArrayList<>();
            List<String> errors = new ArrayList<>();

            for (String targetHash : request.getTargets()) {
                String sourcePath = decodePath(targetHash);
                String fileName = getFileName(sourcePath);
                String targetPath = StringUtils.hasText(dstPath) ? 
                    dstPath + "/" + fileName : fileName;

                boolean success;
                if (cut) {
                    success = fileManagerService.moveFile(sourcePath, targetPath, userId);
                    if (success) {
                        removed.add(targetHash);
                    }
                } else {
                    success = fileManagerService.copyFile(sourcePath, targetPath, userId);
                }

                if (success) {
                    FileInfo fileInfo = fileManagerService.getFileInfo(targetPath, userId);
                    if (fileInfo != null) {
                        ElFinderFile elFile = convertToElFinderFile(fileInfo, dstHash);
                        added.add(elFile);
                    }
                } else {
                    errors.add("Failed to " + (cut ? "move" : "copy") + ": " + sourcePath);
                }
            }

            response.setAdded(added);
            response.setRemoved(removed);
            if (!errors.isEmpty()) {
                response.setError(errors);
            }

            logger.info("Paste command completed. Added: {}, Removed: {}, Errors: {}", 
                       added.size(), removed.size(), errors.size());
            return response;

        } catch (Exception e) {
            logger.error("Error executing paste command: {}", e.getMessage(), e);
            return createErrorResponse("error.paste.failed", httpRequest, e.getMessage());
        }
    }
}