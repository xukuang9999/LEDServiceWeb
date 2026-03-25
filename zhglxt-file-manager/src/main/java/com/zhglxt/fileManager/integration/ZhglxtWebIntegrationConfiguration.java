package com.zhglxt.fileManager.integration;

import com.zhglxt.fileManager.service.I18nService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * zhglxt-web Integration Configuration
 * 
 * Configures integration components for seamless integration with the zhglxt-web parent module.
 * This includes authentication integration, session management, styling consistency, and menu integration.
 * 
 * @author zhglxt
 */
@Configuration
public class ZhglxtWebIntegrationConfiguration implements WebMvcConfigurer {

    /**
     * Configure static resource handlers for file manager assets
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // File manager static resources
        registry.addResourceHandler("/file-manager/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600);
        
        // elFinder resources
        registry.addResourceHandler("/elfinder/**")
                .addResourceLocations("classpath:/elfinder/")
                .setCachePeriod(3600);
    }

    /**
     * Authentication integration service
     */
    @Bean
    public AuthenticationIntegrationService authenticationIntegrationService() {
        return new AuthenticationIntegrationService();
    }

    /**
     * Session management integration service
     */
    @Bean
    public SessionIntegrationService sessionIntegrationService() {
        return new SessionIntegrationService();
    }

    /**
     * Menu integration service
     */
    @Bean
    public MenuIntegrationService menuIntegrationService() {
        return new MenuIntegrationService();
    }

    /**
     * Theme integration service
     */
    @Bean
    public ThemeIntegrationService themeIntegrationService() {
        return new ThemeIntegrationService();
    }

    /**
     * File manager web integration controller
     */
    @Bean
    public FileManagerWebIntegrationController fileManagerWebIntegrationController(
            AuthenticationIntegrationService authService,
            SessionIntegrationService sessionService,
            MenuIntegrationService menuService,
            ThemeIntegrationService themeService,
            I18nService i18nService) {
        return new FileManagerWebIntegrationController(
            authService, sessionService, menuService, themeService, i18nService);
    }
}