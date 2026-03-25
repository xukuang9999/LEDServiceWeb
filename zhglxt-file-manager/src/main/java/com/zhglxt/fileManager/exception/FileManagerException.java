package com.zhglxt.fileManager.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Base File Manager Exception
 * 
 * Base exception class for all file manager related exceptions.
 * Provides enhanced error context and monitoring capabilities.
 * 
 * @author zhglxt
 */
public class FileManagerException extends RuntimeException {

    /**
     * Error code for categorizing the exception
     */
    private final String errorCode;
    
    /**
     * Timestamp when the exception occurred
     */
    private final LocalDateTime timestamp;
    
    /**
     * Additional context information
     */
    private final Map<String, Object> context;
    
    /**
     * User ID associated with the operation (if available)
     */
    private String userId;
    
    /**
     * Operation that was being performed when the exception occurred
     */
    private String operation;
    
    /**
     * File path involved in the operation (if applicable)
     */
    private String filePath;

    /**
     * Constructs a new FileManagerException with the specified detail message.
     * 
     * @param message the detail message
     */
    public FileManagerException(String message) {
        super(message);
        this.errorCode = "FILE_MANAGER_ERROR";
        this.timestamp = LocalDateTime.now();
        this.context = new HashMap<>();
    }

    /**
     * Constructs a new FileManagerException with the specified error code and message.
     * 
     * @param errorCode the error code
     * @param message the detail message
     */
    public FileManagerException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.timestamp = LocalDateTime.now();
        this.context = new HashMap<>();
    }

    /**
     * Constructs a new FileManagerException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause
     */
    public FileManagerException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "FILE_MANAGER_ERROR";
        this.timestamp = LocalDateTime.now();
        this.context = new HashMap<>();
    }

    /**
     * Constructs a new FileManagerException with the specified error code, message and cause.
     * 
     * @param errorCode the error code
     * @param message the detail message
     * @param cause the cause
     */
    public FileManagerException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.timestamp = LocalDateTime.now();
        this.context = new HashMap<>();
    }

    /**
     * Constructs a new FileManagerException with the specified cause.
     * 
     * @param cause the cause
     */
    public FileManagerException(Throwable cause) {
        super(cause);
        this.errorCode = "FILE_MANAGER_ERROR";
        this.timestamp = LocalDateTime.now();
        this.context = new HashMap<>();
    }

    // Getters
    public String getErrorCode() {
        return errorCode;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Map<String, Object> getContext() {
        return new HashMap<>(context);
    }

    public String getUserId() {
        return userId;
    }

    public String getOperation() {
        return operation;
    }

    public String getFilePath() {
        return filePath;
    }

    // Setters for context information
    public FileManagerException withUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public FileManagerException withOperation(String operation) {
        this.operation = operation;
        return this;
    }

    public FileManagerException withFilePath(String filePath) {
        this.filePath = filePath;
        return this;
    }

    public FileManagerException withContext(String key, Object value) {
        this.context.put(key, value);
        return this;
    }

    public FileManagerException withContext(Map<String, Object> additionalContext) {
        this.context.putAll(additionalContext);
        return this;
    }

    /**
     * Get a formatted error message with context information
     */
    public String getFormattedMessage() {
        StringBuilder sb = new StringBuilder(getMessage());
        
        if (userId != null) {
            sb.append(" [User: ").append(userId).append("]");
        }
        
        if (operation != null) {
            sb.append(" [Operation: ").append(operation).append("]");
        }
        
        if (filePath != null) {
            sb.append(" [File: ").append(filePath).append("]");
        }
        
        if (!context.isEmpty()) {
            sb.append(" [Context: ").append(context).append("]");
        }
        
        return sb.toString();
    }

    @Override
    public String toString() {
        return String.format("%s{errorCode='%s', timestamp=%s, message='%s', userId='%s', operation='%s', filePath='%s', context=%s}",
                getClass().getSimpleName(), errorCode, timestamp, getMessage(), userId, operation, filePath, context);
    }
}