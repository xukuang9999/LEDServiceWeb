package com.zhglxt.fileManager.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import java.io.InputStream;

/**
 * File Download Result Model
 * 
 * @author zhglxt
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileDownloadResult {

    /**
     * Whether the download was successful
     */
    private boolean success;

    /**
     * File input stream
     */
    private InputStream inputStream;

    /**
     * File name
     */
    private String fileName;

    /**
     * File size in bytes
     */
    @PositiveOrZero(message = "File size must be zero or positive")
    private long fileSize;

    /**
     * MIME type
     */
    private String mimeType;

    /**
     * Content disposition (inline or attachment)
     */
    private String contentDisposition = "attachment";

    /**
     * Error message if download failed
     */
    private String errorMessage;

    /**
     * File extension
     */
    private String extension;

    /**
     * Whether the file should be cached
     */
    private boolean cacheable = true;

    /**
     * Cache control header value
     */
    private String cacheControl = "max-age=3600";

    // Constructors
    public FileDownloadResult() {
    }

    public FileDownloadResult(boolean success, String errorMessage) {
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public FileDownloadResult(InputStream inputStream, String fileName, long fileSize, String mimeType) {
        this.success = true;
        this.inputStream = inputStream;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
    }

    // Static factory methods
    public static FileDownloadResult success(InputStream inputStream, String fileName, long fileSize, String mimeType) {
        return new FileDownloadResult(inputStream, fileName, fileSize, mimeType);
    }

    public static FileDownloadResult failure(String errorMessage) {
        return new FileDownloadResult(false, errorMessage);
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
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

    public String getContentDisposition() {
        return contentDisposition;
    }

    public void setContentDisposition(String contentDisposition) {
        this.contentDisposition = contentDisposition;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public boolean isCacheable() {
        return cacheable;
    }

    public void setCacheable(boolean cacheable) {
        this.cacheable = cacheable;
    }

    public String getCacheControl() {
        return cacheControl;
    }

    public void setCacheControl(String cacheControl) {
        this.cacheControl = cacheControl;
    }

    /**
     * Get formatted file size
     */
    public String getFormattedFileSize() {
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else if (fileSize < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", fileSize / (1024.0 * 1024.0 * 1024.0));
        }
    }

    /**
     * Check if file is an image
     */
    public boolean isImage() {
        return mimeType != null && mimeType.startsWith("image/");
    }

    /**
     * Check if file should be displayed inline
     */
    public boolean isInline() {
        return "inline".equals(contentDisposition);
    }

    @Override
    public String toString() {
        return "FileDownloadResult{" +
                "success=" + success +
                ", fileName='" + fileName + '\'' +
                ", fileSize=" + fileSize +
                ", mimeType='" + mimeType + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}