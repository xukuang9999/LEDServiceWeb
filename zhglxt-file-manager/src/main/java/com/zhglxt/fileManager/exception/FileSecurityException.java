package com.zhglxt.fileManager.exception;

/**
 * File Security Exception
 * Thrown when file security scanning detects threats
 * 
 * @author zhglxt
 */
public class FileSecurityException extends StorageException {

    private final String fileName;
    private final String threatType;

    public FileSecurityException(String fileName, String threatType) {
        super("FILE_SECURITY_THREAT", 
              String.format("Security threat detected in file '%s': %s", fileName, threatType));
        this.fileName = fileName;
        this.threatType = threatType;
    }

    public FileSecurityException(String fileName, String threatType, Throwable cause) {
        super("FILE_SECURITY_THREAT", 
              String.format("Security threat detected in file '%s': %s", fileName, threatType), 
              cause);
        this.fileName = fileName;
        this.threatType = threatType;
    }

    public String getFileName() {
        return fileName;
    }

    public String getThreatType() {
        return threatType;
    }
}