package com.zhglxt.fileManager.service.security;

import com.zhglxt.fileManager.service.monitoring.FileOperationAuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Security Audit Service
 * 
 * Provides comprehensive security audit logging for all file operations.
 * Extends the basic audit service with security-focused logging and monitoring.
 * 
 * @author zhglxt
 */
@Service
public class SecurityAuditService {

    private static final Logger securityLogger = LoggerFactory.getLogger("FILE_MANAGER_SECURITY_AUDIT");
    private static final Logger complianceLogger = LoggerFactory.getLogger("FILE_MANAGER_COMPLIANCE");
    
    @Autowired
    private FileOperationAuditService auditService;
    
    // Security event counters
    private final Map<String, AtomicLong> securityEventCounters = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> userActivityCounters = new ConcurrentHashMap<>();
    
    /**
     * Log comprehensive file operation audit
     */
    public void auditFileOperation(String userId, String operation, String filePath, 
                                 boolean success, HttpServletRequest request, 
                                 Map<String, Object> additionalData) {
        try {
            // Set comprehensive MDC context
            MDC.put("userId", userId);
            MDC.put("operation", operation);
            MDC.put("filePath", filePath);
            MDC.put("success", String.valueOf(success));
            MDC.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            MDC.put("sessionId", request.getSession().getId());
            MDC.put("remoteAddr", getClientIpAddress(request));
            MDC.put("userAgent", request.getHeader("User-Agent"));
            MDC.put("referer", request.getHeader("Referer"));
            
            // Add request details
            Map<String, Object> auditData = new HashMap<>();
            auditData.put("method", request.getMethod());
            auditData.put("requestUri", request.getRequestURI());
            auditData.put("queryString", request.getQueryString());
            auditData.put("protocol", request.getProtocol());
            auditData.put("serverName", request.getServerName());
            auditData.put("serverPort", request.getServerPort());
            
            if (additionalData != null) {
                auditData.putAll(additionalData);
            }
            
            // Log the comprehensive audit entry
            if (success) {
                securityLogger.info("FILE_OPERATION_SUCCESS: user={}, operation={}, file={}, session={}, ip={}, data={}", 
                    userId, operation, filePath, request.getSession().getId(), 
                    getClientIpAddress(request), auditData);
                
                // Also log to basic audit service
                auditService.logSuccessfulOperation(userId, operation, filePath, auditData);
            } else {
                securityLogger.warn("FILE_OPERATION_FAILED: user={}, operation={}, file={}, session={}, ip={}, data={}", 
                    userId, operation, filePath, request.getSession().getId(), 
                    getClientIpAddress(request), auditData);
            }
            
            // Update activity counters
            String userKey = userId + "_" + operation;
            userActivityCounters.computeIfAbsent(userKey, k -> new AtomicLong(0)).incrementAndGet();
            
        } finally {
            MDC.clear();
        }
    }
    
    /**
     * Log security violation
     */
    public void auditSecurityViolation(String userId, String violationType, String description, 
                                     HttpServletRequest request, Map<String, Object> details) {
        try {
            // Set MDC context for security violation
            MDC.put("userId", userId);
            MDC.put("violationType", violationType);
            MDC.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            MDC.put("sessionId", request.getSession().getId());
            MDC.put("remoteAddr", getClientIpAddress(request));
            MDC.put("userAgent", request.getHeader("User-Agent"));
            MDC.put("severity", "HIGH");
            
            // Prepare violation data
            Map<String, Object> violationData = new HashMap<>();
            violationData.put("description", description);
            violationData.put("method", request.getMethod());
            violationData.put("requestUri", request.getRequestURI());
            violationData.put("queryString", request.getQueryString());
            violationData.put("headers", getSecurityRelevantHeaders(request));
            
            if (details != null) {
                violationData.putAll(details);
            }
            
            // Log security violation
            securityLogger.error("SECURITY_VIOLATION: type={}, user={}, ip={}, session={}, description={}, data={}", 
                violationType, userId, getClientIpAddress(request), request.getSession().getId(), 
                description, violationData);
            
            // Update security event counters
            securityEventCounters.computeIfAbsent(violationType, k -> new AtomicLong(0)).incrementAndGet();
            
            // Also log to basic audit service
            auditService.logSecurityEvent(userId, violationType, "", description, violationData);
            
        } finally {
            MDC.clear();
        }
    }
    
    /**
     * Log authentication event
     */
    public void auditAuthenticationEvent(String userId, String eventType, boolean success, 
                                       HttpServletRequest request, String reason) {
        try {
            // Set MDC context for authentication event
            MDC.put("userId", userId);
            MDC.put("eventType", eventType);
            MDC.put("success", String.valueOf(success));
            MDC.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            MDC.put("sessionId", request.getSession().getId());
            MDC.put("remoteAddr", getClientIpAddress(request));
            MDC.put("userAgent", request.getHeader("User-Agent"));
            
            // Prepare authentication data
            Map<String, Object> authData = new HashMap<>();
            authData.put("reason", reason);
            authData.put("method", request.getMethod());
            authData.put("requestUri", request.getRequestURI());
            authData.put("headers", getSecurityRelevantHeaders(request));
            
            // Log authentication event
            if (success) {
                securityLogger.info("AUTHENTICATION_SUCCESS: type={}, user={}, ip={}, session={}, reason={}", 
                    eventType, userId, getClientIpAddress(request), request.getSession().getId(), reason);
            } else {
                securityLogger.warn("AUTHENTICATION_FAILURE: type={}, user={}, ip={}, session={}, reason={}, data={}", 
                    eventType, userId, getClientIpAddress(request), request.getSession().getId(), reason, authData);
                
                // Log to basic audit service for failures
                auditService.logAuthenticationFailure(userId, eventType, reason);
            }
            
        } finally {
            MDC.clear();
        }
    }
    
    /**
     * Log authorization event
     */
    public void auditAuthorizationEvent(String userId, String resource, String action, 
                                      boolean granted, HttpServletRequest request, String reason) {
        try {
            // Set MDC context for authorization event
            MDC.put("userId", userId);
            MDC.put("resource", resource);
            MDC.put("action", action);
            MDC.put("granted", String.valueOf(granted));
            MDC.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            MDC.put("sessionId", request.getSession().getId());
            MDC.put("remoteAddr", getClientIpAddress(request));
            
            // Log authorization event
            if (granted) {
                securityLogger.info("AUTHORIZATION_GRANTED: user={}, resource={}, action={}, ip={}, session={}", 
                    userId, resource, action, getClientIpAddress(request), request.getSession().getId());
            } else {
                securityLogger.warn("AUTHORIZATION_DENIED: user={}, resource={}, action={}, ip={}, session={}, reason={}", 
                    userId, resource, action, getClientIpAddress(request), request.getSession().getId(), reason);
                
                // Log to basic audit service for denials
                auditService.logPermissionDenied(userId, action, resource, reason);
            }
            
        } finally {
            MDC.clear();
        }
    }
    
    /**
     * Log compliance event
     */
    public void auditComplianceEvent(String userId, String eventType, String description, 
                                   HttpServletRequest request, Map<String, Object> complianceData) {
        try {
            // Set MDC context for compliance event
            MDC.put("userId", userId);
            MDC.put("eventType", eventType);
            MDC.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            MDC.put("sessionId", request.getSession().getId());
            MDC.put("remoteAddr", getClientIpAddress(request));
            MDC.put("complianceType", "FILE_MANAGEMENT");
            
            // Prepare compliance data
            Map<String, Object> auditData = new HashMap<>();
            auditData.put("description", description);
            auditData.put("requestUri", request.getRequestURI());
            auditData.put("method", request.getMethod());
            
            if (complianceData != null) {
                auditData.putAll(complianceData);
            }
            
            // Log compliance event
            complianceLogger.info("COMPLIANCE_EVENT: type={}, user={}, ip={}, session={}, description={}, data={}", 
                eventType, userId, getClientIpAddress(request), request.getSession().getId(), 
                description, auditData);
            
        } finally {
            MDC.clear();
        }
    }
    
    /**
     * Log data access event
     */
    public void auditDataAccess(String userId, String dataType, String identifier, 
                              String accessType, HttpServletRequest request, 
                              Map<String, Object> dataDetails) {
        try {
            // Set MDC context for data access event
            MDC.put("userId", userId);
            MDC.put("dataType", dataType);
            MDC.put("identifier", identifier);
            MDC.put("accessType", accessType);
            MDC.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            MDC.put("sessionId", request.getSession().getId());
            MDC.put("remoteAddr", getClientIpAddress(request));
            
            // Prepare data access details
            Map<String, Object> accessData = new HashMap<>();
            accessData.put("requestUri", request.getRequestURI());
            accessData.put("method", request.getMethod());
            accessData.put("userAgent", request.getHeader("User-Agent"));
            
            if (dataDetails != null) {
                accessData.putAll(dataDetails);
            }
            
            // Log data access event
            securityLogger.info("DATA_ACCESS: user={}, type={}, id={}, access={}, ip={}, session={}, data={}", 
                userId, dataType, identifier, accessType, getClientIpAddress(request), 
                request.getSession().getId(), accessData);
            
        } finally {
            MDC.clear();
        }
    }
    
    /**
     * Get client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * Get security relevant headers
     */
    private Map<String, String> getSecurityRelevantHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        
        // Security relevant headers
        String[] securityHeaders = {
            "Authorization", "X-Forwarded-For", "X-Real-IP", "X-Forwarded-Proto",
            "User-Agent", "Referer", "Origin", "X-Requested-With"
        };
        
        for (String headerName : securityHeaders) {
            String headerValue = request.getHeader(headerName);
            if (headerValue != null) {
                headers.put(headerName, headerValue);
            }
        }
        
        return headers;
    }
    
    /**
     * Get security statistics
     */
    public Map<String, Object> getSecurityStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Security event statistics
        Map<String, Long> securityEvents = new HashMap<>();
        securityEventCounters.forEach((event, count) -> 
            securityEvents.put(event, count.get()));
        stats.put("securityEvents", securityEvents);
        
        // User activity statistics
        Map<String, Long> userActivity = new HashMap<>();
        userActivityCounters.forEach((userOp, count) -> 
            userActivity.put(userOp, count.get()));
        stats.put("userActivity", userActivity);
        
        // Combined with basic audit statistics
        stats.putAll(auditService.getOperationStatistics());
        
        return stats;
    }
    
    /**
     * Reset security statistics
     */
    public void resetSecurityStatistics() {
        securityEventCounters.clear();
        userActivityCounters.clear();
        auditService.resetStatistics();
    }
}