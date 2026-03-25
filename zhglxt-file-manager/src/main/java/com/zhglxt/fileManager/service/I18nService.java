package com.zhglxt.fileManager.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Locale;

/**
 * Internationalization Service for File Manager
 * 
 * Provides dynamic message resolution with locale support for English and Chinese.
 * Integrates with Spring's MessageSource and LocaleResolver for consistent
 * internationalization across the file manager module.
 * 
 * @author zhglxt
 */
@Service
public class I18nService {
    
    private static final Logger logger = LoggerFactory.getLogger(I18nService.class);
    
    @Autowired
    @Qualifier("fileManagerMessageSource")
    private MessageSource messageSource;
    
    /**
     * Get localized message using current locale from context
     * 
     * @param key Message key
     * @return Localized message
     */
    public String getMessage(String key) {
        return getMessage(key, null, getCurrentLocale());
    }
    
    /**
     * Get localized message with arguments using current locale from context
     * 
     * @param key Message key
     * @param args Message arguments
     * @return Localized message
     */
    public String getMessage(String key, Object[] args) {
        return getMessage(key, args, getCurrentLocale());
    }
    
    /**
     * Get localized message for specific locale
     * 
     * @param key Message key
     * @param locale Target locale
     * @return Localized message
     */
    public String getMessage(String key, Locale locale) {
        return getMessage(key, null, locale);
    }
    
    /**
     * Get localized message with arguments for specific locale
     * 
     * @param key Message key
     * @param args Message arguments
     * @param locale Target locale
     * @return Localized message
     */
    public String getMessage(String key, Object[] args, Locale locale) {
        try {
            return messageSource.getMessage(key, args, key, locale);
        } catch (Exception e) {
            logger.warn("Failed to get localized message for key: {} with locale: {}", key, locale, e);
            return key; // Return key as fallback
        }
    }
    
    /**
     * Get current locale from various sources
     * Priority: Request parameter > Session > LocaleContextHolder > Default (English)
     * 
     * @return Current locale
     */
    public Locale getCurrentLocale() {
        try {
            // Try to get locale from request context
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return getLocaleFromRequest(request);
            }
            
            // Fallback to LocaleContextHolder
            Locale locale = LocaleContextHolder.getLocale();
            if (locale != null) {
                return locale;
            }
        } catch (Exception e) {
            logger.debug("Failed to get locale from context, using default", e);
        }
        
        // Default to English
        return Locale.ENGLISH;
    }
    
    /**
     * Get locale from HTTP request
     * Checks request parameter 'lang', then Accept-Language header
     * 
     * @param request HTTP request
     * @return Locale from request
     */
    public Locale getLocaleFromRequest(HttpServletRequest request) {
        if (request == null) {
            return Locale.ENGLISH;
        }
        
        try {
            // Check for explicit language parameter
            String langParam = request.getParameter("lang");
            if (langParam != null && !langParam.trim().isEmpty()) {
                return parseLocale(langParam);
            }
            
            // Check Accept-Language header
            Locale headerLocale = request.getLocale();
            if (headerLocale != null) {
                return normalizeLocale(headerLocale);
            }
        } catch (Exception e) {
            logger.debug("Failed to get locale from request, using default", e);
        }
        
        return Locale.ENGLISH;
    }
    
    /**
     * Parse locale string to Locale object
     * Supports formats: en, zh, en_US, zh_CN
     * 
     * @param localeString Locale string
     * @return Parsed locale
     */
    public Locale parseLocale(String localeString) {
        if (localeString == null || localeString.trim().isEmpty()) {
            return Locale.ENGLISH;
        }
        
        String normalized = localeString.trim().toLowerCase();
        
        // Handle common locale formats
        switch (normalized) {
            case "en":
            case "en_us":
            case "english":
                return Locale.ENGLISH;
            case "zh":
            case "zh_cn":
            case "chinese":
                return Locale.SIMPLIFIED_CHINESE;
            default:
                try {
                    // Try to parse as standard locale format
                    String[] parts = normalized.split("_");
                    if (parts.length == 1) {
                        return new Locale(parts[0]);
                    } else if (parts.length == 2) {
                        return new Locale(parts[0], parts[1].toUpperCase());
                    }
                } catch (Exception e) {
                    logger.debug("Failed to parse locale: {}", localeString, e);
                }
                return Locale.ENGLISH;
        }
    }
    
    /**
     * Normalize locale to supported locales
     * Maps various Chinese locales to Simplified Chinese
     * Maps various English locales to English
     * 
     * @param locale Input locale
     * @return Normalized locale
     */
    public Locale normalizeLocale(Locale locale) {
        if (locale == null) {
            return Locale.ENGLISH;
        }
        
        String language = locale.getLanguage().toLowerCase();
        
        switch (language) {
            case "zh":
                return Locale.SIMPLIFIED_CHINESE;
            case "en":
                return Locale.ENGLISH;
            default:
                return Locale.ENGLISH;
        }
    }
    
    /**
     * Check if locale is supported
     * 
     * @param locale Locale to check
     * @return true if supported
     */
    public boolean isLocaleSupported(Locale locale) {
        if (locale == null) {
            return false;
        }
        
        String language = locale.getLanguage().toLowerCase();
        return "en".equals(language) || "zh".equals(language);
    }
    
    /**
     * Get supported locales
     * 
     * @return Array of supported locales
     */
    public Locale[] getSupportedLocales() {
        return new Locale[]{Locale.ENGLISH, Locale.SIMPLIFIED_CHINESE};
    }
    
    /**
     * Get locale display name in the locale's own language
     * 
     * @param locale Target locale
     * @return Display name
     */
    public String getLocaleDisplayName(Locale locale) {
        if (locale == null) {
            return "English";
        }
        
        switch (locale.getLanguage().toLowerCase()) {
            case "zh":
                return "中文";
            case "en":
            default:
                return "English";
        }
    }
    
    /**
     * Get all messages for current locale
     * This is a placeholder method for compatibility
     * 
     * @return Empty map (placeholder implementation)
     */
    public java.util.Map<String, String> getAllMessages() {
        return java.util.Collections.emptyMap();
    }
}