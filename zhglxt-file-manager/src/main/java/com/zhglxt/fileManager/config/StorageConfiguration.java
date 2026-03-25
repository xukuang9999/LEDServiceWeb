package com.zhglxt.fileManager.config;

import com.zhglxt.fileManager.domain.config.StorageConfig;
import com.zhglxt.fileManager.service.storage.CloudStorageService;
import com.zhglxt.fileManager.service.storage.StorageService;
import com.zhglxt.fileManager.service.storage.impl.AlibabaOssStorageService;
import com.zhglxt.fileManager.service.storage.impl.AwsS3StorageService;
import com.zhglxt.fileManager.service.storage.impl.LocalStorageService;
import com.zhglxt.fileManager.service.storage.impl.TencentCosStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;

import jakarta.annotation.PreDestroy;
import java.util.List;

/**
 * Storage Configuration
 * Auto-configuration for different storage backends
 * 
 * @author zhglxt
 */
@Configuration
public class StorageConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(StorageConfiguration.class);

    private final FileManagerProperties properties;
    private CloudStorageService cloudStorageService;

    public StorageConfiguration(@Lazy FileManagerProperties properties) {
        this.properties = properties;
    }

    /**
     * Local Storage Service Bean
     */
    @Bean
    @ConditionalOnProperty(name = "zhglxt.file-manager.storage.type", havingValue = "LOCAL", matchIfMissing = true)
    @ConditionalOnMissingBean(StorageService.class)
    public StorageService localStorageService() {
        logger.info("Configuring Local Storage Service");
        return new LocalStorageService(properties);
    }

    /**
     * AWS S3 Storage Service Bean
     */
    @Bean
    @ConditionalOnProperty(name = "zhglxt.file-manager.storage.type", havingValue = "AWS_S3")
    @ConditionalOnMissingBean(StorageService.class)
    public CloudStorageService awsS3StorageService() {
        logger.info("Configuring AWS S3 Storage Service");
        this.cloudStorageService = new AwsS3StorageService(properties);
        return this.cloudStorageService;
    }

    /**
     * Alibaba OSS Storage Service Bean
     */
    @Bean
    @ConditionalOnProperty(name = "zhglxt.file-manager.storage.type", havingValue = "ALIBABA_OSS")
    @ConditionalOnMissingBean(StorageService.class)
    public CloudStorageService alibabaOssStorageService() {
        logger.info("Configuring Alibaba OSS Storage Service");
        this.cloudStorageService = new AlibabaOssStorageService(properties);
        return this.cloudStorageService;
    }

    /**
     * Tencent COS Storage Service Bean
     */
    @Bean
    @ConditionalOnProperty(name = "zhglxt.file-manager.storage.type", havingValue = "TENCENT_COS")
    @ConditionalOnMissingBean(StorageService.class)
    public CloudStorageService tencentCosStorageService() {
        logger.info("Configuring Tencent COS Storage Service");
        this.cloudStorageService = new TencentCosStorageService(properties);
        return this.cloudStorageService;
    }

    /**
     * Primary Storage Service Bean
     * This ensures there's always a primary storage service available
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean(name = "primaryStorageService")
    public StorageService primaryStorageService(List<StorageService> storageServices) {
        if (storageServices.isEmpty()) {
            logger.warn("No storage service configured, falling back to local storage");
            return new LocalStorageService(properties);
        }
        
        StorageService primaryService = storageServices.get(0);
        logger.info("Primary storage service configured: {}", primaryService.getClass().getSimpleName());
        
        // Test connection for cloud storage services
        if (primaryService instanceof CloudStorageService) {
            CloudStorageService cloudService = (CloudStorageService) primaryService;
            try {
                if (cloudService.testConnection()) {
                    logger.info("Cloud storage connection test successful for: {}", 
                               cloudService.getStorageTypeName());
                } else {
                    logger.warn("Cloud storage connection test failed for: {}", 
                               cloudService.getStorageTypeName());
                }
            } catch (Exception e) {
                logger.error("Error testing cloud storage connection for {}: {}", 
                           cloudService.getStorageTypeName(), e.getMessage());
            }
        }
        
        return primaryService;
    }

    /**
     * Storage Health Check Bean
     */
    @Bean
    public StorageHealthIndicator storageHealthIndicator(StorageService storageService) {
        return new StorageHealthIndicator(storageService);
    }

    /**
     * Cleanup cloud storage resources on shutdown
     */
    @PreDestroy
    public void cleanup() {
        if (cloudStorageService != null) {
            try {
                cloudStorageService.shutdown();
                logger.info("Cloud storage service shutdown completed");
            } catch (Exception e) {
                logger.error("Error during cloud storage service shutdown: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Storage Health Indicator
     */
    public static class StorageHealthIndicator {
        private final StorageService storageService;

        public StorageHealthIndicator(StorageService storageService) {
            this.storageService = storageService;
        }

        public boolean isHealthy() {
            try {
                if (storageService instanceof CloudStorageService) {
                    return ((CloudStorageService) storageService).testConnection();
                }
                // For local storage, just check if it's not null
                return storageService != null;
            } catch (Exception e) {
                logger.error("Storage health check failed: {}", e.getMessage());
                return false;
            }
        }

        public String getStorageType() {
            if (storageService instanceof CloudStorageService) {
                return ((CloudStorageService) storageService).getStorageTypeName();
            }
            return "Local Storage";
        }

        public StorageService getStorageService() {
            return storageService;
        }
    }
}