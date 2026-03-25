package com.zhglxt.fileManager.service.security.impl;

import com.zhglxt.fileManager.config.FileManagerProperties;
import com.zhglxt.fileManager.domain.FileOperation;
import com.zhglxt.fileManager.service.security.FilePermissionEvaluator;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Shiro-based File Permission Evaluator Implementation
 * 
 * @author zhglxt
 */
@Service
public class ShiroFilePermissionEvaluator implements FilePermissionEvaluator {

    private static final Logger logger = LoggerFactory.getLogger(ShiroFilePermissionEvaluator.class);

    // Permission strings for Shiro
    private static final String FILE_MANAGER_PERMISSION = "file-manager";
    private static final String ADMIN_PERMISSION = "file-manager:admin";
    private static final String READ_PERMISSION = "file-manager:read";
    private static final String WRITE_PERMISSION = "file-manager:write";
    private static final String DELETE_PERMISSION = "file-manager:delete";

    private final FileManagerProperties properties;

    @Autowired
    public ShiroFilePermissionEvaluator(FileManagerProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean hasFilePermission(String userId, String filePath, FileOperation operation) {
        try {
            Subject subject = SecurityUtils.getSubject();
            
            // Check if user is authenticated
            if (!subject.isAuthenticated()) {
                logger.warn("Unauthenticated access attempt for file: {}", filePath);
                return false;
            }

            // Check if path is allowed
            if (!isPathAllowed(userId, filePath)) {
                logger.warn("Access denied to path outside allowed scope: {} for user: {}", filePath, userId);
                return false;
            }

            // Check admin privileges first
            if (hasAdminPrivileges(userId)) {
                return true;
            }

            // Check specific operation permissions
            String permission = getPermissionForOperation(operation);
            boolean hasPermission = subject.isPermitted(permission);
            
            if (!hasPermission) {
                logger.warn("Permission denied for operation {} on file {} for user {}", operation, filePath, userId);
            }

            return hasPermission;

        } catch (Exception e) {
            logger.error("Error checking file permission for user {} on file {}: {}", userId, filePath, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean hasDirectoryPermission(String userId, String directoryPath, FileOperation operation) {
        try {
            Subject subject = SecurityUtils.getSubject();
            
            // Check if user is authenticated
            if (!subject.isAuthenticated()) {
                logger.warn("Unauthenticated access attempt for directory: {}", directoryPath);
                return false;
            }

            // Check if path is allowed
            if (!isPathAllowed(userId, directoryPath)) {
                logger.warn("Access denied to directory outside allowed scope: {} for user: {}", directoryPath, userId);
                return false;
            }

            // Check admin privileges first
            if (hasAdminPrivileges(userId)) {
                return true;
            }

            // Check specific operation permissions
            String permission = getPermissionForOperation(operation);
            boolean hasPermission = subject.isPermitted(permission);
            
            if (!hasPermission) {
                logger.warn("Permission denied for operation {} on directory {} for user {}", operation, directoryPath, userId);
            }

            return hasPermission;

        } catch (Exception e) {
            logger.error("Error checking directory permission for user {} on directory {}: {}", userId, directoryPath, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean canAccessFile(String userId, String filePath) {
        return hasFilePermission(userId, filePath, FileOperation.READ);
    }

    @Override
    public boolean canAccessDirectory(String userId, String directoryPath) {
        return hasDirectoryPermission(userId, directoryPath, FileOperation.READ);
    }

    @Override
    public boolean hasAdminPrivileges(String userId) {
        try {
            Subject subject = SecurityUtils.getSubject();
            return subject.isAuthenticated() && subject.isPermitted(ADMIN_PERMISSION);
        } catch (Exception e) {
            logger.error("Error checking admin privileges for user {}: {}", userId, e.getMessage());
            return false;
        }
    }

    @Override
    public String getUserRootDirectory(String userId) {
        // For now, return the configured root directory
        // In the future, this could be user-specific
        return properties.getRootDirectory();
    }

    @Override
    public boolean isPathAllowed(String userId, String path) {
        try {
            if (path == null || path.trim().isEmpty()) {
                return false;
            }

            // Normalize the path
            Path normalizedPath = Paths.get(path).normalize();
            String normalizedPathString = normalizedPath.toString();

            // Check for path traversal attempts
            if (normalizedPathString.contains("..") || normalizedPathString.startsWith("/")) {
                logger.warn("Path traversal attempt detected: {} for user: {}", path, userId);
                return false;
            }

            // Get user's root directory
            String userRootDirectory = getUserRootDirectory(userId);
            Path userRootPath = Paths.get(userRootDirectory).normalize();

            // Check if the path is within the user's allowed directory
            Path fullPath = userRootPath.resolve(normalizedPath).normalize();
            
            // Ensure the resolved path is still within the user's root directory
            if (!fullPath.startsWith(userRootPath)) {
                logger.warn("Path outside allowed scope: {} for user: {}", path, userId);
                return false;
            }

            return true;

        } catch (Exception e) {
            logger.error("Error validating path {} for user {}: {}", path, userId, e.getMessage());
            return false;
        }
    }

    private String getPermissionForOperation(FileOperation operation) {
        switch (operation) {
            case READ:
            case LIST:
            case PREVIEW:
                return READ_PERMISSION;
            case WRITE:
            case UPLOAD:
            case MOVE:
            case COPY:
                return WRITE_PERMISSION;
            case DELETE:
                return DELETE_PERMISSION;
            default:
                return FILE_MANAGER_PERMISSION;
        }
    }
}