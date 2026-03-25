package com.zhglxt.fileManager.service.elfinder.command;

import com.zhglxt.fileManager.domain.FileInfo;
import com.zhglxt.fileManager.domain.FileUploadResult;
import com.zhglxt.fileManager.domain.elfinder.ElFinderFile;
import com.zhglxt.fileManager.domain.elfinder.ElFinderRequest;
import com.zhglxt.fileManager.domain.elfinder.ElFinderResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * elFinder Upload Command
 * Handles file upload operations
 * 
 * @author zhglxt
 */
@Component
public class UploadCommand extends AbstractElFinderCommand {

    @Override
    public String getCommandName() {
        return "upload";
    }

    @Override
    public boolean isValidRequest(ElFinderRequest request) {
        // Upload requires target directory and files
        return (StringUtils.hasText(request.getTarget()) || StringUtils.hasText(request.getDst())) &&
               request.hasUploads();
    }

    @Override
    public ElFinderResponse execute(ElFinderRequest request, String userId, HttpServletRequest httpRequest) {
        try {
            logger.info("Executing upload command for user: {}", userId);

            // Determine target directory
            String targetHash = StringUtils.hasText(request.getTarget()) ? 
                request.getTarget() : request.getDst();
            String targetPath = decodePath(targetHash);

            logger.debug("Uploading files to directory: '{}' (hash: '{}')", targetPath, targetHash);

            ElFinderResponse response = createSuccessResponse();
            List<ElFinderFile> addedFiles = new ArrayList<>();
            List<String> errors = new ArrayList<>();

            // Process each uploaded file
            List<MultipartFile> uploadFiles = request.getUpload();
            for (MultipartFile file : uploadFiles) {
                try {
                    logger.debug("Processing upload file: {}", file.getOriginalFilename());

                    // Upload the file
                    FileUploadResult uploadResult = fileManagerService.uploadFile(file, targetPath, userId);
                    
                    if (uploadResult.isSuccess()) {
                        // Get file info for the uploaded file
                        FileInfo fileInfo = fileManagerService.getFileInfo(uploadResult.getFilePath(), userId);
                        if (fileInfo != null) {
                            ElFinderFile elFile = convertToElFinderFile(fileInfo, targetHash);
                            addedFiles.add(elFile);
                            
                            logger.debug("File uploaded successfully: {} -> {}", 
                                        file.getOriginalFilename(), uploadResult.getFilePath());
                        } else {
                            logger.warn("Could not get file info for uploaded file: {}", uploadResult.getFilePath());
                        }
                    } else {
                        // Add upload errors
                        String errorMsg = String.format("Failed to upload %s: %s", 
                                                       file.getOriginalFilename(), 
                                                       String.join(", ", uploadResult.getErrors()));
                        errors.add(errorMsg);
                        logger.warn("File upload failed: {}", errorMsg);
                    }

                } catch (Exception e) {
                    String errorMsg = String.format("Error uploading %s: %s", 
                                                   file.getOriginalFilename(), e.getMessage());
                    errors.add(errorMsg);
                    logger.error("Error processing upload file: {}", e.getMessage(), e);
                }
            }

            // Set response data
            response.setAdded(addedFiles);
            
            // Add errors if any
            if (!errors.isEmpty()) {
                response.setError(errors);
            }

            // Add warning if some files failed but others succeeded
            if (!addedFiles.isEmpty() && !errors.isEmpty()) {
                response.addWarning("Some files were uploaded successfully, but others failed");
            }

            logger.info("Upload command completed. Successful: {}, Failed: {}", 
                       addedFiles.size(), errors.size());

            return response;

        } catch (Exception e) {
            logger.error("Error executing upload command: {}", e.getMessage(), e);
            return createErrorResponse("error.upload.failed", httpRequest, e.getMessage());
        }
    }
}