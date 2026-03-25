package com.zhglxt.fileManager.integration;

import com.zhglxt.system.service.ISysConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Theme Integration Service
 * 
 * Handles integration with zhglxt-web theme system, ensuring consistent styling
 * and visual appearance across the file manager module.
 * 
 * @author zhglxt
 */
@Service
public class ThemeIntegrationService {

    private static final Logger logger = LoggerFactory.getLogger(ThemeIntegrationService.class);

    @Autowired(required = false)
    private ISysConfigService configService;

    /**
     * Get current theme configuration from zhglxt-web
     */
    public Map<String, String> getCurrentThemeConfig() {
        Map<String, String> themeConfig = new HashMap<>();
        
        try {
            if (configService != null) {
                // Get theme settings from system configuration
                String sideTheme = configService.selectConfigByKey("sys.index.sideTheme");
                String skinName = configService.selectConfigByKey("sys.index.skinName");
                String menuStyle = configService.selectConfigByKey("sys.index.menuStyle");
                
                themeConfig.put("sideTheme", sideTheme != null ? sideTheme : "skin-blue");
                themeConfig.put("skinName", skinName != null ? skinName : "skin-blue");
                themeConfig.put("menuStyle", menuStyle != null ? menuStyle : "index");
            } else {
                // Default theme configuration
                themeConfig.put("sideTheme", "skin-blue");
                themeConfig.put("skinName", "skin-blue");
                themeConfig.put("menuStyle", "index");
            }
        } catch (Exception e) {
            logger.warn("Failed to get theme configuration: {}", e.getMessage());
            // Fallback to default theme
            themeConfig.put("sideTheme", "skin-blue");
            themeConfig.put("skinName", "skin-blue");
            themeConfig.put("menuStyle", "index");
        }
        
        return themeConfig;
    }

    /**
     * Get CSS classes for file manager based on current theme
     */
    public String getFileManagerCssClasses() {
        Map<String, String> themeConfig = getCurrentThemeConfig();
        StringBuilder cssClasses = new StringBuilder();
        
        cssClasses.append("file-manager-container ");
        cssClasses.append(themeConfig.get("sideTheme")).append(" ");
        cssClasses.append(themeConfig.get("skinName"));
        
        return cssClasses.toString().trim();
    }

    /**
     * Get theme-specific CSS variables
     */
    public Map<String, String> getThemeCssVariables() {
        Map<String, String> themeConfig = getCurrentThemeConfig();
        Map<String, String> cssVariables = new HashMap<>();
        
        // Map theme names to CSS variables
        String skinName = themeConfig.get("skinName");
        switch (skinName) {
            case "skin-blue":
                cssVariables.put("--primary-color", "#3c8dbc");
                cssVariables.put("--primary-hover", "#367fa9");
                cssVariables.put("--sidebar-bg", "#222d32");
                break;
            case "skin-green":
                cssVariables.put("--primary-color", "#00a65a");
                cssVariables.put("--primary-hover", "#008d4c");
                cssVariables.put("--sidebar-bg", "#222d32");
                break;
            case "skin-purple":
                cssVariables.put("--primary-color", "#605ca8");
                cssVariables.put("--primary-hover", "#555299");
                cssVariables.put("--sidebar-bg", "#222d32");
                break;
            case "skin-red":
                cssVariables.put("--primary-color", "#dd4b39");
                cssVariables.put("--primary-hover", "#d73925");
                cssVariables.put("--sidebar-bg", "#222d32");
                break;
            case "skin-yellow":
                cssVariables.put("--primary-color", "#f39c12");
                cssVariables.put("--primary-hover", "#e08e0b");
                cssVariables.put("--sidebar-bg", "#222d32");
                break;
            default:
                cssVariables.put("--primary-color", "#3c8dbc");
                cssVariables.put("--primary-hover", "#367fa9");
                cssVariables.put("--sidebar-bg", "#222d32");
        }
        
        return cssVariables;
    }

    /**
     * Generate CSS style string for theme variables
     */
    public String getThemeCssStyle() {
        Map<String, String> cssVariables = getThemeCssVariables();
        StringBuilder styleBuilder = new StringBuilder();
        
        for (Map.Entry<String, String> entry : cssVariables.entrySet()) {
            styleBuilder.append(entry.getKey())
                       .append(": ")
                       .append(entry.getValue())
                       .append("; ");
        }
        
        return styleBuilder.toString().trim();
    }

    /**
     * Check if footer should be displayed
     */
    public boolean shouldDisplayFooter() {
        try {
            if (configService != null) {
                String footerConfig = configService.selectConfigByKey("sys.index.footer");
                return !"false".equalsIgnoreCase(footerConfig);
            }
        } catch (Exception e) {
            logger.debug("Failed to get footer configuration: {}", e.getMessage());
        }
        return true; // Default to show footer
    }

    /**
     * Check if tags view should be displayed
     */
    public boolean shouldDisplayTagsView() {
        try {
            if (configService != null) {
                String tagsViewConfig = configService.selectConfigByKey("sys.index.tagsView");
                return !"false".equalsIgnoreCase(tagsViewConfig);
            }
        } catch (Exception e) {
            logger.debug("Failed to get tags view configuration: {}", e.getMessage());
        }
        return true; // Default to show tags view
    }

    /**
     * Get main content CSS class based on footer and tags view settings
     */
    public String getMainContentClass() {
        boolean footer = shouldDisplayFooter();
        boolean tagsView = shouldDisplayTagsView();
        
        if (!footer && !tagsView) {
            return "tagsview-footer-hide";
        } else if (!footer) {
            return "footer-hide";
        } else if (!tagsView) {
            return "tagsview-hide";
        }
        return "";
    }

    /**
     * Get elFinder theme configuration
     */
    public Map<String, Object> getElFinderThemeConfig() {
        Map<String, Object> elFinderConfig = new HashMap<>();
        Map<String, String> themeConfig = getCurrentThemeConfig();
        
        // Configure elFinder theme based on current zhglxt theme
        elFinderConfig.put("theme", "material");
        elFinderConfig.put("cssAutoLoad", false); // We'll load our custom CSS
        
        // Add theme-specific configurations
        String skinName = themeConfig.get("skinName");
        switch (skinName) {
            case "skin-blue":
                elFinderConfig.put("uiOptions", Map.of("toolbar", Map.of("theme", "blue")));
                break;
            case "skin-green":
                elFinderConfig.put("uiOptions", Map.of("toolbar", Map.of("theme", "green")));
                break;
            case "skin-purple":
                elFinderConfig.put("uiOptions", Map.of("toolbar", Map.of("theme", "purple")));
                break;
            case "skin-red":
                elFinderConfig.put("uiOptions", Map.of("toolbar", Map.of("theme", "red")));
                break;
            case "skin-yellow":
                elFinderConfig.put("uiOptions", Map.of("toolbar", Map.of("theme", "yellow")));
                break;
            default:
                elFinderConfig.put("uiOptions", Map.of("toolbar", Map.of("theme", "blue")));
        }
        
        return elFinderConfig;
    }
}