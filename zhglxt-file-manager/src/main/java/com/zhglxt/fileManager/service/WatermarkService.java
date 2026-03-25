package com.zhglxt.fileManager.service;

import com.zhglxt.fileManager.domain.config.WatermarkConfig;
import com.zhglxt.fileManager.exception.WatermarkException;

import java.io.InputStream;

/**
 * Watermark Service Interface
 * 
 * Provides functionality for applying watermarks to images with configurable
 * text watermarks, opacity, positioning, and conditional watermarking based on file types.
 * 
 * @author zhglxt
 */
public interface WatermarkService {

    /**
     * Apply watermark to an image file
     * 
     * @param imageInputStream Input stream of the original image
     * @param fileName Original file name (used for type detection)
     * @return InputStream of the watermarked image
     * @throws WatermarkException if watermarking fails
     */
    InputStream applyWatermark(InputStream imageInputStream, String fileName);

    /**
     * Apply watermark to an image file with custom configuration
     * 
     * @param imageInputStream Input stream of the original image
     * @param fileName Original file name (used for type detection)
     * @param config Custom watermark configuration
     * @return InputStream of the watermarked image
     * @throws WatermarkException if watermarking fails
     */
    InputStream applyWatermark(InputStream imageInputStream, String fileName, WatermarkConfig config);

    /**
     * Check if watermarking is enabled globally
     * 
     * @return true if watermarking is enabled
     */
    boolean isWatermarkEnabled();

    /**
     * Get the current watermark configuration
     * 
     * @return Current watermark configuration
     */
    WatermarkConfig getWatermarkConfig();

    /**
     * Check if a file type supports watermarking
     * 
     * @param fileName File name to check
     * @return true if the file type supports watermarking
     */
    boolean supportsWatermarking(String fileName);

    /**
     * Check if an image meets the minimum size requirements for watermarking
     * 
     * @param imageInputStream Input stream of the image
     * @param fileName File name for type detection
     * @return true if the image meets minimum size requirements
     */
    boolean meetsMinimumSize(InputStream imageInputStream, String fileName);

    /**
     * Validate watermark configuration
     * 
     * @param config Watermark configuration to validate
     * @throws IllegalArgumentException if configuration is invalid
     */
    void validateConfig(WatermarkConfig config);
}