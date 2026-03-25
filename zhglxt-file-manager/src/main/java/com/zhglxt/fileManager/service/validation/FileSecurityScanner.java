package com.zhglxt.fileManager.service.validation;

import com.zhglxt.fileManager.domain.validation.FileValidationResult;

import java.io.InputStream;

/**
 * File Security Scanner Interface
 * Provides security scanning for uploaded files
 * 
 * @author zhglxt
 */
public interface FileSecurityScanner {

    /**
     * Scan file for security threats
     * 
     * @param fileName the file name
     * @param inputStream the file content stream
     * @return validation result indicating if file is safe
     */
    FileValidationResult scanFile(String fileName, InputStream inputStream);

    /**
     * Check if file contains executable content
     * 
     * @param fileName the file name
     * @param inputStream the file content stream
     * @return true if file contains executable content
     */
    boolean containsExecutableContent(String fileName, InputStream inputStream);

    /**
     * Check if file contains malicious patterns
     * 
     * @param fileName the file name
     * @param inputStream the file content stream
     * @return true if file contains malicious patterns
     */
    boolean containsMaliciousPatterns(String fileName, InputStream inputStream);

    /**
     * Validate file magic number/signature
     * 
     * @param fileName the file name
     * @param inputStream the file content stream
     * @return true if file signature matches expected type
     */
    boolean validateFileSignature(String fileName, InputStream inputStream);
}