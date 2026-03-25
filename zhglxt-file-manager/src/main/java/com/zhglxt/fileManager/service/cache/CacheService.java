package com.zhglxt.fileManager.service.cache;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Cache Service Interface
 * Provides caching functionality for file manager operations
 * 
 * @author zhglxt
 */
public interface CacheService {

    /**
     * Store a value in cache
     * 
     * @param key cache key
     * @param value value to cache
     * @param ttl time to live
     */
    void put(String key, Object value, Duration ttl);

    /**
     * Store a value in cache with default TTL
     * 
     * @param key cache key
     * @param value value to cache
     */
    void put(String key, Object value);

    /**
     * Get a value from cache
     * 
     * @param key cache key
     * @param type expected value type
     * @return cached value or empty if not found
     */
    <T> Optional<T> get(String key, Class<T> type);

    /**
     * Get a value from cache asynchronously
     * 
     * @param key cache key
     * @param type expected value type
     * @return future with cached value or empty if not found
     */
    <T> CompletableFuture<Optional<T>> getAsync(String key, Class<T> type);

    /**
     * Remove a value from cache
     * 
     * @param key cache key
     * @return true if value was removed
     */
    boolean evict(String key);

    /**
     * Remove all values matching a pattern
     * 
     * @param pattern key pattern (supports wildcards)
     * @return number of evicted entries
     */
    long evictByPattern(String pattern);

    /**
     * Clear all cache entries
     */
    void clear();

    /**
     * Check if key exists in cache
     * 
     * @param key cache key
     * @return true if key exists
     */
    boolean exists(String key);

    /**
     * Get cache statistics
     * 
     * @return cache statistics
     */
    CacheStats getStats();

    /**
     * Get cache configuration
     * 
     * @return cache configuration
     */
    CacheConfig getConfig();

    /**
     * Cache statistics
     */
    interface CacheStats {
        long getHitCount();
        long getMissCount();
        double getHitRate();
        long getEvictionCount();
        long getSize();
        Map<String, Object> getAdditionalStats();
    }

    /**
     * Cache configuration
     */
    interface CacheConfig {
        String getProvider();
        Duration getDefaultTtl();
        long getMaxSize();
        String getEvictionPolicy();
        boolean isStatisticsEnabled();
    }
}