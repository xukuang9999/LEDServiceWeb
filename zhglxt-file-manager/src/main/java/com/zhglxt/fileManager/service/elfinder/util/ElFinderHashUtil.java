package com.zhglxt.fileManager.service.elfinder.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

/**
 * elFinder Hash Utility
 * Handles encoding and decoding of file paths to/from elFinder hashes
 * 
 * @author zhglxt
 */
@Component
public class ElFinderHashUtil {

    private static final Logger logger = LoggerFactory.getLogger(ElFinderHashUtil.class);

    private static final String VOLUME_PREFIX = "l1_"; // Local volume prefix
    private static final String ROOT_HASH = VOLUME_PREFIX + "Lw"; // Base64 encoded empty string

    // Cache for frequently used paths
    private final ConcurrentHashMap<String, String> encodeCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> decodeCache = new ConcurrentHashMap<>();

    /**
     * Encode file path to elFinder hash
     * 
     * @param path file path (relative to root)
     * @return encoded hash
     */
    public String encode(String path) {
        if (!StringUtils.hasText(path)) {
            return ROOT_HASH;
        }

        // Check cache first
        String cached = encodeCache.get(path);
        if (cached != null) {
            return cached;
        }

        try {
            // Normalize path
            String normalizedPath = normalizePath(path);
            
            // Encode path using Base64
            byte[] pathBytes = normalizedPath.getBytes(StandardCharsets.UTF_8);
            String encoded = Base64.getEncoder().encodeToString(pathBytes);
            
            // Add volume prefix
            String hash = VOLUME_PREFIX + encoded;
            
            // Cache the result
            encodeCache.put(path, hash);
            decodeCache.put(hash, normalizedPath);
            
            logger.debug("Encoded path '{}' to hash '{}'", path, hash);
            return hash;

        } catch (Exception e) {
            logger.error("Error encoding path '{}': {}", path, e.getMessage(), e);
            return ROOT_HASH;
        }
    }

    /**
     * Decode elFinder hash to file path
     * 
     * @param hash elFinder hash
     * @return decoded file path
     */
    public String decode(String hash) {
        if (!StringUtils.hasText(hash)) {
            return "";
        }

        // Handle root hash
        if (ROOT_HASH.equals(hash)) {
            return "";
        }

        // Check cache first
        String cached = decodeCache.get(hash);
        if (cached != null) {
            return cached;
        }

        try {
            // Remove volume prefix
            if (!hash.startsWith(VOLUME_PREFIX)) {
                logger.warn("Invalid hash format: {}", hash);
                return "";
            }
            
            String encodedPath = hash.substring(VOLUME_PREFIX.length());
            
            // Decode from Base64
            byte[] pathBytes = Base64.getDecoder().decode(encodedPath);
            String path = new String(pathBytes, StandardCharsets.UTF_8);
            
            // Normalize path
            String normalizedPath = normalizePath(path);
            
            // Cache the result
            decodeCache.put(hash, normalizedPath);
            encodeCache.put(normalizedPath, hash);
            
            logger.debug("Decoded hash '{}' to path '{}'", hash, normalizedPath);
            return normalizedPath;

        } catch (Exception e) {
            logger.error("Error decoding hash '{}': {}", hash, e.getMessage(), e);
            return "";
        }
    }

    /**
     * Get root hash
     * 
     * @return root hash
     */
    public String getRootHash() {
        return ROOT_HASH;
    }

    /**
     * Get volume prefix
     * 
     * @return volume prefix
     */
    public String getVolumePrefix() {
        return VOLUME_PREFIX;
    }

    /**
     * Check if hash is root
     * 
     * @param hash hash to check
     * @return true if root hash
     */
    public boolean isRootHash(String hash) {
        return ROOT_HASH.equals(hash);
    }

    /**
     * Check if hash is valid
     * 
     * @param hash hash to validate
     * @return true if valid
     */
    public boolean isValidHash(String hash) {
        if (!StringUtils.hasText(hash)) {
            return false;
        }

        if (!hash.startsWith(VOLUME_PREFIX)) {
            return false;
        }

        try {
            String encodedPath = hash.substring(VOLUME_PREFIX.length());
            Base64.getDecoder().decode(encodedPath);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get parent hash
     * 
     * @param hash child hash
     * @return parent hash
     */
    public String getParentHash(String hash) {
        String path = decode(hash);
        if (!StringUtils.hasText(path)) {
            return null; // Root has no parent
        }

        String parentPath = getParentPath(path);
        return encode(parentPath);
    }

    /**
     * Create hash for child path
     * 
     * @param parentHash parent hash
     * @param childName child name
     * @return child hash
     */
    public String createChildHash(String parentHash, String childName) {
        String parentPath = decode(parentHash);
        String childPath = StringUtils.hasText(parentPath) ? 
            parentPath + "/" + childName : childName;
        return encode(childPath);
    }

    /**
     * Clear cache
     */
    public void clearCache() {
        encodeCache.clear();
        decodeCache.clear();
        logger.info("Hash cache cleared");
    }

    /**
     * Get cache statistics
     * 
     * @return cache statistics
     */
    public String getCacheStats() {
        return String.format("Encode cache: %d entries, Decode cache: %d entries", 
                           encodeCache.size(), decodeCache.size());
    }

    /**
     * Normalize path
     * 
     * @param path file path
     * @return normalized path
     */
    private String normalizePath(String path) {
        if (!StringUtils.hasText(path)) {
            return "";
        }
        
        // Remove leading/trailing slashes and normalize
        path = path.trim();
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        
        // Normalize path separators
        path = path.replace("\\", "/");
        
        // Remove any path traversal attempts
        path = path.replaceAll("\\.\\./", "");
        path = path.replaceAll("/\\.\\.\\.", "");
        
        return path;
    }

    /**
     * Get parent path
     * 
     * @param filePath file path
     * @return parent path
     */
    private String getParentPath(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return "";
        }
        
        int lastSlash = filePath.lastIndexOf('/');
        if (lastSlash == -1) {
            return "";
        }
        
        return filePath.substring(0, lastSlash);
    }
}