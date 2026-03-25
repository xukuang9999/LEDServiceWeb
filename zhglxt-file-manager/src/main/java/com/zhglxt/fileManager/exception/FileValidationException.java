package com.zhglxt.fileManager.exception;

import java.util.List;

/**
 * File Validation Exception
 * Thrown when file validation fails
 * 
 * @author zhglxt
 */
public class FileValidationException extends StorageException {

    private final List<String> validationErrors;

    public FileValidationException(String message) {
        super("FILE_VALIDATION_ERROR", message);
        this.validationErrors = List.of(message);
    }

    public FileValidationException(List<String> validationErrors) {
        super("FILE_VALIDATION_ERROR", String.join("; ", validationErrors));
        this.validationErrors = validationErrors;
    }

    public FileValidationException(String message, Throwable cause) {
        super("FILE_VALIDATION_ERROR", message, cause);
        this.validationErrors = List.of(message);
    }

    public List<String> getValidationErrors() {
        return validationErrors;
    }
}