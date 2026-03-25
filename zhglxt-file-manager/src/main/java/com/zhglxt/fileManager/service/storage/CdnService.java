package com.zhglxt.fileManager.service.storage;

import com.zhglxt.fileManager.config.FileManagerProperties;
import com.zhglxt.fileManager.domain.config.StorageConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * CDN Service for Cloud Storage Integration
 * Handles CDN URL generation and cache management
 * 
 * @author zhglxt
 */
@Service
@ConditionalOnProperty(name = "zhglxt.file-manager.storage.cdn-enabled", havingValue = "true")
public class CdnService {

    private static final Logger logger = LoggerFactory.getLogger(CdnService.class);

    private final FileManagerProperties properties;
    private final StorageConfig storageConfig;

    public CdnService(FileManagerProperties properties) {
        this.properties = properties;
        this.storageConfig = properties.getStorage();
    }

    /**
     * Generate CDN URL for a file
     * 
     * @param objectKey the object key in storage
     * @return CDN URL or null if CDN is not configured
     */
    public String generateCdnUrl(String objectKey) {
        if (!storageConfig.isCdnEnabled() || !StringUtils.hasText(storageConfig.getCdnDomain())) {
            return null;
        }

        String protocol = storageConfig.isUseHttps() ? "https" : "http";
        String cleanObjectKey = objectKey.startsWith("/") ? objectKey.substring(1) : objectKey;
        
        return protocol + "://" + storageConfig.getCdnDomain() + "/" + cleanObjectKey;
    }

    /**
     * Generate CDN URL with query parameters
     * 
     * @param objectKey the object key in storage
     * @param parameters query parameters to append
     * @return CDN URL with parameters
     */
    public String generateCdnUrlWithParams(String objectKey, Map<String, String> parameters) {
        String baseUrl = generateCdnUrl(objectKey);
        if (baseUrl == null || parameters == null || parameters.isEmpty()) {
            return baseUrl;
        }

        StringBuilder urlBuilder = new StringBuilder(baseUrl);
        urlBuilder.append("?");
        
        boolean first = true;
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            if (!first) {
                urlBuilder.append("&");
            }
            urlBuilder.append(entry.getKey()).append("=").append(entry.getValue());
            first = false;
        }

        return urlBuilder.toString();
    }

    /**
     * Generate thumbnail CDN URL
     * 
     * @param objectKey the object key in storage
     * @param width thumbnail width
     * @param height thumbnail height
     * @return CDN URL for thumbnail
     */
    public String generateThumbnailCdnUrl(String objectKey, int width, int height) {
        if (!isCdnEnabled()) {
            return null;
        }

        Map<String, String> params = new HashMap<>();
        
        // Different CDN providers have different thumbnail parameters
        StorageConfig.StorageType storageType = storageConfig.getType();
        switch (storageType) {
            case ALIBABA_OSS:
                // Alibaba OSS image processing parameters
                params.put("x-oss-process", "image/resize,w_" + width + ",h_" + height);
                break;
            case TENCENT_COS:
                // Tencent COS image processing parameters
                params.put("imageMogr2/thumbnail", width + "x" + height);
                break;
            case AWS_S3:
                // AWS CloudFront with Lambda@Edge or custom processing
                params.put("w", String.valueOf(width));
                params.put("h", String.valueOf(height));
                break;
            default:
                // Generic parameters
                params.put("width", String.valueOf(width));
                params.put("height", String.valueOf(height));
                break;
        }

        return generateCdnUrlWithParams(objectKey, params);
    }

    /**
     * Generate watermarked image CDN URL
     * 
     * @param objectKey the object key in storage
     * @param watermarkText watermark text
     * @return CDN URL for watermarked image
     */
    public String generateWatermarkCdnUrl(String objectKey, String watermarkText) {
        if (!isCdnEnabled() || !StringUtils.hasText(watermarkText)) {
            return generateCdnUrl(objectKey);
        }

        Map<String, String> params = new HashMap<>();
        
        StorageConfig.StorageType storageType = storageConfig.getType();
        switch (storageType) {
            case ALIBABA_OSS:
                // Alibaba OSS watermark parameters
                params.put("x-oss-process", "image/watermark,text_" + encodeWatermarkText(watermarkText));
                break;
            case TENCENT_COS:
                // Tencent COS watermark parameters
                params.put("watermark/1/text", encodeWatermarkText(watermarkText));
                break;
            default:
                // For other providers, return original URL
                return generateCdnUrl(objectKey);
        }

        return generateCdnUrlWithParams(objectKey, params);
    }

    /**
     * Purge CDN cache for a file
     * 
     * @param objectKey the object key to purge
     * @return true if purge was successful
     */
    public boolean purgeCdnCache(String objectKey) {
        if (!isCdnEnabled()) {
            return false;
        }

        try {
            // This would typically call the CDN provider's API to purge cache
            // Implementation depends on the specific CDN provider
            logger.info("CDN cache purge requested for: {}", objectKey);
            
            // For now, just log the request
            // In a real implementation, you would call the appropriate CDN API
            return performCdnCachePurge(objectKey);
        } catch (Exception e) {
            logger.error("Failed to purge CDN cache for {}: {}", objectKey, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Get CDN statistics
     * 
     * @return CDN statistics map
     */
    public Map<String, Object> getCdnStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("cdnEnabled", isCdnEnabled());
        stats.put("cdnDomain", storageConfig.getCdnDomain());
        stats.put("useHttps", storageConfig.isUseHttps());
        stats.put("storageType", storageConfig.getType().name());
        
        if (isCdnEnabled()) {
            try {
                Map<String, Object> providerStats = getCdnProviderStats();
                if (providerStats != null) {
                    stats.putAll(providerStats);
                }
            } catch (Exception e) {
                logger.warn("Failed to get CDN provider stats: {}", e.getMessage());
            }
        }
        
        return stats;
    }

    /**
     * Check if CDN is enabled and properly configured
     * 
     * @return true if CDN is enabled
     */
    public boolean isCdnEnabled() {
        return storageConfig.isCdnEnabled() && StringUtils.hasText(storageConfig.getCdnDomain());
    }

    /**
     * Get CDN domain
     * 
     * @return CDN domain or null if not configured
     */
    public String getCdnDomain() {
        return isCdnEnabled() ? storageConfig.getCdnDomain() : null;
    }

    /**
     * Perform actual CDN cache purge
     * This method should be implemented based on the specific CDN provider
     * 
     * @param objectKey the object key to purge
     * @return true if successful
     */
    private boolean performCdnCachePurge(String objectKey) {
        // This is a placeholder implementation
        // In a real scenario, you would integrate with specific CDN APIs:
        // - Alibaba CDN API
        // - Tencent CDN API
        // - AWS CloudFront API
        // - etc.
        
        logger.debug("Performing CDN cache purge for: {}", objectKey);
        return true;
    }

    /**
     * Get CDN provider specific statistics
     * 
     * @return provider stats map
     */
    private Map<String, Object> getCdnProviderStats() {
        // This is a placeholder implementation
        // In a real scenario, you would call the CDN provider's API to get statistics
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("provider", "Generic CDN");
        stats.put("status", "active");
        return stats;
    }

    /**
     * Encode watermark text for URL parameters
     * 
     * @param text watermark text
     * @return encoded text
     */
    private String encodeWatermarkText(String text) {
        try {
            return java.net.URLEncoder.encode(text, "UTF-8");
        } catch (Exception e) {
            logger.warn("Failed to encode watermark text: {}", e.getMessage());
            return text;
        }
    }
}