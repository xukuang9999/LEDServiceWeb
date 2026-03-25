package com.zhglxt.fileManager.service.storage;

import com.zhglxt.fileManager.domain.config.StorageConfig;

import java.io.InputStream;
import java.util.Map;

/**
 * Cloud Storage Service Interface
 * Extended interface for cloud storage specific operations
 * 
 * @author zhglxt
 */
public interface CloudStorageService extends StorageService {

    /**
     * Get storage configuration
     * 
     * @return storage configuration
     */
    StorageConfig getStorageConfig();

    /**
     * Test connection to cloud storage
     * 
     * @return true if connection is successful
     */
    boolean testConnection();

    /**
     * Get storage statistics
     * 
     * @return storage statistics map
     */
    Map<String, Object> getStorageStats();

    /**
     * Generate presigned URL for file upload
     * 
     * @param objectKey object key
     * @param expirationMinutes expiration time in minutes
     * @return presigned URL
     */
    String generatePresignedUploadUrl(String objectKey, int expirationMinutes);

    /**
     * Generate presigned URL for file download
     * 
     * @param objectKey object key
     * @param expirationMinutes expiration time in minutes
     * @return presigned URL
     */
    String generatePresignedDownloadUrl(String objectKey, int expirationMinutes);

    /**
     * Copy file within the same storage
     * 
     * @param sourceKey source object key
     * @param targetKey target object key
     * @return true if copied successfully
     */
    boolean copyFile(String sourceKey, String targetKey);

    /**
     * Get file metadata
     * 
     * @param objectKey object key
     * @return metadata map
     */
    Map<String, String> getFileMetadata(String objectKey);

    /**
     * Set file metadata
     * 
     * @param objectKey object key
     * @param metadata metadata map
     * @return true if set successfully
     */
    boolean setFileMetadata(String objectKey, Map<String, String> metadata);

    /**
     * Get storage type name
     * 
     * @return storage type name
     */
    String getStorageTypeName();

    /**
     * Initialize storage client
     */
    void initializeClient();

    /**
     * Shutdown storage client
     */
    void shutdown();
}