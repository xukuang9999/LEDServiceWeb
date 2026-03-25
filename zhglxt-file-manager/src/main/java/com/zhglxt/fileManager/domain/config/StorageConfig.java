package com.zhglxt.fileManager.domain.config;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.util.HashMap;
import java.util.Map;

/**
 * Storage Configuration Model
 * 
 * @author zhglxt
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StorageConfig {

    /**
     * Storage type
     */
    private StorageType type = StorageType.LOCAL;

    /**
     * Cloud storage endpoint
     */
    private String endpoint;

    /**
     * Access key for cloud storage
     */
    private String accessKey;

    /**
     * Secret key for cloud storage
     */
    private String secretKey;

    /**
     * Bucket name for cloud storage
     */
    private String bucketName;

    /**
     * Region for cloud storage
     */
    private String region;

    /**
     * Whether CDN is enabled
     */
    private boolean cdnEnabled = false;

    /**
     * CDN domain
     */
    private String cdnDomain;

    /**
     * Connection timeout in milliseconds
     */
    @Positive(message = "Connection timeout must be positive")
    private int connectionTimeout = 30000;

    /**
     * Read timeout in milliseconds
     */
    @Positive(message = "Read timeout must be positive")
    private int readTimeout = 60000;

    /**
     * Maximum connections in pool
     */
    @Positive(message = "Max connections must be positive")
    private int maxConnections = 50;

    /**
     * Whether to use HTTPS
     */
    private boolean useHttps = true;

    /**
     * Path style access (for S3 compatible storage)
     */
    private boolean pathStyleAccess = false;

    /**
     * Custom domain for storage access
     */
    private String customDomain;

    /**
     * Whether to enable server-side encryption
     */
    private boolean serverSideEncryption = false;

    /**
     * Encryption algorithm
     */
    private String encryptionAlgorithm = "AES256";

    /**
     * Storage class (for cloud storage)
     */
    private String storageClass = "STANDARD";

    /**
     * Additional storage-specific properties
     */
    private Map<String, String> properties = new HashMap<>();

    /**
     * Retry configuration
     */
    private RetryConfig retry = new RetryConfig();

    // Constructors
    public StorageConfig() {
    }

    public StorageConfig(StorageType type) {
        this.type = type;
    }

    // Getters and Setters
    public StorageType getType() {
        return type;
    }

    public void setType(StorageType type) {
        this.type = type;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public boolean isCdnEnabled() {
        return cdnEnabled;
    }

    public void setCdnEnabled(boolean cdnEnabled) {
        this.cdnEnabled = cdnEnabled;
    }

    public String getCdnDomain() {
        return cdnDomain;
    }

    public void setCdnDomain(String cdnDomain) {
        this.cdnDomain = cdnDomain;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    public boolean isUseHttps() {
        return useHttps;
    }

    public void setUseHttps(boolean useHttps) {
        this.useHttps = useHttps;
    }

    public boolean isPathStyleAccess() {
        return pathStyleAccess;
    }

    public void setPathStyleAccess(boolean pathStyleAccess) {
        this.pathStyleAccess = pathStyleAccess;
    }

    public String getCustomDomain() {
        return customDomain;
    }

    public void setCustomDomain(String customDomain) {
        this.customDomain = customDomain;
    }

    public boolean isServerSideEncryption() {
        return serverSideEncryption;
    }

    public void setServerSideEncryption(boolean serverSideEncryption) {
        this.serverSideEncryption = serverSideEncryption;
    }

    public String getEncryptionAlgorithm() {
        return encryptionAlgorithm;
    }

    public void setEncryptionAlgorithm(String encryptionAlgorithm) {
        this.encryptionAlgorithm = encryptionAlgorithm;
    }

    public String getStorageClass() {
        return storageClass;
    }

    public void setStorageClass(String storageClass) {
        this.storageClass = storageClass;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public RetryConfig getRetry() {
        return retry;
    }

    public void setRetry(RetryConfig retry) {
        this.retry = retry;
    }

    /**
     * Add a custom property
     */
    public void addProperty(String key, String value) {
        this.properties.put(key, value);
    }

    /**
     * Get a custom property
     */
    public String getProperty(String key) {
        return this.properties.get(key);
    }

    /**
     * Check if this is local storage
     */
    public boolean isLocal() {
        return type == StorageType.LOCAL;
    }

    /**
     * Check if this is cloud storage
     */
    public boolean isCloud() {
        return type != StorageType.LOCAL;
    }

    /**
     * Storage Type Enum
     */
    public enum StorageType {
        LOCAL("Local File System"),
        AWS_S3("Amazon S3"),
        ALIBABA_OSS("Alibaba Cloud OSS"),
        TENCENT_COS("Tencent Cloud COS"),
        MINIO("MinIO"),
        AZURE_BLOB("Azure Blob Storage"),
        GOOGLE_CLOUD("Google Cloud Storage");

        private final String displayName;

        StorageType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Retry Configuration
     */
    public static class RetryConfig {
        /**
         * Maximum retry attempts
         */
        @Positive(message = "Max attempts must be positive")
        private int maxAttempts = 3;

        /**
         * Initial retry delay in milliseconds
         */
        @Positive(message = "Initial delay must be positive")
        private long initialDelay = 1000;

        /**
         * Maximum retry delay in milliseconds
         */
        @Positive(message = "Max delay must be positive")
        private long maxDelay = 10000;

        /**
         * Backoff multiplier
         */
        private double backoffMultiplier = 2.0;

        // Getters and Setters
        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public long getInitialDelay() {
            return initialDelay;
        }

        public void setInitialDelay(long initialDelay) {
            this.initialDelay = initialDelay;
        }

        public long getMaxDelay() {
            return maxDelay;
        }

        public void setMaxDelay(long maxDelay) {
            this.maxDelay = maxDelay;
        }

        public double getBackoffMultiplier() {
            return backoffMultiplier;
        }

        public void setBackoffMultiplier(double backoffMultiplier) {
            this.backoffMultiplier = backoffMultiplier;
        }
    }

    @Override
    public String toString() {
        return "StorageConfig{" +
                "type=" + type +
                ", endpoint='" + endpoint + '\'' +
                ", bucketName='" + bucketName + '\'' +
                ", region='" + region + '\'' +
                ", cdnEnabled=" + cdnEnabled +
                '}';
    }
}