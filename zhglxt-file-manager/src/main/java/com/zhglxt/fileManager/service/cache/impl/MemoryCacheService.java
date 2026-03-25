package com.zhglxt.fileManager.service.cache.impl;

import com.zhglxt.fileManager.service.cache.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

/**
 * Memory-based cache service implementation
 * 
 * @author zhglxt
 */
@Service
public class MemoryCacheService implements CacheService {

    private static final Logger logger = LoggerFactory.getLogger(MemoryCacheService.class);

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
    
    // Statistics
    private final AtomicLong hitCount = new AtomicLong(0);
    private final AtomicLong missCount = new AtomicLong(0);
    private final AtomicLong evictionCount = new AtomicLong(0);
    
    // Configuration
    private final Duration defaultTtl = Duration.ofHours(1);
    private final long maxSize = 10000;
    private final boolean statisticsEnabled = true;

    public MemoryCacheService() {
        // Schedule cleanup task every 5 minutes
        cleanupExecutor.scheduleAtFixedRate(this::cleanup, 5, 5, TimeUnit.MINUTES);
    }

    @Override
    public void put(String key, Object value, Duration ttl) {
        if (key == null || value == null) {
            return;
        }

        // Check size limit
        if (cache.size() >= maxSize) {
            evictOldest();
        }

        Instant expiry = Instant.now().plus(ttl);
        cache.put(key, new CacheEntry(value, expiry));
        
        logger.debug("Cached value for key: {}, TTL: {}", key, ttl);
    }

    @Override
    public void put(String key, Object value) {
        put(key, value, defaultTtl);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key, Class<T> type) {
        if (key == null) {
            return Optional.empty();
        }

        CacheEntry entry = cache.get(key);
        if (entry == null) {
            if (statisticsEnabled) {
                missCount.incrementAndGet();
            }
            return Optional.empty();
        }

        if (entry.isExpired()) {
            cache.remove(key);
            if (statisticsEnabled) {
                missCount.incrementAndGet();
                evictionCount.incrementAndGet();
            }
            return Optional.empty();
        }

        if (statisticsEnabled) {
            hitCount.incrementAndGet();
        }

        try {
            return Optional.of((T) entry.getValue());
        } catch (ClassCastException e) {
            logger.warn("Type mismatch for cache key: {}, expected: {}, actual: {}", 
                       key, type.getSimpleName(), entry.getValue().getClass().getSimpleName());
            return Optional.empty();
        }
    }

    @Override
    public <T> CompletableFuture<Optional<T>> getAsync(String key, Class<T> type) {
        return CompletableFuture.supplyAsync(() -> get(key, type));
    }

    @Override
    public boolean evict(String key) {
        if (key == null) {
            return false;
        }

        boolean removed = cache.remove(key) != null;
        if (removed && statisticsEnabled) {
            evictionCount.incrementAndGet();
        }
        return removed;
    }

    @Override
    public long evictByPattern(String pattern) {
        if (pattern == null) {
            return 0;
        }

        Pattern regex = Pattern.compile(pattern.replace("*", ".*"));
        long evicted = 0;

        for (String key : cache.keySet()) {
            if (regex.matcher(key).matches()) {
                if (cache.remove(key) != null) {
                    evicted++;
                }
            }
        }

        if (statisticsEnabled) {
            evictionCount.addAndGet(evicted);
        }

        logger.debug("Evicted {} entries matching pattern: {}", evicted, pattern);
        return evicted;
    }

    @Override
    public void clear() {
        long size = cache.size();
        cache.clear();
        
        if (statisticsEnabled) {
            evictionCount.addAndGet(size);
        }
        
        logger.debug("Cleared cache, evicted {} entries", size);
    }

    @Override
    public boolean exists(String key) {
        if (key == null) {
            return false;
        }

        CacheEntry entry = cache.get(key);
        if (entry == null) {
            return false;
        }

        if (entry.isExpired()) {
            cache.remove(key);
            if (statisticsEnabled) {
                evictionCount.incrementAndGet();
            }
            return false;
        }

        return true;
    }

    @Override
    public CacheStats getStats() {
        return new MemoryCacheStats();
    }

    @Override
    public CacheConfig getConfig() {
        return new MemoryCacheConfig();
    }

    private void cleanup() {
        long expired = 0;
        Instant now = Instant.now();

        for (Map.Entry<String, CacheEntry> entry : cache.entrySet()) {
            if (entry.getValue().getExpiry().isBefore(now)) {
                cache.remove(entry.getKey());
                expired++;
            }
        }

        if (expired > 0) {
            if (statisticsEnabled) {
                evictionCount.addAndGet(expired);
            }
            logger.debug("Cleanup removed {} expired entries", expired);
        }
    }

    private void evictOldest() {
        // Simple LRU eviction - remove first entry
        if (!cache.isEmpty()) {
            String firstKey = cache.keySet().iterator().next();
            cache.remove(firstKey);
            if (statisticsEnabled) {
                evictionCount.incrementAndGet();
            }
        }
    }

    private static class CacheEntry {
        private final Object value;
        private final Instant expiry;

        public CacheEntry(Object value, Instant expiry) {
            this.value = value;
            this.expiry = expiry;
        }

        public Object getValue() {
            return value;
        }

        public Instant getExpiry() {
            return expiry;
        }

        public boolean isExpired() {
            return Instant.now().isAfter(expiry);
        }
    }

    private class MemoryCacheStats implements CacheStats {
        @Override
        public long getHitCount() {
            return hitCount.get();
        }

        @Override
        public long getMissCount() {
            return missCount.get();
        }

        @Override
        public double getHitRate() {
            long hits = getHitCount();
            long total = hits + getMissCount();
            return total == 0 ? 0.0 : (double) hits / total;
        }

        @Override
        public long getEvictionCount() {
            return evictionCount.get();
        }

        @Override
        public long getSize() {
            return cache.size();
        }

        @Override
        public Map<String, Object> getAdditionalStats() {
            return Map.of(
                "maxSize", maxSize,
                "loadFactor", (double) getSize() / maxSize
            );
        }
    }

    private class MemoryCacheConfig implements CacheConfig {
        @Override
        public String getProvider() {
            return "memory";
        }

        @Override
        public Duration getDefaultTtl() {
            return defaultTtl;
        }

        @Override
        public long getMaxSize() {
            return maxSize;
        }

        @Override
        public String getEvictionPolicy() {
            return "LRU";
        }

        @Override
        public boolean isStatisticsEnabled() {
            return statisticsEnabled;
        }
    }
}