package com.zhglxt.fileManager.exception;

/**
 * File Permission Exception
 * Thrown when user lacks permission for file operation
 * 
 * @author zhglxt
 */
public class FilePermissionException extends StorageException {

    private final String userId;
    private final String filePath;
    private final String operation;

    public FilePermissionException(String userId, String filePath, String operation) {
        super("FILE_PERMISSION_DENIED", 
              String.format("Permission denied for user '%s' to perform '%s' on '%s'", userId, operation, filePath));
        this.userId = userId;
        this.filePath = filePath;
        this.operation = operation;
    }

    public FilePermissionException(String userId, String filePath, String operation, Throwable cause) {
        super("FILE_PERMISSION_DENIED", 
              String.format("Permission denied for user '%s' to perform '%s' on '%s'", userId, operation, filePath), 
              cause);
        this.userId = userId;
        this.filePath = filePath;
        this.operation = operation;
    }

    public String getUserId() {
        return userId;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getOperation() {
        return operation;
    }
}