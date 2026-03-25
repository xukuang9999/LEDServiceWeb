package com.zhglxt.fileManager.config;

import com.zhglxt.fileManager.domain.config.StorageConfig;
import com.zhglxt.fileManager.exception.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Configuration Validator for File Manager Properties
 * 
 * Validates configuration settings and provides detailed error messages
 * for invalid configurations.
 * 
 * @author zhglxt
 */
@Component
public class FileManagerConfigurationValidator {

    private static final Logger logger = LoggerFactory.getLogger(FileManagerConfigurationValidator.class);

    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$");
    private static final Pattern DIRECTORY_PATTERN = Pattern.compile("^[a-zA-Z0-9_\\-/\\\\]+$");

    /**
     * Validate the complete file manager configuration
     */
    public void validateConfiguration(FileManagerProperties properties) {
        List<String> errors = new ArrayList<>();

        try {
            validateBasicSettings(properties, errors);
            validateStorageConfiguration(properties.getStorage(), errors);
            validateThumbnailConfiguration(properties.getThumbnail(), errors);
            validateWatermarkConfiguration(properties.getWatermark(), errors);
            validatePerformanceConfiguration(properties.getPerformance(), errors);
            validateSecurityConfiguration(properties.getSecurity(), errors);
            validateCacheConfiguration(properties.getCache(), errors);
            validateDirectoryStructure(properties, errors);

            if (!errors.isEmpty()) {
                String errorMessage = "File Manager configuration validation failed:\n" + 
                    String.join("\n", errors);
                logger.error("Configuration validation failed: {}", errorMessage);
                throw new ConfigurationException(errorMessage);
            }

            logger.info("File Manager configuration validation passed successfully");

        } catch (Exception e) {
            logger.error("Error during configuration validation", e);
            throw new ConfigurationException("Configuration validation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Validate basic file manager settings
     */
    private void validateBasicSettings(FileManagerProperties properties, List<String> errors) {
        // Validate root directory
        if (!StringUtils.hasText(properties.getRootDirectory())) {
            errors.add("Root directory cannot be empty");
        } else if (!DIRECTORY_PATTERN.matcher(properties.getRootDirectory()).matches()) {
            errors.add("Root directory contains invalid characters: " + properties.getRootDirectory());
        }

        // Validate file size limits
        if (properties.getMaxFileSize() <= 0) {
            errors.add("Maximum file size must be positive");
        }

        if (properties.getMaxFileSize() > 10L * 1024 * 1024 * 1024) { // 10GB
            errors.add("Maximum file size is too large (max: 10GB)");
        }

        if (properties.getMaxStorageSize() > 0 && 
            properties.getMaxStorageSize() < properties.getMaxFileSize()) {
            errors.add("Maximum storage size cannot be smaller than maximum file size");
        }

        // Validate file extensions
        if (properties.getAllowedExtensions().isEmpty()) {
            errors.add("At least one file extension must be allowed");
        }

        // Check for conflicting extensions
        for (String blocked : properties.getBlockedExtensions()) {
            if (properties.getAllowedExtensions().contains(blocked)) {
                errors.add("Extension '" + blocked + "' is both allowed and blocked");
            }
        }

        // Validate timeout settings
        if (properties.getOperationTimeoutSeconds() < 10) {
            errors.add("Operation timeout is too short (minimum: 10 seconds)");
        }

        if (properties.getOperationTimeoutSeconds() > 3600) {
            errors.add("Operation timeout is too long (maximum: 1 hour)");
        }

        // Validate temp directory
        if (StringUtils.hasText(properties.getTempDirectory())) {
            Path tempPath = Paths.get(properties.getTempDirectory());
            if (!Files.exists(tempPath)) {
                try {
                    Files.createDirectories(tempPath);
                } catch (Exception e) {
                    errors.add("Cannot create temp directory: " + properties.getTempDirectory());
                }
            } else if (!Files.isDirectory(tempPath)) {
                errors.add("Temp directory path is not a directory: " + properties.getTempDirectory());
            }
        }

        // Validate compression settings
        if (properties.isCompressionEnabled()) {
            if (properties.getCompressionQuality() < 0.1f || properties.getCompressionQuality() > 1.0f) {
                errors.add("Compression quality must be between 0.1 and 1.0");
            }
        }
    }

    /**
     * Validate storage configuration
     */
    private void validateStorageConfiguration(StorageConfig storage, List<String> errors) {
        if (storage == null) {
            errors.add("Storage configuration is required");
            return;
        }

        if (storage.getType() == null) {
            errors.add("Storage type is required");
            return;
        }

        // Validate cloud storage settings
        if (storage.isCloud()) {
            if (!StringUtils.hasText(storage.getAccessKey())) {
                errors.add("Access key is required for cloud storage");
            }

            if (!StringUtils.hasText(storage.getSecretKey())) {
                errors.add("Secret key is required for cloud storage");
            }

            if (!StringUtils.hasText(storage.getBucketName())) {
                errors.add("Bucket name is required for cloud storage");
            }

            // Validate endpoint for specific storage types
            if (storage.getType() == StorageConfig.StorageType.MINIO && 
                !StringUtils.hasText(storage.getEndpoint())) {
                errors.add("Endpoint is required for MinIO storage");
            }

            // Validate CDN configuration
            if (storage.isCdnEnabled() && !StringUtils.hasText(storage.getCdnDomain())) {
                errors.add("CDN domain is required when CDN is enabled");
            }
        }

        // Validate connection settings
        if (storage.getConnectionTimeout() < 1000) {
            errors.add("Connection timeout is too short (minimum: 1 second)");
        }

        if (storage.getReadTimeout() < 5000) {
            errors.add("Read timeout is too short (minimum: 5 seconds)");
        }

        if (storage.getMaxConnections() < 1) {
            errors.add("Maximum connections must be at least 1");
        }

        if (storage.getMaxConnections() > 1000) {
            errors.add("Maximum connections is too high (maximum: 1000)");
        }
    }

    /**
     * Validate thumbnail configuration
     */
    private void validateThumbnailConfiguration(com.zhglxt.fileManager.domain.config.ThumbnailConfig thumbnail, List<String> errors) {
        if (thumbnail == null) {
            errors.add("Thumbnail configuration is required");
            return;
        }

        if (thumbnail.isEnabled()) {
            if (thumbnail.getWidth() < 50 || thumbnail.getWidth() > 2000) {
                errors.add("Thumbnail width must be between 50 and 2000 pixels");
            }

            if (thumbnail.getHeight() < 50 || thumbnail.getHeight() > 2000) {
                errors.add("Thumbnail height must be between 50 and 2000 pixels");
            }

            if (thumbnail.getMaxWidth() < thumbnail.getWidth()) {
                errors.add("Thumbnail max width cannot be smaller than default width");
            }

            if (thumbnail.getMaxHeight() < thumbnail.getHeight()) {
                errors.add("Thumbnail max height cannot be smaller than default height");
            }

            if (thumbnail.getQuality() < 0.1f || thumbnail.getQuality() > 1.0f) {
                errors.add("Thumbnail quality must be between 0.1 and 1.0");
            }

            if (!StringUtils.hasText(thumbnail.getDirectory())) {
                errors.add("Thumbnail directory cannot be empty");
            }

            // Validate background color format
            if (StringUtils.hasText(thumbnail.getBackgroundColor()) && 
                !HEX_COLOR_PATTERN.matcher(thumbnail.getBackgroundColor()).matches()) {
                errors.add("Invalid thumbnail background color format: " + thumbnail.getBackgroundColor());
            }

            // Validate format
            String format = thumbnail.getFormat().toLowerCase();
            if (!format.equals("jpg") && !format.equals("png") && !format.equals("webp")) {
                errors.add("Unsupported thumbnail format: " + thumbnail.getFormat());
            }
        }
    }

    /**
     * Validate watermark configuration
     */
    private void validateWatermarkConfiguration(com.zhglxt.fileManager.domain.config.WatermarkConfig watermark, List<String> errors) {
        if (watermark == null) {
            errors.add("Watermark configuration is required");
            return;
        }

        if (watermark.isEnabled()) {
            if (!StringUtils.hasText(watermark.getText()) && !StringUtils.hasText(watermark.getImagePath())) {
                errors.add("Watermark text or image path is required when watermark is enabled");
            }

            if (watermark.getOpacity() < 0.0f || watermark.getOpacity() > 1.0f) {
                errors.add("Watermark opacity must be between 0.0 and 1.0");
            }

            if (watermark.getFontSize() < 8 || watermark.getFontSize() > 200) {
                errors.add("Watermark font size must be between 8 and 200");
            }

            // Validate color format
            if (StringUtils.hasText(watermark.getColor()) && 
                !HEX_COLOR_PATTERN.matcher(watermark.getColor()).matches()) {
                errors.add("Invalid watermark color format: " + watermark.getColor());
            }

            // Validate image watermark
            if (StringUtils.hasText(watermark.getImagePath())) {
                File imageFile = new File(watermark.getImagePath());
                if (!imageFile.exists()) {
                    errors.add("Watermark image file does not exist: " + watermark.getImagePath());
                } else if (!imageFile.isFile()) {
                    errors.add("Watermark image path is not a file: " + watermark.getImagePath());
                }
            }

            // Validate minimum image dimensions
            if (watermark.getMinImageWidth() < 50) {
                errors.add("Minimum image width for watermark is too small (minimum: 50px)");
            }

            if (watermark.getMinImageHeight() < 50) {
                errors.add("Minimum image height for watermark is too small (minimum: 50px)");
            }
        }
    }

    /**
     * Validate performance configuration
     */
    private void validatePerformanceConfiguration(FileManagerProperties.PerformanceConfig performance, List<String> errors) {
        if (performance == null) {
            errors.add("Performance configuration is required");
            return;
        }

        if (performance.getThreadPoolSize() < 1) {
            errors.add("Thread pool size must be at least 1");
        }

        if (performance.getThreadPoolSize() > 100) {
            errors.add("Thread pool size is too large (maximum: 100)");
        }

        if (performance.getQueueSize() < 10) {
            errors.add("Queue size is too small (minimum: 10)");
        }

        if (performance.getBatchSize() < 1) {
            errors.add("Batch size must be at least 1");
        }

        if (performance.getBatchSize() > 1000) {
            errors.add("Batch size is too large (maximum: 1000)");
        }

        if (performance.getMemoryThreshold() < 1024 * 1024) { // 1MB
            errors.add("Memory threshold is too small (minimum: 1MB)");
        }
    }

    /**
     * Validate security configuration
     */
    private void validateSecurityConfiguration(FileManagerProperties.SecurityConfig security, List<String> errors) {
        if (security == null) {
            errors.add("Security configuration is required");
            return;
        }

        if (security.getMaxFilenameLength() < 10) {
            errors.add("Maximum filename length is too short (minimum: 10)");
        }

        if (security.getMaxFilenameLength() > 1000) {
            errors.add("Maximum filename length is too long (maximum: 1000)");
        }

        if (security.getMaxPathDepth() < 1) {
            errors.add("Maximum path depth must be at least 1");
        }

        if (security.getMaxPathDepth() > 50) {
            errors.add("Maximum path depth is too large (maximum: 50)");
        }

        if (security.isRateLimitingEnabled() && security.getRateLimitPerMinute() < 1) {
            errors.add("Rate limit per minute must be at least 1");
        }

        // Validate quarantine directory
        if (StringUtils.hasText(security.getQuarantineDirectory()) && 
            !DIRECTORY_PATTERN.matcher(security.getQuarantineDirectory()).matches()) {
            errors.add("Quarantine directory contains invalid characters: " + security.getQuarantineDirectory());
        }
    }

    /**
     * Validate cache configuration
     */
    private void validateCacheConfiguration(FileManagerProperties.CacheConfig cache, List<String> errors) {
        if (cache == null) {
            errors.add("Cache configuration is required");
            return;
        }

        if (cache.isEnabled()) {
            if (!StringUtils.hasText(cache.getProvider())) {
                errors.add("Cache provider is required when caching is enabled");
            }

            if (cache.getDefaultTtlSeconds() < 60) {
                errors.add("Cache TTL is too short (minimum: 60 seconds)");
            }

            if (cache.getMaxSize() < 100) {
                errors.add("Cache max size is too small (minimum: 100)");
            }

            String provider = cache.getProvider().toLowerCase();
            if (!provider.equals("memory") && !provider.equals("redis") && !provider.equals("caffeine")) {
                errors.add("Unsupported cache provider: " + cache.getProvider());
            }

            String evictionPolicy = cache.getEvictionPolicy().toUpperCase();
            if (!evictionPolicy.equals("LRU") && !evictionPolicy.equals("LFU") && !evictionPolicy.equals("FIFO")) {
                errors.add("Unsupported cache eviction policy: " + cache.getEvictionPolicy());
            }
        }
    }

    /**
     * Validate directory structure and permissions
     */
    private void validateDirectoryStructure(FileManagerProperties properties, List<String> errors) {
        try {
            // Validate root directory
            Path rootPath = Paths.get(properties.getRootDirectory());
            validateDirectory(rootPath, "root", errors);

            // Validate thumbnail directory
            if (properties.getThumbnail().isEnabled()) {
                Path thumbnailPath = rootPath.resolve(properties.getThumbnail().getDirectory());
                validateDirectory(thumbnailPath, "thumbnail", errors);
            }

            // Validate temp directory
            if (StringUtils.hasText(properties.getTempDirectory())) {
                Path tempPath = Paths.get(properties.getTempDirectory());
                validateDirectory(tempPath, "temp", errors);
            }

            // Validate quarantine directory
            if (StringUtils.hasText(properties.getSecurity().getQuarantineDirectory())) {
                Path quarantinePath = rootPath.resolve(properties.getSecurity().getQuarantineDirectory());
                validateDirectory(quarantinePath, "quarantine", errors);
            }

        } catch (Exception e) {
            errors.add("Error validating directory structure: " + e.getMessage());
        }
    }

    /**
     * Validate a specific directory
     */
    private void validateDirectory(Path path, String type, List<String> errors) {
        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                logger.info("Created {} directory: {}", type, path);
            }

            if (!Files.isDirectory(path)) {
                errors.add(type + " path is not a directory: " + path);
                return;
            }

            if (!Files.isReadable(path)) {
                errors.add(type + " directory is not readable: " + path);
            }

            if (!Files.isWritable(path)) {
                errors.add(type + " directory is not writable: " + path);
            }

        } catch (Exception e) {
            errors.add("Cannot validate " + type + " directory " + path + ": " + e.getMessage());
        }
    }

    /**
     * Validate configuration at runtime (for dynamic updates)
     */
    public boolean isValidConfiguration(FileManagerProperties properties) {
        try {
            validateConfiguration(properties);
            return true;
        } catch (ConfigurationException e) {
            logger.warn("Configuration validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get validation warnings (non-critical issues)
     */
    public List<String> getConfigurationWarnings(FileManagerProperties properties) {
        List<String> warnings = new ArrayList<>();

        // Check for performance warnings
        if (properties.getMaxFileSize() > 1024 * 1024 * 1024) { // 1GB
            warnings.add("Large maximum file size may impact performance: " + 
                (properties.getMaxFileSize() / (1024 * 1024)) + "MB");
        }

        if (properties.getPerformance().getThreadPoolSize() > 20) {
            warnings.add("Large thread pool size may consume excessive resources: " + 
                properties.getPerformance().getThreadPoolSize());
        }

        // Check for security warnings
        if (!properties.isVirusScanEnabled()) {
            warnings.add("Virus scanning is disabled - consider enabling for better security");
        }

        if (properties.getAllowedExtensions().contains("exe") || 
            properties.getAllowedExtensions().contains("bat")) {
            warnings.add("Executable file types are allowed - this may pose security risks");
        }

        // Check for storage warnings
        if (properties.getStorage().isCloud() && !properties.getStorage().isUseHttps()) {
            warnings.add("HTTPS is disabled for cloud storage - this may pose security risks");
        }

        return warnings;
    }

    /**
     * Validate root directory
     */
    public boolean validateRootDirectory(String rootDirectory) {
        if (!StringUtils.hasText(rootDirectory)) {
            return false;
        }
        
        // Check for valid directory pattern
        if (!DIRECTORY_PATTERN.matcher(rootDirectory).matches()) {
            return false;
        }
        
        return true;
    }

    /**
     * Validate max file size
     */
    public boolean validateMaxFileSize(long maxFileSize) {
        return maxFileSize > 0 && maxFileSize <= 10L * 1024 * 1024 * 1024; // Max 10GB
    }
}