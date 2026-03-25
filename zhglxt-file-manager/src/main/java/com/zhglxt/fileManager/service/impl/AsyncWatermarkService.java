package com.zhglxt.fileManager.service.impl;

import com.zhglxt.fileManager.domain.config.WatermarkConfig;
import com.zhglxt.fileManager.exception.WatermarkException;
import com.zhglxt.fileManager.service.WatermarkService;
import com.zhglxt.fileManager.service.async.AsyncProcessingService;
import com.zhglxt.fileManager.service.cache.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Async implementation of watermark service with caching
 * 
 * @author zhglxt
 */
@Service
public class AsyncWatermarkService implements WatermarkService {

    private static final Logger logger = LoggerFactory.getLogger(AsyncWatermarkService.class);

    private static final String CACHE_PREFIX = "watermark:";
    private static final Duration CACHE_TTL = Duration.ofHours(12);

    @Autowired
    private AsyncProcessingService asyncProcessingService;

    @Autowired
    private CacheService cacheService;

    private WatermarkConfig defaultConfig = createDefaultConfig();

    @Override
    public InputStream applyWatermark(InputStream imageInputStream, String fileName) {
        return applyWatermark(imageInputStream, fileName, defaultConfig);
    }

    @Override
    public InputStream applyWatermark(InputStream imageInputStream, String fileName, WatermarkConfig config) {
        if (!isWatermarkEnabled() || !supportsWatermarking(fileName)) {
            return imageInputStream;
        }

        try {
            // Convert input stream to byte array for caching and processing
            byte[] imageBytes = readAllBytes(imageInputStream);
            String cacheKey = generateCacheKey(imageBytes, config);

            // Check cache first
            Optional<byte[]> cached = cacheService.get(cacheKey, byte[].class);
            if (cached.isPresent()) {
                logger.debug("Watermark cache hit for file: {}", fileName);
                return new ByteArrayInputStream(cached.get());
            }

            // Check if image meets minimum size requirements
            ByteArrayInputStream sizeCheckStream = new ByteArrayInputStream(imageBytes);
            if (!meetsMinimumSize(sizeCheckStream, fileName)) {
                logger.debug("Image {} does not meet minimum size requirements for watermarking", fileName);
                return new ByteArrayInputStream(imageBytes);
            }

            // Apply watermark asynchronously for better performance
            CompletableFuture<byte[]> watermarkFuture = asyncProcessingService.executeAsync(() -> {
                try {
                    return doApplyWatermark(imageBytes, fileName, config);
                } catch (Exception e) {
                    logger.error("Error applying watermark to: {}", fileName, e);
                    return imageBytes; // Return original on error
                }
            }, 3); // Medium priority for watermarking

            byte[] watermarkedBytes = watermarkFuture.get();
            
            // Cache the result if it's different from original
            if (watermarkedBytes != imageBytes) {
                cacheService.put(cacheKey, watermarkedBytes, CACHE_TTL);
                logger.debug("Applied and cached watermark for: {}", fileName);
            }

            return new ByteArrayInputStream(watermarkedBytes);

        } catch (Exception e) {
            logger.error("Error in watermark processing for: {}", fileName, e);
            throw new WatermarkException("Failed to apply watermark", e);
        }
    }

    @Override
    public boolean isWatermarkEnabled() {
        return defaultConfig.isEnabled();
    }

    @Override
    public WatermarkConfig getWatermarkConfig() {
        return defaultConfig;
    }

    @Override
    public boolean supportsWatermarking(String fileName) {
        if (fileName == null) {
            return false;
        }
        
        String extension = fileName.toLowerCase();
        return extension.endsWith(".jpg") || extension.endsWith(".jpeg") || 
               extension.endsWith(".png") || extension.endsWith(".bmp") ||
               extension.endsWith(".gif") || extension.endsWith(".webp");
    }

    @Override
    public boolean meetsMinimumSize(InputStream imageInputStream, String fileName) {
        try {
            // This is a simplified check - in real implementation, you would
            // read image dimensions using ImageIO or similar
            byte[] imageBytes = readAllBytes(imageInputStream);
            
            // Mock size check based on file size as proxy for image dimensions
            int minFileSize = 10 * 1024; // 10KB as minimum
            boolean meetsSize = imageBytes.length >= minFileSize;
            
            logger.debug("Size check for {}: {} bytes, meets minimum: {}", 
                        fileName, imageBytes.length, meetsSize);
            
            return meetsSize;
            
        } catch (IOException e) {
            logger.error("Error checking image size for: {}", fileName, e);
            return false;
        }
    }

    @Override
    public void validateConfig(WatermarkConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Watermark config cannot be null");
        }
        
        if (config.getOpacity() < 0.0 || config.getOpacity() > 1.0) {
            throw new IllegalArgumentException("Opacity must be between 0.0 and 1.0");
        }
        
        if (config.getFontSize() <= 0) {
            throw new IllegalArgumentException("Font size must be positive");
        }
        
        if (config.getMargin() < 0) {
            throw new IllegalArgumentException("Margin cannot be negative");
        }
        
        if (config.getMinImageWidth() <= 0 || config.getMinImageHeight() <= 0) {
            throw new IllegalArgumentException("Minimum image dimensions must be positive");
        }
    }

    /**
     * Apply watermark asynchronously and return future
     */
    public CompletableFuture<InputStream> applyWatermarkAsync(InputStream imageInputStream, String fileName) {
        return applyWatermarkAsync(imageInputStream, fileName, defaultConfig);
    }

    /**
     * Apply watermark asynchronously with custom config
     */
    public CompletableFuture<InputStream> applyWatermarkAsync(InputStream imageInputStream, String fileName, WatermarkConfig config) {
        return asyncProcessingService.executeAsync(() -> {
            return applyWatermark(imageInputStream, fileName, config);
        }, 3);
    }

    /**
     * Batch watermark processing for multiple images
     */
    public CompletableFuture<Void> batchApplyWatermark(java.util.List<String> imagePaths, WatermarkConfig config) {
        return asyncProcessingService.executeAsync(() -> {
            for (String imagePath : imagePaths) {
                try {
                    // Process each image
                    logger.debug("Processing watermark for batch item: {}", imagePath);
                    // Implementation would process actual files
                } catch (Exception e) {
                    logger.error("Error processing watermark for batch item: {}", imagePath, e);
                }
            }
        }, 1); // Low priority for batch operations
    }

    /**
     * Clear watermark cache
     */
    public void clearWatermarkCache() {
        cacheService.evictByPattern(CACHE_PREFIX + "*");
        logger.info("Cleared watermark cache");
    }

    /**
     * Get watermark cache statistics
     */
    public java.util.Map<String, Object> getWatermarkCacheStats() {
        CacheService.CacheStats stats = cacheService.getStats();
        return java.util.Map.of(
            "hitCount", stats.getHitCount(),
            "missCount", stats.getMissCount(),
            "hitRate", stats.getHitRate(),
            "size", stats.getSize()
        );
    }

    // Private helper methods

    private byte[] doApplyWatermark(byte[] imageBytes, String fileName, WatermarkConfig config) {
        // This is a placeholder for actual watermark implementation
        // In real implementation, you would use libraries like ImageIO, BufferedImage, Graphics2D
        logger.debug("Applying watermark to {} with config: {}", fileName, config.getText());
        
        // Mock watermark processing - return original bytes
        // In real implementation, this would modify the image
        return imageBytes;
    }

    private String generateCacheKey(byte[] imageBytes, WatermarkConfig config) {
        // Generate cache key based on image content hash and config
        int imageHash = java.util.Arrays.hashCode(imageBytes);
        int configHash = config.hashCode();
        return CACHE_PREFIX + imageHash + ":" + configHash;
    }

    private byte[] readAllBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int bytesRead;
        
        while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, bytesRead);
        }
        
        return buffer.toByteArray();
    }

    private WatermarkConfig createDefaultConfig() {
        // Create default watermark configuration
        // This would typically be loaded from application properties
        WatermarkConfig config = new WatermarkConfig();
        config.setEnabled(false); // Disabled by default
        config.setText("zhglxt");
        config.setOpacity(0.5f);
        config.setFontSize(24);
        config.setColor("#FFFFFF");
        config.setPosition(WatermarkConfig.WatermarkPosition.BOTTOM_RIGHT);
        config.setFontFamily("Arial");
        config.setFontStyle(WatermarkConfig.FontStyle.PLAIN);
        config.setMargin(10);
        config.setRotation(0);
        config.setApplyToThumbnails(false);
        config.setMinImageWidth(200);
        config.setMinImageHeight(200);
        config.setImagePath(null);
        config.setImageScale(0.2f);
        return config;
    }
}