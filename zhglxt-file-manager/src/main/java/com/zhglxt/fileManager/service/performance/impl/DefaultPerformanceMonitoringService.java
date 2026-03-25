package com.zhglxt.fileManager.service.performance.impl;

import com.zhglxt.fileManager.service.performance.PerformanceMonitoringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Default implementation of performance monitoring service
 * 
 * @author zhglxt
 */
@Service
public class DefaultPerformanceMonitoringService implements PerformanceMonitoringService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultPerformanceMonitoringService.class);

    // Operation metrics
    private final Map<String, OperationMetrics> operationMetrics = new ConcurrentHashMap<>();
    
    // Cache metrics
    private final Map<String, CacheMetrics> cacheMetrics = new ConcurrentHashMap<>();
    
    // Error metrics
    private final Map<String, LongAdder> errorCounts = new ConcurrentHashMap<>();
    
    // System beans for monitoring
    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

    @Override
    public void recordOperationTime(String operation, long executionTimeMs) {
        recordOperationTime(operation, executionTimeMs, Collections.emptyMap());
    }

    @Override
    public void recordOperationTime(String operation, long executionTimeMs, Map<String, Object> metadata) {
        OperationMetrics metrics = operationMetrics.computeIfAbsent(operation, k -> new OperationMetrics());
        metrics.recordExecution(executionTimeMs, metadata);
        
        logger.debug("Recorded operation time: {} = {}ms", operation, executionTimeMs);
    }

    @Override
    public TimerContext startTimer(String operation) {
        return new DefaultTimerContext(operation);
    }

    @Override
    public void recordCacheHit(String cacheType) {
        CacheMetrics metrics = cacheMetrics.computeIfAbsent(cacheType, k -> new CacheMetrics());
        metrics.recordHit();
        
        logger.debug("Recorded cache hit for: {}", cacheType);
    }

    @Override
    public void recordCacheMiss(String cacheType) {
        CacheMetrics metrics = cacheMetrics.computeIfAbsent(cacheType, k -> new CacheMetrics());
        metrics.recordMiss();
        
        logger.debug("Recorded cache miss for: {}", cacheType);
    }

    @Override
    public void recordFileSize(String operation, long fileSizeBytes) {
        OperationMetrics metrics = operationMetrics.computeIfAbsent(operation, k -> new OperationMetrics());
        metrics.recordFileSize(fileSizeBytes);
        
        logger.debug("Recorded file size for {}: {} bytes", operation, fileSizeBytes);
    }

    @Override
    public void recordError(String operation, String errorType) {
        String errorKey = operation + ":" + errorType;
        errorCounts.computeIfAbsent(errorKey, k -> new LongAdder()).increment();
        
        logger.debug("Recorded error for {}: {}", operation, errorType);
    }

    @Override
    public PerformanceMetrics getMetrics() {
        return new DefaultPerformanceMetrics();
    }

    @Override
    public CompletableFuture<PerformanceMetrics> getMetricsAsync() {
        return CompletableFuture.supplyAsync(this::getMetrics);
    }

    @Override
    public void resetMetrics() {
        operationMetrics.clear();
        cacheMetrics.clear();
        errorCounts.clear();
        
        logger.info("Reset all performance metrics");
    }

    @Override
    public String getPerformanceReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== File Manager Performance Report ===\n\n");
        
        // Operation statistics
        report.append("Operation Statistics:\n");
        for (Map.Entry<String, OperationMetrics> entry : operationMetrics.entrySet()) {
            String operation = entry.getKey();
            OperationMetrics metrics = entry.getValue();
            
            report.append(String.format("  %s:\n", operation));
            report.append(String.format("    Count: %d\n", metrics.getCount()));
            report.append(String.format("    Average Time: %.2f ms\n", metrics.getAverageTime()));
            report.append(String.format("    Min/Max Time: %d/%d ms\n", metrics.getMinTime(), metrics.getMaxTime()));
            report.append(String.format("    Operations/sec: %.2f\n", metrics.getOperationsPerSecond()));
            report.append(String.format("    Average File Size: %.2f MB\n", metrics.getAverageFileSizeMB()));
            report.append("\n");
        }
        
        // Cache statistics
        report.append("Cache Statistics:\n");
        for (Map.Entry<String, CacheMetrics> entry : cacheMetrics.entrySet()) {
            String cacheType = entry.getKey();
            CacheMetrics metrics = entry.getValue();
            
            report.append(String.format("  %s:\n", cacheType));
            report.append(String.format("    Hit Rate: %.2f%%\n", metrics.getHitRate() * 100));
            report.append(String.format("    Hits/Misses: %d/%d\n", metrics.getHitCount(), metrics.getMissCount()));
            report.append("\n");
        }
        
        // Error statistics
        if (!errorCounts.isEmpty()) {
            report.append("Error Statistics:\n");
            for (Map.Entry<String, LongAdder> entry : errorCounts.entrySet()) {
                report.append(String.format("  %s: %d\n", entry.getKey(), entry.getValue().sum()));
            }
            report.append("\n");
        }
        
        // System performance
        SystemPerformance systemPerf = getSystemPerformance();
        report.append("System Performance:\n");
        report.append(String.format("  Memory Usage: %.2f%% (%.2f MB / %.2f MB)\n", 
                     systemPerf.getMemoryUsagePercent(),
                     systemPerf.getMemoryUsedBytes() / (1024.0 * 1024.0),
                     systemPerf.getMemoryTotalBytes() / (1024.0 * 1024.0)));
        report.append(String.format("  Active Threads: %d\n", systemPerf.getActiveThreads()));
        
        return report.toString();
    }

    private SystemPerformance getSystemPerformance() {
        return new DefaultSystemPerformance();
    }

    // Timer context implementation
    private class DefaultTimerContext implements TimerContext {
        private final String operation;
        private final long startTime;

        public DefaultTimerContext(String operation) {
            this.operation = operation;
            this.startTime = System.currentTimeMillis();
        }

        @Override
        public void stop() {
            stop(Collections.emptyMap());
        }

        @Override
        public void stop(Map<String, Object> metadata) {
            long duration = System.currentTimeMillis() - startTime;
            recordOperationTime(operation, duration, metadata);
        }
    }

    // Operation metrics implementation
    private static class OperationMetrics {
        private final LongAdder count = new LongAdder();
        private final LongAdder totalTime = new LongAdder();
        private final AtomicLong minTime = new AtomicLong(Long.MAX_VALUE);
        private final AtomicLong maxTime = new AtomicLong(0);
        private final List<Long> executionTimes = Collections.synchronizedList(new ArrayList<>());
        private final LongAdder totalFileSize = new LongAdder();
        private final LongAdder fileSizeCount = new LongAdder();
        private final long startTime = System.currentTimeMillis();

        public void recordExecution(long timeMs, Map<String, Object> metadata) {
            count.increment();
            totalTime.add(timeMs);
            
            // Update min/max
            minTime.updateAndGet(current -> Math.min(current, timeMs));
            maxTime.updateAndGet(current -> Math.max(current, timeMs));
            
            // Store for percentile calculations (limit size to prevent memory issues)
            synchronized (executionTimes) {
                if (executionTimes.size() < 10000) {
                    executionTimes.add(timeMs);
                }
            }
        }

        public void recordFileSize(long sizeBytes) {
            totalFileSize.add(sizeBytes);
            fileSizeCount.increment();
        }

        public long getCount() {
            return count.sum();
        }

        public double getAverageTime() {
            long c = count.sum();
            return c > 0 ? (double) totalTime.sum() / c : 0.0;
        }

        public long getMinTime() {
            long min = minTime.get();
            return min == Long.MAX_VALUE ? 0 : min;
        }

        public long getMaxTime() {
            return maxTime.get();
        }

        public double getPercentile95() {
            return calculatePercentile(0.95);
        }

        public double getPercentile99() {
            return calculatePercentile(0.99);
        }

        public long getTotalTime() {
            return totalTime.sum();
        }

        public double getOperationsPerSecond() {
            long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
            return elapsedSeconds > 0 ? (double) count.sum() / elapsedSeconds : 0.0;
        }

        public double getAverageFileSizeMB() {
            long fileCount = fileSizeCount.sum();
            return fileCount > 0 ? (double) totalFileSize.sum() / (fileCount * 1024 * 1024) : 0.0;
        }

        private double calculatePercentile(double percentile) {
            synchronized (executionTimes) {
                if (executionTimes.isEmpty()) {
                    return 0.0;
                }
                
                List<Long> sorted = new ArrayList<>(executionTimes);
                Collections.sort(sorted);
                
                int index = (int) Math.ceil(percentile * sorted.size()) - 1;
                index = Math.max(0, Math.min(index, sorted.size() - 1));
                
                return sorted.get(index);
            }
        }
    }

    // Cache metrics implementation
    private static class CacheMetrics {
        private final LongAdder hitCount = new LongAdder();
        private final LongAdder missCount = new LongAdder();

        public void recordHit() {
            hitCount.increment();
        }

        public void recordMiss() {
            missCount.increment();
        }

        public long getHitCount() {
            return hitCount.sum();
        }

        public long getMissCount() {
            return missCount.sum();
        }

        public double getHitRate() {
            long hits = hitCount.sum();
            long total = hits + missCount.sum();
            return total > 0 ? (double) hits / total : 0.0;
        }

        public double getMissRate() {
            return 1.0 - getHitRate();
        }
    }

    // Performance metrics implementation
    private class DefaultPerformanceMetrics implements PerformanceMetrics {
        @Override
        public OperationStats getOperationStats(String operation) {
            OperationMetrics metrics = operationMetrics.get(operation);
            return metrics != null ? new DefaultOperationStats(metrics) : null;
        }

        @Override
        public Map<String, OperationStats> getAllOperationStats() {
            Map<String, OperationStats> stats = new ConcurrentHashMap<>();
            for (Map.Entry<String, OperationMetrics> entry : operationMetrics.entrySet()) {
                stats.put(entry.getKey(), new DefaultOperationStats(entry.getValue()));
            }
            return stats;
        }

        @Override
        public CacheStats getCacheStats(String cacheType) {
            CacheMetrics metrics = cacheMetrics.get(cacheType);
            return metrics != null ? new DefaultCacheStats(metrics) : null;
        }

        @Override
        public Map<String, CacheStats> getAllCacheStats() {
            Map<String, CacheStats> stats = new ConcurrentHashMap<>();
            for (Map.Entry<String, CacheMetrics> entry : cacheMetrics.entrySet()) {
                stats.put(entry.getKey(), new DefaultCacheStats(entry.getValue()));
            }
            return stats;
        }

        @Override
        public Map<String, Long> getErrorStats() {
            Map<String, Long> stats = new ConcurrentHashMap<>();
            for (Map.Entry<String, LongAdder> entry : errorCounts.entrySet()) {
                stats.put(entry.getKey(), entry.getValue().sum());
            }
            return stats;
        }

        @Override
        public SystemPerformance getSystemPerformance() {
            return DefaultPerformanceMonitoringService.this.getSystemPerformance();
        }
    }

    // Operation stats implementation
    private static class DefaultOperationStats implements OperationStats {
        private final OperationMetrics metrics;

        public DefaultOperationStats(OperationMetrics metrics) {
            this.metrics = metrics;
        }

        @Override
        public long getCount() {
            return metrics.getCount();
        }

        @Override
        public double getAverageTimeMs() {
            return metrics.getAverageTime();
        }

        @Override
        public long getMinTimeMs() {
            return metrics.getMinTime();
        }

        @Override
        public long getMaxTimeMs() {
            return metrics.getMaxTime();
        }

        @Override
        public double getPercentile95Ms() {
            return metrics.getPercentile95();
        }

        @Override
        public double getPercentile99Ms() {
            return metrics.getPercentile99();
        }

        @Override
        public long getTotalTimeMs() {
            return metrics.getTotalTime();
        }

        @Override
        public double getOperationsPerSecond() {
            return metrics.getOperationsPerSecond();
        }

        @Override
        public long getTotalFileSizeBytes() {
            return metrics.totalFileSize.sum();
        }

        @Override
        public double getAverageFileSizeMB() {
            return metrics.getAverageFileSizeMB();
        }
    }

    // Cache stats implementation
    private static class DefaultCacheStats implements CacheStats {
        private final CacheMetrics metrics;

        public DefaultCacheStats(CacheMetrics metrics) {
            this.metrics = metrics;
        }

        @Override
        public long getHitCount() {
            return metrics.getHitCount();
        }

        @Override
        public long getMissCount() {
            return metrics.getMissCount();
        }

        @Override
        public double getHitRate() {
            return metrics.getHitRate();
        }

        @Override
        public double getMissRate() {
            return metrics.getMissRate();
        }
    }

    // System performance implementation
    private class DefaultSystemPerformance implements SystemPerformance {
        @Override
        public double getCpuUsage() {
            // CPU usage monitoring would require additional libraries
            return 0.0;
        }

        @Override
        public long getMemoryUsedBytes() {
            return memoryBean.getHeapMemoryUsage().getUsed();
        }

        @Override
        public long getMemoryTotalBytes() {
            return memoryBean.getHeapMemoryUsage().getMax();
        }

        @Override
        public double getMemoryUsagePercent() {
            long used = getMemoryUsedBytes();
            long total = getMemoryTotalBytes();
            return total > 0 ? (double) used / total : 0.0;
        }

        @Override
        public long getDiskUsedBytes() {
            // Disk usage monitoring would require file system access
            return 0;
        }

        @Override
        public long getDiskTotalBytes() {
            return 0;
        }

        @Override
        public double getDiskUsagePercent() {
            return 0.0;
        }

        @Override
        public int getActiveThreads() {
            return threadBean.getThreadCount();
        }

        @Override
        public int getQueuedTasks() {
            // Would need access to thread pool executor
            return 0;
        }
    }
}