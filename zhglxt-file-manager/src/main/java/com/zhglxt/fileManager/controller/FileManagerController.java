package com.zhglxt.fileManager.controller;

import com.zhglxt.fileManager.domain.FileDownloadResult;
import com.zhglxt.fileManager.domain.FileInfo;
import com.zhglxt.fileManager.domain.FilePreviewResult;
import com.zhglxt.fileManager.domain.FileUploadResult;
import com.zhglxt.fileManager.exception.FileManagerException;
import com.zhglxt.fileManager.exception.FileNotFoundException;
import com.zhglxt.fileManager.exception.FilePermissionException;
import com.zhglxt.fileManager.exception.FileValidationException;
import com.zhglxt.fileManager.service.FileManagerService;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * File Manager REST API Controller
 * Provides RESTful endpoints for file operations
 * 
 * @author zhglxt
 */
@RestController
@RequestMapping("/api/file-manager")
@RequiresAuthentication
@Validated
public class FileManagerController {

    private static final Logger logger = LoggerFactory.getLogger(FileManagerController.class);

    @Autowired
    private FileManagerService fileManagerService;

    /**
     * Upload file
     * 
     * @param file uploaded file
     * @param directory target directory (optional, defaults to root)
     * @param request HTTP request
     * @return upload result
     */
    @PostMapping("/upload")
    @RequiresPermissions("file:upload")
    public ResponseEntity<ApiResponse<FileUploadResult>> uploadFile(
            @RequestParam("file") @NotNull MultipartFile file,
            @RequestParam(value = "directory", defaultValue = "") String directory,
            HttpServletRequest request) {
        
        try {
            logger.info("File upload request - filename: {}, size: {}, directory: {}", 
                       file.getOriginalFilename(), file.getSize(), directory);

            String userId = getCurrentUserId(request);
            FileUploadResult result = fileManagerService.uploadFile(file, directory, userId);

            logger.info("File uploaded successfully - path: {}", result.getFilePath());
            return ResponseEntity.ok(ApiResponse.success(result));

        } catch (FileValidationException e) {
            logger.warn("File validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("VALIDATION_ERROR", e.getMessage(), e.getValidationErrors()));
        } catch (FilePermissionException e) {
            logger.warn("File upload permission denied: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("PERMISSION_DENIED", e.getMessage()));
        } catch (Exception e) {
            logger.error("File upload failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("UPLOAD_FAILED", "File upload failed: " + e.getMessage()));
        }
    }

    /**
     * Download file
     * 
     * @param filePath file path
     * @param request HTTP request
     * @return file content
     */
    @GetMapping("/download")
    @RequiresPermissions("file:download")
    public ResponseEntity<?> downloadFile(
            @RequestParam("path") @NotBlank String filePath,
            HttpServletRequest request) {
        
        try {
            logger.info("File download request - path: {}", filePath);

            String userId = getCurrentUserId(request);
            FileDownloadResult result = fileManagerService.downloadFile(filePath, userId);

            // Set response headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(result.getMimeType()));
            headers.setContentLength(result.getFileSize());
            
            // Set filename for download
            String encodedFilename = URLEncoder.encode(result.getFileName(), StandardCharsets.UTF_8);
            headers.setContentDispositionFormData("attachment", encodedFilename);
            
            // Add cache control headers
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.setPragma("no-cache");
            headers.setExpires(0);

            logger.info("File download started - filename: {}, size: {}", 
                       result.getFileName(), result.getFileSize());

            return ResponseEntity.ok()
                .headers(headers)
                .body(new InputStreamResource(result.getInputStream()));

        } catch (FileNotFoundException e) {
            logger.warn("File not found for download: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (FilePermissionException e) {
            logger.warn("File download permission denied: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("PERMISSION_DENIED", e.getMessage()));
        } catch (Exception e) {
            logger.error("File download failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("DOWNLOAD_FAILED", "File download failed: " + e.getMessage()));
        }
    }

    /**
     * Delete file
     * 
     * @param filePath file path
     * @param request HTTP request
     * @return deletion result
     */
    @DeleteMapping("/delete")
    @RequiresPermissions("file:delete")
    public ResponseEntity<ApiResponse<Boolean>> deleteFile(
            @RequestParam("path") @NotBlank String filePath,
            HttpServletRequest request) {
        
        try {
            logger.info("File delete request - path: {}", filePath);

            String userId = getCurrentUserId(request);
            boolean deleted = fileManagerService.deleteFile(filePath, userId);

            if (deleted) {
                logger.info("File deleted successfully - path: {}", filePath);
                return ResponseEntity.ok(ApiResponse.success(true));
            } else {
                logger.warn("File deletion failed - path: {}", filePath);
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("DELETE_FAILED", "File could not be deleted"));
            }

        } catch (FileNotFoundException e) {
            logger.warn("File not found for deletion: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("FILE_NOT_FOUND", e.getMessage()));
        } catch (FilePermissionException e) {
            logger.warn("File delete permission denied: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("PERMISSION_DENIED", e.getMessage()));
        } catch (Exception e) {
            logger.error("File delete failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("DELETE_FAILED", "File deletion failed: " + e.getMessage()));
        }
    }

    /**
     * Move file
     * 
     * @param sourcePath source file path
     * @param targetPath target file path
     * @param request HTTP request
     * @return move result
     */
    @PostMapping("/move")
    @RequiresPermissions("file:move")
    public ResponseEntity<ApiResponse<Boolean>> moveFile(
            @RequestParam("source") @NotBlank String sourcePath,
            @RequestParam("target") @NotBlank String targetPath,
            HttpServletRequest request) {
        
        try {
            logger.info("File move request - source: {}, target: {}", sourcePath, targetPath);

            String userId = getCurrentUserId(request);
            boolean moved = fileManagerService.moveFile(sourcePath, targetPath, userId);

            if (moved) {
                logger.info("File moved successfully - source: {}, target: {}", sourcePath, targetPath);
                return ResponseEntity.ok(ApiResponse.success(true));
            } else {
                logger.warn("File move failed - source: {}, target: {}", sourcePath, targetPath);
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("MOVE_FAILED", "File could not be moved"));
            }

        } catch (FileNotFoundException e) {
            logger.warn("File not found for move: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("FILE_NOT_FOUND", e.getMessage()));
        } catch (FilePermissionException e) {
            logger.warn("File move permission denied: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("PERMISSION_DENIED", e.getMessage()));
        } catch (Exception e) {
            logger.error("File move failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("MOVE_FAILED", "File move failed: " + e.getMessage()));
        }
    }

    /**
     * Copy file
     * 
     * @param sourcePath source file path
     * @param targetPath target file path
     * @param request HTTP request
     * @return copy result
     */
    @PostMapping("/copy")
    @RequiresPermissions("file:copy")
    public ResponseEntity<ApiResponse<Boolean>> copyFile(
            @RequestParam("source") @NotBlank String sourcePath,
            @RequestParam("target") @NotBlank String targetPath,
            HttpServletRequest request) {
        
        try {
            logger.info("File copy request - source: {}, target: {}", sourcePath, targetPath);

            String userId = getCurrentUserId(request);
            boolean copied = fileManagerService.copyFile(sourcePath, targetPath, userId);

            if (copied) {
                logger.info("File copied successfully - source: {}, target: {}", sourcePath, targetPath);
                return ResponseEntity.ok(ApiResponse.success(true));
            } else {
                logger.warn("File copy failed - source: {}, target: {}", sourcePath, targetPath);
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("COPY_FAILED", "File could not be copied"));
            }

        } catch (FileNotFoundException e) {
            logger.warn("File not found for copy: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("FILE_NOT_FOUND", e.getMessage()));
        } catch (FilePermissionException e) {
            logger.warn("File copy permission denied: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("PERMISSION_DENIED", e.getMessage()));
        } catch (Exception e) {
            logger.error("File copy failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("COPY_FAILED", "File copy failed: " + e.getMessage()));
        }
    }

    /**
     * List files in directory
     * 
     * @param directory directory path (optional, defaults to root)
     * @param request HTTP request
     * @return list of files
     */
    @GetMapping("/list")
    @RequiresPermissions("file:list")
    public ResponseEntity<ApiResponse<List<FileInfo>>> listFiles(
            @RequestParam(value = "directory", defaultValue = "") String directory,
            HttpServletRequest request) {
        
        try {
            logger.debug("File list request - directory: {}", directory);

            String userId = getCurrentUserId(request);
            List<FileInfo> files = fileManagerService.listFiles(directory, userId);

            logger.debug("File list completed - directory: {}, count: {}", directory, files.size());
            return ResponseEntity.ok(ApiResponse.success(files));

        } catch (FileNotFoundException e) {
            logger.warn("Directory not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("DIRECTORY_NOT_FOUND", e.getMessage()));
        } catch (FilePermissionException e) {
            logger.warn("Directory list permission denied: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("PERMISSION_DENIED", e.getMessage()));
        } catch (Exception e) {
            logger.error("File list failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("LIST_FAILED", "File listing failed: " + e.getMessage()));
        }
    }    
/**
     * Get file information
     * 
     * @param filePath file path
     * @param request HTTP request
     * @return file information
     */
    @GetMapping("/info")
    @RequiresPermissions("file:read")
    public ResponseEntity<ApiResponse<FileInfo>> getFileInfo(
            @RequestParam("path") @NotBlank String filePath,
            HttpServletRequest request) {
        
        try {
            logger.debug("File info request - path: {}", filePath);

            String userId = getCurrentUserId(request);
            FileInfo fileInfo = fileManagerService.getFileInfo(filePath, userId);

            logger.debug("File info retrieved - path: {}, size: {}", filePath, fileInfo.getSize());
            return ResponseEntity.ok(ApiResponse.success(fileInfo));

        } catch (FileNotFoundException e) {
            logger.warn("File not found for info: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("FILE_NOT_FOUND", e.getMessage()));
        } catch (FilePermissionException e) {
            logger.warn("File info permission denied: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("PERMISSION_DENIED", e.getMessage()));
        } catch (Exception e) {
            logger.error("File info failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INFO_FAILED", "File info retrieval failed: " + e.getMessage()));
        }
    }

    /**
     * Preview file
     * 
     * @param filePath file path
     * @param request HTTP request
     * @return file preview
     */
    @GetMapping("/preview")
    @RequiresPermissions("file:read")
    public ResponseEntity<?> previewFile(
            @RequestParam("path") @NotBlank String filePath,
            HttpServletRequest request) {
        
        try {
            logger.debug("File preview request - path: {}", filePath);

            String userId = getCurrentUserId(request);
            FilePreviewResult result = fileManagerService.previewFile(filePath, userId);

            if (result.getContent() != null) {
                // Return content directly for text files
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(result.getMimeType()));
                
                return ResponseEntity.ok()
                    .headers(headers)
                    .body(result.getContent());
            } else {
                // Return preview info
                return ResponseEntity.ok(ApiResponse.success(result));
            }

        } catch (FileNotFoundException e) {
            logger.warn("File not found for preview: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("FILE_NOT_FOUND", e.getMessage()));
        } catch (FilePermissionException e) {
            logger.warn("File preview permission denied: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("PERMISSION_DENIED", e.getMessage()));
        } catch (Exception e) {
            logger.error("File preview failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("PREVIEW_FAILED", "File preview failed: " + e.getMessage()));
        }
    }

    /**
     * Create directory
     * 
     * @param directoryPath directory path
     * @param request HTTP request
     * @return creation result
     */
    @PostMapping("/mkdir")
    @RequiresPermissions("file:create")
    public ResponseEntity<ApiResponse<Boolean>> createDirectory(
            @RequestParam("path") @NotBlank String directoryPath,
            HttpServletRequest request) {
        
        try {
            logger.info("Directory create request - path: {}", directoryPath);

            String userId = getCurrentUserId(request);
            boolean created = fileManagerService.createDirectory(directoryPath, userId);

            if (created) {
                logger.info("Directory created successfully - path: {}", directoryPath);
                return ResponseEntity.ok(ApiResponse.success(true));
            } else {
                logger.warn("Directory creation failed - path: {}", directoryPath);
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("CREATE_FAILED", "Directory could not be created"));
            }

        } catch (FilePermissionException e) {
            logger.warn("Directory create permission denied: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("PERMISSION_DENIED", e.getMessage()));
        } catch (Exception e) {
            logger.error("Directory create failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("CREATE_FAILED", "Directory creation failed: " + e.getMessage()));
        }
    }

    /**
     * Delete directory
     * 
     * @param directoryPath directory path
     * @param recursive whether to delete recursively
     * @param request HTTP request
     * @return deletion result
     */
    @DeleteMapping("/rmdir")
    @RequiresPermissions("file:delete")
    public ResponseEntity<ApiResponse<Boolean>> deleteDirectory(
            @RequestParam("path") @NotBlank String directoryPath,
            @RequestParam(value = "recursive", defaultValue = "false") boolean recursive,
            HttpServletRequest request) {
        
        try {
            logger.info("Directory delete request - path: {}, recursive: {}", directoryPath, recursive);

            String userId = getCurrentUserId(request);
            boolean deleted = fileManagerService.deleteDirectory(directoryPath, userId, recursive);

            if (deleted) {
                logger.info("Directory deleted successfully - path: {}", directoryPath);
                return ResponseEntity.ok(ApiResponse.success(true));
            } else {
                logger.warn("Directory deletion failed - path: {}", directoryPath);
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("DELETE_FAILED", "Directory could not be deleted"));
            }

        } catch (FileNotFoundException e) {
            logger.warn("Directory not found for deletion: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("DIRECTORY_NOT_FOUND", e.getMessage()));
        } catch (FilePermissionException e) {
            logger.warn("Directory delete permission denied: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("PERMISSION_DENIED", e.getMessage()));
        } catch (Exception e) {
            logger.error("Directory delete failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("DELETE_FAILED", "Directory deletion failed: " + e.getMessage()));
        }
    }

    /**
     * Rename file or directory
     * 
     * @param oldPath old path
     * @param newName new name
     * @param request HTTP request
     * @return rename result
     */
    @PostMapping("/rename")
    @RequiresPermissions("file:update")
    public ResponseEntity<ApiResponse<Boolean>> rename(
            @RequestParam("path") @NotBlank String oldPath,
            @RequestParam("name") @NotBlank String newName,
            HttpServletRequest request) {
        
        try {
            logger.info("Rename request - path: {}, newName: {}", oldPath, newName);

            String userId = getCurrentUserId(request);
            boolean renamed = fileManagerService.rename(oldPath, newName, userId);

            if (renamed) {
                logger.info("Renamed successfully - path: {}, newName: {}", oldPath, newName);
                return ResponseEntity.ok(ApiResponse.success(true));
            } else {
                logger.warn("Rename failed - path: {}, newName: {}", oldPath, newName);
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("RENAME_FAILED", "File could not be renamed"));
            }

        } catch (FileNotFoundException e) {
            logger.warn("File not found for rename: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("FILE_NOT_FOUND", e.getMessage()));
        } catch (FilePermissionException e) {
            logger.warn("Rename permission denied: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("PERMISSION_DENIED", e.getMessage()));
        } catch (Exception e) {
            logger.error("Rename failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("RENAME_FAILED", "Rename failed: " + e.getMessage()));
        }
    }

    /**
     * Search files
     * 
     * @param directory directory to search in
     * @param pattern search pattern
     * @param recursive whether to search recursively
     * @param request HTTP request
     * @return search results
     */
    @GetMapping("/search")
    @RequiresPermissions("file:read")
    public ResponseEntity<ApiResponse<List<FileInfo>>> searchFiles(
            @RequestParam(value = "directory", defaultValue = "") String directory,
            @RequestParam("pattern") @NotBlank String pattern,
            @RequestParam(value = "recursive", defaultValue = "true") boolean recursive,
            HttpServletRequest request) {
        
        try {
            logger.info("File search request - directory: {}, pattern: {}, recursive: {}", 
                       directory, pattern, recursive);

            String userId = getCurrentUserId(request);
            List<FileInfo> results = fileManagerService.searchFiles(directory, pattern, userId, recursive);

            logger.info("File search completed - directory: {}, pattern: {}, results: {}", 
                       directory, pattern, results.size());
            return ResponseEntity.ok(ApiResponse.success(results));

        } catch (FilePermissionException e) {
            logger.warn("File search permission denied: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("PERMISSION_DENIED", e.getMessage()));
        } catch (Exception e) {
            logger.error("File search failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("SEARCH_FAILED", "File search failed: " + e.getMessage()));
        }
    }

    /**
     * Check if file exists
     * 
     * @param filePath file path
     * @param request HTTP request
     * @return existence result
     */
    @GetMapping("/exists")
    @RequiresPermissions("file:read")
    public ResponseEntity<ApiResponse<Boolean>> fileExists(
            @RequestParam("path") @NotBlank String filePath,
            HttpServletRequest request) {
        
        try {
            logger.debug("File exists request - path: {}", filePath);

            String userId = getCurrentUserId(request);
            boolean exists = fileManagerService.fileExists(filePath, userId);

            logger.debug("File exists result - path: {}, exists: {}", filePath, exists);
            return ResponseEntity.ok(ApiResponse.success(exists));

        } catch (Exception e) {
            logger.error("File exists check failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("EXISTS_CHECK_FAILED", "File existence check failed: " + e.getMessage()));
        }
    }

    /**
     * Get file size
     * 
     * @param filePath file path
     * @param request HTTP request
     * @return file size
     */
    @GetMapping("/size")
    @RequiresPermissions("file:read")
    public ResponseEntity<ApiResponse<Long>> getFileSize(
            @RequestParam("path") @NotBlank String filePath,
            HttpServletRequest request) {
        
        try {
            logger.debug("File size request - path: {}", filePath);

            String userId = getCurrentUserId(request);
            long size = fileManagerService.getFileSize(filePath, userId);

            if (size >= 0) {
                logger.debug("File size result - path: {}, size: {}", filePath, size);
                return ResponseEntity.ok(ApiResponse.success(size));
            } else {
                logger.warn("File not found or no access - path: {}", filePath);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("FILE_NOT_FOUND", "File not found or no access"));
            }

        } catch (Exception e) {
            logger.error("File size check failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("SIZE_CHECK_FAILED", "File size check failed: " + e.getMessage()));
        }
    }

    /**
     * Get current user ID from request
     * 
     * @param request HTTP servlet request
     * @return user ID
     */
    private String getCurrentUserId(HttpServletRequest request) {
        // Try to get user ID from session
        Object userId = request.getSession().getAttribute("userId");
        if (userId != null) {
            return userId.toString();
        }

        // Try to get from Shiro security context
        try {
            org.apache.shiro.subject.Subject subject = org.apache.shiro.SecurityUtils.getSubject();
            if (subject != null && subject.isAuthenticated()) {
                Object principal = subject.getPrincipal();
                if (principal != null) {
                    return principal.toString();
                }
            }
        } catch (Exception e) {
            logger.debug("Could not get user from Shiro security context: {}", e.getMessage());
        }

        // Fallback to request parameter or header
        String userIdParam = request.getParameter("userId");
        if (StringUtils.hasText(userIdParam)) {
            return userIdParam;
        }

        String userIdHeader = request.getHeader("X-User-Id");
        if (StringUtils.hasText(userIdHeader)) {
            return userIdHeader;
        }

        // Default user for development/testing
        logger.warn("No user ID found in request, using default user");
        return "anonymous";
    }

    /**
     * Standard API Response wrapper
     */
    public static class ApiResponse<T> {
        private boolean success;
        private String code;
        private String message;
        private T data;
        private Object errors;
        private long timestamp;

        public ApiResponse() {
            this.timestamp = System.currentTimeMillis();
        }

        public static <T> ApiResponse<T> success(T data) {
            ApiResponse<T> response = new ApiResponse<>();
            response.success = true;
            response.data = data;
            return response;
        }

        public static <T> ApiResponse<T> error(String code, String message) {
            ApiResponse<T> response = new ApiResponse<>();
            response.success = false;
            response.code = code;
            response.message = message;
            return response;
        }

        public static <T> ApiResponse<T> error(String code, String message, Object errors) {
            ApiResponse<T> response = new ApiResponse<>();
            response.success = false;
            response.code = code;
            response.message = message;
            response.errors = errors;
            return response;
        }

        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public T getData() { return data; }
        public void setData(T data) { this.data = data; }
        
        public Object getErrors() { return errors; }
        public void setErrors(Object errors) { this.errors = errors; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
}