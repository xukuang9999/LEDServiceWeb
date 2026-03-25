package com.zhglxt.fileManager.service.monitoring;

import com.zhglxt.fileManager.config.FileManagerProperties;
import com.zhglxt.fileManager.service.cache.CacheService;
import com.zhglxt.fileManager.service.storage.StorageService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Health check service for monitoring file manager components
 * 
 * @author zhglxt
 */
@Service
public class HealthCheckService {

    private static final Logger logger = LoggerFactory.getLogger(HealthCheckService.class);

    @Autowired
    private FileManagerProperties properties;

    @Autowired
    private StorageService storageService;

    @Autowired(required = false)
    private CacheService cacheService;

    private LocalDateTime startTime;
    private final RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
    private final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

    @PostConstruct
    public void init() {
        startTime = LocalDateTime.now();
        logger.info("Health check service initialized at {}", startTime);
    }

    /**
     * Perform comprehensive health check
     */
    public Map<String, Object> checkHealth() {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> details = new HashMap<>();
        boolean overallHealthy = true;

        // Check storage
        try {
            boolean storageHealthy = checkStorageHealth();
            details.put("storage", storageHealthy ? "UP" : "DOWN");
            if (!storageHealthy) {
                overallHealthy = false;
            }
        } catch (Exception e) {
            logger.error("Storage health check failed", e);
            details.put("storage", "DOWN");
            details.put("storageError", e.getMessage());
            overallHealthy = false;
        }

        // Check cache
        try {
            boolean cacheHealthy = checkCacheHealth();
            details.put("cache", cacheHealthy ? "UP" : "DOWN");
            // Cache is not critical, so don't fail overall health
        } catch (Exception e) {
            logger.error("Cache health check failed", e);
            details.put("cache", "DOWN");
            details.put("cacheError", e.getMessage());
        }

        // Check disk space
        try {
            Map<String, Object> diskInfo = checkDiskSpace();
            details.put("diskSpace", diskInfo);
            
            Double usagePercentage = (Double) diskInfo.get("usagePercentage");
            if (usagePercentage != null && usagePercentage > properties.getHealth().getDiskSpaceThreshold()) {
                overallHealthy = false;
                details.put("diskSpaceWarning", "Disk usage exceeds threshold");
            }
        } catch (Exception e) {
            logger.error("Disk space check failed", e);
            details.put("diskSpace", "ERROR");
            details.put("diskSpaceError", e.getMessage());
        }

        // Check memory
        try {
            Map<String, Object> memoryInfo = checkMemoryUsage();
            details.put("memory", memoryInfo);
        } catch (Exception e) {
            logger.error("Memory check failed", e);
            details.put("memory", "ERROR");
            details.put("memoryError", e.getMessage());
        }

        result.put("healthy", overallHealthy);
        result.put("details", details);
        
        return result;
    }

    /**
     * Check if service is ready to accept requests
     */
    public boolean isReady() {
        try {
            // Check if storage is accessible
            if (!checkStorageHealth()) {
                return false;
            }

            // Check if required directories exist
            if (properties.getStorage().getType().equals("local")) {
                File rootDir = new File(properties.getRootDirectory());
                if (!rootDir.exists() || !rootDir.canRead() || !rootDir.canWrite()) {
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            logger.error("Readiness check failed", e);
            return false;
        }
    }

    /**
     * Get service uptime
     */
    public Duration getUptime() {
        return Duration.between(startTime, LocalDateTime.now());
    }

    /**
     * Get service information
     */
    public Map<String, Object> getServiceInfo() {
        Map<String, Object> info = new HashMap<>();
        
        info.put("startTime", startTime);
        info.put("uptime", getUptime().toString());
        info.put("jvmUptime", runtimeMXBean.getUptime());
        info.put("jvmVersion", System.getProperty("java.version"));
        info.put("storageType", properties.getStorage().getType());
        info.put("maxFileSize", properties.getMaxFileSize());
        info.put("allowedExtensions", properties.getAllowedExtensions().size());
        info.put("watermarkEnabled", properties.getWatermark().isEnabled());
        info.put("thumbnailEnabled", properties.getThumbnail().isEnabled());
        
        return info;
    }

    /**
     * Check storage health
     */
    private boolean checkStorageHealth() {
        try {
            // Try to perform a basic storage operation
            // This is a simple check - in a real implementation, you might want to
            // create a test file or check connectivity to cloud storage
            return storageService != null;
        } catch (Exception e) {
            logger.error("Storage health check failed", e);
            return false;
        }
    }

    /**
     * Check cache health
     */
    private boolean checkCacheHealth() {
        if (cacheService == null) {
            return true; // Cache is optional
        }
        
        try {
            // Try to perform a basic cache operation
            String testKey = "health-check-" + System.currentTimeMillis();
            String testValue = "test";
            
            cacheService.put(testKey, testValue, Duration.ofSeconds(60));
            Optional<String> retrieved = cacheService.get(testKey, String.class);
            cacheService.evict(testKey);
            
            return retrieved.isPresent() && testValue.equals(retrieved.get());
        } catch (Exception e) {
            logger.error("Cache health check failed", e);
            return false;
        }
    }

    /**
     * Check disk space
     */
    private Map<String, Object> checkDiskSpace() {
        Map<String, Object> diskInfo = new HashMap<>();
        
        try {
            File rootDir = new File(properties.getRootDirectory());
            if (!rootDir.exists()) {
                rootDir = new File(".");
            }
            
            long totalSpace = rootDir.getTotalSpace();
            long freeSpace = rootDir.getFreeSpace();
            long usedSpace = totalSpace - freeSpace;
            double usagePercentage = (double) usedSpace / totalSpace * 100;
            
            diskInfo.put("totalSpace", totalSpace);
            diskInfo.put("freeSpace", freeSpace);
            diskInfo.put("usedSpace", usedSpace);
            diskInfo.put("usagePercentage", Math.round(usagePercentage * 100.0) / 100.0);
            diskInfo.put("status", usagePercentage > properties.getHealth().getDiskSpaceThreshold() ? "WARNING" : "OK");
            
        } catch (Exception e) {
            logger.error("Failed to check disk space", e);
            diskInfo.put("status", "ERROR");
            diskInfo.put("error", e.getMessage());
        }
        
        return diskInfo;
    }

    /**
     * Check memory usage
     */
    private Map<String, Object> checkMemoryUsage() {
        Map<String, Object> memoryInfo = new HashMap<>();
        
        try {
            long heapUsed = memoryMXBean.getHeapMemoryUsage().getUsed();
            long heapMax = memoryMXBean.getHeapMemoryUsage().getMax();
            long nonHeapUsed = memoryMXBean.getNonHeapMemoryUsage().getUsed();
            long nonHeapMax = memoryMXBean.getNonHeapMemoryUsage().getMax();
            
            double heapUsagePercentage = (double) heapUsed / heapMax * 100;
            
            memoryInfo.put("heapUsed", heapUsed);
            memoryInfo.put("heapMax", heapMax);
            memoryInfo.put("heapUsagePercentage", Math.round(heapUsagePercentage * 100.0) / 100.0);
            memoryInfo.put("nonHeapUsed", nonHeapUsed);
            memoryInfo.put("nonHeapMax", nonHeapMax);
            memoryInfo.put("status", heapUsagePercentage > 90 ? "WARNING" : "OK");
            
        } catch (Exception e) {
            logger.error("Failed to check memory usage", e);
            memoryInfo.put("status", "ERROR");
            memoryInfo.put("error", e.getMessage());
        }
        
        return memoryInfo;
    }
}