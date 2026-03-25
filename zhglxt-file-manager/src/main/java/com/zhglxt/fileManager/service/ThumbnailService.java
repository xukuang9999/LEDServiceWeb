package com.zhglxt.fileManager.service;

import com.zhglxt.fileManager.domain.config.ThumbnailConfig.ThumbnailSize;

import java.io.InputStream;
import java.util.List;

/**
 * Thumbnail Service Interface
 * Handles thumbnail generation, caching, and management
 * 
 * @author zhglxt
 */
public interface ThumbnailService {

    /**
     * Generate thumbnail for an image file
     * 
     * @param imagePath path to the original image file
     * @param size thumbnail size
     * @return path to the generated thumbnail, null if generation failed
     */
    String generateThumbnail(String imagePath, ThumbnailSize size);

    /**
     * Generate thumbnail with custom dimensions
     * 
     * @param imagePath path to the original image file
     * @param width thumbnail width
     * @param height thumbnail height
     * @return path to the generated thumbnail, null if generation failed
     */
    String generateThumbnail(String imagePath, int width, int height);

    /**
     * Generate thumbnail from input stream
     * 
     * @param inputStream image input stream
     * @param originalFileName original file name (for extension detection)
     * @param size thumbnail size
     * @return path to the generated thumbnail, null if generation failed
     */
    String generateThumbnail(InputStream inputStream, String originalFileName, ThumbnailSize size);

    /**
     * Check if thumbnail exists for the given image and size
     * 
     * @param imagePath path to the original image file
     * @param size thumbnail size
     * @return true if thumbnail exists
     */
    boolean thumbnailExists(String imagePath, ThumbnailSize size);

    /**
     * Check if thumbnail exists with custom dimensions
     * 
     * @param imagePath path to the original image file
     * @param width thumbnail width
     * @param height thumbnail height
     * @return true if thumbnail exists
     */
    boolean thumbnailExists(String imagePath, int width, int height);

    /**
     * Get thumbnail path for an image
     * 
     * @param imagePath path to the original image file
     * @param size thumbnail size
     * @return path to the thumbnail file
     */
    String getThumbnailPath(String imagePath, ThumbnailSize size);

    /**
     * Get thumbnail path with custom dimensions
     * 
     * @param imagePath path to the original image file
     * @param width thumbnail width
     * @param height thumbnail height
     * @return path to the thumbnail file
     */
    String getThumbnailPath(String imagePath, int width, int height);

    /**
     * Delete all thumbnails for a specific image
     * 
     * @param imagePath path to the original image file
     * @return true if all thumbnails were deleted successfully
     */
    boolean deleteThumbnails(String imagePath);

    /**
     * Delete specific thumbnail
     * 
     * @param imagePath path to the original image file
     * @param size thumbnail size
     * @return true if thumbnail was deleted successfully
     */
    boolean deleteThumbnail(String imagePath, ThumbnailSize size);

    /**
     * Delete thumbnail with custom dimensions
     * 
     * @param imagePath path to the original image file
     * @param width thumbnail width
     * @param height thumbnail height
     * @return true if thumbnail was deleted successfully
     */
    boolean deleteThumbnail(String imagePath, int width, int height);

    /**
     * Clean up orphaned thumbnails (thumbnails without corresponding original files)
     * 
     * @return number of orphaned thumbnails cleaned up
     */
    int cleanupOrphanedThumbnails();

    /**
     * Get all thumbnail sizes for an image
     * 
     * @param imagePath path to the original image file
     * @return list of available thumbnail sizes
     */
    List<ThumbnailSize> getAvailableThumbnailSizes(String imagePath);

    /**
     * Check if file type is supported for thumbnail generation
     * 
     * @param fileName file name or path
     * @return true if file type is supported
     */
    boolean isSupportedImageType(String fileName);

    /**
     * Get thumbnail input stream
     * 
     * @param imagePath path to the original image file
     * @param size thumbnail size
     * @return input stream for the thumbnail, null if not found
     */
    InputStream getThumbnailInputStream(String imagePath, ThumbnailSize size);

    /**
     * Refresh thumbnail cache for an image
     * 
     * @param imagePath path to the original image file
     * @return true if cache was refreshed successfully
     */
    boolean refreshThumbnailCache(String imagePath);

    /**
     * Clear all thumbnail cache
     * 
     * @return true if cache was cleared successfully
     */
    boolean clearThumbnailCache();

    /**
     * Get thumbnail cache statistics
     * 
     * @return cache statistics as a map
     */
    java.util.Map<String, Object> getThumbnailCacheStats();
}