package com.zhglxt.fileManager.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Session Integration Service
 * 
 * Handles integration with zhglxt-web session management, ensuring consistent
 * session handling and state management across the file manager module.
 * 
 * @author zhglxt
 */
@Service
public class SessionIntegrationService {

    private static final Logger logger = LoggerFactory.getLogger(SessionIntegrationService.class);

    private static final String FILE_MANAGER_SESSION_PREFIX = "file_manager_";
    private static final String LAST_ACCESS_TIME_KEY = FILE_MANAGER_SESSION_PREFIX + "last_access";
    private static final String USER_PREFERENCES_KEY = FILE_MANAGER_SESSION_PREFIX + "preferences";
    private static final String CURRENT_DIRECTORY_KEY = FILE_MANAGER_SESSION_PREFIX + "current_dir";

    /**
     * Get current HTTP session
     */
    public HttpSession getCurrentSession() {
        try {
            // Use reflection to avoid compile-time dependency on ServletUtils
            Class<?> servletUtilsClass = Class.forName("com.zhglxt.common.utils.ServletUtils");
            java.lang.reflect.Method getRequestMethod = servletUtilsClass.getMethod("getRequest");
            Object request = getRequestMethod.invoke(null);
            if (request != null) {
                java.lang.reflect.Method getSessionMethod = request.getClass().getMethod("getSession", boolean.class);
                return (HttpSession) getSessionMethod.invoke(request, false);
            }
            return null;
        } catch (Exception e) {
            logger.warn("Failed to get current session: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get or create HTTP session
     */
    public HttpSession getOrCreateSession() {
        try {
            // Use reflection to avoid compile-time dependency on ServletUtils
            Class<?> servletUtilsClass = Class.forName("com.zhglxt.common.utils.ServletUtils");
            java.lang.reflect.Method getRequestMethod = servletUtilsClass.getMethod("getRequest");
            Object request = getRequestMethod.invoke(null);
            if (request != null) {
                java.lang.reflect.Method getSessionMethod = request.getClass().getMethod("getSession", boolean.class);
                return (HttpSession) getSessionMethod.invoke(request, true);
            }
            return null;
        } catch (Exception e) {
            logger.warn("Failed to get or create session: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Check if session is valid and active
     */
    public boolean isSessionValid() {
        HttpSession session = getCurrentSession();
        if (session == null) {
            return false;
        }

        try {
            // Check if session is still valid
            session.getLastAccessedTime();
            return true;
        } catch (IllegalStateException e) {
            logger.debug("Session is invalid: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get session attribute
     */
    public Object getSessionAttribute(String key) {
        HttpSession session = getCurrentSession();
        if (session == null) {
            return null;
        }

        try {
            return session.getAttribute(key);
        } catch (Exception e) {
            logger.warn("Failed to get session attribute {}: {}", key, e.getMessage());
            return null;
        }
    }

    /**
     * Set session attribute
     */
    public void setSessionAttribute(String key, Object value) {
        HttpSession session = getOrCreateSession();
        if (session == null) {
            logger.warn("Cannot set session attribute - no session available");
            return;
        }

        try {
            session.setAttribute(key, value);
        } catch (Exception e) {
            logger.warn("Failed to set session attribute {}: {}", key, e.getMessage());
        }
    }

    /**
     * Remove session attribute
     */
    public void removeSessionAttribute(String key) {
        HttpSession session = getCurrentSession();
        if (session == null) {
            return;
        }

        try {
            session.removeAttribute(key);
        } catch (Exception e) {
            logger.warn("Failed to remove session attribute {}: {}", key, e.getMessage());
        }
    }

    /**
     * Update last access time for file manager
     */
    public void updateLastAccessTime() {
        setSessionAttribute(LAST_ACCESS_TIME_KEY, System.currentTimeMillis());
    }

    /**
     * Get last access time for file manager
     */
    public Long getLastAccessTime() {
        Object lastAccess = getSessionAttribute(LAST_ACCESS_TIME_KEY);
        return lastAccess instanceof Long ? (Long) lastAccess : null;
    }

    /**
     * Set user preferences in session
     */
    public void setUserPreferences(Map<String, Object> preferences) {
        setSessionAttribute(USER_PREFERENCES_KEY, preferences);
    }

    /**
     * Get user preferences from session
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getUserPreferences() {
        Object preferences = getSessionAttribute(USER_PREFERENCES_KEY);
        if (preferences instanceof Map) {
            return (Map<String, Object>) preferences;
        }
        return new HashMap<>();
    }

    /**
     * Set current directory in session
     */
    public void setCurrentDirectory(String directory) {
        setSessionAttribute(CURRENT_DIRECTORY_KEY, directory);
    }

    /**
     * Get current directory from session
     */
    public String getCurrentDirectory() {
        Object directory = getSessionAttribute(CURRENT_DIRECTORY_KEY);
        return directory instanceof String ? (String) directory : "/";
    }

    /**
     * Clear file manager session data
     */
    public void clearFileManagerSession() {
        HttpSession session = getCurrentSession();
        if (session == null) {
            return;
        }

        try {
            Enumeration<String> attributeNames = session.getAttributeNames();
            while (attributeNames.hasMoreElements()) {
                String attributeName = attributeNames.nextElement();
                if (attributeName.startsWith(FILE_MANAGER_SESSION_PREFIX)) {
                    session.removeAttribute(attributeName);
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to clear file manager session data: {}", e.getMessage());
        }
    }

    /**
     * Get session ID
     */
    public String getSessionId() {
        HttpSession session = getCurrentSession();
        return session != null ? session.getId() : null;
    }

    /**
     * Check if session is from mobile device
     */
    public boolean isMobileSession() {
        try {
            // Use reflection to avoid compile-time dependency on ServletUtils
            Class<?> servletUtilsClass = Class.forName("com.zhglxt.common.utils.ServletUtils");
            java.lang.reflect.Method getRequestMethod = servletUtilsClass.getMethod("getRequest");
            Object request = getRequestMethod.invoke(null);
            if (request == null) {
                return false;
            }
            
            java.lang.reflect.Method getHeaderMethod = request.getClass().getMethod("getHeader", String.class);
            String userAgent = (String) getHeaderMethod.invoke(request, "User-Agent");
            
            java.lang.reflect.Method checkAgentIsMobileMethod = servletUtilsClass.getMethod("checkAgentIsMobile", String.class);
            return (Boolean) checkAgentIsMobileMethod.invoke(null, userAgent);
        } catch (Exception e) {
            logger.debug("Failed to check mobile session: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get session creation time
     */
    public Long getSessionCreationTime() {
        HttpSession session = getCurrentSession();
        if (session == null) {
            return null;
        }

        try {
            return session.getCreationTime();
        } catch (Exception e) {
            logger.debug("Failed to get session creation time: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get session last accessed time
     */
    public Long getSessionLastAccessedTime() {
        HttpSession session = getCurrentSession();
        if (session == null) {
            return null;
        }

        try {
            return session.getLastAccessedTime();
        } catch (Exception e) {
            logger.debug("Failed to get session last accessed time: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Invalidate current session
     */
    public void invalidateSession() {
        HttpSession session = getCurrentSession();
        if (session == null) {
            return;
        }

        try {
            session.invalidate();
        } catch (Exception e) {
            logger.warn("Failed to invalidate session: {}", e.getMessage());
        }
    }

    /**
     * Get all file manager session attributes
     */
    public Map<String, Object> getFileManagerSessionAttributes() {
        HttpSession session = getCurrentSession();
        if (session == null) {
            return new HashMap<>();
        }

        Map<String, Object> attributes = new HashMap<>();
        try {
            Enumeration<String> attributeNames = session.getAttributeNames();
            while (attributeNames.hasMoreElements()) {
                String attributeName = attributeNames.nextElement();
                if (attributeName.startsWith(FILE_MANAGER_SESSION_PREFIX)) {
                    attributes.put(attributeName, session.getAttribute(attributeName));
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to get file manager session attributes: {}", e.getMessage());
        }

        return attributes;
    }
}