package com.zhglxt.fileManager.service.storage.impl;

import com.zhglxt.fileManager.config.FileManagerProperties;
import com.zhglxt.fileManager.domain.config.StorageConfig;
import com.zhglxt.fileManager.exception.StorageException;
import com.zhglxt.fileManager.service.storage.CloudStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract Cloud Storage Service
 * Base class for cloud storage implementations
 * 
 * This class will be extended by specific cloud storage implementations
 * (AWS S3, Alibaba OSS, Tencent COS, etc.)
 * 
 * @author zhglxt
 */
public abstract class AbstractCloudStorageService implements CloudStorageService {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    protected final FileManagerProperties properties;
    protected final StorageConfig storageConfig;

    protected AbstractCloudStorageService(FileManagerProperties properties) {
        this.properties = properties;
        this.storageConfig = properties.getStorage();
        validateConfiguration();
        initializeClient();
    }

    /**
     * Validate cloud storage configuration
     */
    protected void validateConfiguration() {
        if (storageConfig == null) {
            throw new StorageException("INVALID_CONFIG", "Storage configuration is required");
        }
        
        if (!StringUtils.hasText(storageConfig.getAccessKey())) {
            throw new StorageException("INVALID_CONFIG", "Access key is required for cloud storage");
        }
        
        if (!StringUtils.hasText(storageConfig.getSecretKey())) {
            throw new StorageException("INVALID_CONFIG", "Secret key is required for cloud storage");
        }
        
        if (!StringUtils.hasText(storageConfig.getBucketName())) {
            throw new StorageException("INVALID_CONFIG", "Bucket name is required for cloud storage");
        }
        
        // Additional validation can be performed by subclasses
        validateSpecificConfiguration();
    }

    /**
     * Validate storage-specific configuration
     * To be implemented by subclasses
     */
    protected abstract void validateSpecificConfiguration();

    /**
     * Initialize cloud storage client
     * To be implemented by subclasses
     */
    public abstract void initializeClient();

    /**
     * Get storage type name for logging
     */
    public abstract String getStorageTypeName();

    // CloudStorageService interface implementations

    @Override
    public StorageConfig getStorageConfig() {
        return storageConfig;
    }

    @Override
    public boolean testConnection() {
        try {
            return executeWithRetry(() -> performConnectionTest(), "testConnection");
        } catch (Exception e) {
            logger.error("Connection test failed for {}: {}", getStorageTypeName(), e.getMessage());
            return false;
        }
    }

    @Override
    public Map<String, Object> getStorageStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("storageType", getStorageTypeName());
        stats.put("bucketName", storageConfig.getBucketName());
        stats.put("region", storageConfig.getRegion());
        stats.put("cdnEnabled", storageConfig.isCdnEnabled());
        stats.put("cdnDomain", storageConfig.getCdnDomain());
        
        try {
            Map<String, Object> specificStats = getSpecificStorageStats();
            if (specificStats != null) {
                stats.putAll(specificStats);
            }
        } catch (Exception e) {
            logger.warn("Failed to get specific storage stats for {}: {}", getStorageTypeName(), e.getMessage());
        }
        
        return stats;
    }

    @Override
    public void shutdown() {
        try {
            performShutdown();
            logger.info("{} storage client shutdown completed", getStorageTypeName());
        } catch (Exception e) {
            logger.error("Error during {} storage client shutdown: {}", getStorageTypeName(), e.getMessage(), e);
        }
    }

    /**
     * Perform connection test
     * To be implemented by subclasses
     */
    protected abstract boolean performConnectionTest() throws Exception;

    /**
     * Get storage-specific statistics
     * To be implemented by subclasses
     */
    protected abstract Map<String, Object> getSpecificStorageStats() throws Exception;

    /**
     * Perform shutdown operations
     * To be implemented by subclasses
     */
    protected abstract void performShutdown() throws Exception;

    /**
     * Sanitize object key for cloud storage
     */
    protected String sanitizeObjectKey(String key) {
        if (!StringUtils.hasText(key)) {
            throw new IllegalArgumentException("Object key cannot be empty");
        }
        
        // Remove leading slashes and normalize path separators
        String sanitized = key.replaceAll("^/+", "").replace('\\', '/');
        
        // Remove double slashes
        sanitized = sanitized.replaceAll("/+", "/");
        
        return sanitized;
    }

    /**
     * Build full object key with directory
     */
    protected String buildObjectKey(String fileName, String directory) {
        if (!StringUtils.hasText(directory)) {
            return sanitizeObjectKey(fileName);
        }
        
        String dir = sanitizeObjectKey(directory);
        if (!dir.endsWith("/")) {
            dir += "/";
        }
        
        return dir + sanitizeObjectKey(fileName);
    }

    /**
     * Generate CDN URL if CDN is enabled
     */
    protected String generateCdnUrl(String objectKey) {
        if (storageConfig.isCdnEnabled() && StringUtils.hasText(storageConfig.getCdnDomain())) {
            String protocol = storageConfig.isUseHttps() ? "https" : "http";
            return protocol + "://" + storageConfig.getCdnDomain() + "/" + objectKey;
        }
        return null;
    }

    /**
     * Handle retry logic for cloud operations
     */
    protected <T> T executeWithRetry(CloudOperation<T> operation, String operationName) {
        StorageConfig.RetryConfig retryConfig = storageConfig.getRetry();
        int maxAttempts = retryConfig.getMaxAttempts();
        long delay = retryConfig.getInitialDelay();
        
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.execute();
            } catch (Exception e) {
                lastException = e;
                logger.warn("Attempt {} of {} failed for operation {}: {}", 
                           attempt, maxAttempts, operationName, e.getMessage());
                
                if (attempt < maxAttempts) {
                    try {
                        Thread.sleep(delay);
                        delay = Math.min((long) (delay * retryConfig.getBackoffMultiplier()), 
                                       retryConfig.getMaxDelay());
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new StorageException("OPERATION_INTERRUPTED", 
                            "Operation was interrupted during retry", ie);
                    }
                }
            }
        }
        
        throw new StorageException("OPERATION_FAILED", 
            "Operation " + operationName + " failed after " + maxAttempts + " attempts", 
            lastException);
    }

    /**
     * Functional interface for cloud operations that can be retried
     */
    @FunctionalInterface
    protected interface CloudOperation<T> {
        T execute() throws Exception;
    }
}