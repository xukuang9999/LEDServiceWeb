package com.zhglxt.fileManager.exception;

/**
 * Storage Exception
 * Base exception for storage operations
 * 
 * @author zhglxt
 */
public class StorageException extends RuntimeException {

    private final String errorCode;

    public StorageException(String message) {
        super(message);
        this.errorCode = "STORAGE_ERROR";
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "STORAGE_ERROR";
    }

    public StorageException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public StorageException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}