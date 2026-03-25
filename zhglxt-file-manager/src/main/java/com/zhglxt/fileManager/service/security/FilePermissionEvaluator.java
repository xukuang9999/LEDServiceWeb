package com.zhglxt.fileManager.service.security;

import com.zhglxt.fileManager.domain.FileOperation;

/**
 * File Permission Evaluator Interface
 * Integrates with Shiro for file operation permissions
 * 
 * @author zhglxt
 */
public interface FilePermissionEvaluator {

    /**
     * Check if user has permission for file operation
     * 
     * @param userId the user ID
     * @param filePath the file path
     * @param operation the file operation
     * @return true if user has permission
     */
    boolean hasFilePermission(String userId, String filePath, FileOperation operation);

    /**
     * Check if user has permission for directory operation
     * 
     * @param userId the user ID
     * @param directoryPath the directory path
     * @param operation the file operation
     * @return true if user has permission
     */
    boolean hasDirectoryPermission(String userId, String directoryPath, FileOperation operation);

    /**
     * Check if user can access file
     * 
     * @param userId the user ID
     * @param filePath the file path
     * @return true if user can access file
     */
    boolean canAccessFile(String userId, String filePath);

    /**
     * Check if user can access directory
     * 
     * @param userId the user ID
     * @param directoryPath the directory path
     * @return true if user can access directory
     */
    boolean canAccessDirectory(String userId, String directoryPath);

    /**
     * Check if user has admin privileges
     * 
     * @param userId the user ID
     * @return true if user has admin privileges
     */
    boolean hasAdminPrivileges(String userId);

    /**
     * Get user's root directory (if user-specific directories are used)
     * 
     * @param userId the user ID
     * @return user's root directory path
     */
    String getUserRootDirectory(String userId);

    /**
     * Check if path is within user's allowed scope
     * 
     * @param userId the user ID
     * @param path the path to check
     * @return true if path is within allowed scope
     */
    boolean isPathAllowed(String userId, String path);
}