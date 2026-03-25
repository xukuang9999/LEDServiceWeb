package com.zhglxt.fileManager.service.impl;

import com.zhglxt.fileManager.config.FileManagerProperties;
import com.zhglxt.fileManager.domain.FileDownloadResult;
import com.zhglxt.fileManager.domain.FileInfo;
import com.zhglxt.fileManager.domain.FileOperation;
import com.zhglxt.fileManager.domain.FilePreviewResult;
import com.zhglxt.fileManager.domain.FileUploadResult;
import com.zhglxt.fileManager.domain.validation.FileValidationResult;
import com.zhglxt.fileManager.exception.FileNotFoundException;
import com.zhglxt.fileManager.exception.FilePermissionException;
import com.zhglxt.fileManager.exception.FileSecurityException;
import com.zhglxt.fileManager.exception.StorageException;
import com.zhglxt.fileManager.service.FileManagerService;
import com.zhglxt.fileManager.service.security.FilePermissionEvaluator;
import com.zhglxt.fileManager.service.storage.StorageFileInfo;
import com.zhglxt.fileManager.service.storage.StorageService;
import com.zhglxt.fileManager.service.validation.FileValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * File Manager Service Implementation
 * Core business logic for file operations with security and validation
 * 
 * @author zhglxt
 */
@Service
public class FileManagerServiceImpl implements FileManagerService {

    private static final Logger logger = LoggerFactory.getLogger(FileManagerServiceImpl.class);

    @Autowired
    private StorageService storageService;

    @Autowired
    private FileValidationService fileValidationService;

    @Autowired
    private FilePermissionEvaluator permissionEvaluator;

    @Autowired
    private FileManagerProperties properties;

    @Autowired
    private com.zhglxt.fileManager.service.security.FileSecurityScanningService securityScanningService;

    @Override
    public FileUploadResult uploadFile(MultipartFile file, String directory, String userId) {
        long startTime = System.currentTimeMillis();
        
        try {
            logger.info("Starting file upload: {} to directory: {} by user: {}", 
                       file.getOriginalFilename(), directory, userId);

            // Validate input parameters
            if (file == null || file.isEmpty()) {
                return FileUploadResult.failure("File is empty or null");
            }

            if (!StringUtils.hasText(directory)) {
                directory = "";
            }

            // Sanitize directory path
            directory = sanitizePath(directory);

            // Check directory permission
            if (!permissionEvaluator.hasDirectoryPermission(userId, directory, FileOperation.UPLOAD)) {
                throw new FilePermissionException(userId, directory, "UPLOAD");
            }

            // Validate file
            FileValidationResult validationResult = fileValidationService.validateFile(file);
            if (!validationResult.isValid()) {
                return FileUploadResult.failure(validationResult.getErrors());
            }

            // Sanitize file name
            String originalFileName = file.getOriginalFilename();
            String sanitizedFileName = fileValidationService.sanitizeFileName(originalFileName);
            
            // Check for file content security
            try (InputStream inputStream = file.getInputStream()) {
                FileValidationResult contentValidation = fileValidationService.validateFileContent(
                    sanitizedFileName, inputStream);
                if (!contentValidation.isValid()) {
                    throw new FileSecurityException(sanitizedFileName, 
                                                   String.join(", ", contentValidation.getErrors()));
                }
            }

            // Perform comprehensive security scanning
            com.zhglxt.fileManager.service.security.FileSecurityScanningService.SecurityScanResult scanResult = 
                securityScanningService.scanFile(file, userId, 
                    ((org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes() != null) ?
                        ((org.springframework.web.context.request.ServletRequestAttributes) 
                            org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes()).getRequest() : null));
            
            // Check scan results
            if (!scanResult.isClean()) {
                if (scanResult.isBlocked()) {
                    // Block high-threat files
                    throw new FileSecurityException(sanitizedFileName, 
                        "File blocked due to security threats: " + scanResult.getOverallThreatLevel());
                } else {
                    // Log medium/low threats but allow upload
                    logger.warn("File {} has security concerns but is allowed: {}", 
                        sanitizedFileName, scanResult.getThreats());
                }
            }

            // Store the file
            String storedFilePath;
            try (InputStream inputStream = file.getInputStream()) {
                storedFilePath = storageService.storeFile(inputStream, sanitizedFileName, directory);
            }

            // Create successful result
            FileUploadResult result = FileUploadResult.success(storedFilePath, sanitizedFileName, file.getSize());
            result.setMimeType(file.getContentType());
            result.setExtension(getFileExtension(sanitizedFileName));
            result.setUploadDuration(System.currentTimeMillis() - startTime);
            
            // Set public URL if available
            String publicUrl = storageService.generatePublicUrl(storedFilePath);
            if (StringUtils.hasText(publicUrl)) {
                result.setPublicUrl(publicUrl);
            }

            logger.info("File upload completed successfully: {} -> {}", originalFileName, storedFilePath);
            return result;

        } catch (FilePermissionException | FileSecurityException e) {
            logger.warn("File upload failed due to security/permission: {}", e.getMessage());
            return FileUploadResult.failure(e.getMessage());
        } catch (IOException e) {
            logger.error("File upload failed due to IO error: {}", e.getMessage(), e);
            return FileUploadResult.failure("Failed to read file content: " + e.getMessage());
        } catch (StorageException e) {
            logger.error("File upload failed due to storage error: {}", e.getMessage(), e);
            return FileUploadResult.failure("Failed to store file: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during file upload: {}", e.getMessage(), e);
            return FileUploadResult.failure("Unexpected error occurred during upload");
        }
    }

    @Override
    public FileDownloadResult downloadFile(String filePath, String userId) {
        try {
            logger.info("Starting file download: {} by user: {}", filePath, userId);

            // Validate input
            if (!StringUtils.hasText(filePath)) {
                return FileDownloadResult.failure("File path is required");
            }

            // Sanitize file path
            filePath = sanitizePath(filePath);

            // Check file permission
            if (!permissionEvaluator.hasFilePermission(userId, filePath, FileOperation.DOWNLOAD)) {
                throw new FilePermissionException(userId, filePath, "DOWNLOAD");
            }

            // Check if file exists
            if (!storageService.fileExists(filePath)) {
                throw new FileNotFoundException("File not found: " + filePath);
            }

            // Get file info for metadata
            final String finalFilePath = filePath;
            List<StorageFileInfo> fileInfos = storageService.listFiles(getParentDirectory(filePath));
            StorageFileInfo fileInfo = fileInfos.stream()
                .filter(info -> info.getPath().equals(finalFilePath))
                .findFirst()
                .orElseThrow(() -> new FileNotFoundException("File not found: " + finalFilePath));

            // Retrieve file stream
            InputStream inputStream = storageService.retrieveFile(filePath);
            
            // Create download result
            String fileName = getFileName(filePath);
            FileDownloadResult result = FileDownloadResult.success(
                inputStream, fileName, fileInfo.getSize(), fileInfo.getMimeType());
            
            result.setExtension(getFileExtension(fileName));
            
            // Set content disposition based on file type
            if (isPreviewableFile(fileInfo.getMimeType())) {
                result.setContentDisposition("inline");
            }

            logger.info("File download prepared successfully: {}", filePath);
            return result;

        } catch (FilePermissionException e) {
            logger.warn("File download failed due to permission: {}", e.getMessage());
            return FileDownloadResult.failure(e.getMessage());
        } catch (FileNotFoundException e) {
            logger.warn("File download failed - file not found: {}", e.getMessage());
            return FileDownloadResult.failure(e.getMessage());
        } catch (StorageException e) {
            logger.error("File download failed due to storage error: {}", e.getMessage(), e);
            return FileDownloadResult.failure("Failed to retrieve file: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during file download: {}", e.getMessage(), e);
            return FileDownloadResult.failure("Unexpected error occurred during download");
        }
    }

    @Override
    public boolean deleteFile(String filePath, String userId) {
        try {
            logger.info("Starting file deletion: {} by user: {}", filePath, userId);

            // Validate input
            if (!StringUtils.hasText(filePath)) {
                logger.warn("File deletion failed - empty file path");
                return false;
            }

            // Sanitize file path
            filePath = sanitizePath(filePath);

            // Check file permission
            if (!permissionEvaluator.hasFilePermission(userId, filePath, FileOperation.DELETE)) {
                throw new FilePermissionException(userId, filePath, "DELETE");
            }

            // Check if file exists
            if (!storageService.fileExists(filePath)) {
                logger.warn("File deletion failed - file not found: {}", filePath);
                return false;
            }

            // Delete the file
            boolean deleted = storageService.deleteFile(filePath);
            
            if (deleted) {
                logger.info("File deleted successfully: {}", filePath);
            } else {
                logger.warn("File deletion failed: {}", filePath);
            }
            
            return deleted;

        } catch (FilePermissionException e) {
            logger.warn("File deletion failed due to permission: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error during file deletion: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean moveFile(String sourcePath, String targetPath, String userId) {
        try {
            logger.info("Starting file move: {} -> {} by user: {}", sourcePath, targetPath, userId);

            // Validate input
            if (!StringUtils.hasText(sourcePath) || !StringUtils.hasText(targetPath)) {
                logger.warn("File move failed - empty source or target path");
                return false;
            }

            // Sanitize paths
            sourcePath = sanitizePath(sourcePath);
            targetPath = sanitizePath(targetPath);

            // Check source file permission
            if (!permissionEvaluator.hasFilePermission(userId, sourcePath, FileOperation.MOVE)) {
                throw new FilePermissionException(userId, sourcePath, "MOVE");
            }

            // Check target directory permission
            String targetDirectory = getParentDirectory(targetPath);
            if (!permissionEvaluator.hasDirectoryPermission(userId, targetDirectory, FileOperation.CREATE)) {
                throw new FilePermissionException(userId, targetDirectory, "CREATE");
            }

            // Check if source file exists
            if (!storageService.fileExists(sourcePath)) {
                logger.warn("File move failed - source file not found: {}", sourcePath);
                return false;
            }

            // Check if target already exists
            if (storageService.fileExists(targetPath)) {
                logger.warn("File move failed - target file already exists: {}", targetPath);
                return false;
            }

            // Move the file
            boolean moved = storageService.moveFile(sourcePath, targetPath);
            
            if (moved) {
                logger.info("File moved successfully: {} -> {}", sourcePath, targetPath);
            } else {
                logger.warn("File move failed: {} -> {}", sourcePath, targetPath);
            }
            
            return moved;

        } catch (FilePermissionException e) {
            logger.warn("File move failed due to permission: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error during file move: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean copyFile(String sourcePath, String targetPath, String userId) {
        try {
            logger.info("Starting file copy: {} -> {} by user: {}", sourcePath, targetPath, userId);

            // Validate input
            if (!StringUtils.hasText(sourcePath) || !StringUtils.hasText(targetPath)) {
                logger.warn("File copy failed - empty source or target path");
                return false;
            }

            // Sanitize paths
            sourcePath = sanitizePath(sourcePath);
            targetPath = sanitizePath(targetPath);

            // Check source file permission
            if (!permissionEvaluator.hasFilePermission(userId, sourcePath, FileOperation.READ)) {
                throw new FilePermissionException(userId, sourcePath, "READ");
            }

            // Check target directory permission
            String targetDirectory = getParentDirectory(targetPath);
            if (!permissionEvaluator.hasDirectoryPermission(userId, targetDirectory, FileOperation.CREATE)) {
                throw new FilePermissionException(userId, targetDirectory, "CREATE");
            }

            // Check if source file exists
            if (!storageService.fileExists(sourcePath)) {
                logger.warn("File copy failed - source file not found: {}", sourcePath);
                return false;
            }

            // Check if target already exists
            if (storageService.fileExists(targetPath)) {
                logger.warn("File copy failed - target file already exists: {}", targetPath);
                return false;
            }

            // Copy the file by reading and storing
            try (InputStream inputStream = storageService.retrieveFile(sourcePath)) {
                String fileName = getFileName(targetPath);
                String directory = getParentDirectory(targetPath);
                storageService.storeFile(inputStream, fileName, directory);
                
                logger.info("File copied successfully: {} -> {}", sourcePath, targetPath);
                return true;
            }

        } catch (FilePermissionException e) {
            logger.warn("File copy failed due to permission: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error during file copy: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public List<FileInfo> listFiles(String directory, String userId) {
        try {
            logger.debug("Listing files in directory: {} for user: {}", directory, userId);

            // Validate input
            if (!StringUtils.hasText(directory)) {
                directory = "";
            }

            // Sanitize directory path
            directory = sanitizePath(directory);

            // Check directory permission
            if (!permissionEvaluator.hasDirectoryPermission(userId, directory, FileOperation.LIST)) {
                throw new FilePermissionException(userId, directory, "LIST");
            }

            // Get files from storage
            List<StorageFileInfo> storageFiles = storageService.listFiles(directory);
            
            // Convert to FileInfo and filter by permissions
            List<FileInfo> fileInfos = new ArrayList<>();
            for (StorageFileInfo storageFile : storageFiles) {
                // Check if user has access to this file/directory
                if (storageFile.isDirectory()) {
                    if (permissionEvaluator.canAccessDirectory(userId, storageFile.getPath())) {
                        fileInfos.add(convertToFileInfo(storageFile));
                    }
                } else {
                    if (permissionEvaluator.canAccessFile(userId, storageFile.getPath())) {
                        fileInfos.add(convertToFileInfo(storageFile));
                    }
                }
            }

            logger.debug("Listed {} files in directory: {}", fileInfos.size(), directory);
            return fileInfos;

        } catch (FilePermissionException e) {
            logger.warn("Directory listing failed due to permission: {}", e.getMessage());
            return new ArrayList<>();
        } catch (Exception e) {
            logger.error("Unexpected error during directory listing: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public FileInfo getFileInfo(String filePath, String userId) {
        try {
            logger.debug("Getting file info: {} for user: {}", filePath, userId);

            // Validate input
            if (!StringUtils.hasText(filePath)) {
                return null;
            }

            // Sanitize file path
            filePath = sanitizePath(filePath);

            // Check file permission
            if (!permissionEvaluator.canAccessFile(userId, filePath)) {
                throw new FilePermissionException(userId, filePath, "ACCESS");
            }

            // Check if file exists
            if (!storageService.fileExists(filePath)) {
                return null;
            }

            // Get file info from parent directory listing
            final String finalFilePath = filePath;
            String parentDirectory = getParentDirectory(filePath);
            List<StorageFileInfo> files = storageService.listFiles(parentDirectory);
            
            StorageFileInfo storageFile = files.stream()
                .filter(file -> file.getPath().equals(finalFilePath))
                .findFirst()
                .orElse(null);

            if (storageFile == null) {
                return null;
            }

            return convertToFileInfo(storageFile);

        } catch (FilePermissionException e) {
            logger.warn("Get file info failed due to permission: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("Unexpected error getting file info: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public FilePreviewResult previewFile(String filePath, String userId) {
        try {
            logger.debug("Generating file preview: {} for user: {}", filePath, userId);

            // Validate input
            if (!StringUtils.hasText(filePath)) {
                return FilePreviewResult.unavailable("File path is required");
            }

            // Sanitize file path
            filePath = sanitizePath(filePath);

            // Check file permission
            if (!permissionEvaluator.hasFilePermission(userId, filePath, FileOperation.PREVIEW)) {
                return FilePreviewResult.unavailable("No permission to preview file");
            }

            // Check if file exists
            if (!storageService.fileExists(filePath)) {
                return FilePreviewResult.unavailable("File not found");
            }

            // Get file info
            FileInfo fileInfo = getFileInfo(filePath, userId);
            if (fileInfo == null) {
                return FilePreviewResult.unavailable("Unable to get file information");
            }

            // Determine preview type based on MIME type
            FilePreviewResult.PreviewType previewType = determinePreviewType(fileInfo.getMimeType());
            
            if (previewType == FilePreviewResult.PreviewType.NONE) {
                return FilePreviewResult.unavailable("Preview not supported for this file type");
            }

            // Generate preview based on type
            FilePreviewResult result = new FilePreviewResult(true, previewType);
            result.setFileName(fileInfo.getName());
            result.setFileSize(fileInfo.getSize());
            result.setMimeType(fileInfo.getMimeType());

            // For images, use the file URL directly
            if (previewType == FilePreviewResult.PreviewType.IMAGE) {
                String publicUrl = storageService.generatePublicUrl(filePath);
                result.setPreviewUrl(publicUrl);
            }
            
            // For text files, read content (limited)
            else if (previewType == FilePreviewResult.PreviewType.TEXT && fileInfo.getSize() < 1024 * 1024) {
                try (InputStream inputStream = storageService.retrieveFile(filePath)) {
                    byte[] content = inputStream.readNBytes(10240); // Read max 10KB
                    result.setContent(new String(content));
                }
            }

            logger.debug("File preview generated: {} (type: {})", filePath, previewType);
            return result;

        } catch (FilePermissionException e) {
            logger.warn("File preview failed due to permission: {}", e.getMessage());
            return FilePreviewResult.unavailable("Permission denied");
        } catch (Exception e) {
            logger.error("Unexpected error during file preview: {}", e.getMessage(), e);
            return FilePreviewResult.unavailable("Error generating preview");
        }
    }

    @Override
    public boolean createDirectory(String directoryPath, String userId) {
        try {
            logger.info("Creating directory: {} by user: {}", directoryPath, userId);

            // Validate input
            if (!StringUtils.hasText(directoryPath)) {
                logger.warn("Directory creation failed - empty directory path");
                return false;
            }

            // Sanitize directory path
            directoryPath = sanitizePath(directoryPath);

            // Check parent directory permission
            String parentDirectory = getParentDirectory(directoryPath);
            if (!permissionEvaluator.hasDirectoryPermission(userId, parentDirectory, FileOperation.MKDIR)) {
                throw new FilePermissionException(userId, parentDirectory, "MKDIR");
            }

            // For local storage, we need to create the directory through the file system
            // This is a limitation of the current StorageService interface
            // In a real implementation, we would extend the interface to support directory creation
            
            logger.info("Directory creation requested: {}", directoryPath);
            // TODO: Implement directory creation in StorageService interface
            return true;

        } catch (FilePermissionException e) {
            logger.warn("Directory creation failed due to permission: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error during directory creation: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean deleteDirectory(String directoryPath, String userId, boolean recursive) {
        try {
            logger.info("Deleting directory: {} (recursive: {}) by user: {}", directoryPath, recursive, userId);

            // Validate input
            if (!StringUtils.hasText(directoryPath)) {
                logger.warn("Directory deletion failed - empty directory path");
                return false;
            }

            // Sanitize directory path
            directoryPath = sanitizePath(directoryPath);

            // Check directory permission
            if (!permissionEvaluator.hasDirectoryPermission(userId, directoryPath, FileOperation.RMDIR)) {
                throw new FilePermissionException(userId, directoryPath, "RMDIR");
            }

            // TODO: Implement directory deletion in StorageService interface
            logger.info("Directory deletion requested: {} (recursive: {})", directoryPath, recursive);
            return true;

        } catch (FilePermissionException e) {
            logger.warn("Directory deletion failed due to permission: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error during directory deletion: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean rename(String oldPath, String newName, String userId) {
        try {
            logger.info("Renaming: {} to {} by user: {}", oldPath, newName, userId);

            // Validate input
            if (!StringUtils.hasText(oldPath) || !StringUtils.hasText(newName)) {
                logger.warn("Rename failed - empty old path or new name");
                return false;
            }

            // Sanitize paths
            oldPath = sanitizePath(oldPath);
            newName = fileValidationService.sanitizeFileName(newName);

            // Construct new path
            String parentDirectory = getParentDirectory(oldPath);
            String newPath = parentDirectory.isEmpty() ? newName : parentDirectory + "/" + newName;

            // Use move operation for rename
            return moveFile(oldPath, newPath, userId);

        } catch (Exception e) {
            logger.error("Unexpected error during rename: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean fileExists(String filePath, String userId) {
        try {
            // Validate input
            if (!StringUtils.hasText(filePath)) {
                return false;
            }

            // Sanitize file path
            filePath = sanitizePath(filePath);

            // Check file permission
            if (!permissionEvaluator.canAccessFile(userId, filePath)) {
                return false;
            }

            return storageService.fileExists(filePath);

        } catch (Exception e) {
            logger.error("Error checking file existence: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public long getFileSize(String filePath, String userId) {
        try {
            FileInfo fileInfo = getFileInfo(filePath, userId);
            return fileInfo != null ? fileInfo.getSize() : -1;
        } catch (Exception e) {
            logger.error("Error getting file size: {}", e.getMessage(), e);
            return -1;
        }
    }

    @Override
    public List<FileInfo> searchFiles(String directory, String pattern, String userId, boolean recursive) {
        try {
            logger.debug("Searching files in directory: {} with pattern: {} (recursive: {}) for user: {}", 
                        directory, pattern, recursive, userId);

            // For now, implement basic search by listing files and filtering
            List<FileInfo> allFiles = listFiles(directory, userId);
            
            // Filter by pattern (simple contains match for now)
            return allFiles.stream()
                .filter(file -> file.getName().toLowerCase().contains(pattern.toLowerCase()))
                .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("Error during file search: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    // Helper methods

    private String sanitizePath(String path) {
        if (!StringUtils.hasText(path)) {
            return "";
        }
        
        // Remove leading/trailing slashes and normalize
        path = path.trim();
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        
        // Normalize path separators
        path = path.replace("\\", "/");
        
        // Remove any path traversal attempts
        path = path.replaceAll("\\.\\./", "");
        path = path.replaceAll("/\\.\\.\\.", "");
        
        return path;
    }

    private String getParentDirectory(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return "";
        }
        
        int lastSlash = filePath.lastIndexOf('/');
        if (lastSlash == -1) {
            return "";
        }
        
        return filePath.substring(0, lastSlash);
    }

    private String getFileName(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return "";
        }
        
        int lastSlash = filePath.lastIndexOf('/');
        if (lastSlash == -1) {
            return filePath;
        }
        
        return filePath.substring(lastSlash + 1);
    }

    private String getFileExtension(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "";
        }
        
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1 || lastDot == fileName.length() - 1) {
            return "";
        }
        
        return fileName.substring(lastDot + 1).toLowerCase();
    }

    private boolean isPreviewableFile(String mimeType) {
        if (!StringUtils.hasText(mimeType)) {
            return false;
        }
        
        return mimeType.startsWith("image/") || 
               mimeType.startsWith("text/") ||
               mimeType.equals("application/pdf");
    }

    private FilePreviewResult.PreviewType determinePreviewType(String mimeType) {
        if (!StringUtils.hasText(mimeType)) {
            return FilePreviewResult.PreviewType.NONE;
        }
        
        if (mimeType.startsWith("image/")) {
            return FilePreviewResult.PreviewType.IMAGE;
        } else if (mimeType.startsWith("text/") || mimeType.equals("application/json")) {
            return FilePreviewResult.PreviewType.TEXT;
        } else if (mimeType.equals("application/pdf")) {
            return FilePreviewResult.PreviewType.PDF;
        } else if (mimeType.startsWith("video/")) {
            return FilePreviewResult.PreviewType.VIDEO;
        } else if (mimeType.startsWith("audio/")) {
            return FilePreviewResult.PreviewType.AUDIO;
        } else if (mimeType.startsWith("application/") && 
                  (mimeType.contains("document") || mimeType.contains("word") || 
                   mimeType.contains("excel") || mimeType.contains("powerpoint"))) {
            return FilePreviewResult.PreviewType.DOCUMENT;
        } else if (mimeType.startsWith("application/") && 
                  (mimeType.contains("zip") || mimeType.contains("tar") || 
                   mimeType.contains("rar") || mimeType.contains("7z"))) {
            return FilePreviewResult.PreviewType.ARCHIVE;
        }
        
        return FilePreviewResult.PreviewType.NONE;
    }

    private FileInfo convertToFileInfo(StorageFileInfo storageFile) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setName(storageFile.getName());
        fileInfo.setPath(storageFile.getPath());
        fileInfo.setSize(storageFile.getSize());
        fileInfo.setLastModified(storageFile.getLastModified());
        fileInfo.setMimeType(storageFile.getMimeType());
        fileInfo.setDirectory(storageFile.isDirectory());
        fileInfo.setExtension(getFileExtension(storageFile.getName()));
        
        // Set public URL if available
        String publicUrl = storageService.generatePublicUrl(storageFile.getPath());
        if (StringUtils.hasText(publicUrl)) {
            fileInfo.setPublicUrl(publicUrl);
        }
        
        return fileInfo;
    }
}