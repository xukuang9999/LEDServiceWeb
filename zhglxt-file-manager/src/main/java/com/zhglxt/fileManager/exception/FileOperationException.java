package com.zhglxt.fileManager.exception;

/**
 * File Operation Exception
 * 
 * Thrown when file operations (upload, download, move, copy, delete) fail.
 * 
 * @author zhglxt
 */
public class FileOperationException extends FileManagerException {

    /**
     * The type of operation that failed
     */
    public enum OperationType {
        UPLOAD, DOWNLOAD, MOVE, COPY, DELETE, RENAME, CREATE_DIRECTORY, LIST_FILES
    }

    private final OperationType operationType;

    public FileOperationException(OperationType operationType, String message) {
        super("FILE_OPERATION_ERROR", message);
        this.operationType = operationType;
        withOperation(operationType.name());
    }

    public FileOperationException(OperationType operationType, String message, Throwable cause) {
        super("FILE_OPERATION_ERROR", message, cause);
        this.operationType = operationType;
        withOperation(operationType.name());
    }

    public FileOperationException(OperationType operationType, String filePath, String message) {
        super("FILE_OPERATION_ERROR", message);
        this.operationType = operationType;
        withOperation(operationType.name()).withFilePath(filePath);
    }

    public FileOperationException(OperationType operationType, String filePath, String message, Throwable cause) {
        super("FILE_OPERATION_ERROR", message, cause);
        this.operationType = operationType;
        withOperation(operationType.name()).withFilePath(filePath);
    }

    public OperationType getOperationType() {
        return operationType;
    }
}