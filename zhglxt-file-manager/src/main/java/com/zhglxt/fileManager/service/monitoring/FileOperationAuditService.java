package com.zhglxt.fileManager.service.monitoring;

import com.zhglxt.fileManager.exception.FileManagerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * File Operation Audit Service
 * 
 * Provides comprehensive audit logging and monitoring for all file operations.
 * Tracks operation metrics, security events, and performance data.
 * 
 * @author zhglxt
 */
@Service
public class FileOperationAuditService {

    private static final Logger auditLogger = LoggerFactory.getLogger("FILE_MANAGER_AUDIT");
    private static final Logger securityLogger = LoggerFactory.getLogger("FILE_MANAGER_SECURITY");
    private static final Logger performanceLogger = LoggerFactory.getLogger("FILE_MANAGER_PERFORMANCE");
    
    // Operation counters
    private final Map<String, AtomicLong> operationCounters = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> errorCounters = new ConcurrentHashMap<>();
    
    // Performance tracking
    private final Map<String, Long> operationStartTimes = new ConcurrentHashMap<>();

    /**
     * Log successful file operation
     */
    public void logSuccessfulOperation(String userId, String operation, String filePath, Map<String, Object> details) {
        try {
            // Set MDC context for structured logging
            MDC.put("userId", userId);
            MDC.put("operation", operation);
            MDC.put("filePath", filePath);
            MDC.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            MDC.put("status", "SUCCESS");
            
            // Log the operation
            auditLogger.info("File operation completed successfully: operation={}, user={}, file={}, details={}", 
                operation, userId, filePath, details);
            
            // Update counters
            operationCounters.computeIfAbsent(operation, k -> new AtomicLong(0)).incrementAndGet();
            
        } finally {
            MDC.clear();
        }
    }

    /**
     * Log failed file operation
     */
    public void logFailedOperation(String userId, String operation, String filePath, Exception exception, Map<String, Object> details) {
        try {
            // Set MDC context for structured logging
            MDC.put("userId", userId);
            MDC.put("operation", operation);
            MDC.put("filePath", filePath);
            MDC.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            MDC.put("status", "FAILED");
            MDC.put("errorType", exception.getClass().getSimpleName());
            
            if (exception instanceof FileManagerException) {
                FileManagerException fme = (FileManagerException) exception;
                MDC.put("errorCode", fme.getErrorCode());
            }
            
            // Log the operation
            auditLogger.error("File operation failed: operation={}, user={}, file={}, error={}, details={}", 
                operation, userId, filePath, exception.getMessage(), details, exception);
            
            // Update error counters
            String errorKey = operation + "_ERROR";
            errorCounters.computeIfAbsent(errorKey, k -> new AtomicLong(0)).incrementAndGet();
            
        } finally {
            MDC.clear();
        }
    }

    /**
     * Log security event
     */
    public void logSecurityEvent(String userId, String eventType, String filePath, String description, Map<String, Object> details) {
        try {
            // Set MDC context for structured logging
            MDC.put("userId", userId);
            MDC.put("eventType", eventType);
            MDC.put("filePath", filePath);
            MDC.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            MDC.put("severity", "HIGH");
            
            // Log the security event
            securityLogger.warn("Security event detected: type={}, user={}, file={}, description={}, details={}", 
                eventType, userId, filePath, description, details);
            
        } finally {
            MDC.clear();
        }
    }

    /**
     * Log permission denied event
     */
    public void logPermissionDenied(String userId, String operation, String filePath, String reason) {
        try {
            // Set MDC context for structured logging
            MDC.put("userId", userId);
            MDC.put("operation", operation);
            MDC.put("filePath", filePath);
            MDC.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            MDC.put("eventType", "PERMISSION_DENIED");
            MDC.put("severity", "MEDIUM");
            
            // Log the permission denial
            securityLogger.warn("Permission denied: operation={}, user={}, file={}, reason={}", 
                operation, userId, filePath, reason);
            
        } finally {
            MDC.clear();
        }
    }

    /**
     * Log authentication failure
     */
    public void logAuthenticationFailure(String userId, String operation, String reason) {
        try {
            // Set MDC context for structured logging
            MDC.put("userId", userId);
            MDC.put("operation", operation);
            MDC.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            MDC.put("eventType", "AUTHENTICATION_FAILURE");
            MDC.put("severity", "HIGH");
            
            // Log the authentication failure
            securityLogger.error("Authentication failure: operation={}, user={}, reason={}", 
                operation, userId, reason);
            
        } finally {
            MDC.clear();
        }
    }

    /**
     * Start performance tracking for an operation
     */
    public void startPerformanceTracking(String operationId) {
        operationStartTimes.put(operationId, System.currentTimeMillis());
    }

    /**
     * End performance tracking and log metrics
     */
    public void endPerformanceTracking(String operationId, String operation, String userId, String filePath, long fileSize) {
        Long startTime = operationStartTimes.remove(operationId);
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            
            try {
                // Set MDC context for structured logging
                MDC.put("userId", userId);
                MDC.put("operation", operation);
                MDC.put("filePath", filePath);
                MDC.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                MDC.put("duration", String.valueOf(duration));
                MDC.put("fileSize", String.valueOf(fileSize));
                
                // Calculate throughput if file size is available
                if (fileSize > 0 && duration > 0) {
                    double throughputMBps = (fileSize / 1024.0 / 1024.0) / (duration / 1000.0);
                    MDC.put("throughputMBps", String.format("%.2f", throughputMBps));
                }
                
                // Log performance metrics
                performanceLogger.info("Operation performance: operation={}, user={}, file={}, duration={}ms, size={}bytes", 
                    operation, userId, filePath, duration, fileSize);
                
            } finally {
                MDC.clear();
            }
        }
    }

    /**
     * Log system resource usage
     */
    public void logResourceUsage(String operation, long memoryUsed, long diskSpaceUsed) {
        try {
            // Set MDC context for structured logging
            MDC.put("operation", operation);
            MDC.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            MDC.put("memoryUsed", String.valueOf(memoryUsed));
            MDC.put("diskSpaceUsed", String.valueOf(diskSpaceUsed));
            
            // Log resource usage
            performanceLogger.info("Resource usage: operation={}, memory={}bytes, disk={}bytes", 
                operation, memoryUsed, diskSpaceUsed);
            
        } finally {
            MDC.clear();
        }
    }

    /**
     * Get operation statistics
     */
    public Map<String, Long> getOperationStatistics() {
        Map<String, Long> stats = new ConcurrentHashMap<>();
        
        // Add operation counts
        operationCounters.forEach((operation, count) -> 
            stats.put(operation + "_COUNT", count.get()));
        
        // Add error counts
        errorCounters.forEach((errorType, count) -> 
            stats.put(errorType + "_COUNT", count.get()));
        
        return stats;
    }

    /**
     * Reset statistics
     */
    public void resetStatistics() {
        operationCounters.clear();
        errorCounters.clear();
        operationStartTimes.clear();
    }

    /**
     * Log configuration change
     */
    public void logConfigurationChange(String userId, String configKey, String oldValue, String newValue) {
        try {
            // Set MDC context for structured logging
            MDC.put("userId", userId);
            MDC.put("eventType", "CONFIGURATION_CHANGE");
            MDC.put("configKey", configKey);
            MDC.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            // Log the configuration change
            auditLogger.info("Configuration changed: key={}, user={}, oldValue={}, newValue={}", 
                configKey, userId, oldValue, newValue);
            
        } finally {
            MDC.clear();
        }
    }

    /**
     * Log storage backend operation
     */
    public void logStorageOperation(String storageType, String operation, String filePath, boolean success, long duration) {
        try {
            // Set MDC context for structured logging
            MDC.put("storageType", storageType);
            MDC.put("operation", operation);
            MDC.put("filePath", filePath);
            MDC.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            MDC.put("status", success ? "SUCCESS" : "FAILED");
            MDC.put("duration", String.valueOf(duration));
            
            // Log the storage operation
            if (success) {
                auditLogger.info("Storage operation completed: type={}, operation={}, file={}, duration={}ms", 
                    storageType, operation, filePath, duration);
            } else {
                auditLogger.error("Storage operation failed: type={}, operation={}, file={}, duration={}ms", 
                    storageType, operation, filePath, duration);
            }
            
        } finally {
            MDC.clear();
        }
    }
}