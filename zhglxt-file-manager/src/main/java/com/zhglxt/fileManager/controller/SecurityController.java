package com.zhglxt.fileManager.controller;

import com.zhglxt.fileManager.service.security.CsrfProtectionService;
import com.zhglxt.fileManager.service.security.RateLimitingService;
import com.zhglxt.fileManager.service.security.SecurityAuditService;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Security Controller
 * 
 * Provides security-related endpoints for CSRF tokens, security statistics,
 * and security configuration management.
 * 
 * @author zhglxt
 */
@RestController
@RequestMapping("/api/file-manager/security")
@RequiresAuthentication
public class SecurityController {

    private static final Logger logger = LoggerFactory.getLogger(SecurityController.class);

    @Autowired
    private CsrfProtectionService csrfProtectionService;
    
    @Autowired
    private RateLimitingService rateLimitingService;
    
    @Autowired
    private SecurityAuditService securityAuditService;

    /**
     * Get CSRF token for the current session
     */
    @GetMapping("/csrf-token")
    public ResponseEntity<Map<String, Object>> getCsrfToken(HttpServletRequest request) {
        try {
            String token = csrfProtectionService.getCurrentCsrfToken(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("headerName", "X-CSRF-Token");
            response.put("parameterName", "_csrf");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error generating CSRF token: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to generate CSRF token"));
        }
    }
    
    /**
     * Refresh CSRF token
     */
    @PostMapping("/csrf-token/refresh")
    public ResponseEntity<Map<String, Object>> refreshCsrfToken(HttpServletRequest request) {
        try {
            // Invalidate current token
            csrfProtectionService.invalidateCsrfToken(request);
            
            // Generate new token
            String newToken = csrfProtectionService.generateCsrfToken(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("token", newToken);
            response.put("headerName", "X-CSRF-Token");
            response.put("parameterName", "_csrf");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error refreshing CSRF token: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to refresh CSRF token"));
        }
    }
    
    /**
     * Get security statistics (admin only)
     */
    @GetMapping("/statistics")
    @RequiresPermissions("file:admin")
    public ResponseEntity<Map<String, Object>> getSecurityStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();
            
            // Rate limiting statistics
            statistics.put("rateLimiting", rateLimitingService.getRateLimitingStatistics());
            
            // CSRF protection statistics
            statistics.put("csrf", csrfProtectionService.getCsrfStatistics());
            
            // Security audit statistics
            statistics.put("security", securityAuditService.getSecurityStatistics());
            
            return ResponseEntity.ok(statistics);
            
        } catch (Exception e) {
            logger.error("Error getting security statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to get security statistics"));
        }
    }
    
    /**
     * Reset security statistics (admin only)
     */
    @PostMapping("/statistics/reset")
    @RequiresPermissions("file:admin")
    public ResponseEntity<Map<String, Object>> resetSecurityStatistics() {
        try {
            // Reset security statistics
            securityAuditService.resetSecurityStatistics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Security statistics reset successfully");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error resetting security statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to reset security statistics"));
        }
    }
    
    /**
     * Get current user's rate limit status
     */
    @GetMapping("/rate-limit/status")
    public ResponseEntity<Map<String, Object>> getRateLimitStatus(HttpServletRequest request) {
        try {
            String userId = getCurrentUserId(request);
            
            Map<String, Object> status = new HashMap<>();
            status.put("userId", userId);
            status.put("clientIp", getClientIpAddress(request));
            
            // Check current rate limit status for different operations
            Map<String, Boolean> operationStatus = new HashMap<>();
            operationStatus.put("upload", rateLimitingService.isRequestAllowed(userId, "upload", request));
            operationStatus.put("download", rateLimitingService.isRequestAllowed(userId, "download", request));
            operationStatus.put("delete", rateLimitingService.isRequestAllowed(userId, "delete", request));
            
            status.put("allowed", operationStatus);
            
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            logger.error("Error getting rate limit status: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to get rate limit status"));
        }
    }
    
    /**
     * Health check endpoint (no authentication required)
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        health.put("services", Map.of(
            "csrfProtection", "UP",
            "rateLimiting", "UP",
            "securityAudit", "UP"
        ));
        
        return ResponseEntity.ok(health);
    }
    
    /**
     * Get current user ID from request
     */
    private String getCurrentUserId(HttpServletRequest request) {
        // Try to get user ID from session
        Object userId = request.getSession().getAttribute("userId");
        if (userId != null) {
            return userId.toString();
        }

        // Try to get from Shiro security context
        try {
            org.apache.shiro.subject.Subject subject = org.apache.shiro.SecurityUtils.getSubject();
            if (subject != null && subject.isAuthenticated()) {
                Object principal = subject.getPrincipal();
                if (principal != null) {
                    return principal.toString();
                }
            }
        } catch (Exception e) {
            logger.debug("Could not get user from Shiro security context: {}", e.getMessage());
        }

        // Try request parameter or header
        String userIdParam = request.getParameter("userId");
        if (userIdParam != null && !userIdParam.trim().isEmpty()) {
            return userIdParam;
        }

        String userIdHeader = request.getHeader("X-User-Id");
        if (userIdHeader != null && !userIdHeader.trim().isEmpty()) {
            return userIdHeader;
        }

        // Default user for development/testing
        return "anonymous";
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
}