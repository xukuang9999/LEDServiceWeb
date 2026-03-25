package com.zhglxt.fileManager.service.monitoring;

import com.zhglxt.fileManager.exception.FileManagerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Error Monitoring Service
 * 
 * Monitors and tracks error patterns, provides alerting capabilities,
 * and maintains error statistics for system health monitoring.
 * 
 * @author zhglxt
 */
@Service
public class ErrorMonitoringService {

    private static final Logger logger = LoggerFactory.getLogger(ErrorMonitoringService.class);
    private static final Logger alertLogger = LoggerFactory.getLogger("FILE_MANAGER_ALERTS");

    @Autowired
    private FileOperationAuditService auditService;

    // Error tracking
    private final Map<String, AtomicLong> errorCounts = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> lastErrorTimes = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> errorRates = new ConcurrentHashMap<>();

    // Alert thresholds
    private static final long ERROR_RATE_THRESHOLD = 10; // errors per minute
    private static final long CONSECUTIVE_ERROR_THRESHOLD = 5;
    private static final long CRITICAL_ERROR_THRESHOLD = 100; // total errors

    /**
     * Record an error occurrence
     */
    public void recordError(FileManagerException exception, String userId, String operation, String filePath) {
        String errorKey = exception.getErrorCode();
        String operationKey = operation + "_ERROR";
        
        // Update error counts
        errorCounts.computeIfAbsent(errorKey, k -> new AtomicLong(0)).incrementAndGet();
        errorCounts.computeIfAbsent(operationKey, k -> new AtomicLong(0)).incrementAndGet();
        
        // Update last error time
        LocalDateTime now = LocalDateTime.now();
        lastErrorTimes.put(errorKey, now);
        
        // Calculate error rate (errors per minute)
        updateErrorRate(errorKey, now);
        
        // Check for alert conditions
        checkAlertConditions(errorKey, operationKey, exception, userId, operation, filePath);
        
        // Log detailed error information
        logDetailedError(exception, userId, operation, filePath);
    }

    /**
     * Record a general exception
     */
    public void recordError(Exception exception, String userId, String operation, String filePath) {
        String errorKey = exception.getClass().getSimpleName();
        String operationKey = operation + "_ERROR";
        
        // Update error counts
        errorCounts.computeIfAbsent(errorKey, k -> new AtomicLong(0)).incrementAndGet();
        errorCounts.computeIfAbsent(operationKey, k -> new AtomicLong(0)).incrementAndGet();
        
        // Update last error time
        LocalDateTime now = LocalDateTime.now();
        lastErrorTimes.put(errorKey, now);
        
        // Calculate error rate
        updateErrorRate(errorKey, now);
        
        // Check for alert conditions
        checkAlertConditions(errorKey, operationKey, exception, userId, operation, filePath);
        
        // Log detailed error information
        logDetailedError(exception, userId, operation, filePath);
    }

    /**
     * Update error rate calculation
     */
    private void updateErrorRate(String errorKey, LocalDateTime currentTime) {
        String rateKey = errorKey + "_RATE";
        AtomicLong rate = errorRates.computeIfAbsent(rateKey, k -> new AtomicLong(0));
        
        // Simple rate calculation - increment for current minute
        rate.incrementAndGet();
        
        // Reset rate counter every minute (simplified implementation)
        // In production, you might want to use a sliding window approach
    }

    /**
     * Check for alert conditions and trigger alerts if necessary
     */
    private void checkAlertConditions(String errorKey, String operationKey, Exception exception, 
                                    String userId, String operation, String filePath) {
        
        long errorCount = errorCounts.get(errorKey).get();
        long operationErrorCount = errorCounts.get(operationKey).get();
        
        // Check for high error rate
        String rateKey = errorKey + "_RATE";
        long errorRate = errorRates.getOrDefault(rateKey, new AtomicLong(0)).get();
        
        if (errorRate >= ERROR_RATE_THRESHOLD) {
            triggerAlert("HIGH_ERROR_RATE", 
                String.format("High error rate detected: %s errors/minute for %s", errorRate, errorKey),
                userId, operation, filePath, exception);
        }
        
        // Check for consecutive errors
        if (operationErrorCount >= CONSECUTIVE_ERROR_THRESHOLD) {
            triggerAlert("CONSECUTIVE_ERRORS", 
                String.format("Consecutive errors detected: %d errors for operation %s", operationErrorCount, operation),
                userId, operation, filePath, exception);
        }
        
        // Check for critical error count
        if (errorCount >= CRITICAL_ERROR_THRESHOLD) {
            triggerAlert("CRITICAL_ERROR_COUNT", 
                String.format("Critical error count reached: %d total errors for %s", errorCount, errorKey),
                userId, operation, filePath, exception);
        }
        
        // Check for specific critical error types
        if (exception instanceof FileManagerException) {
            FileManagerException fme = (FileManagerException) exception;
            if ("STORAGE_ERROR".equals(fme.getErrorCode()) || "SECURITY_VIOLATION".equals(fme.getErrorCode())) {
                triggerAlert("CRITICAL_ERROR_TYPE", 
                    String.format("Critical error type detected: %s", fme.getErrorCode()),
                    userId, operation, filePath, exception);
            }
        }
    }

    /**
     * Trigger an alert
     */
    private void triggerAlert(String alertType, String message, String userId, String operation, 
                            String filePath, Exception exception) {
        
        // Log alert
        alertLogger.error("ALERT [{}]: {} - User: {}, Operation: {}, File: {}, Exception: {}", 
            alertType, message, userId, operation, filePath, exception.getMessage());
        
        // In a production system, you might want to:
        // - Send notifications to administrators
        // - Integrate with monitoring systems (e.g., Prometheus, Grafana)
        // - Send emails or SMS alerts
        // - Create tickets in issue tracking systems
        
        logger.error("Alert triggered: type={}, message={}, user={}, operation={}, file={}", 
            alertType, message, userId, operation, filePath, exception);
    }

    /**
     * Log detailed error information
     */
    private void logDetailedError(Exception exception, String userId, String operation, String filePath) {
        if (exception instanceof FileManagerException) {
            FileManagerException fme = (FileManagerException) exception;
            
            logger.error("File Manager Error Details: " +
                "errorCode={}, operation={}, user={}, file={}, timestamp={}, context={}, message={}", 
                fme.getErrorCode(), operation, userId, filePath, fme.getTimestamp(), 
                fme.getContext(), fme.getMessage(), exception);
                
            // Log to audit service
            auditService.logFailedOperation(userId, operation, filePath, exception, fme.getContext());
            
        } else {
            logger.error("General Error Details: " +
                "errorType={}, operation={}, user={}, file={}, message={}", 
                exception.getClass().getSimpleName(), operation, userId, filePath, exception.getMessage(), exception);
                
            // Log to audit service
            auditService.logFailedOperation(userId, operation, filePath, exception, Map.of());
        }
    }

    /**
     * Get error statistics
     */
    public Map<String, Object> getErrorStatistics() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        
        // Add error counts
        errorCounts.forEach((errorType, count) -> 
            stats.put(errorType + "_COUNT", count.get()));
        
        // Add error rates
        errorRates.forEach((rateKey, rate) -> 
            stats.put(rateKey, rate.get()));
        
        // Add last error times
        lastErrorTimes.forEach((errorType, time) -> 
            stats.put(errorType + "_LAST_OCCURRENCE", time));
        
        // Calculate total errors
        long totalErrors = errorCounts.values().stream()
            .mapToLong(AtomicLong::get)
            .sum();
        stats.put("TOTAL_ERRORS", totalErrors);
        
        return stats;
    }

    /**
     * Get error health status
     */
    public String getHealthStatus() {
        long totalErrors = errorCounts.values().stream()
            .mapToLong(AtomicLong::get)
            .sum();
        
        // Check for recent errors (within last 5 minutes)
        long recentErrors = lastErrorTimes.values().stream()
            .mapToLong(time -> ChronoUnit.MINUTES.between(time, LocalDateTime.now()) <= 5 ? 1 : 0)
            .sum();
        
        if (totalErrors == 0) {
            return "HEALTHY";
        } else if (recentErrors == 0 && totalErrors < 10) {
            return "STABLE";
        } else if (recentErrors < 5) {
            return "WARNING";
        } else {
            return "CRITICAL";
        }
    }

    /**
     * Reset error statistics
     */
    public void resetStatistics() {
        errorCounts.clear();
        lastErrorTimes.clear();
        errorRates.clear();
        logger.info("Error monitoring statistics reset");
    }

    /**
     * Check if error rate is within acceptable limits
     */
    public boolean isErrorRateAcceptable(String errorType) {
        String rateKey = errorType + "_RATE";
        long rate = errorRates.getOrDefault(rateKey, new AtomicLong(0)).get();
        return rate < ERROR_RATE_THRESHOLD;
    }

    /**
     * Get error trend analysis
     */
    public Map<String, String> getErrorTrends() {
        Map<String, String> trends = new ConcurrentHashMap<>();
        
        errorCounts.forEach((errorType, count) -> {
            LocalDateTime lastError = lastErrorTimes.get(errorType);
            if (lastError != null) {
                long minutesSinceLastError = ChronoUnit.MINUTES.between(lastError, LocalDateTime.now());
                
                if (minutesSinceLastError < 5) {
                    trends.put(errorType, "INCREASING");
                } else if (minutesSinceLastError < 30) {
                    trends.put(errorType, "STABLE");
                } else {
                    trends.put(errorType, "DECREASING");
                }
            } else {
                trends.put(errorType, "NO_DATA");
            }
        });
        
        return trends;
    }
}