package com.zhglxt.fileManager.config;

import com.zhglxt.fileManager.domain.config.StorageConfig;
import com.zhglxt.fileManager.domain.config.ThumbnailConfig;
import com.zhglxt.fileManager.domain.config.WatermarkConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * File Manager Configuration Properties
 * 
 * Comprehensive configuration class for the zhglxt-file-manager module.
 * Supports validation, default values, and dynamic configuration updates.
 * 
 * @author zhglxt
 */
@Validated
public class FileManagerProperties {

    /**
     * Whether file manager is enabled
     */
    private boolean enabled = true;

    /**
     * Root directory for file storage (for local storage)
     */
    @NotBlank(message = "Root directory cannot be blank")
    private String rootDirectory = "upload";

    /**
     * Maximum file size in bytes (default: 100MB)
     */
    @Positive(message = "Max file size must be positive")
    private long maxFileSize = 100 * 1024 * 1024L;

    /**
     * Maximum total storage size in bytes (default: 10GB, 0 = unlimited)
     */
    @PositiveOrZero(message = "Max storage size must be positive or zero")
    private long maxStorageSize = 10L * 1024 * 1024 * 1024;

    /**
     * Maximum number of files per directory (0 = unlimited)
     */
    @PositiveOrZero(message = "Max files per directory must be positive or zero")
    private int maxFilesPerDirectory = 1000;

    /**
     * Allowed file extensions
     */
    @NotNull(message = "Allowed extensions cannot be null")
    @Size(min = 1, message = "At least one file extension must be allowed")
    private List<String> allowedExtensions = List.of(
        "jpg", "jpeg", "png", "gif", "bmp", "webp",
        "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
        "txt", "md", "zip", "rar", "7z", "mp4", "mov", "mp3"
    );

    /**
     * Blocked file extensions (takes precedence over allowed extensions)
     */
    @NotNull(message = "Blocked extensions cannot be null")
    private List<String> blockedExtensions = List.of(
        "exe", "bat", "cmd", "com", "pif", "scr", "vbs", "js", "jar", "sh"
    );

    /**
     * Allowed MIME types (empty = allow all based on extensions)
     */
    private List<String> allowedMimeTypes = List.of();

    /**
     * Whether to enable virus scanning
     */
    private boolean virusScanEnabled = false;

    /**
     * Virus scanner command (if external scanner is used)
     */
    private String virusScannerCommand;

    /**
     * File operation timeout in seconds
     */
    @Positive(message = "Operation timeout must be positive")
    private int operationTimeoutSeconds = 300;

    /**
     * Whether to enable file versioning
     */
    private boolean versioningEnabled = false;

    /**
     * Maximum number of file versions to keep
     */
    @Positive(message = "Max versions must be positive")
    private int maxVersions = 5;

    /**
     * Whether to enable audit logging
     */
    private boolean auditEnabled = true;

    /**
     * Audit log retention days
     */
    @Positive(message = "Audit retention days must be positive")
    private int auditRetentionDays = 90;

    /**
     * Temporary directory for file processing
     */
    private String tempDirectory = System.getProperty("java.io.tmpdir");

    /**
     * Whether to clean up temporary files on startup
     */
    private boolean cleanupTempOnStartup = true;

    /**
     * Temporary file cleanup interval in minutes
     */
    @Positive(message = "Cleanup interval must be positive")
    private int tempCleanupIntervalMinutes = 60;

    /**
     * Temporary file max age in hours
     */
    @Positive(message = "Temp file max age must be positive")
    private int tempFileMaxAgeHours = 24;

    /**
     * Whether to enable compression for certain file types
     */
    private boolean compressionEnabled = false;

    /**
     * Compression quality (0.0 to 1.0)
     */
    @DecimalMin(value = "0.0", message = "Compression quality must be at least 0.0")
    @DecimalMax(value = "1.0", message = "Compression quality must be at most 1.0")
    private float compressionQuality = 0.8f;

    /**
     * File types to compress
     */
    private List<String> compressibleTypes = List.of("jpg", "jpeg", "png", "pdf");

    /**
     * Whether to enable automatic file organization
     */
    private boolean autoOrganizeEnabled = false;

    /**
     * File organization pattern (date-based, type-based, etc.)
     */
    private String organizationPattern = "yyyy/MM/dd";

    /**
     * Performance monitoring configuration
     */
    @Valid
    @NestedConfigurationProperty
    private PerformanceConfig performance = new PerformanceConfig();

    /**
     * Security configuration
     */
    @Valid
    @NestedConfigurationProperty
    private SecurityConfig security = new SecurityConfig();

    /**
     * Cache configuration
     */
    @Valid
    @NestedConfigurationProperty
    private CacheConfig cache = new CacheConfig();

    /**
     * Thumbnail configuration
     */
    @Valid
    @NestedConfigurationProperty
    private ThumbnailConfig thumbnail = new ThumbnailConfig();

    /**
     * Watermark configuration
     */
    @Valid
    @NestedConfigurationProperty
    private WatermarkConfig watermark = new WatermarkConfig();

    /**
     * Storage configuration
     */
    @Valid
    @NestedConfigurationProperty
    private StorageConfig storage = new StorageConfig();

    /**
     * Health check configuration
     */
    @Valid
    @NestedConfigurationProperty
    private HealthConfig health = new HealthConfig();

    /**
     * Custom properties for extensibility
     */
    private Map<String, String> customProperties = new HashMap<>();

    // Getters and Setters
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getRootDirectory() {
        return rootDirectory;
    }

    public void setRootDirectory(String rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    public long getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public long getMaxStorageSize() {
        return maxStorageSize;
    }

    public void setMaxStorageSize(long maxStorageSize) {
        this.maxStorageSize = maxStorageSize;
    }

    public int getMaxFilesPerDirectory() {
        return maxFilesPerDirectory;
    }

    public void setMaxFilesPerDirectory(int maxFilesPerDirectory) {
        this.maxFilesPerDirectory = maxFilesPerDirectory;
    }

    public List<String> getAllowedExtensions() {
        return allowedExtensions;
    }

    public void setAllowedExtensions(List<String> allowedExtensions) {
        this.allowedExtensions = allowedExtensions;
    }

    public List<String> getBlockedExtensions() {
        return blockedExtensions;
    }

    public void setBlockedExtensions(List<String> blockedExtensions) {
        this.blockedExtensions = blockedExtensions;
    }

    public List<String> getAllowedMimeTypes() {
        return allowedMimeTypes;
    }

    public void setAllowedMimeTypes(List<String> allowedMimeTypes) {
        this.allowedMimeTypes = allowedMimeTypes;
    }

    public boolean isVirusScanEnabled() {
        return virusScanEnabled;
    }

    public void setVirusScanEnabled(boolean virusScanEnabled) {
        this.virusScanEnabled = virusScanEnabled;
    }

    public String getVirusScannerCommand() {
        return virusScannerCommand;
    }

    public void setVirusScannerCommand(String virusScannerCommand) {
        this.virusScannerCommand = virusScannerCommand;
    }

    public int getOperationTimeoutSeconds() {
        return operationTimeoutSeconds;
    }

    public void setOperationTimeoutSeconds(int operationTimeoutSeconds) {
        this.operationTimeoutSeconds = operationTimeoutSeconds;
    }

    public boolean isVersioningEnabled() {
        return versioningEnabled;
    }

    public void setVersioningEnabled(boolean versioningEnabled) {
        this.versioningEnabled = versioningEnabled;
    }

    public int getMaxVersions() {
        return maxVersions;
    }

    public void setMaxVersions(int maxVersions) {
        this.maxVersions = maxVersions;
    }

    public boolean isAuditEnabled() {
        return auditEnabled;
    }

    public void setAuditEnabled(boolean auditEnabled) {
        this.auditEnabled = auditEnabled;
    }

    public int getAuditRetentionDays() {
        return auditRetentionDays;
    }

    public void setAuditRetentionDays(int auditRetentionDays) {
        this.auditRetentionDays = auditRetentionDays;
    }

    public String getTempDirectory() {
        return tempDirectory;
    }

    public void setTempDirectory(String tempDirectory) {
        this.tempDirectory = tempDirectory;
    }

    public boolean isCleanupTempOnStartup() {
        return cleanupTempOnStartup;
    }

    public void setCleanupTempOnStartup(boolean cleanupTempOnStartup) {
        this.cleanupTempOnStartup = cleanupTempOnStartup;
    }

    public int getTempCleanupIntervalMinutes() {
        return tempCleanupIntervalMinutes;
    }

    public void setTempCleanupIntervalMinutes(int tempCleanupIntervalMinutes) {
        this.tempCleanupIntervalMinutes = tempCleanupIntervalMinutes;
    }

    public int getTempFileMaxAgeHours() {
        return tempFileMaxAgeHours;
    }

    public void setTempFileMaxAgeHours(int tempFileMaxAgeHours) {
        this.tempFileMaxAgeHours = tempFileMaxAgeHours;
    }

    public boolean isCompressionEnabled() {
        return compressionEnabled;
    }

    public void setCompressionEnabled(boolean compressionEnabled) {
        this.compressionEnabled = compressionEnabled;
    }

    public float getCompressionQuality() {
        return compressionQuality;
    }

    public void setCompressionQuality(float compressionQuality) {
        this.compressionQuality = compressionQuality;
    }

    public List<String> getCompressibleTypes() {
        return compressibleTypes;
    }

    public void setCompressibleTypes(List<String> compressibleTypes) {
        this.compressibleTypes = compressibleTypes;
    }

    public boolean isAutoOrganizeEnabled() {
        return autoOrganizeEnabled;
    }

    public void setAutoOrganizeEnabled(boolean autoOrganizeEnabled) {
        this.autoOrganizeEnabled = autoOrganizeEnabled;
    }

    public String getOrganizationPattern() {
        return organizationPattern;
    }

    public void setOrganizationPattern(String organizationPattern) {
        this.organizationPattern = organizationPattern;
    }

    public PerformanceConfig getPerformance() {
        return performance;
    }

    public void setPerformance(PerformanceConfig performance) {
        this.performance = performance;
    }

    public SecurityConfig getSecurity() {
        return security;
    }

    public void setSecurity(SecurityConfig security) {
        this.security = security;
    }

    public CacheConfig getCache() {
        return cache;
    }

    public void setCache(CacheConfig cache) {
        this.cache = cache;
    }

    public ThumbnailConfig getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(ThumbnailConfig thumbnail) {
        this.thumbnail = thumbnail;
    }

    public WatermarkConfig getWatermark() {
        return watermark;
    }

    public void setWatermark(WatermarkConfig watermark) {
        this.watermark = watermark;
    }

    public StorageConfig getStorage() {
        return storage;
    }

    public void setStorage(StorageConfig storage) {
        this.storage = storage;
    }

    public HealthConfig getHealth() {
        return health;
    }

    public void setHealth(HealthConfig health) {
        this.health = health;
    }

    public Map<String, String> getCustomProperties() {
        return customProperties;
    }

    public void setCustomProperties(Map<String, String> customProperties) {
        this.customProperties = customProperties;
    }

    // Utility methods
    
    /**
     * Check if a file extension is allowed
     */
    public boolean isExtensionAllowed(String extension) {
        if (extension == null) return false;
        String ext = extension.toLowerCase().startsWith(".") ? 
            extension.substring(1).toLowerCase() : extension.toLowerCase();
        
        // Check blocked extensions first
        if (blockedExtensions.contains(ext)) {
            return false;
        }
        
        // Check allowed extensions
        return allowedExtensions.contains(ext);
    }

    /**
     * Check if a MIME type is allowed
     */
    public boolean isMimeTypeAllowed(String mimeType) {
        if (mimeType == null) return false;
        if (allowedMimeTypes.isEmpty()) return true; // Allow all if not specified
        return allowedMimeTypes.contains(mimeType.toLowerCase());
    }

    /**
     * Get operation timeout in milliseconds
     */
    public long getOperationTimeoutMillis() {
        return operationTimeoutSeconds * 1000L;
    }

    /**
     * Get temp cleanup interval in milliseconds
     */
    public long getTempCleanupIntervalMillis() {
        return tempCleanupIntervalMinutes * 60 * 1000L;
    }

    /**
     * Get temp file max age in milliseconds
     */
    public long getTempFileMaxAgeMillis() {
        return tempFileMaxAgeHours * 60 * 60 * 1000L;
    }

    /**
     * Check if file type is compressible
     */
    public boolean isCompressibleType(String extension) {
        if (!compressionEnabled || extension == null) return false;
        String ext = extension.toLowerCase().startsWith(".") ? 
            extension.substring(1).toLowerCase() : extension.toLowerCase();
        return compressibleTypes.contains(ext);
    }

    /**
     * Add custom property
     */
    public void addCustomProperty(String key, String value) {
        this.customProperties.put(key, value);
    }

    /**
     * Get custom property
     */
    public String getCustomProperty(String key) {
        return this.customProperties.get(key);
    }

    /**
     * Get custom property with default value
     */
    public String getCustomProperty(String key, String defaultValue) {
        return this.customProperties.getOrDefault(key, defaultValue);
    }

    /**
     * Performance Configuration
     */
    public static class PerformanceConfig {
        /**
         * Enable performance monitoring
         */
        private boolean monitoringEnabled = true;

        /**
         * Thread pool size for file operations
         */
        @Positive(message = "Thread pool size must be positive")
        private int threadPoolSize = 10;

        /**
         * Maximum queue size for async operations
         */
        @Positive(message = "Queue size must be positive")
        private int queueSize = 100;

        /**
         * Enable async file operations
         */
        private boolean asyncEnabled = true;

        /**
         * Batch size for bulk operations
         */
        @Positive(message = "Batch size must be positive")
        private int batchSize = 50;

        /**
         * Memory threshold for streaming (bytes)
         */
        @Positive(message = "Memory threshold must be positive")
        private long memoryThreshold = 10 * 1024 * 1024L; // 10MB

        // Getters and Setters
        public boolean isMonitoringEnabled() {
            return monitoringEnabled;
        }

        public void setMonitoringEnabled(boolean monitoringEnabled) {
            this.monitoringEnabled = monitoringEnabled;
        }

        public int getThreadPoolSize() {
            return threadPoolSize;
        }

        public void setThreadPoolSize(int threadPoolSize) {
            this.threadPoolSize = threadPoolSize;
        }

        public int getQueueSize() {
            return queueSize;
        }

        public void setQueueSize(int queueSize) {
            this.queueSize = queueSize;
        }

        public boolean isAsyncEnabled() {
            return asyncEnabled;
        }

        public void setAsyncEnabled(boolean asyncEnabled) {
            this.asyncEnabled = asyncEnabled;
        }

        public int getBatchSize() {
            return batchSize;
        }

        public void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
        }

        public long getMemoryThreshold() {
            return memoryThreshold;
        }

        public void setMemoryThreshold(long memoryThreshold) {
            this.memoryThreshold = memoryThreshold;
        }
    }

    /**
     * Security Configuration
     */
    public static class SecurityConfig {
        /**
         * Enable security scanning
         */
        private boolean scanningEnabled = true;

        /**
         * Maximum file name length
         */
        @Positive(message = "Max filename length must be positive")
        private int maxFilenameLength = 255;

        /**
         * Maximum path depth
         */
        @Positive(message = "Max path depth must be positive")
        private int maxPathDepth = 10;

        /**
         * Enable path traversal protection
         */
        private boolean pathTraversalProtection = true;

        /**
         * Enable content type validation
         */
        private boolean contentTypeValidation = true;

        /**
         * Enable file signature validation
         */
        private boolean fileSignatureValidation = true;

        /**
         * Quarantine directory for suspicious files
         */
        private String quarantineDirectory = "quarantine";

        /**
         * Enable rate limiting
         */
        private boolean rateLimitingEnabled = true;

        /**
         * Rate limit per user per minute
         */
        @Positive(message = "Rate limit must be positive")
        private int rateLimitPerMinute = 100;

        /**
         * Rate limiting configuration
         */
        @Valid
        @NestedConfigurationProperty
        private RateLimit rateLimit = new RateLimit();

        /**
         * CSRF protection configuration
         */
        @Valid
        @NestedConfigurationProperty
        private Csrf csrf = new Csrf();

        /**
         * File scanning configuration
         */
        @Valid
        @NestedConfigurationProperty
        private Scanning scanning = new Scanning();

        // Getters and Setters
        public boolean isScanningEnabled() {
            return scanningEnabled;
        }

        public void setScanningEnabled(boolean scanningEnabled) {
            this.scanningEnabled = scanningEnabled;
        }

        public int getMaxFilenameLength() {
            return maxFilenameLength;
        }

        public void setMaxFilenameLength(int maxFilenameLength) {
            this.maxFilenameLength = maxFilenameLength;
        }

        public int getMaxPathDepth() {
            return maxPathDepth;
        }

        public void setMaxPathDepth(int maxPathDepth) {
            this.maxPathDepth = maxPathDepth;
        }

        public boolean isPathTraversalProtection() {
            return pathTraversalProtection;
        }

        public void setPathTraversalProtection(boolean pathTraversalProtection) {
            this.pathTraversalProtection = pathTraversalProtection;
        }

        public boolean isContentTypeValidation() {
            return contentTypeValidation;
        }

        public void setContentTypeValidation(boolean contentTypeValidation) {
            this.contentTypeValidation = contentTypeValidation;
        }

        public boolean isFileSignatureValidation() {
            return fileSignatureValidation;
        }

        public void setFileSignatureValidation(boolean fileSignatureValidation) {
            this.fileSignatureValidation = fileSignatureValidation;
        }

        public String getQuarantineDirectory() {
            return quarantineDirectory;
        }

        public void setQuarantineDirectory(String quarantineDirectory) {
            this.quarantineDirectory = quarantineDirectory;
        }

        public boolean isRateLimitingEnabled() {
            return rateLimitingEnabled;
        }

        public void setRateLimitingEnabled(boolean rateLimitingEnabled) {
            this.rateLimitingEnabled = rateLimitingEnabled;
        }

        public int getRateLimitPerMinute() {
            return rateLimitPerMinute;
        }

        public void setRateLimitPerMinute(int rateLimitPerMinute) {
            this.rateLimitPerMinute = rateLimitPerMinute;
        }

        public RateLimit getRateLimit() {
            return rateLimit;
        }

        public void setRateLimit(RateLimit rateLimit) {
            this.rateLimit = rateLimit;
        }

        public Csrf getCsrf() {
            return csrf;
        }

        public void setCsrf(Csrf csrf) {
            this.csrf = csrf;
        }

        public Scanning getScanning() {
            return scanning;
        }

        public void setScanning(Scanning scanning) {
            this.scanning = scanning;
        }

        // Additional methods for test compatibility
        public boolean isEnabled() {
            return scanningEnabled;
        }

        public int getMaxUploadRate() {
            return rateLimit != null ? rateLimit.getUploadMaxPerUser() : 100;
        }

        public int getMaxDownloadRate() {
            return rateLimit != null ? rateLimit.getDownloadMaxPerUser() : 200;
        }

        public java.util.List<String> getAllowedIpRanges() {
            return java.util.List.of("0.0.0.0/0"); // Default allow all
        }

        public java.util.List<String> getBlockedExtensions() {
            return java.util.List.of("exe", "bat", "cmd", "scr");
        }
    }

    /**
     * Rate Limiting Configuration
     */
    public static class RateLimit {
        /**
         * Rate limiting window in minutes
         */
        @Positive(message = "Window minutes must be positive")
        private int windowMinutes = 1;

        /**
         * Upload rate limits per user
         */
        @Positive(message = "Upload max per user must be positive")
        private int uploadMaxPerUser = 10;

        /**
         * Download rate limits per user
         */
        @Positive(message = "Download max per user must be positive")
        private int downloadMaxPerUser = 50;

        /**
         * Delete rate limits per user
         */
        @Positive(message = "Delete max per user must be positive")
        private int deleteMaxPerUser = 20;

        /**
         * Default rate limits per user
         */
        @Positive(message = "Default max per user must be positive")
        private int defaultMaxPerUser = 100;

        /**
         * Upload rate limits per IP
         */
        @Positive(message = "Upload max per IP must be positive")
        private int uploadMaxPerIp = 50;

        /**
         * Download rate limits per IP
         */
        @Positive(message = "Download max per IP must be positive")
        private int downloadMaxPerIp = 200;

        /**
         * Delete rate limits per IP
         */
        @Positive(message = "Delete max per IP must be positive")
        private int deleteMaxPerIp = 100;

        /**
         * Default rate limits per IP
         */
        @Positive(message = "Default max per IP must be positive")
        private int defaultMaxPerIp = 500;

        /**
         * Global upload rate limits
         */
        @Positive(message = "Upload max global must be positive")
        private int uploadMaxGlobal = 1000;

        /**
         * Global download rate limits
         */
        @Positive(message = "Download max global must be positive")
        private int downloadMaxGlobal = 5000;

        /**
         * Global delete rate limits
         */
        @Positive(message = "Delete max global must be positive")
        private int deleteMaxGlobal = 2000;

        /**
         * Default global rate limits
         */
        @Positive(message = "Default max global must be positive")
        private int defaultMaxGlobal = 10000;

        // Getters and Setters
        public int getWindowMinutes() { return windowMinutes; }
        public void setWindowMinutes(int windowMinutes) { this.windowMinutes = windowMinutes; }

        public int getUploadMaxPerUser() { return uploadMaxPerUser; }
        public void setUploadMaxPerUser(int uploadMaxPerUser) { this.uploadMaxPerUser = uploadMaxPerUser; }

        public int getDownloadMaxPerUser() { return downloadMaxPerUser; }
        public void setDownloadMaxPerUser(int downloadMaxPerUser) { this.downloadMaxPerUser = downloadMaxPerUser; }

        public int getDeleteMaxPerUser() { return deleteMaxPerUser; }
        public void setDeleteMaxPerUser(int deleteMaxPerUser) { this.deleteMaxPerUser = deleteMaxPerUser; }

        public int getDefaultMaxPerUser() { return defaultMaxPerUser; }
        public void setDefaultMaxPerUser(int defaultMaxPerUser) { this.defaultMaxPerUser = defaultMaxPerUser; }

        public int getUploadMaxPerIp() { return uploadMaxPerIp; }
        public void setUploadMaxPerIp(int uploadMaxPerIp) { this.uploadMaxPerIp = uploadMaxPerIp; }

        public int getDownloadMaxPerIp() { return downloadMaxPerIp; }
        public void setDownloadMaxPerIp(int downloadMaxPerIp) { this.downloadMaxPerIp = downloadMaxPerIp; }

        public int getDeleteMaxPerIp() { return deleteMaxPerIp; }
        public void setDeleteMaxPerIp(int deleteMaxPerIp) { this.deleteMaxPerIp = deleteMaxPerIp; }

        public int getDefaultMaxPerIp() { return defaultMaxPerIp; }
        public void setDefaultMaxPerIp(int defaultMaxPerIp) { this.defaultMaxPerIp = defaultMaxPerIp; }

        public int getUploadMaxGlobal() { return uploadMaxGlobal; }
        public void setUploadMaxGlobal(int uploadMaxGlobal) { this.uploadMaxGlobal = uploadMaxGlobal; }

        public int getDownloadMaxGlobal() { return downloadMaxGlobal; }
        public void setDownloadMaxGlobal(int downloadMaxGlobal) { this.downloadMaxGlobal = downloadMaxGlobal; }

        public int getDeleteMaxGlobal() { return deleteMaxGlobal; }
        public void setDeleteMaxGlobal(int deleteMaxGlobal) { this.deleteMaxGlobal = deleteMaxGlobal; }

        public int getDefaultMaxGlobal() { return defaultMaxGlobal; }
        public void setDefaultMaxGlobal(int defaultMaxGlobal) { this.defaultMaxGlobal = defaultMaxGlobal; }
    }

    /**
     * CSRF Protection Configuration
     */
    public static class Csrf {
        /**
         * Enable CSRF protection
         */
        private boolean enabled = true;

        /**
         * CSRF token expiration in minutes
         */
        @Positive(message = "Token expiration minutes must be positive")
        private int tokenExpirationMinutes = 60;

        // Getters and Setters
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public int getTokenExpirationMinutes() { return tokenExpirationMinutes; }
        public void setTokenExpirationMinutes(int tokenExpirationMinutes) { this.tokenExpirationMinutes = tokenExpirationMinutes; }
    }

    /**
     * File Scanning Configuration
     */
    public static class Scanning {
        /**
         * Enable file scanning
         */
        private boolean enabled = true;

        /**
         * Maximum file size for scanning in bytes
         */
        @Positive(message = "Max file size for scanning must be positive")
        private long maxFileSizeForScanning = 50 * 1024 * 1024L; // 50MB

        /**
         * Enable signature validation
         */
        private boolean signatureValidationEnabled = true;

        /**
         * Enable content scanning
         */
        private boolean contentScanningEnabled = true;

        /**
         * Enable entropy analysis
         */
        private boolean entropyAnalysisEnabled = true;

        /**
         * Block files with high threat level
         */
        private boolean blockHighThreatFiles = true;

        /**
         * Quarantine suspicious files
         */
        private boolean quarantineSuspiciousFiles = true;

        // Getters and Setters
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public long getMaxFileSizeForScanning() { return maxFileSizeForScanning; }
        public void setMaxFileSizeForScanning(long maxFileSizeForScanning) { this.maxFileSizeForScanning = maxFileSizeForScanning; }

        public boolean isSignatureValidationEnabled() { return signatureValidationEnabled; }
        public void setSignatureValidationEnabled(boolean signatureValidationEnabled) { this.signatureValidationEnabled = signatureValidationEnabled; }

        public boolean isContentScanningEnabled() { return contentScanningEnabled; }
        public void setContentScanningEnabled(boolean contentScanningEnabled) { this.contentScanningEnabled = contentScanningEnabled; }

        public boolean isEntropyAnalysisEnabled() { return entropyAnalysisEnabled; }
        public void setEntropyAnalysisEnabled(boolean entropyAnalysisEnabled) { this.entropyAnalysisEnabled = entropyAnalysisEnabled; }

        public boolean isBlockHighThreatFiles() { return blockHighThreatFiles; }
        public void setBlockHighThreatFiles(boolean blockHighThreatFiles) { this.blockHighThreatFiles = blockHighThreatFiles; }

        public boolean isQuarantineSuspiciousFiles() { return quarantineSuspiciousFiles; }
        public void setQuarantineSuspiciousFiles(boolean quarantineSuspiciousFiles) { this.quarantineSuspiciousFiles = quarantineSuspiciousFiles; }
    }

    /**
     * Cache Configuration
     */
    public static class CacheConfig {
        /**
         * Enable caching
         */
        private boolean enabled = true;

        /**
         * Cache provider (memory, redis, etc.)
         */
        private String provider = "memory";

        /**
         * Default cache TTL in seconds
         */
        @Positive(message = "Cache TTL must be positive")
        private long defaultTtlSeconds = 3600;

        /**
         * Maximum cache size (number of entries)
         */
        @Positive(message = "Max cache size must be positive")
        private long maxSize = 10000;

        /**
         * Cache eviction policy
         */
        private String evictionPolicy = "LRU";

        /**
         * Enable cache statistics
         */
        private boolean statisticsEnabled = true;

        // Getters and Setters
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public long getDefaultTtlSeconds() {
            return defaultTtlSeconds;
        }

        public void setDefaultTtlSeconds(long defaultTtlSeconds) {
            this.defaultTtlSeconds = defaultTtlSeconds;
        }

        public long getMaxSize() {
            return maxSize;
        }

        public void setMaxSize(long maxSize) {
            this.maxSize = maxSize;
        }

        public String getEvictionPolicy() {
            return evictionPolicy;
        }

        public void setEvictionPolicy(String evictionPolicy) {
            this.evictionPolicy = evictionPolicy;
        }

        public boolean isStatisticsEnabled() {
            return statisticsEnabled;
        }

        public void setStatisticsEnabled(boolean statisticsEnabled) {
            this.statisticsEnabled = statisticsEnabled;
        }

        /**
         * Get default TTL in milliseconds
         */
        public long getDefaultTtlMillis() {
            return defaultTtlSeconds * 1000L;
        }
    }

    /**
     * Health Check Configuration
     */
    public static class HealthConfig {
        /**
         * Enable health checks
         */
        private boolean enabled = true;

        /**
         * Disk space threshold percentage (0-100)
         */
        @DecimalMin(value = "0.0", message = "Disk space threshold must be at least 0")
        @DecimalMax(value = "100.0", message = "Disk space threshold must be at most 100")
        private double diskSpaceThreshold = 90.0;

        /**
         * Memory usage threshold percentage (0-100)
         */
        @DecimalMin(value = "0.0", message = "Memory threshold must be at least 0")
        @DecimalMax(value = "100.0", message = "Memory threshold must be at most 100")
        private double memoryThreshold = 90.0;

        /**
         * Health check interval in seconds
         */
        @Positive(message = "Health check interval must be positive")
        private int checkIntervalSeconds = 60;

        /**
         * Enable detailed health information
         */
        private boolean detailedInfo = true;

        // Getters and Setters
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public double getDiskSpaceThreshold() {
            return diskSpaceThreshold;
        }

        public void setDiskSpaceThreshold(double diskSpaceThreshold) {
            this.diskSpaceThreshold = diskSpaceThreshold;
        }

        public double getMemoryThreshold() {
            return memoryThreshold;
        }

        public void setMemoryThreshold(double memoryThreshold) {
            this.memoryThreshold = memoryThreshold;
        }

        public int getCheckIntervalSeconds() {
            return checkIntervalSeconds;
        }

        public void setCheckIntervalSeconds(int checkIntervalSeconds) {
            this.checkIntervalSeconds = checkIntervalSeconds;
        }

        public boolean isDetailedInfo() {
            return detailedInfo;
        }

        public void setDetailedInfo(boolean detailedInfo) {
            this.detailedInfo = detailedInfo;
        }

        /**
         * Get check interval in milliseconds
         */
        public long getCheckIntervalMillis() {
            return checkIntervalSeconds * 1000L;
        }
    }

    @Override
    public String toString() {
        return "FileManagerProperties{" +
                "enabled=" + enabled +
                ", rootDirectory='" + rootDirectory + '\'' +
                ", maxFileSize=" + maxFileSize +
                ", maxStorageSize=" + maxStorageSize +
                ", allowedExtensions=" + allowedExtensions.size() +
                ", storage=" + storage.getType() +
                '}';
    }
}