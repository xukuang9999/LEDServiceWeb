package com.zhglxt.fileManager.service.security;

import com.zhglxt.fileManager.config.FileManagerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CSRF Protection Service
 * 
 * Provides Cross-Site Request Forgery protection for state-changing operations.
 * Implements token-based CSRF protection with configurable token expiration.
 * 
 * @author zhglxt
 */
@Service
public class CsrfProtectionService {

    private static final Logger logger = LoggerFactory.getLogger(CsrfProtectionService.class);
    
    private static final String CSRF_TOKEN_SESSION_ATTRIBUTE = "CSRF_TOKEN";
    private static final String CSRF_TOKEN_HEADER = "X-CSRF-Token";
    private static final String CSRF_TOKEN_PARAMETER = "_csrf";
    
    @Autowired
    private FileManagerProperties properties;
    
    @Autowired
    private SecurityAuditService securityAuditService;
    
    private final SecureRandom secureRandom = new SecureRandom();
    
    // Token cache for stateless scenarios
    private final Map<String, CsrfToken> tokenCache = new ConcurrentHashMap<>();
    
    /**
     * Generate CSRF token for session
     */
    public String generateCsrfToken(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        
        // Generate new token
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        
        // Store in session
        session.setAttribute(CSRF_TOKEN_SESSION_ATTRIBUTE, token);
        
        // Also store in cache with expiration for stateless scenarios
        CsrfToken csrfToken = new CsrfToken(token, LocalDateTime.now().plusMinutes(getCsrfTokenExpirationMinutes()));
        tokenCache.put(token, csrfToken);
        
        logger.debug("Generated CSRF token for session: {}", session.getId());
        
        return token;
    }
    
    /**
     * Validate CSRF token for state-changing operations
     */
    public boolean validateCsrfToken(HttpServletRequest request, String userId) {
        if (!isCsrfProtectionEnabled()) {
            return true; // CSRF protection disabled
        }
        
        if (!isStateChangingOperation(request)) {
            return true; // Only protect state-changing operations
        }
        
        String providedToken = getCsrfTokenFromRequest(request);
        if (!StringUtils.hasText(providedToken)) {
            // Log CSRF violation - missing token
            Map<String, Object> details = Map.of(
                "reason", "MISSING_TOKEN",
                "method", request.getMethod(),
                "uri", request.getRequestURI()
            );
            
            securityAuditService.auditSecurityViolation(userId, "CSRF_VIOLATION", 
                "CSRF token missing for state-changing operation", request, details);
            
            logger.warn("CSRF token missing for state-changing operation - user: {}, method: {}, uri: {}", 
                userId, request.getMethod(), request.getRequestURI());
            
            return false;
        }
        
        boolean valid = isValidCsrfToken(request, providedToken);
        
        if (!valid) {
            // Log CSRF violation - invalid token
            Map<String, Object> details = Map.of(
                "reason", "INVALID_TOKEN",
                "method", request.getMethod(),
                "uri", request.getRequestURI(),
                "providedToken", providedToken.substring(0, Math.min(8, providedToken.length())) + "..."
            );
            
            securityAuditService.auditSecurityViolation(userId, "CSRF_VIOLATION", 
                "Invalid CSRF token for state-changing operation", request, details);
            
            logger.warn("Invalid CSRF token for state-changing operation - user: {}, method: {}, uri: {}", 
                userId, request.getMethod(), request.getRequestURI());
        }
        
        return valid;
    }
    
    /**
     * Get CSRF token from request (header or parameter)
     */
    private String getCsrfTokenFromRequest(HttpServletRequest request) {
        // Try header first
        String token = request.getHeader(CSRF_TOKEN_HEADER);
        if (StringUtils.hasText(token)) {
            return token;
        }
        
        // Try parameter
        token = request.getParameter(CSRF_TOKEN_PARAMETER);
        if (StringUtils.hasText(token)) {
            return token;
        }
        
        return null;
    }
    
    /**
     * Validate CSRF token against session and cache
     */
    private boolean isValidCsrfToken(HttpServletRequest request, String providedToken) {
        // Check session token first
        HttpSession session = request.getSession(false);
        if (session != null) {
            String sessionToken = (String) session.getAttribute(CSRF_TOKEN_SESSION_ATTRIBUTE);
            if (StringUtils.hasText(sessionToken) && sessionToken.equals(providedToken)) {
                return true;
            }
        }
        
        // Check token cache for stateless scenarios
        CsrfToken cachedToken = tokenCache.get(providedToken);
        if (cachedToken != null && !cachedToken.isExpired()) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Check if operation is state-changing
     */
    private boolean isStateChangingOperation(HttpServletRequest request) {
        String method = request.getMethod().toUpperCase();
        
        // State-changing HTTP methods
        if ("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method) || "PATCH".equals(method)) {
            return true;
        }
        
        // Check for specific operations that change state even with GET
        String uri = request.getRequestURI();
        if (uri.contains("/delete") || uri.contains("/move") || uri.contains("/copy") || 
            uri.contains("/rename") || uri.contains("/mkdir") || uri.contains("/rmdir")) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Check if CSRF protection is enabled
     */
    private boolean isCsrfProtectionEnabled() {
        FileManagerProperties.SecurityConfig security = properties.getSecurity();
        return security != null && security.getCsrf() != null && security.getCsrf().isEnabled();
    }
    
    /**
     * Get CSRF token expiration minutes from configuration
     */
    private int getCsrfTokenExpirationMinutes() {
        FileManagerProperties.SecurityConfig security = properties.getSecurity();
        if (security != null && security.getCsrf() != null) {
            return security.getCsrf().getTokenExpirationMinutes();
        }
        return 60; // Default 1 hour
    }
    
    /**
     * Get current CSRF token for session
     */
    public String getCurrentCsrfToken(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            String token = (String) session.getAttribute(CSRF_TOKEN_SESSION_ATTRIBUTE);
            if (StringUtils.hasText(token)) {
                return token;
            }
        }
        
        // Generate new token if none exists
        return generateCsrfToken(request);
    }
    
    /**
     * Invalidate CSRF token
     */
    public void invalidateCsrfToken(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            String token = (String) session.getAttribute(CSRF_TOKEN_SESSION_ATTRIBUTE);
            if (StringUtils.hasText(token)) {
                session.removeAttribute(CSRF_TOKEN_SESSION_ATTRIBUTE);
                tokenCache.remove(token);
                
                logger.debug("Invalidated CSRF token for session: {}", session.getId());
            }
        }
    }
    
    /**
     * Clean up expired tokens from cache
     */
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        
        tokenCache.entrySet().removeIf(entry -> {
            CsrfToken token = entry.getValue();
            return token.isExpired(now);
        });
        
        logger.debug("Cleaned up expired CSRF tokens, remaining: {}", tokenCache.size());
    }
    
    /**
     * Get CSRF protection statistics
     */
    public Map<String, Object> getCsrfStatistics() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        
        stats.put("enabled", isCsrfProtectionEnabled());
        stats.put("tokenExpirationMinutes", getCsrfTokenExpirationMinutes());
        stats.put("cachedTokens", tokenCache.size());
        
        // Count expired tokens
        LocalDateTime now = LocalDateTime.now();
        long expiredTokens = tokenCache.values().stream()
            .mapToLong(token -> token.isExpired(now) ? 1 : 0)
            .sum();
        
        stats.put("expiredTokens", expiredTokens);
        
        return stats;
    }
    
    /**
     * CSRF Token representation
     */
    private static class CsrfToken {
        private final String token;
        private final LocalDateTime expirationTime;
        
        public CsrfToken(String token, LocalDateTime expirationTime) {
            this.token = token;
            this.expirationTime = expirationTime;
        }
        
        public String getToken() {
            return token;
        }
        
        public boolean isExpired() {
            return isExpired(LocalDateTime.now());
        }
        
        public boolean isExpired(LocalDateTime now) {
            return now.isAfter(expirationTime);
        }
    }
}