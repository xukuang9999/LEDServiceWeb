package com.zhglxt.fileManager.integration;

import com.zhglxt.fileManager.service.I18nService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple AjaxResult for responses when zhglxt common is not available
 */
class AjaxResult {
    private boolean success;
    private String message;
    private Object data;
    
    public AjaxResult(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }
    
    public static AjaxResult success(Object data) {
        return new AjaxResult(true, "Success", data);
    }
    
    public static AjaxResult error(String message) {
        return new AjaxResult(false, message, null);
    }
    
    // Getters
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Object getData() { return data; }
}

/**
 * File Manager Web Integration Controller
 * 
 * Handles integration endpoints for seamless integration with zhglxt-web,
 * including theme synchronization, authentication validation, and menu integration.
 * 
 * @author zhglxt
 */
@Controller
@RequestMapping("/file-manager")
@ConditionalOnClass(name = "com.zhglxt.common.core.controller.BaseController")
public class FileManagerWebIntegrationController {

    private final AuthenticationIntegrationService authService;
    private final SessionIntegrationService sessionService;
    private final MenuIntegrationService menuService;
    private final ThemeIntegrationService themeService;
    private final I18nService i18nService;

    public FileManagerWebIntegrationController(
            AuthenticationIntegrationService authService,
            SessionIntegrationService sessionService,
            MenuIntegrationService menuService,
            ThemeIntegrationService themeService,
            I18nService i18nService) {
        this.authService = authService;
        this.sessionService = sessionService;
        this.menuService = menuService;
        this.themeService = themeService;
        this.i18nService = i18nService;
    }

    /**
     * File manager main page with zhglxt-web integration
     */
    @GetMapping("/index")
    public String index(ModelMap mmap) {
        // Validate authentication
        if (!authService.isAuthenticated()) {
            return "redirect:/login";
        }

        // Check file manager permissions
        if (!menuService.hasFileManagerPermission()) {
            return "error/403";
        }

        // Get current user
        Object user = authService.getCurrentUser();
        mmap.put("user", user);

        // Add theme configuration
        Map<String, String> themeConfig = themeService.getCurrentThemeConfig();
        mmap.put("sideTheme", themeConfig.get("sideTheme"));
        mmap.put("skinName", themeConfig.get("skinName"));
        mmap.put("mainClass", themeService.getMainContentClass());
        mmap.put("themeCssClasses", themeService.getFileManagerCssClasses());
        mmap.put("themeCssStyle", themeService.getThemeCssStyle());

        // Add layout configuration
        mmap.put("footer", themeService.shouldDisplayFooter());
        mmap.put("tagsView", themeService.shouldDisplayTagsView());

        // Add elFinder configuration
        mmap.put("elFinderConfig", themeService.getElFinderThemeConfig());

        // Add internationalization
        mmap.put("currentLocale", i18nService.getCurrentLocale().toString());
        // Note: getAllMessages() method may not exist, using empty map as fallback
        mmap.put("messages", new HashMap<>());

        // Update session
        sessionService.updateLastAccessTime();
        sessionService.setCurrentDirectory("/");

        return "file-manager/index";
    }

    /**
     * Get current integration status
     */
    @GetMapping("/integration/status")
    @ResponseBody
    public Map<String, Object> getIntegrationStatus() {
        Map<String, Object> status = new HashMap<>();
        
        // Authentication status
        status.put("authenticated", authService.isAuthenticated());
        status.put("user", authService.getCurrentUser());
        status.put("authContext", authService.getAuthenticationContext());

        // Session status
        status.put("sessionValid", sessionService.isSessionValid());
        status.put("sessionId", sessionService.getSessionId());
        status.put("isMobile", sessionService.isMobileSession());
        status.put("lastAccess", sessionService.getLastAccessTime());

        // Menu permissions
        status.put("hasFileManagerPermission", menuService.hasFileManagerPermission());
        status.put("canUpload", menuService.canAccessFeature("upload"));
        status.put("canDownload", menuService.canAccessFeature("download"));
        status.put("canDelete", menuService.canAccessFeature("delete"));

        // Theme configuration
        status.put("themeConfig", themeService.getCurrentThemeConfig());
        status.put("cssClasses", themeService.getFileManagerCssClasses());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", status);
        return result;
    }

    /**
     * Validate user permissions for specific operation
     */
    @GetMapping("/integration/validate-permission")
    @ResponseBody
    public Map<String, Object> validatePermission(String operation, String path) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Validate authentication
            authService.validateAuthentication();
            
            // Check operation permission
            boolean canPerform = authService.canPerformFileOperation(operation);
            result.put("canPerform", canPerform);
            
            // Check path access if provided
            if (path != null && !path.isEmpty()) {
                boolean canAccess = authService.canAccessFile(path);
                result.put("canAccess", canAccess);
                result.put("allowed", canPerform && canAccess);
            } else {
                result.put("allowed", canPerform);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);
            return response;
        } catch (SecurityException e) {
            result.put("allowed", false);
            result.put("error", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("data", result);
            return response;
        }
    }

    /**
     * Get user's file manager configuration
     */
    @GetMapping("/integration/user-config")
    @ResponseBody
    public Map<String, Object> getUserConfig() {
        if (!authService.isAuthenticated()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "User not authenticated");
            return response;
        }

        Map<String, Object> config = new HashMap<>();
        
        // User information
        config.put("userId", authService.getCurrentUserId());
        config.put("username", authService.getCurrentUsername());
        config.put("displayName", authService.getCurrentUsername());
        
        // User storage configuration
        config.put("storageRoot", authService.getUserStorageRoot());
        config.put("currentDirectory", sessionService.getCurrentDirectory());
        
        // User preferences
        config.put("preferences", sessionService.getUserPreferences());
        
        // Permissions
        Map<String, Boolean> permissions = new HashMap<>();
        permissions.put("upload", authService.canPerformFileOperation("upload"));
        permissions.put("download", authService.canPerformFileOperation("download"));
        permissions.put("delete", authService.canPerformFileOperation("delete"));
        permissions.put("move", authService.canPerformFileOperation("move"));
        permissions.put("preview", authService.canPerformFileOperation("preview"));
        config.put("permissions", permissions);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", config);
        return response;
    }

    /**
     * Update user preferences
     */
    @GetMapping("/integration/update-preferences")
    @ResponseBody
    public Map<String, Object> updatePreferences(String key, String value) {
        if (!authService.isAuthenticated()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "User not authenticated");
            return response;
        }

        try {
            Map<String, Object> preferences = sessionService.getUserPreferences();
            preferences.put(key, value);
            sessionService.setUserPreferences(preferences);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Preferences updated successfully");
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to update preferences: " + e.getMessage());
            return response;
        }
    }

    /**
     * Get theme configuration for JavaScript
     */
    @GetMapping("/integration/theme-config")
    @ResponseBody
    public AjaxResult getThemeConfig() {
        Map<String, Object> config = new HashMap<>();
        
        config.put("themeConfig", themeService.getCurrentThemeConfig());
        config.put("cssVariables", themeService.getThemeCssVariables());
        config.put("elFinderConfig", themeService.getElFinderThemeConfig());
        config.put("cssClasses", themeService.getFileManagerCssClasses());
        
        return AjaxResult.success(config);
    }

    /**
     * Health check endpoint for integration
     */
    @GetMapping("/integration/health")
    @ResponseBody
    public AjaxResult healthCheck() {
        Map<String, Object> health = new HashMap<>();
        
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        health.put("authenticated", authService.isAuthenticated());
        health.put("sessionValid", sessionService.isSessionValid());
        health.put("hasPermission", menuService.hasFileManagerPermission());
        
        return AjaxResult.success(health);
    }
}