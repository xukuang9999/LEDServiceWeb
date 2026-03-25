package com.zhglxt.fileManager.service.performance;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Performance Monitoring Service Interface
 * Monitors and reports performance metrics for file operations
 * 
 * @author zhglxt
 */
public interface PerformanceMonitoringService {

    /**
     * Record operation execution time
     * 
     * @param operation operation name
     * @param executionTimeMs execution time in milliseconds
     */
    void recordOperationTime(String operation, long executionTimeMs);

    /**
     * Record operation execution time with additional metadata
     * 
     * @param operation operation name
     * @param executionTimeMs execution time in milliseconds
     * @param metadata additional metadata
     */
    void recordOperationTime(String operation, long executionTimeMs, Map<String, Object> metadata);

    /**
     * Start timing an operation
     * 
     * @param operation operation name
     * @return timer context
     */
    TimerContext startTimer(String operation);

    /**
     * Record cache hit
     * 
     * @param cacheType cache type (thumbnail, watermark, etc.)
     */
    void recordCacheHit(String cacheType);

    /**
     * Record cache miss
     * 
     * @param cacheType cache type
     */
    void recordCacheMiss(String cacheType);

    /**
     * Record file size processed
     * 
     * @param operation operation name
     * @param fileSizeBytes file size in bytes
     */
    void recordFileSize(String operation, long fileSizeBytes);

    /**
     * Record error occurrence
     * 
     * @param operation operation name
     * @param errorType error type
     */
    void recordError(String operation, String errorType);

    /**
     * Get performance metrics
     * 
     * @return performance metrics
     */
    PerformanceMetrics getMetrics();

    /**
     * Get performance metrics asynchronously
     * 
     * @return future with performance metrics
     */
    CompletableFuture<PerformanceMetrics> getMetricsAsync();

    /**
     * Reset all metrics
     */
    void resetMetrics();

    /**
     * Get performance report
     * 
     * @return formatted performance report
     */
    String getPerformanceReport();

    /**
     * Timer context for measuring operation duration
     */
    interface TimerContext extends AutoCloseable {
        /**
         * Stop the timer and record the duration
         */
        void stop();

        /**
         * Stop the timer with additional metadata
         * 
         * @param metadata additional metadata
         */
        void stop(Map<String, Object> metadata);

        @Override
        default void close() {
            stop();
        }
    }

    /**
     * Performance metrics container
     */
    interface PerformanceMetrics {
        /**
         * Get operation statistics
         * 
         * @param operation operation name
         * @return operation statistics
         */
        OperationStats getOperationStats(String operation);

        /**
         * Get all operation statistics
         * 
         * @return map of operation name to statistics
         */
        Map<String, OperationStats> getAllOperationStats();

        /**
         * Get cache statistics
         * 
         * @param cacheType cache type
         * @return cache statistics
         */
        CacheStats getCacheStats(String cacheType);

        /**
         * Get all cache statistics
         * 
         * @return map of cache type to statistics
         */
        Map<String, CacheStats> getAllCacheStats();

        /**
         * Get error statistics
         * 
         * @return error statistics
         */
        Map<String, Long> getErrorStats();

        /**
         * Get overall system performance
         * 
         * @return system performance metrics
         */
        SystemPerformance getSystemPerformance();
    }

    /**
     * Operation statistics
     */
    interface OperationStats {
        long getCount();
        double getAverageTimeMs();
        long getMinTimeMs();
        long getMaxTimeMs();
        double getPercentile95Ms();
        double getPercentile99Ms();
        long getTotalTimeMs();
        double getOperationsPerSecond();
        long getTotalFileSizeBytes();
        double getAverageFileSizeMB();
    }

    /**
     * Cache statistics
     */
    interface CacheStats {
        long getHitCount();
        long getMissCount();
        double getHitRate();
        double getMissRate();
    }

    /**
     * System performance metrics
     */
    interface SystemPerformance {
        double getCpuUsage();
        long getMemoryUsedBytes();
        long getMemoryTotalBytes();
        double getMemoryUsagePercent();
        long getDiskUsedBytes();
        long getDiskTotalBytes();
        double getDiskUsagePercent();
        int getActiveThreads();
        int getQueuedTasks();
    }
}