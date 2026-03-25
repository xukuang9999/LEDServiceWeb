package com.zhglxt.fileManager.service.security;

import com.zhglxt.fileManager.config.FileManagerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate Limiting Service
 * 
 * Provides rate limiting functionality for file operations to prevent abuse
 * and ensure system stability.
 * 
 * @author zhglxt
 */
@Service
public class RateLimitingService {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitingService.class);
    
    @Autowired
    private FileManagerProperties properties;
    
    @Autowired
    private SecurityAuditService securityAuditService;
    
    // Rate limiting buckets - key: identifier, value: bucket
    private final Map<String, RateLimitBucket> rateLimitBuckets = new ConcurrentHashMap<>();
    
    // Global rate limiting buckets
    private final Map<String, RateLimitBucket> globalBuckets = new ConcurrentHashMap<>();
    
    /**
     * Check if request is allowed based on rate limits
     */
    public boolean isRequestAllowed(String userId, String operation, HttpServletRequest request) {
        String clientIp = getClientIpAddress(request);
        
        // Check user-based rate limit
        boolean userAllowed = checkUserRateLimit(userId, operation);
        
        // Check IP-based rate limit
        boolean ipAllowed = checkIpRateLimit(clientIp, operation);
        
        // Check global rate limit
        boolean globalAllowed = checkGlobalRateLimit(operation);
        
        boolean allowed = userAllowed && ipAllowed && globalAllowed;
        
        if (!allowed) {
            // Log rate limit violation
            Map<String, Object> details = Map.of(
                "userId", userId,
                "clientIp", clientIp,
                "operation", operation,
                "userAllowed", userAllowed,
                "ipAllowed", ipAllowed,
                "globalAllowed", globalAllowed
            );
            
            securityAuditService.auditSecurityViolation(userId, "RATE_LIMIT_EXCEEDED", 
                "Rate limit exceeded for operation: " + operation, request, details);
            
            logger.warn("Rate limit exceeded - user: {}, ip: {}, operation: {}, userAllowed: {}, ipAllowed: {}, globalAllowed: {}", 
                userId, clientIp, operation, userAllowed, ipAllowed, globalAllowed);
        }
        
        return allowed;
    }
    
    /**
     * Check user-based rate limit
     */
    private boolean checkUserRateLimit(String userId, String operation) {
        String key = "user:" + userId + ":" + operation;
        RateLimitConfig config = getUserRateLimitConfig(operation);
        
        return checkRateLimit(key, config);
    }
    
    /**
     * Check IP-based rate limit
     */
    private boolean checkIpRateLimit(String clientIp, String operation) {
        String key = "ip:" + clientIp + ":" + operation;
        RateLimitConfig config = getIpRateLimitConfig(operation);
        
        return checkRateLimit(key, config);
    }
    
    /**
     * Check global rate limit
     */
    private boolean checkGlobalRateLimit(String operation) {
        String key = "global:" + operation;
        RateLimitConfig config = getGlobalRateLimitConfig(operation);
        
        return checkRateLimit(key, config);
    }
    
    /**
     * Check rate limit using token bucket algorithm
     */
    private boolean checkRateLimit(String key, RateLimitConfig config) {
        if (config == null || !config.isEnabled()) {
            return true; // No rate limiting configured
        }
        
        RateLimitBucket bucket = rateLimitBuckets.computeIfAbsent(key, 
            k -> new RateLimitBucket(config.getMaxRequests(), config.getWindowMinutes()));
        
        return bucket.tryConsume();
    }
    
    /**
     * Get user rate limit configuration
     */
    private RateLimitConfig getUserRateLimitConfig(String operation) {
        FileManagerProperties.SecurityConfig security = properties.getSecurity();
        if (security == null || security.getRateLimit() == null) {
            return null;
        }
        
        FileManagerProperties.RateLimit rateLimit = security.getRateLimit();
        
        switch (operation.toLowerCase()) {
            case "upload":
                return new RateLimitConfig(true, rateLimit.getUploadMaxPerUser(), rateLimit.getWindowMinutes());
            case "download":
                return new RateLimitConfig(true, rateLimit.getDownloadMaxPerUser(), rateLimit.getWindowMinutes());
            case "delete":
                return new RateLimitConfig(true, rateLimit.getDeleteMaxPerUser(), rateLimit.getWindowMinutes());
            default:
                return new RateLimitConfig(true, rateLimit.getDefaultMaxPerUser(), rateLimit.getWindowMinutes());
        }
    }
    
    /**
     * Get IP rate limit configuration
     */
    private RateLimitConfig getIpRateLimitConfig(String operation) {
        FileManagerProperties.SecurityConfig security = properties.getSecurity();
        if (security == null || security.getRateLimit() == null) {
            return null;
        }
        
        FileManagerProperties.RateLimit rateLimit = security.getRateLimit();
        
        switch (operation.toLowerCase()) {
            case "upload":
                return new RateLimitConfig(true, rateLimit.getUploadMaxPerIp(), rateLimit.getWindowMinutes());
            case "download":
                return new RateLimitConfig(true, rateLimit.getDownloadMaxPerIp(), rateLimit.getWindowMinutes());
            case "delete":
                return new RateLimitConfig(true, rateLimit.getDeleteMaxPerIp(), rateLimit.getWindowMinutes());
            default:
                return new RateLimitConfig(true, rateLimit.getDefaultMaxPerIp(), rateLimit.getWindowMinutes());
        }
    }
    
    /**
     * Get global rate limit configuration
     */
    private RateLimitConfig getGlobalRateLimitConfig(String operation) {
        FileManagerProperties.SecurityConfig security = properties.getSecurity();
        if (security == null || security.getRateLimit() == null) {
            return null;
        }
        
        FileManagerProperties.RateLimit rateLimit = security.getRateLimit();
        
        switch (operation.toLowerCase()) {
            case "upload":
                return new RateLimitConfig(true, rateLimit.getUploadMaxGlobal(), rateLimit.getWindowMinutes());
            case "download":
                return new RateLimitConfig(true, rateLimit.getDownloadMaxGlobal(), rateLimit.getWindowMinutes());
            case "delete":
                return new RateLimitConfig(true, rateLimit.getDeleteMaxGlobal(), rateLimit.getWindowMinutes());
            default:
                return new RateLimitConfig(true, rateLimit.getDefaultMaxGlobal(), rateLimit.getWindowMinutes());
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
     * Get rate limiting statistics
     */
    public Map<String, Object> getRateLimitingStatistics() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        
        // Count active buckets by type
        int userBuckets = 0;
        int ipBuckets = 0;
        int globalBuckets = 0;
        
        for (String key : rateLimitBuckets.keySet()) {
            if (key.startsWith("user:")) {
                userBuckets++;
            } else if (key.startsWith("ip:")) {
                ipBuckets++;
            } else if (key.startsWith("global:")) {
                globalBuckets++;
            }
        }
        
        stats.put("activeBuckets", Map.of(
            "user", userBuckets,
            "ip", ipBuckets,
            "global", globalBuckets,
            "total", rateLimitBuckets.size()
        ));
        
        return stats;
    }
    
    /**
     * Clean up expired buckets
     */
    public void cleanupExpiredBuckets() {
        LocalDateTime now = LocalDateTime.now();
        
        rateLimitBuckets.entrySet().removeIf(entry -> {
            RateLimitBucket bucket = entry.getValue();
            return bucket.isExpired(now);
        });
        
        logger.debug("Cleaned up expired rate limit buckets, remaining: {}", rateLimitBuckets.size());
    }
    
    /**
     * Rate limit configuration
     */
    private static class RateLimitConfig {
        private final boolean enabled;
        private final int maxRequests;
        private final int windowMinutes;
        
        public RateLimitConfig(boolean enabled, int maxRequests, int windowMinutes) {
            this.enabled = enabled;
            this.maxRequests = maxRequests;
            this.windowMinutes = windowMinutes;
        }
        
        public boolean isEnabled() { return enabled; }
        public int getMaxRequests() { return maxRequests; }
        public int getWindowMinutes() { return windowMinutes; }
    }
    
    /**
     * Token bucket implementation for rate limiting
     */
    private static class RateLimitBucket {
        private final int maxTokens;
        private final int windowMinutes;
        private final AtomicInteger tokens;
        private volatile LocalDateTime lastRefill;
        
        public RateLimitBucket(int maxTokens, int windowMinutes) {
            this.maxTokens = maxTokens;
            this.windowMinutes = windowMinutes;
            this.tokens = new AtomicInteger(maxTokens);
            this.lastRefill = LocalDateTime.now();
        }
        
        public synchronized boolean tryConsume() {
            refillTokens();
            
            if (tokens.get() > 0) {
                tokens.decrementAndGet();
                return true;
            }
            
            return false;
        }
        
        private void refillTokens() {
            LocalDateTime now = LocalDateTime.now();
            long minutesSinceLastRefill = ChronoUnit.MINUTES.between(lastRefill, now);
            
            if (minutesSinceLastRefill >= windowMinutes) {
                // Full refill after window period
                tokens.set(maxTokens);
                lastRefill = now;
            } else if (minutesSinceLastRefill > 0) {
                // Partial refill based on time elapsed
                int tokensToAdd = (int) ((minutesSinceLastRefill * maxTokens) / windowMinutes);
                if (tokensToAdd > 0) {
                    int currentTokens = tokens.get();
                    int newTokens = Math.min(maxTokens, currentTokens + tokensToAdd);
                    tokens.set(newTokens);
                    lastRefill = now;
                }
            }
        }
        
        public boolean isExpired(LocalDateTime now) {
            return ChronoUnit.HOURS.between(lastRefill, now) > 24; // Expire after 24 hours of inactivity
        }
    }
}