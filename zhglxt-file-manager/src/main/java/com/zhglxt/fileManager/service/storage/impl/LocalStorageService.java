package com.zhglxt.fileManager.service.storage.impl;

import com.zhglxt.fileManager.config.FileManagerProperties;
import com.zhglxt.fileManager.exception.FileNotFoundException;
import com.zhglxt.fileManager.exception.StorageException;
import com.zhglxt.fileManager.service.storage.StorageFileInfo;
import com.zhglxt.fileManager.service.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Local Storage Service Implementation
 * Handles file operations on the local file system
 * 
 * @author zhglxt
 */
@Service
public class LocalStorageService implements StorageService {

    private static final Logger logger = LoggerFactory.getLogger(LocalStorageService.class);

    private final FileManagerProperties properties;
    private final Path rootPath;

    public LocalStorageService(FileManagerProperties properties) {
        this.properties = properties;
        this.rootPath = Paths.get(properties.getRootDirectory()).toAbsolutePath().normalize();
        initializeRootDirectory();
    }

    /**
     * Initialize root directory
     */
    private void initializeRootDirectory() {
        try {
            if (!Files.exists(rootPath)) {
                Files.createDirectories(rootPath);
                logger.info("Created root directory: {}", rootPath);
            }
        } catch (IOException e) {
            throw new StorageException("INIT_ERROR", "Failed to initialize root directory: " + rootPath, e);
        }
    }

    @Override
    public String storeFile(InputStream inputStream, String fileName, String directory) {
        if (inputStream == null) {
            throw new IllegalArgumentException("Input stream cannot be null");
        }
        if (!StringUtils.hasText(fileName)) {
            throw new IllegalArgumentException("File name cannot be empty");
        }

        try {
            // Sanitize file name
            String sanitizedFileName = sanitizeFileName(fileName);
            
            // Create target directory
            Path targetDir = resolveDirectory(directory);
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
                logger.debug("Created directory: {}", targetDir);
            }

            // Generate unique file name if file already exists
            Path targetFile = targetDir.resolve(sanitizedFileName);
            if (Files.exists(targetFile)) {
                sanitizedFileName = generateUniqueFileName(sanitizedFileName);
                targetFile = targetDir.resolve(sanitizedFileName);
            }

            // Store file
            Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
            
            String relativePath = rootPath.relativize(targetFile).toString().replace('\\', '/');
            logger.info("Stored file: {} -> {}", fileName, relativePath);
            
            return relativePath;

        } catch (IOException e) {
            logger.error("Failed to store file: {}", fileName, e);
            throw new StorageException("STORE_ERROR", "Failed to store file: " + fileName, e);
        }
    }

    @Override
    public InputStream retrieveFile(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            throw new IllegalArgumentException("File path cannot be empty");
        }

        try {
            Path file = resolveFilePath(filePath);
            if (!Files.exists(file)) {
                throw new FileNotFoundException(filePath);
            }
            if (Files.isDirectory(file)) {
                throw new StorageException("INVALID_FILE", "Path is a directory, not a file: " + filePath);
            }

            logger.debug("Retrieving file: {}", filePath);
            return Files.newInputStream(file, StandardOpenOption.READ);

        } catch (IOException e) {
            logger.error("Failed to retrieve file: {}", filePath, e);
            throw new StorageException("RETRIEVE_ERROR", "Failed to retrieve file: " + filePath, e);
        }
    }

    @Override
    public boolean deleteFile(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            throw new IllegalArgumentException("File path cannot be empty");
        }

        try {
            Path file = resolveFilePath(filePath);
            if (!Files.exists(file)) {
                logger.warn("File not found for deletion: {}", filePath);
                return false;
            }

            boolean deleted = Files.deleteIfExists(file);
            if (deleted) {
                logger.info("Deleted file: {}", filePath);
            }
            return deleted;

        } catch (IOException e) {
            logger.error("Failed to delete file: {}", filePath, e);
            throw new StorageException("DELETE_ERROR", "Failed to delete file: " + filePath, e);
        }
    }

    @Override
    public boolean moveFile(String sourcePath, String targetPath) {
        if (!StringUtils.hasText(sourcePath) || !StringUtils.hasText(targetPath)) {
            throw new IllegalArgumentException("Source and target paths cannot be empty");
        }

        try {
            Path source = resolveFilePath(sourcePath);
            Path target = resolveFilePath(targetPath);

            if (!Files.exists(source)) {
                throw new FileNotFoundException(sourcePath);
            }

            // Create target directory if it doesn't exist
            Path targetDir = target.getParent();
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Moved file: {} -> {}", sourcePath, targetPath);
            return true;

        } catch (IOException e) {
            logger.error("Failed to move file: {} -> {}", sourcePath, targetPath, e);
            throw new StorageException("MOVE_ERROR", "Failed to move file: " + sourcePath + " -> " + targetPath, e);
        }
    }

    @Override
    public List<StorageFileInfo> listFiles(String directory) {
        try {
            Path dir = resolveDirectory(directory);
            if (!Files.exists(dir)) {
                logger.debug("Directory not found: {}", directory);
                return new ArrayList<>();
            }
            if (!Files.isDirectory(dir)) {
                throw new StorageException("INVALID_DIRECTORY", "Path is not a directory: " + directory);
            }

            List<StorageFileInfo> fileInfos = new ArrayList<>();
            
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
                for (Path entry : stream) {
                    try {
                        BasicFileAttributes attrs = Files.readAttributes(entry, BasicFileAttributes.class);
                        String relativePath = rootPath.relativize(entry).toString().replace('\\', '/');
                        
                        StorageFileInfo fileInfo = new StorageFileInfo(
                            entry.getFileName().toString(),
                            relativePath,
                            attrs.isDirectory() ? 0 : attrs.size(),
                            attrs.isDirectory(),
                            LocalDateTime.ofInstant(attrs.lastModifiedTime().toInstant(), ZoneId.systemDefault()),
                            attrs.isDirectory() ? "directory" : getMimeType(entry)
                        );
                        
                        fileInfos.add(fileInfo);
                    } catch (IOException e) {
                        logger.warn("Failed to read attributes for file: {}", entry, e);
                    }
                }
            }

            logger.debug("Listed {} files in directory: {}", fileInfos.size(), directory);
            return fileInfos;

        } catch (IOException e) {
            logger.error("Failed to list files in directory: {}", directory, e);
            throw new StorageException("LIST_ERROR", "Failed to list files in directory: " + directory, e);
        }
    }

    @Override
    public String generatePublicUrl(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            throw new IllegalArgumentException("File path cannot be empty");
        }

        // For local storage, return a relative URL that can be served by the web server
        // This assumes the files are accessible via HTTP at /files/ endpoint
        return "/files/" + filePath.replace('\\', '/');
    }

    @Override
    public boolean fileExists(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return false;
        }

        try {
            Path file = resolveFilePath(filePath);
            return Files.exists(file);
        } catch (Exception e) {
            logger.debug("Error checking file existence: {}", filePath, e);
            return false;
        }
    }

    /**
     * Resolve file path relative to root directory
     */
    private Path resolveFilePath(String filePath) {
        Path path = Paths.get(filePath).normalize();
        Path resolved = rootPath.resolve(path).normalize();
        
        // Security check: ensure the resolved path is within the root directory
        if (!resolved.startsWith(rootPath)) {
            throw new StorageException("SECURITY_ERROR", "Path traversal attempt detected: " + filePath);
        }
        
        return resolved;
    }

    /**
     * Resolve directory path relative to root directory
     */
    private Path resolveDirectory(String directory) {
        if (!StringUtils.hasText(directory)) {
            return rootPath;
        }
        return resolveFilePath(directory);
    }

    /**
     * Sanitize file name to prevent security issues
     */
    private String sanitizeFileName(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            throw new IllegalArgumentException("File name cannot be empty");
        }

        // Remove path separators and other dangerous characters
        String sanitized = fileName.replaceAll("[/\\\\:*?\"<>|]", "_");
        
        // Remove leading/trailing dots and spaces
        sanitized = sanitized.trim().replaceAll("^[.\\s]+|[.\\s]+$", "");
        
        // Ensure the file name is not empty after sanitization
        if (!StringUtils.hasText(sanitized)) {
            sanitized = "file_" + System.currentTimeMillis();
        }
        
        return sanitized;
    }

    /**
     * Generate unique file name by appending UUID
     */
    private String generateUniqueFileName(String fileName) {
        String name = fileName;
        String extension = "";
        
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            name = fileName.substring(0, lastDot);
            extension = fileName.substring(lastDot);
        }
        
        return name + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
    }

    /**
     * Get MIME type for file
     */
    private String getMimeType(Path file) {
        try {
            String mimeType = Files.probeContentType(file);
            return mimeType != null ? mimeType : "application/octet-stream";
        } catch (IOException e) {
            logger.debug("Failed to determine MIME type for file: {}", file, e);
            return "application/octet-stream";
        }
    }

    @Override
    public long getFileSize(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return -1;
        }

        try {
            Path file = resolveFilePath(filePath);
            if (!Files.exists(file)) {
                return -1;
            }
            return Files.size(file);
        } catch (IOException e) {
            logger.error("Failed to get file size: {}", filePath, e);
            return -1;
        }
    }
}