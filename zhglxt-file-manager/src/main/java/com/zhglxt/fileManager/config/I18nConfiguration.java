package com.zhglxt.fileManager.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.util.Locale;

/**
 * Internationalization Configuration for File Manager
 * 
 * Configures message sources, locale resolver, and locale change interceptor
 * to support English and Chinese localization throughout the file manager module.
 * 
 * @author zhglxt
 */
@Configuration
public class I18nConfiguration implements WebMvcConfigurer {
    
    /**
     * Configure message source for file manager internationalization
     * Loads messages from classpath:messages/file-manager_*.properties
     * 
     * @return MessageSource bean
     */
    @Bean(name = "fileManagerMessageSource")
    public MessageSource fileManagerMessageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        
        // Set base names for message files
        messageSource.setBasenames(
            "classpath:messages/file-manager",
            "classpath:messages"  // Fallback to parent application messages
        );
        
        // Set encoding
        messageSource.setDefaultEncoding("UTF-8");
        
        // Set default locale to English
        messageSource.setDefaultLocale(Locale.ENGLISH);
        
        // Enable fallback to system locale
        messageSource.setFallbackToSystemLocale(false);
        
        // Cache messages for performance
        messageSource.setCacheSeconds(3600); // 1 hour cache
        
        // Use code as default message if not found
        messageSource.setUseCodeAsDefaultMessage(true);
        
        return messageSource;
    }
    
    /**
     * Configure locale change interceptor specifically for file manager
     * Allows changing locale via 'lang' request parameter
     * Uses a different bean name to avoid conflicts
     * 
     * @return LocaleChangeInterceptor bean
     */
    @Bean(name = "fileManagerLocaleChangeInterceptor")
    public LocaleChangeInterceptor fileManagerLocaleChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }
    
    /**
     * Register file manager locale change interceptor
     * Note: The main application's localeResolver will be used
     * 
     * @param registry Interceptor registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(fileManagerLocaleChangeInterceptor());
    }
}