package com.zhglxt.fileManager.integration;

import com.zhglxt.system.service.ISysMenuService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Menu Integration Service
 * 
 * Handles integration with zhglxt-web navigation system, including menu registration,
 * permission checking, and dynamic menu updates.
 * 
 * @author zhglxt
 */
@Service
@ConditionalOnClass(name = "com.zhglxt.system.service.ISysMenuService")
public class MenuIntegrationService {

    private static final Logger logger = LoggerFactory.getLogger(MenuIntegrationService.class);

    @Autowired(required = false)
    private ISysMenuService menuService;

    /**
     * Check if user has permission to access file manager
     */
    public boolean hasFileManagerPermission() {
        try {
            // Get current user using reflection
            Class<?> shiroUtilsClass = Class.forName("com.zhglxt.common.utils.ShiroUtils");
            java.lang.reflect.Method getSysUserMethod = shiroUtilsClass.getMethod("getSysUser");
            Object user = getSysUserMethod.invoke(null);
            
            if (user == null || menuService == null) {
                return false;
            }

            // Check if user has file manager menu permission using reflection
            java.lang.reflect.Method selectMenusByUserMethod = menuService.getClass().getMethod("selectMenusByUser", user.getClass());
            @SuppressWarnings("unchecked")
            List<Object> menus = (List<Object>) selectMenusByUserMethod.invoke(menuService, user);
            
            return menus.stream()
                    .anyMatch(menu -> {
                        try {
                            java.lang.reflect.Method getUrlMethod = menu.getClass().getMethod("getUrl");
                            String url = (String) getUrlMethod.invoke(menu);
                            return url != null && (url.contains("/file-manager") || url.contains("/fileManager"));
                        } catch (Exception e) {
                            return false;
                        }
                    });
        } catch (Exception e) {
            logger.warn("Failed to check file manager permission: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get file manager menu items for current user
     */
    public List<Object> getFileManagerMenus() {
        try {
            // Get current user using reflection
            Class<?> shiroUtilsClass = Class.forName("com.zhglxt.common.utils.ShiroUtils");
            java.lang.reflect.Method getSysUserMethod = shiroUtilsClass.getMethod("getSysUser");
            Object user = getSysUserMethod.invoke(null);
            
            if (user == null || menuService == null) {
                return List.of();
            }

            // Get menus using reflection
            java.lang.reflect.Method selectMenusByUserMethod = menuService.getClass().getMethod("selectMenusByUser", user.getClass());
            @SuppressWarnings("unchecked")
            List<Object> allMenus = (List<Object>) selectMenusByUserMethod.invoke(menuService, user);
            
            return allMenus.stream()
                    .filter(menu -> {
                        try {
                            java.lang.reflect.Method getUrlMethod = menu.getClass().getMethod("getUrl");
                            String url = (String) getUrlMethod.invoke(menu);
                            return url != null && (url.contains("/file-manager") || url.contains("/fileManager"));
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .toList();
        } catch (Exception e) {
            logger.warn("Failed to get file manager menus: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Check if file manager menu should be displayed
     */
    public boolean shouldDisplayFileManagerMenu() {
        return hasFileManagerPermission();
    }

    /**
     * Get file manager menu URL based on user permissions
     */
    public String getFileManagerMenuUrl() {
        if (hasFileManagerPermission()) {
            return "/file-manager/index";
        }
        return null;
    }

    /**
     * Get file manager menu icon
     */
    public String getFileManagerMenuIcon() {
        return "fa fa-folder-open";
    }

    /**
     * Get file manager menu name
     */
    public String getFileManagerMenuName() {
        return "文件管理";
    }

    /**
     * Check if current user can access specific file manager feature
     */
    public boolean canAccessFeature(String feature) {
        if (!hasFileManagerPermission()) {
            return false;
        }

        try {
            // Get current user using reflection
            Class<?> shiroUtilsClass = Class.forName("com.zhglxt.common.utils.ShiroUtils");
            java.lang.reflect.Method getSysUserMethod = shiroUtilsClass.getMethod("getSysUser");
            Object user = getSysUserMethod.invoke(null);
            
            if (user == null) {
                return false;
            }

            // Check specific feature permissions
            switch (feature) {
                case "upload":
                    return hasPermission("file:upload");
                case "download":
                    return hasPermission("file:download");
                case "delete":
                    return hasPermission("file:delete");
                case "move":
                    return hasPermission("file:move");
                case "preview":
                    return hasPermission("file:preview");
                default:
                    return hasFileManagerPermission();
            }
        } catch (Exception e) {
            logger.warn("Failed to check feature permission for {}: {}", feature, e.getMessage());
            return false;
        }
    }

    /**
     * Check if user has specific permission
     */
    private boolean hasPermission(String permission) {
        try {
            Class<?> shiroUtilsClass = Class.forName("com.zhglxt.common.utils.ShiroUtils");
            java.lang.reflect.Method hasPermissionMethod = shiroUtilsClass.getMethod("hasPermission", String.class);
            return (Boolean) hasPermissionMethod.invoke(null, permission);
        } catch (Exception e) {
            logger.debug("Permission check failed for {}: {}", permission, e.getMessage());
            return true; // Default to allow if permission system is not available
        }
    }
}