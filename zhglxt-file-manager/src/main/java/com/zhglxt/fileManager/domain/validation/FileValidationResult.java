package com.zhglxt.fileManager.domain.validation;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

/**
 * File Validation Result Model
 * 
 * @author zhglxt
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileValidationResult {

    /**
     * Whether validation passed
     */
    private boolean valid;

    /**
     * List of validation error messages
     */
    private List<String> errors = new ArrayList<>();

    /**
     * List of validation warning messages
     */
    private List<String> warnings = new ArrayList<>();

    /**
     * File name that was validated
     */
    private String fileName;

    /**
     * File size that was validated
     */
    private long fileSize;

    /**
     * MIME type that was validated
     */
    private String mimeType;

    /**
     * File extension that was validated
     */
    private String extension;

    // Constructors
    public FileValidationResult() {
        this.valid = true;
    }

    public FileValidationResult(boolean valid) {
        this.valid = valid;
    }

    public FileValidationResult(String fileName, long fileSize, String mimeType) {
        this.valid = true;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
    }

    // Static factory methods
    public static FileValidationResult valid() {
        return new FileValidationResult(true);
    }

    public static FileValidationResult invalid(String error) {
        FileValidationResult result = new FileValidationResult(false);
        result.addError(error);
        return result;
    }

    public static FileValidationResult invalid(List<String> errors) {
        FileValidationResult result = new FileValidationResult(false);
        result.setErrors(errors);
        return result;
    }

    // Getters and Setters
    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
        if (!errors.isEmpty()) {
            this.valid = false;
        }
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
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

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    // Utility methods
    public void addError(String error) {
        this.errors.add(error);
        this.valid = false;
    }

    public void addWarning(String warning) {
        this.warnings.add(warning);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    public String getFirstError() {
        return errors.isEmpty() ? null : errors.get(0);
    }

    public String getFirstWarning() {
        return warnings.isEmpty() ? null : warnings.get(0);
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
        return "FileValidationResult{" +
                "valid=" + valid +
                ", fileName='" + fileName + '\'' +
                ", fileSize=" + fileSize +
                ", errorsCount=" + errors.size() +
                ", warningsCount=" + warnings.size() +
                '}';
    }
}