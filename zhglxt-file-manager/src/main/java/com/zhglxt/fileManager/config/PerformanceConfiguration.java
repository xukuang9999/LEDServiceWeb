package com.zhglxt.fileManager.config;

import com.zhglxt.fileManager.service.async.AsyncProcessingService;
import com.zhglxt.fileManager.service.async.impl.DefaultAsyncProcessingService;
import com.zhglxt.fileManager.service.cache.CacheService;
import com.zhglxt.fileManager.service.cache.impl.MemoryCacheService;
import com.zhglxt.fileManager.service.performance.PerformanceMonitoringService;
import com.zhglxt.fileManager.service.performance.impl.DefaultPerformanceMonitoringService;
import com.zhglxt.fileManager.service.pool.ConnectionPoolService;
import com.zhglxt.fileManager.service.pool.impl.DefaultConnectionPoolService;
import com.zhglxt.fileManager.service.streaming.FileStreamingService;
import com.zhglxt.fileManager.service.streaming.impl.DefaultFileStreamingService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Performance Configuration
 * Configures performance optimization components
 * 
 * @author zhglxt
 */
@Configuration
@ConditionalOnProperty(prefix = "zhglxt.file-manager.performance", name = "enabled", havingValue = "true", matchIfMissing = true)
public class PerformanceConfiguration {

    /**
     * Cache service bean
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "zhglxt.file-manager.cache", name = "enabled", havingValue = "true", matchIfMissing = true)
    public CacheService cacheService() {
        return new MemoryCacheService();
    }

    /**
     * Async processing service bean
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "zhglxt.file-manager.performance", name = "async-enabled", havingValue = "true", matchIfMissing = true)
    public AsyncProcessingService asyncProcessingService() {
        return new DefaultAsyncProcessingService(10, 100); // Default values, can be overridden by properties
    }

    /**
     * File streaming service bean
     */
    @Bean
    @ConditionalOnMissingBean
    public FileStreamingService fileStreamingService() {
        return new DefaultFileStreamingService();
    }

    /**
     * Connection pool service bean
     */
    @Bean
    @ConditionalOnMissingBean
    public ConnectionPoolService connectionPoolService() {
        return new DefaultConnectionPoolService(50, 30000); // Default values
    }

    /**
     * Performance monitoring service bean
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "zhglxt.file-manager.performance", name = "monitoring-enabled", havingValue = "true", matchIfMissing = true)
    public PerformanceMonitoringService performanceMonitoringService() {
        return new DefaultPerformanceMonitoringService();
    }
}