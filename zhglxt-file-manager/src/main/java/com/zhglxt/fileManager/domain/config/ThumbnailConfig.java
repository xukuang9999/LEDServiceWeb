package com.zhglxt.fileManager.domain.config;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * Thumbnail Configuration Model
 * 
 * @author zhglxt
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ThumbnailConfig {

    /**
     * Whether thumbnail generation is enabled
     */
    private boolean enabled = true;

    /**
     * Thumbnail directory relative to root directory
     */
    @NotBlank(message = "Thumbnail directory cannot be blank")
    private String directory = "thumbnails";

    /**
     * Default thumbnail width
     */
    @Positive(message = "Thumbnail width must be positive")
    private int width = 200;

    /**
     * Default thumbnail height
     */
    @Positive(message = "Thumbnail height must be positive")
    private int height = 200;

    /**
     * Thumbnail quality (0.0 to 1.0)
     */
    @DecimalMin(value = "0.0", message = "Quality must be at least 0.0")
    @DecimalMax(value = "1.0", message = "Quality must be at most 1.0")
    private float quality = 0.8f;

    /**
     * Maximum width for thumbnails
     */
    @Positive(message = "Max width must be positive")
    private int maxWidth = 800;

    /**
     * Maximum height for thumbnails
     */
    @Positive(message = "Max height must be positive")
    private int maxHeight = 600;

    /**
     * Thumbnail format (jpg, png, webp)
     */
    private String format = "jpg";

    /**
     * Whether to maintain aspect ratio
     */
    private boolean maintainAspectRatio = true;

    /**
     * Background color for transparent images (hex format)
     */
    private String backgroundColor = "#FFFFFF";

    /**
     * Whether to enable progressive JPEG
     */
    private boolean progressive = false;

    /**
     * Cache duration in seconds
     */
    @Positive(message = "Cache duration must be positive")
    private long cacheDuration = 3600;

    // Constructors
    public ThumbnailConfig() {
    }

    public ThumbnailConfig(boolean enabled, String directory, int width, int height) {
        this.enabled = enabled;
        this.directory = directory;
        this.width = width;
        this.height = height;
    }

    // Getters and Setters
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public float getQuality() {
        return quality;
    }

    public void setQuality(float quality) {
        this.quality = quality;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public boolean isMaintainAspectRatio() {
        return maintainAspectRatio;
    }

    public void setMaintainAspectRatio(boolean maintainAspectRatio) {
        this.maintainAspectRatio = maintainAspectRatio;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public boolean isProgressive() {
        return progressive;
    }

    public void setProgressive(boolean progressive) {
        this.progressive = progressive;
    }

    public long getCacheDuration() {
        return cacheDuration;
    }

    public void setCacheDuration(long cacheDuration) {
        this.cacheDuration = cacheDuration;
    }

    // Convenience methods for different thumbnail sizes
    public ThumbnailSize getSmallSize() {
        return ThumbnailSize.SMALL;
    }

    public ThumbnailSize getMediumSize() {
        return ThumbnailSize.MEDIUM;
    }

    public ThumbnailSize getLargeSize() {
        return ThumbnailSize.LARGE;
    }

    /**
     * Thumbnail Size Enum
     */
    public enum ThumbnailSize {
        SMALL(100, 100),
        MEDIUM(200, 200),
        LARGE(400, 400),
        XLARGE(800, 600);

        private final int width;
        private final int height;

        ThumbnailSize(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }

    @Override
    public String toString() {
        return "ThumbnailConfig{" +
                "enabled=" + enabled +
                ", directory='" + directory + '\'' +
                ", width=" + width +
                ", height=" + height +
                ", quality=" + quality +
                '}';
    }
}