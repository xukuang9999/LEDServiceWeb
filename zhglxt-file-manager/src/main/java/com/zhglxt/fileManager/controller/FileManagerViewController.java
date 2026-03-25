package com.zhglxt.fileManager.controller;

import com.zhglxt.fileManager.config.FileManagerProperties;
import com.zhglxt.fileManager.integration.AuthenticationIntegrationService;
import com.zhglxt.fileManager.integration.MenuIntegrationService;
import com.zhglxt.fileManager.integration.SessionIntegrationService;
import com.zhglxt.fileManager.integration.ThemeIntegrationService;
import com.zhglxt.fileManager.service.I18nService;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * File Manager View Controller
 * Serves web interface pages with Thymeleaf templates integrated with zhglxt-web
 * 
 * @author zhglxt
 */
@Controller
@RequestMapping("/file-manager")
@RequiresAuthentication
public class FileManagerViewController {

    private static final Logger logger = LoggerFactory.getLogger(FileManagerViewController.class);

    @Autowired
    private FileManagerProperties fileManagerProperties;

    @Autowired
    private AuthenticationIntegrationService authIntegrationService;

    @Autowired
    private SessionIntegrationService sessionIntegrationService;

    @Autowired
    private MenuIntegrationService menuIntegrationService;

    @Autowired
    private ThemeIntegrationService themeService;

    @Autowired
    private I18nService i18nService;

    /**
     * Main file manager interface with zhglxt-web integration
     */
    @GetMapping
    @RequiresPermissions("file:read")
    public String index(Model model, HttpServletRequest request) {
        logger.info("Loading file manager interface with zhglxt-web integration");

        // Check permissions
        if (!menuIntegrationService.hasFileManagerPermission()) {
            return "error/403";
        }

        // Get current user
        Object user = authIntegrationService.getCurrentUser();
        model.addAttribute("user", user);
        model.addAttribute("userId", authIntegrationService.getCurrentUserId() != null ? authIntegrationService.getCurrentUserId().toString() : "anonymous");

        // Add zhglxt-web theme integration
        Map<String, String> themeConfig = themeService.getCurrentThemeConfig();
        model.addAttribute("sideTheme", themeConfig.get("sideTheme"));
        model.addAttribute("skinName", themeConfig.get("skinName"));
        model.addAttribute("mainClass", themeService.getMainContentClass());
        model.addAttribute("themeCssClasses", themeService.getFileManagerCssClasses());
        model.addAttribute("themeCssStyle", themeService.getThemeCssStyle());

        // Add layout configuration
        model.addAttribute("footer", themeService.shouldDisplayFooter());
        model.addAttribute("tagsView", themeService.shouldDisplayTagsView());

        // Add configuration to model
        model.addAttribute("config", createClientConfig());
        
        // Add internationalization
        model.addAttribute("currentLocale", i18nService.getCurrentLocale().toString());
        // Note: getAllMessages() method may not exist, using empty map as fallback
        model.addAttribute("messages", new HashMap<>());
        
        // Add page title and breadcrumb
        model.addAttribute("pageTitle", i18nService.getMessage("file.manager.title"));
        model.addAttribute("breadcrumb", i18nService.getMessage("file.manager.breadcrumb"));

        // Update session
        sessionIntegrationService.updateLastAccessTime();
        sessionIntegrationService.setCurrentDirectory("/");

        return "file-manager/index";
    }

    /**
     * File manager popup interface
     */
    @GetMapping("/popup")
    @RequiresPermissions("file:read")
    public String popup(Model model, HttpServletRequest request) {
        logger.info("Loading file manager popup interface with zhglxt-web integration");

        // Check permissions
        if (!menuIntegrationService.hasFileManagerPermission()) {
            return "error/403";
        }

        // Get current user
        Object user = authIntegrationService.getCurrentUser();
        model.addAttribute("user", user);
        model.addAttribute("userId", authIntegrationService.getCurrentUserId() != null ? authIntegrationService.getCurrentUserId().toString() : "anonymous");

        // Add theme integration for popup
        model.addAttribute("themeCssClasses", themeService.getFileManagerCssClasses());
        model.addAttribute("themeCssStyle", themeService.getThemeCssStyle());

        // Add configuration to model
        model.addAttribute("config", createClientConfig());
        
        // Add internationalization
        model.addAttribute("currentLocale", i18nService.getCurrentLocale().toString());
        model.addAttribute("messages", i18nService.getAllMessages());

        return "file-manager/popup";
    }

    /**
     * File browser interface
     */
    @GetMapping("/browser")
    @RequiresPermissions("file:read")
    public String browser(Model model, HttpServletRequest request) {
        logger.info("Loading file browser interface with zhglxt-web integration");

        // Check permissions
        if (!menuIntegrationService.hasFileManagerPermission()) {
            return "error/403";
        }

        // Get current user
        Object user = authIntegrationService.getCurrentUser();
        model.addAttribute("user", user);
        model.addAttribute("userId", authIntegrationService.getCurrentUserId() != null ? authIntegrationService.getCurrentUserId().toString() : "anonymous");

        // Add minimal theme integration for browser
        model.addAttribute("themeCssClasses", themeService.getFileManagerCssClasses());
        model.addAttribute("themeCssStyle", themeService.getThemeCssStyle());

        // Add configuration to model
        model.addAttribute("config", createClientConfig());
        
        // Add internationalization
        model.addAttribute("currentLocale", i18nService.getCurrentLocale().toString());

        return "file-manager/browser";
    }

    /**
     * Demo page
     */
    @GetMapping("/demo")
    @RequiresPermissions("file:read")
    public String demo(Model model, HttpServletRequest request) {
        logger.info("Loading file manager demo page with zhglxt-web integration");

        // Check permissions
        if (!menuIntegrationService.hasFileManagerPermission()) {
            return "error/403";
        }

        // Get current user
        Object user = authIntegrationService.getCurrentUser();
        model.addAttribute("user", user);

        // Add zhglxt-web theme integration
        Map<String, String> themeConfig = themeService.getCurrentThemeConfig();
        model.addAttribute("sideTheme", themeConfig.get("sideTheme"));
        model.addAttribute("skinName", themeConfig.get("skinName"));
        model.addAttribute("mainClass", themeService.getMainContentClass());
        model.addAttribute("themeCssClasses", themeService.getFileManagerCssClasses());

        // Add internationalization
        model.addAttribute("currentLocale", i18nService.getCurrentLocale().toString());
        model.addAttribute("messages", i18nService.getAllMessages());

        // Add page title and breadcrumb
        model.addAttribute("pageTitle", i18nService.getMessage("file.manager.demo.title"));
        model.addAttribute("breadcrumb", i18nService.getMessage("file.manager.demo.breadcrumb"));

        return "file-manager/demo";
    }

    /**
     * Create client configuration for elFinder with zhglxt-web integration
     */
    private Map<String, Object> createClientConfig() {
        Map<String, Object> config = new HashMap<>();
        
        // Basic configuration
        config.put("url", "/api/file-manager/connector");
        config.put("lang", i18nService.getCurrentLocale().getLanguage());
        config.put("width", "100%");
        config.put("height", sessionIntegrationService.isMobileSession() ? "400px" : "600px");
        config.put("resizable", true);
        
        // Theme integration
        config.putAll(themeService.getElFinderThemeConfig());
        
        // UI configuration with permission-based toolbar
        Map<String, Object> uiOptions = new HashMap<>();
        uiOptions.put("toolbar", createPermissionBasedToolbar());
        uiOptions.put("tree", Map.of("openRootOnLoad", true, "syncTree", true));
        uiOptions.put("navbar", Map.of("minWidth", 150, "maxWidth", 500));
        config.put("uiOptions", uiOptions);
        
        // Upload configuration
        Map<String, Object> uploadConfig = new HashMap<>();
        uploadConfig.put("maxFileSize", fileManagerProperties.getMaxFileSize());
        uploadConfig.put("allowedExtensions", fileManagerProperties.getAllowedExtensions());
        uploadConfig.put("enabled", authIntegrationService.canPerformFileOperation("upload"));
        config.put("upload", uploadConfig);
        
        // User-specific configuration
        Long userId = authIntegrationService.getCurrentUserId();
        if (userId != null) {
            config.put("userId", userId.toString());
            config.put("userRoot", authIntegrationService.getUserStorageRoot());
        }
        
        // Debug mode
        config.put("debug", logger.isDebugEnabled());
        
        return config;
    }

    /**
     * Create permission-based toolbar configuration
     */
    private String[][] createPermissionBasedToolbar() {
        java.util.List<String[]> toolbar = new java.util.ArrayList<>();
        
        // Navigation buttons (always available)
        toolbar.add(new String[]{"back", "forward"});
        toolbar.add(new String[]{"reload"});
        toolbar.add(new String[]{"home", "up"});
        
        // File creation buttons (based on permissions)
        if (authIntegrationService.canPerformFileOperation("upload")) {
            toolbar.add(new String[]{"mkdir", "mkfile", "upload"});
        }
        
        // File access buttons
        if (authIntegrationService.canPerformFileOperation("download")) {
            toolbar.add(new String[]{"open", "download", "getfile"});
        }
        
        // Info button (always available)
        toolbar.add(new String[]{"info"});
        toolbar.add(new String[]{"quicklook"});
        
        // File manipulation buttons (based on permissions)
        if (authIntegrationService.canPerformFileOperation("move")) {
            toolbar.add(new String[]{"copy", "cut", "paste"});
        }
        
        if (authIntegrationService.canPerformFileOperation("delete")) {
            toolbar.add(new String[]{"rm"});
        }
        
        if (authIntegrationService.canPerformFileOperation("move")) {
            toolbar.add(new String[]{"duplicate", "rename", "edit", "resize"});
        }
        
        // Archive buttons
        toolbar.add(new String[]{"extract", "archive"});
        
        // View buttons (always available)
        toolbar.add(new String[]{"search"});
        toolbar.add(new String[]{"view", "sort"});
        toolbar.add(new String[]{"help"});
        
        return toolbar.toArray(new String[0][]);
    }
}