package com.zhglxt.fileManager.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Map;

/**
 * File Preview Result Model
 * 
 * @author zhglxt
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FilePreviewResult {

    /**
     * Whether preview is available
     */
    private boolean available;

    /**
     * Preview type (image, text, pdf, etc.)
     */
    private PreviewType type;

    /**
     * Preview content (for text files)
     */
    private String content;

    /**
     * Preview URL (for images, documents)
     */
    private String previewUrl;

    /**
     * Thumbnail URL
     */
    private String thumbnailUrl;

    /**
     * File name
     */
    private String fileName;

    /**
     * File size
     */
    private long fileSize;

    /**
     * MIME type
     */
    private String mimeType;

    /**
     * Error message if preview failed
     */
    private String errorMessage;

    /**
     * Additional preview metadata
     */
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * Whether the preview requires authentication
     */
    private boolean requiresAuth = true;

    // Constructors
    public FilePreviewResult() {
    }

    public FilePreviewResult(boolean available, PreviewType type) {
        this.available = available;
        this.type = type;
    }

    // Static factory methods
    public static FilePreviewResult available(PreviewType type, String previewUrl) {
        FilePreviewResult result = new FilePreviewResult(true, type);
        result.setPreviewUrl(previewUrl);
        return result;
    }

    public static FilePreviewResult unavailable(String errorMessage) {
        FilePreviewResult result = new FilePreviewResult(false, PreviewType.NONE);
        result.setErrorMessage(errorMessage);
        return result;
    }

    public static FilePreviewResult textPreview(String content) {
        FilePreviewResult result = new FilePreviewResult(true, PreviewType.TEXT);
        result.setContent(content);
        return result;
    }

    // Getters and Setters
    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public PreviewType getType() {
        return type;
    }

    public void setType(PreviewType type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public boolean isRequiresAuth() {
        return requiresAuth;
    }

    public void setRequiresAuth(boolean requiresAuth) {
        this.requiresAuth = requiresAuth;
    }

    /**
     * Add metadata entry
     */
    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }

    /**
     * Preview Type Enum
     */
    public enum PreviewType {
        NONE,
        IMAGE,
        TEXT,
        PDF,
        VIDEO,
        AUDIO,
        DOCUMENT,
        ARCHIVE
    }

    @Override
    public String toString() {
        return "FilePreviewResult{" +
                "available=" + available +
                ", type=" + type +
                ", fileName='" + fileName + '\'' +
                ", fileSize=" + fileSize +
                ", mimeType='" + mimeType + '\'' +
                '}';
    }
}