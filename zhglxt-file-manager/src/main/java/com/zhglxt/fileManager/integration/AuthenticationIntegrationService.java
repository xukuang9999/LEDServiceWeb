package com.zhglxt.fileManager.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Service;

/**
 * Authentication Integration Service
 * 
 * Handles integration with zhglxt-web authentication system (Apache Shiro),
 * providing seamless authentication and authorization for file manager operations.
 * 
 * @author zhglxt
 */
@Service
@ConditionalOnClass(name = "com.zhglxt.common.utils.ShiroUtils")
public class AuthenticationIntegrationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationIntegrationService.class);

    /**
     * Get current authenticated user
     */
    @SuppressWarnings("unchecked")
    public <T> T getCurrentUser() {
        try {
            // Use reflection to avoid compile-time dependency
            Class<?> shiroUtilsClass = Class.forName("com.zhglxt.common.utils.ShiroUtils");
            java.lang.reflect.Method getSysUserMethod = shiroUtilsClass.getMethod("getSysUser");
            return (T) getSysUserMethod.invoke(null);
        } catch (Exception e) {
            logger.warn("Failed to get current user: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Check if user is authenticated
     */
    public boolean isAuthenticated() {
        try {
            Class<?> securityUtilsClass = Class.forName("org.apache.shiro.SecurityUtils");
            java.lang.reflect.Method getSubjectMethod = securityUtilsClass.getMethod("getSubject");
            Object subject = getSubjectMethod.invoke(null);
            if (subject != null) {
                java.lang.reflect.Method isAuthenticatedMethod = subject.getClass().getMethod("isAuthenticated");
                return (Boolean) isAuthenticatedMethod.invoke(subject);
            }
            return false;
        } catch (Exception e) {
            logger.warn("Failed to check authentication status: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get current user ID
     */
    public Long getCurrentUserId() {
        Object user = getCurrentUser();
        if (user != null) {
            try {
                java.lang.reflect.Method getUserIdMethod = user.getClass().getMethod("getUserId");
                return (Long) getUserIdMethod.invoke(user);
            } catch (Exception e) {
                logger.debug("Failed to get user ID: {}", e.getMessage());
            }
        }
        return null;
    }

    /**
     * Get current username
     */
    public String getCurrentUsername() {
        Object user = getCurrentUser();
        if (user != null) {
            try {
                java.lang.reflect.Method getLoginNameMethod = user.getClass().getMethod("getLoginName");
                return (String) getLoginNameMethod.invoke(user);
            } catch (Exception e) {
                logger.debug("Failed to get username: {}", e.getMessage());
            }
        }
        return null;
    }

    /**
     * Check if current user has specific permission
     */
    public boolean hasPermission(String permission) {
        try {
            Class<?> shiroUtilsClass = Class.forName("com.zhglxt.common.utils.ShiroUtils");
            java.lang.reflect.Method hasPermissionMethod = shiroUtilsClass.getMethod("hasPermission", String.class);
            return (Boolean) hasPermissionMethod.invoke(null, permission);
        } catch (Exception e) {
            logger.debug("Permission check failed for {}: {}", permission, e.getMessage());
            // Default to allow if Shiro is not available or configured
            return true;
        }
    }

    /**
     * Check if current user has specific role
     */
    public boolean hasRole(String role) {
        try {
            Class<?> shiroUtilsClass = Class.forName("com.zhglxt.common.utils.ShiroUtils");
            java.lang.reflect.Method hasRoleMethod = shiroUtilsClass.getMethod("hasRole", String.class);
            return (Boolean) hasRoleMethod.invoke(null, role);
        } catch (Exception e) {
            logger.debug("Role check failed for {}: {}", role, e.getMessage());
            // Default to allow if Shiro is not available or configured
            return true;
        }
    }

    /**
     * Check if current user can perform file operation
     */
    public boolean canPerformFileOperation(String operation) {
        if (!isAuthenticated()) {
            return false;
        }

        // Check specific file operation permissions
        switch (operation.toLowerCase()) {
            case "upload":
                return hasPermission("file:upload") || hasPermission("file:write");
            case "download":
                return hasPermission("file:download") || hasPermission("file:read");
            case "delete":
                return hasPermission("file:delete") || hasPermission("file:remove");
            case "move":
            case "rename":
                return hasPermission("file:move") || hasPermission("file:edit");
            case "preview":
                return hasPermission("file:preview") || hasPermission("file:read");
            case "list":
                return hasPermission("file:list") || hasPermission("file:read");
            default:
                // For unknown operations, check general file permission
                return hasPermission("file:*") || hasPermission("file:manage");
        }
    }

    /**
     * Check if current user can access directory
     */
    public boolean canAccessDirectory(String directoryPath) {
        if (!isAuthenticated()) {
            return false;
        }

        // Check directory-specific permissions
        return hasPermission("file:directory:access") || 
               hasPermission("file:read") || 
               hasPermission("file:*");
    }

    /**
     * Get user's file storage root directory
     */
    public String getUserStorageRoot() {
        String username = getCurrentUsername();
        if (username == null) {
            return null;
        }

        // Return user-specific directory or default
        return "/users/" + username;
    }

    /**
     * Check if user can access specific file path
     */
    public boolean canAccessFile(String filePath) {
        if (!isAuthenticated()) {
            return false;
        }

        Object user = getCurrentUser();
        if (user == null) {
            return false;
        }

        // Admin users can access all files
        if (hasRole("admin") || hasRole("sys")) {
            return true;
        }

        // Check if file is in user's allowed directory
        String userRoot = getUserStorageRoot();
        if (userRoot != null && filePath.startsWith(userRoot)) {
            return true;
        }

        // Check if file is in public directory
        if (filePath.startsWith("/public/")) {
            return true;
        }

        // Check specific file permissions
        return hasPermission("file:access:" + filePath) || hasPermission("file:*");
    }

    /**
     * Validate authentication for file manager access
     */
    public void validateAuthentication() throws SecurityException {
        if (!isAuthenticated()) {
            throw new SecurityException("User not authenticated");
        }

        Object user = getCurrentUser();
        if (user == null) {
            throw new SecurityException("Unable to retrieve user information");
        }
    }

    /**
     * Get authentication context for logging
     */
    public String getAuthenticationContext() {
        String username = getCurrentUsername();
        Long userId = getCurrentUserId();
        if (username != null && userId != null) {
            return String.format("User: %s (ID: %d)", username, userId);
        } else if (username != null) {
            return String.format("User: %s", username);
        }
        return "Anonymous";
    }
}