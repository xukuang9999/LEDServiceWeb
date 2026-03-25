package com.zhglxt.fileManager.interceptor;

import com.zhglxt.fileManager.service.security.CsrfProtectionService;
import com.zhglxt.fileManager.service.security.RateLimitingService;
import com.zhglxt.fileManager.service.security.SecurityAuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Security Interceptor
 * 
 * Intercepts all file manager requests to apply security measures including
 * rate limiting, CSRF protection, and comprehensive audit logging.
 * 
 * @author zhglxt
 */
@Component
public class SecurityInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(SecurityInterceptor.class);
    
    @Autowired
    private RateLimitingService rateLimitingService;
    
    @Autowired
    private CsrfProtectionService csrfProtectionService;
    
    @Autowired
    private SecurityAuditService securityAuditService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String userId = getCurrentUserId(request);
        String operation = extractOperation(request);
        
        try {
            // 1. Rate Limiting Check
            if (!rateLimitingService.isRequestAllowed(userId, operation, request)) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Rate limit exceeded\",\"code\":\"RATE_LIMIT_EXCEEDED\"}");
                
                // Log rate limit violation
                Map<String, Object> details = Map.of(
                    "operation", operation,
                    "uri", request.getRequestURI(),
                    "method", request.getMethod()
                );
                
                securityAuditService.auditSecurityViolation(userId, "RATE_LIMIT_EXCEEDED", 
                    "Rate limit exceeded for operation: " + operation, request, details);
                
                return false;
            }
            
            // 2. CSRF Protection Check
            if (!csrfProtectionService.validateCsrfToken(request, userId)) {
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"CSRF token validation failed\",\"code\":\"CSRF_VIOLATION\"}");
                
                return false;
            }
            
            // 3. Log request start
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("operation", operation);
            requestData.put("uri", request.getRequestURI());
            requestData.put("method", request.getMethod());
            requestData.put("queryString", request.getQueryString());
            requestData.put("contentType", request.getContentType());
            requestData.put("contentLength", request.getContentLength());
            
            securityAuditService.auditFileOperation(userId, "REQUEST_START", 
                request.getRequestURI(), true, request, requestData);
            
            // Store request start time for performance tracking
            request.setAttribute("REQUEST_START_TIME", System.currentTimeMillis());
            request.setAttribute("USER_ID", userId);
            request.setAttribute("OPERATION", operation);
            
            return true;
            
        } catch (Exception e) {
            logger.error("Error in security interceptor: {}", e.getMessage(), e);
            
            // Log security error
            Map<String, Object> errorDetails = Map.of(
                "error", e.getMessage(),
                "operation", operation,
                "uri", request.getRequestURI()
            );
            
            securityAuditService.auditSecurityViolation(userId, "SECURITY_INTERCEPTOR_ERROR", 
                "Error in security interceptor: " + e.getMessage(), request, errorDetails);
            
            // Allow request to continue but log the error
            return true;
        }
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                              Object handler, Exception ex) throws Exception {
        try {
            String userId = (String) request.getAttribute("USER_ID");
            String operation = (String) request.getAttribute("OPERATION");
            Long startTime = (Long) request.getAttribute("REQUEST_START_TIME");
            
            if (userId == null || operation == null || startTime == null) {
                return; // Skip if attributes not set
            }
            
            long duration = System.currentTimeMillis() - startTime;
            boolean success = (response.getStatus() < 400);
            
            // Prepare completion data
            Map<String, Object> completionData = new HashMap<>();
            completionData.put("operation", operation);
            completionData.put("uri", request.getRequestURI());
            completionData.put("method", request.getMethod());
            completionData.put("statusCode", response.getStatus());
            completionData.put("duration", duration);
            completionData.put("success", success);
            
            if (ex != null) {
                completionData.put("exception", ex.getClass().getSimpleName());
                completionData.put("exceptionMessage", ex.getMessage());
            }
            
            // Log request completion
            if (success) {
                securityAuditService.auditFileOperation(userId, "REQUEST_COMPLETE", 
                    request.getRequestURI(), true, request, completionData);
            } else {
                securityAuditService.auditFileOperation(userId, "REQUEST_FAILED", 
                    request.getRequestURI(), false, request, completionData);
            }
            
            // Log performance metrics for slow requests
            if (duration > 5000) { // Log requests taking more than 5 seconds
                Map<String, Object> performanceData = new HashMap<>(completionData);
                performanceData.put("slowRequest", true);
                
                securityAuditService.auditFileOperation(userId, "SLOW_REQUEST", 
                    request.getRequestURI(), success, request, performanceData);
            }
            
        } catch (Exception e) {
            logger.error("Error in security interceptor afterCompletion: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Extract operation type from request
     */
    private String extractOperation(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        
        // Extract operation from URI
        if (uri.contains("/upload")) {
            return "upload";
        } else if (uri.contains("/download")) {
            return "download";
        } else if (uri.contains("/delete") || "DELETE".equals(method)) {
            return "delete";
        } else if (uri.contains("/move")) {
            return "move";
        } else if (uri.contains("/copy")) {
            return "copy";
        } else if (uri.contains("/rename")) {
            return "rename";
        } else if (uri.contains("/mkdir")) {
            return "mkdir";
        } else if (uri.contains("/rmdir")) {
            return "rmdir";
        } else if (uri.contains("/list")) {
            return "list";
        } else if (uri.contains("/search")) {
            return "search";
        } else if (uri.contains("/info")) {
            return "info";
        } else if (uri.contains("/preview")) {
            return "preview";
        } else if (uri.contains("/connector")) {
            return "elfinder";
        } else {
            return "unknown";
        }
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
}