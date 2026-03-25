package com.zhglxt.fileManager.util;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Locale;

/**
 * Utility class for elFinder internationalization support
 * 
 * Provides helper methods for configuring elFinder frontend localization
 * and mapping between Java locales and elFinder language codes.
 * 
 * @author zhglxt
 */
@Component
public class ElFinderI18nUtil {
    
    /**
     * Get elFinder language code from Java locale
     * Maps Java locales to elFinder supported language codes
     * 
     * @param locale Java locale
     * @return elFinder language code
     */
    public String getElFinderLanguageCode(Locale locale) {
        if (locale == null) {
            return "en";
        }
        
        String language = locale.getLanguage().toLowerCase();
        
        switch (language) {
            case "zh":
                return "zh";
            case "en":
            default:
                return "en";
        }
    }
    
    /**
     * Get elFinder language code from HTTP request
     * Checks request parameter 'lang', then Accept-Language header
     * 
     * @param request HTTP request
     * @return elFinder language code
     */
    public String getElFinderLanguageCode(HttpServletRequest request) {
        if (request == null) {
            return "en";
        }
        
        // Check for explicit language parameter
        String langParam = request.getParameter("lang");
        if (langParam != null && !langParam.trim().isEmpty()) {
            return getElFinderLanguageCode(parseLocale(langParam));
        }
        
        // Check Accept-Language header
        Locale headerLocale = request.getLocale();
        if (headerLocale != null) {
            return getElFinderLanguageCode(headerLocale);
        }
        
        return "en";
    }
    
    /**
     * Generate elFinder configuration JavaScript for localization
     * 
     * @param languageCode elFinder language code
     * @return JavaScript configuration string
     */
    public String generateElFinderI18nConfig(String languageCode) {
        StringBuilder config = new StringBuilder();
        config.append("{\n");
        config.append("    lang: '").append(languageCode).append("',\n");
        config.append("    customData: {\n");
        config.append("        lang: '").append(languageCode).append("'\n");
        config.append("    }\n");
        config.append("}");
        return config.toString();
    }
    
    /**
     * Get JavaScript file path for elFinder localization
     * 
     * @param languageCode elFinder language code
     * @return JavaScript file path
     */
    public String getElFinderI18nScriptPath(String languageCode) {
        return "/js/i18n/elfinder." + languageCode + ".js";
    }
    
    /**
     * Check if language code is supported by elFinder
     * 
     * @param languageCode elFinder language code
     * @return true if supported
     */
    public boolean isElFinderLanguageSupported(String languageCode) {
        if (languageCode == null || languageCode.trim().isEmpty()) {
            return false;
        }
        
        String normalized = languageCode.trim().toLowerCase();
        return "en".equals(normalized) || "zh".equals(normalized);
    }
    
    /**
     * Get supported elFinder language codes
     * 
     * @return Array of supported language codes
     */
    public String[] getSupportedElFinderLanguages() {
        return new String[]{"en", "zh"};
    }
    
    /**
     * Get display name for elFinder language code
     * 
     * @param languageCode elFinder language code
     * @return Display name
     */
    public String getElFinderLanguageDisplayName(String languageCode) {
        if (languageCode == null) {
            return "English";
        }
        
        switch (languageCode.toLowerCase()) {
            case "zh":
                return "中文";
            case "en":
            default:
                return "English";
        }
    }
    
    /**
     * Parse locale string to Locale object
     * 
     * @param localeString Locale string
     * @return Parsed locale
     */
    private Locale parseLocale(String localeString) {
        if (localeString == null || localeString.trim().isEmpty()) {
            return Locale.ENGLISH;
        }
        
        String normalized = localeString.trim().toLowerCase();
        
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
                    String[] parts = normalized.split("_");
                    if (parts.length == 1) {
                        return new Locale(parts[0]);
                    } else if (parts.length == 2) {
                        return new Locale(parts[0], parts[1].toUpperCase());
                    }
                } catch (Exception e) {
                    // Ignore parsing errors
                }
                return Locale.ENGLISH;
        }
    }
}