package com.zhglxt.fileManager.service.impl;

import com.zhglxt.fileManager.domain.config.ThumbnailConfig.ThumbnailSize;
import com.zhglxt.fileManager.service.ThumbnailService;
import com.zhglxt.fileManager.service.async.AsyncProcessingService;
import com.zhglxt.fileManager.service.cache.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cached implementation of thumbnail service with performance optimizations
 * 
 * @author zhglxt
 */
@Service
public class CachedThumbnailService implements ThumbnailService {

    private static final Logger logger = LoggerFactory.getLogger(CachedThumbnailService.class);

    private static final String CACHE_PREFIX = "thumbnail:";
    private static final String METADATA_CACHE_PREFIX = "thumbnail_meta:";
    private static final Duration CACHE_TTL = Duration.ofHours(24);

    @Autowired
    private CacheService cacheService;

    @Autowired
    private AsyncProcessingService asyncProcessingService;

    // In-memory cache for frequently accessed thumbnails
    private final Map<String, String> thumbnailPathCache = new ConcurrentHashMap<>();

    @Override
    public String generateThumbnail(String imagePath, ThumbnailSize size) {
        String cacheKey = getCacheKey(imagePath, size);
        
        // Check cache first
        Optional<String> cached = cacheService.get(cacheKey, String.class);
        if (cached.isPresent()) {
            logger.debug("Thumbnail cache hit for: {}", imagePath);
            return cached.get();
        }

        // Generate thumbnail asynchronously for better performance
        CompletableFuture<String> thumbnailFuture = asyncProcessingService.executeAsync(() -> {
            return doGenerateThumbnail(imagePath, size);
        }, 5); // High priority for thumbnail generation

        try {
            String thumbnailPath = thumbnailFuture.get();
            if (thumbnailPath != null) {
                // Cache the result
                cacheService.put(cacheKey, thumbnailPath, CACHE_TTL);
                thumbnailPathCache.put(cacheKey, thumbnailPath);
                logger.debug("Generated and cached thumbnail for: {}", imagePath);
            }
            return thumbnailPath;
        } catch (Exception e) {
            logger.error("Error generating thumbnail for: {}", imagePath, e);
            return null;
        }
    }

    @Override
    public String generateThumbnail(String imagePath, int width, int height) {
        String cacheKey = getCacheKey(imagePath, width, height);
        
        // Check cache first
        Optional<String> cached = cacheService.get(cacheKey, String.class);
        if (cached.isPresent()) {
            logger.debug("Custom thumbnail cache hit for: {}", imagePath);
            return cached.get();
        }

        // Generate thumbnail asynchronously
        CompletableFuture<String> thumbnailFuture = asyncProcessingService.executeAsync(() -> {
            return doGenerateThumbnail(imagePath, width, height);
        }, 5);

        try {
            String thumbnailPath = thumbnailFuture.get();
            if (thumbnailPath != null) {
                cacheService.put(cacheKey, thumbnailPath, CACHE_TTL);
                thumbnailPathCache.put(cacheKey, thumbnailPath);
                logger.debug("Generated and cached custom thumbnail for: {}", imagePath);
            }
            return thumbnailPath;
        } catch (Exception e) {
            logger.error("Error generating custom thumbnail for: {}", imagePath, e);
            return null;
        }
    }

    @Override
    public String generateThumbnail(InputStream inputStream, String originalFileName, ThumbnailSize size) {
        // For input streams, we can't cache as effectively, but we can still use async processing
        CompletableFuture<String> thumbnailFuture = asyncProcessingService.executeAsync(() -> {
            return doGenerateThumbnail(inputStream, originalFileName, size);
        }, 5);

        try {
            return thumbnailFuture.get();
        } catch (Exception e) {
            logger.error("Error generating thumbnail from stream for: {}", originalFileName, e);
            return null;
        }
    }

    @Override
    public boolean thumbnailExists(String imagePath, ThumbnailSize size) {
        String cacheKey = getCacheKey(imagePath, size);
        
        // Check memory cache first
        if (thumbnailPathCache.containsKey(cacheKey)) {
            return true;
        }
        
        // Check distributed cache
        if (cacheService.exists(cacheKey)) {
            return true;
        }
        
        // Check file system
        return doThumbnailExists(imagePath, size);
    }

    @Override
    public boolean thumbnailExists(String imagePath, int width, int height) {
        String cacheKey = getCacheKey(imagePath, width, height);
        
        if (thumbnailPathCache.containsKey(cacheKey)) {
            return true;
        }
        
        if (cacheService.exists(cacheKey)) {
            return true;
        }
        
        return doThumbnailExists(imagePath, width, height);
    }

    @Override
    public String getThumbnailPath(String imagePath, ThumbnailSize size) {
        String cacheKey = getCacheKey(imagePath, size);
        
        // Check memory cache first
        String cached = thumbnailPathCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        
        // Check distributed cache
        Optional<String> distributedCached = cacheService.get(cacheKey, String.class);
        if (distributedCached.isPresent()) {
            String path = distributedCached.get();
            thumbnailPathCache.put(cacheKey, path);
            return path;
        }
        
        return doGetThumbnailPath(imagePath, size);
    }

    @Override
    public String getThumbnailPath(String imagePath, int width, int height) {
        String cacheKey = getCacheKey(imagePath, width, height);
        
        String cached = thumbnailPathCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        
        Optional<String> distributedCached = cacheService.get(cacheKey, String.class);
        if (distributedCached.isPresent()) {
            String path = distributedCached.get();
            thumbnailPathCache.put(cacheKey, path);
            return path;
        }
        
        return doGetThumbnailPath(imagePath, width, height);
    }

    @Override
    public boolean deleteThumbnails(String imagePath) {
        // Evict from cache
        String pattern = getCacheKeyPattern(imagePath);
        cacheService.evictByPattern(pattern);
        
        // Remove from memory cache
        thumbnailPathCache.entrySet().removeIf(entry -> entry.getKey().contains(imagePath));
        
        return doDeleteThumbnails(imagePath);
    }

    @Override
    public boolean deleteThumbnail(String imagePath, ThumbnailSize size) {
        String cacheKey = getCacheKey(imagePath, size);
        cacheService.evict(cacheKey);
        thumbnailPathCache.remove(cacheKey);
        
        return doDeleteThumbnail(imagePath, size);
    }

    @Override
    public boolean deleteThumbnail(String imagePath, int width, int height) {
        String cacheKey = getCacheKey(imagePath, width, height);
        cacheService.evict(cacheKey);
        thumbnailPathCache.remove(cacheKey);
        
        return doDeleteThumbnail(imagePath, width, height);
    }

    @Override
    public int cleanupOrphanedThumbnails() {
        // Run cleanup asynchronously
        asyncProcessingService.executeAsync(() -> {
            int cleaned = doCleanupOrphanedThumbnails();
            logger.info("Cleaned up {} orphaned thumbnails", cleaned);
            return cleaned;
        }, 1); // Low priority for cleanup
        
        return 0; // Return immediately, actual cleanup runs async
    }

    @Override
    public List<ThumbnailSize> getAvailableThumbnailSizes(String imagePath) {
        String cacheKey = METADATA_CACHE_PREFIX + "sizes:" + imagePath;
        
        Optional<?> cachedRaw = cacheService.get(cacheKey, List.class);
        @SuppressWarnings("unchecked")
        Optional<List<ThumbnailSize>> cached = (Optional<List<ThumbnailSize>>) cachedRaw;
        if (cached.isPresent()) {
            return cached.get();
        }
        
        List<ThumbnailSize> sizes = doGetAvailableThumbnailSizes(imagePath);
        if (sizes != null && !sizes.isEmpty()) {
            cacheService.put(cacheKey, sizes, Duration.ofMinutes(30));
        }
        
        return sizes;
    }

    @Override
    public boolean isSupportedImageType(String fileName) {
        // This is a simple check, can be cached if needed
        return doIsSupportedImageType(fileName);
    }

    @Override
    public InputStream getThumbnailInputStream(String imagePath, ThumbnailSize size) {
        // For input streams, caching is more complex, but we can cache metadata
        return doGetThumbnailInputStream(imagePath, size);
    }

    @Override
    public boolean refreshThumbnailCache(String imagePath) {
        // Evict all cached thumbnails for this image
        String pattern = getCacheKeyPattern(imagePath);
        long evicted = cacheService.evictByPattern(pattern);
        
        // Remove from memory cache
        thumbnailPathCache.entrySet().removeIf(entry -> entry.getKey().contains(imagePath));
        
        logger.info("Refreshed thumbnail cache for: {}, evicted {} entries", imagePath, evicted);
        return true;
    }

    @Override
    public boolean clearThumbnailCache() {
        // Clear distributed cache
        cacheService.evictByPattern(CACHE_PREFIX + "*");
        cacheService.evictByPattern(METADATA_CACHE_PREFIX + "*");
        
        // Clear memory cache
        thumbnailPathCache.clear();
        
        logger.info("Cleared all thumbnail caches");
        return true;
    }

    @Override
    public Map<String, Object> getThumbnailCacheStats() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        
        // Add cache service stats
        CacheService.CacheStats cacheStats = cacheService.getStats();
        stats.put("distributedCache", Map.of(
            "hitCount", cacheStats.getHitCount(),
            "missCount", cacheStats.getMissCount(),
            "hitRate", cacheStats.getHitRate(),
            "size", cacheStats.getSize()
        ));
        
        // Add memory cache stats
        stats.put("memoryCache", Map.of(
            "size", thumbnailPathCache.size()
        ));
        
        return stats;
    }

    // Helper methods for cache key generation
    private String getCacheKey(String imagePath, ThumbnailSize size) {
        return CACHE_PREFIX + imagePath + ":" + size.name();
    }

    private String getCacheKey(String imagePath, int width, int height) {
        return CACHE_PREFIX + imagePath + ":" + width + "x" + height;
    }

    private String getCacheKeyPattern(String imagePath) {
        return CACHE_PREFIX + imagePath + ":*";
    }

    // Placeholder methods for actual thumbnail operations
    // These would be implemented with actual thumbnail generation logic
    private String doGenerateThumbnail(String imagePath, ThumbnailSize size) {
        // Actual thumbnail generation logic here
        logger.debug("Generating thumbnail for: {} with size: {}", imagePath, size);
        return "thumbnail_path_" + imagePath + "_" + size.name();
    }

    private String doGenerateThumbnail(String imagePath, int width, int height) {
        logger.debug("Generating custom thumbnail for: {} with dimensions: {}x{}", imagePath, width, height);
        return "thumbnail_path_" + imagePath + "_" + width + "x" + height;
    }

    private String doGenerateThumbnail(InputStream inputStream, String originalFileName, ThumbnailSize size) {
        logger.debug("Generating thumbnail from stream for: {} with size: {}", originalFileName, size);
        return "thumbnail_path_stream_" + originalFileName + "_" + size.name();
    }

    private boolean doThumbnailExists(String imagePath, ThumbnailSize size) {
        // Check if thumbnail file exists
        return false; // Placeholder
    }

    private boolean doThumbnailExists(String imagePath, int width, int height) {
        return false; // Placeholder
    }

    private String doGetThumbnailPath(String imagePath, ThumbnailSize size) {
        return "thumbnail_path_" + imagePath + "_" + size.name(); // Placeholder
    }

    private String doGetThumbnailPath(String imagePath, int width, int height) {
        return "thumbnail_path_" + imagePath + "_" + width + "x" + height; // Placeholder
    }

    private boolean doDeleteThumbnails(String imagePath) {
        return true; // Placeholder
    }

    private boolean doDeleteThumbnail(String imagePath, ThumbnailSize size) {
        return true; // Placeholder
    }

    private boolean doDeleteThumbnail(String imagePath, int width, int height) {
        return true; // Placeholder
    }

    private int doCleanupOrphanedThumbnails() {
        return 0; // Placeholder
    }

    private List<ThumbnailSize> doGetAvailableThumbnailSizes(String imagePath) {
        return List.of(); // Placeholder
    }

    private boolean doIsSupportedImageType(String fileName) {
        String extension = fileName.toLowerCase();
        return extension.endsWith(".jpg") || extension.endsWith(".jpeg") || 
               extension.endsWith(".png") || extension.endsWith(".gif") ||
               extension.endsWith(".bmp") || extension.endsWith(".webp");
    }

    private InputStream doGetThumbnailInputStream(String imagePath, ThumbnailSize size) {
        return null; // Placeholder
    }
}