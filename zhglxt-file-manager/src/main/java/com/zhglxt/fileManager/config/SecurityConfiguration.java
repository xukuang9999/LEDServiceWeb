package com.zhglxt.fileManager.config;

import com.zhglxt.fileManager.interceptor.SecurityInterceptor;
import com.zhglxt.fileManager.service.security.CsrfProtectionService;
import com.zhglxt.fileManager.service.security.RateLimitingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Security Configuration
 * 
 * Configures security interceptors and scheduled tasks for the file manager.
 * 
 * @author zhglxt
 */
@Configuration
@EnableScheduling
public class SecurityConfiguration implements WebMvcConfigurer {

    @Autowired
    private SecurityInterceptor securityInterceptor;
    
    @Autowired
    private RateLimitingService rateLimitingService;
    
    @Autowired
    private CsrfProtectionService csrfProtectionService;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(securityInterceptor)
                .addPathPatterns("/api/file-manager/**", "/file-manager/**")
                .excludePathPatterns(
                    "/file-manager/css/**",
                    "/file-manager/js/**",
                    "/file-manager/img/**",
                    "/file-manager/fonts/**",
                    "/api/file-manager/health",
                    "/api/file-manager/csrf-token"
                );
    }
    
    /**
     * Cleanup expired rate limit buckets every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void cleanupExpiredRateLimitBuckets() {
        try {
            rateLimitingService.cleanupExpiredBuckets();
        } catch (Exception e) {
            // Log error but don't fail the application
            org.slf4j.LoggerFactory.getLogger(SecurityConfiguration.class)
                .error("Error cleaning up expired rate limit buckets: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Cleanup expired CSRF tokens every 10 minutes
     */
    @Scheduled(fixedRate = 600000) // 10 minutes
    public void cleanupExpiredCsrfTokens() {
        try {
            csrfProtectionService.cleanupExpiredTokens();
        } catch (Exception e) {
            // Log error but don't fail the application
            org.slf4j.LoggerFactory.getLogger(SecurityConfiguration.class)
                .error("Error cleaning up expired CSRF tokens: {}", e.getMessage(), e);
        }
    }
}