package com.zhglxt.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.util.Locale;

/**
 * MessageSource Configuration for main application
 * This ensures there's a primary MessageSource bean for the application
 */
@Configuration
public class MessageSourceConfig {

    @Bean
    @Primary
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        
        // Set base names for message files
        messageSource.setBasenames(
            "classpath:static/i18n/messages"
        );
        
        // Set encoding
        messageSource.setDefaultEncoding("UTF-8");
        
        // Set default locale
        messageSource.setDefaultLocale(Locale.getDefault());
        
        // Enable fallback to system locale
        messageSource.setFallbackToSystemLocale(true);
        
        // Cache messages for performance
        messageSource.setCacheSeconds(3600); // 1 hour cache
        
        // Use code as default message if not found
        messageSource.setUseCodeAsDefaultMessage(true);
        
        return messageSource;
    }
}