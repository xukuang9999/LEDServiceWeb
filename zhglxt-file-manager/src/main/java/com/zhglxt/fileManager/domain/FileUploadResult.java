package com.zhglxt.fileManager.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.ArrayList;
import java.util.List;

/**
 * File Upload Result Model
 * 
 * @author zhglxt
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileUploadResult {

    /**
     * Whether the upload was successful
     */
    private boolean success;

    /**
     * Uploaded file path
     */
    private String filePath;

    /**
     * Original file name
     */
    private String fileName;

    /**
     * File size in bytes
     */
    @PositiveOrZero(message = "File size must be zero or positive")
    private long fileSize;

    /**
     * Thumbnail path (if generated)
     */
    private String thumbnailPath;

    /**
     * Success or error message
     */
    private String message;

    /**
     * List of error messages
     */
    private List<String> errors = new ArrayList<>();

    /**
     * File MIME type
     */
    private String mimeType;

    /**
     * File extension
     */
    private String extension;

    /**
     * File hash for integrity checking
     */
    private String hash;

    /**
     * Public URL (for cloud storage)
     */
    private String publicUrl;

    /**
     * Whether watermark was applied
     */
    private boolean watermarkApplied = false;

    /**
     * Upload duration in milliseconds
     */
    private long uploadDuration;

    // Constructors
    public FileUploadResult() {
    }

    public FileUploadResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public FileUploadResult(boolean success, String filePath, String fileName, long fileSize) {
        this.success = success;
        this.filePath = filePath;
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    // Static factory methods
    public static FileUploadResult success(String filePath, String fileName, long fileSize) {
        return new FileUploadResult(true, filePath, fileName, fileSize);
    }

    public static FileUploadResult failure(String message) {
        return new FileUploadResult(false, message);
    }

    public static FileUploadResult failure(List<String> errors) {
        FileUploadResult result = new FileUploadResult(false, null);
        result.setErrors(errors);
        return result;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
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

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getPublicUrl() {
        return publicUrl;
    }

    public void setPublicUrl(String publicUrl) {
        this.publicUrl = publicUrl;
    }

    public boolean isWatermarkApplied() {
        return watermarkApplied;
    }

    public void setWatermarkApplied(boolean watermarkApplied) {
        this.watermarkApplied = watermarkApplied;
    }

    public long getUploadDuration() {
        return uploadDuration;
    }

    public void setUploadDuration(long uploadDuration) {
        this.uploadDuration = uploadDuration;
    }

    /**
     * Add an error message
     */
    public void addError(String error) {
        this.errors.add(error);
        this.success = false;
    }

    /**
     * Check if there are any errors
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
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

    @Override
    public String toString() {
        return "FileUploadResult{" +
                "success=" + success +
                ", filePath='" + filePath + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileSize=" + fileSize +
                ", message='" + message + '\'' +
                ", errors=" + errors +
                '}';
    }
}