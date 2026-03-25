package com.zhglxt.fileManager.exception;

/**
 * File Not Found Exception
 * Thrown when a requested file cannot be found
 * 
 * @author zhglxt
 */
public class FileNotFoundException extends StorageException {

    public FileNotFoundException(String filePath) {
        super("FILE_NOT_FOUND", "File not found: " + filePath);
    }

    public FileNotFoundException(String filePath, Throwable cause) {
        super("FILE_NOT_FOUND", "File not found: " + filePath, cause);
    }
}