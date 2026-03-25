package com.zhglxt.fileManager.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zhglxt.fileManager.domain.config.ThumbnailConfig.ThumbnailSize;

import java.time.LocalDateTime;

/**
 * Thumbnail Information Model
 * 
 * @author zhglxt
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ThumbnailInfo {

    /**
     * Original image path
     */
    private String originalPath;

    /**
     * Thumbnail path
     */
    private String thumbnailPath;

    /**
     * Thumbnail size
     */
    private ThumbnailSize size;

    /**
     * Custom width (if not using standard size)
     */
    private Integer width;

    /**
     * Custom height (if not using standard size)
     */
    private Integer height;

    /**
     * Thumbnail file size in bytes
     */
    private long fileSize;

    /**
     * Thumbnail format (jpg, png, etc.)
     */
    private String format;

    /**
     * Creation timestamp
     */
    private LocalDateTime createdAt;

    /**
     * Last modified timestamp
     */
    private LocalDateTime lastModified;

    /**
     * Whether thumbnail exists
     */
    private boolean exists;

    // Constructors
    public ThumbnailInfo() {
    }

    public ThumbnailInfo(String originalPath, String thumbnailPath, ThumbnailSize size) {
        this.originalPath = originalPath;
        this.thumbnailPath = thumbnailPath;
        this.size = size;
        this.width = size.getWidth();
        this.height = size.getHeight();
        this.createdAt = LocalDateTime.now();
    }

    public ThumbnailInfo(String originalPath, String thumbnailPath, int width, int height) {
        this.originalPath = originalPath;
        this.thumbnailPath = thumbnailPath;
        this.width = width;
        this.height = height;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getOriginalPath() {
        return originalPath;
    }

    public void setOriginalPath(String originalPath) {
        this.originalPath = originalPath;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public ThumbnailSize getSize() {
        return size;
    }

    public void setSize(ThumbnailSize size) {
        this.size = size;
        if (size != null) {
            this.width = size.getWidth();
            this.height = size.getHeight();
        }
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }

    public boolean isExists() {
        return exists;
    }

    public void setExists(boolean exists) {
        this.exists = exists;
    }

    /**
     * Get display dimensions as string
     */
    public String getDimensions() {
        return width + "x" + height;
    }

    /**
     * Check if this is a standard size thumbnail
     */
    public boolean isStandardSize() {
        return size != null;
    }

    @Override
    public String toString() {
        return "ThumbnailInfo{" +
                "originalPath='" + originalPath + '\'' +
                ", thumbnailPath='" + thumbnailPath + '\'' +
                ", size=" + size +
                ", width=" + width +
                ", height=" + height +
                ", fileSize=" + fileSize +
                ", format='" + format + '\'' +
                ", exists=" + exists +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ThumbnailInfo that = (ThumbnailInfo) o;

        if (!originalPath.equals(that.originalPath)) return false;
        if (!width.equals(that.width)) return false;
        return height.equals(that.height);
    }

    @Override
    public int hashCode() {
        int result = originalPath.hashCode();
        result = 31 * result + width.hashCode();
        result = 31 * result + height.hashCode();
        return result;
    }
}