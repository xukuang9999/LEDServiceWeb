package com.zhglxt.fileManager.service.validation;

import com.zhglxt.fileManager.config.FileManagerProperties;
import com.zhglxt.fileManager.domain.validation.FileValidationResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

/**
 * File Validation Service Interface
 * Provides validation for file size, type, and security
 * 
 * @author zhglxt
 */
public interface FileValidationService {

    /**
     * Validate a multipart file
     * 
     * @param file the file to validate
     * @return validation result
     */
    FileValidationResult validateFile(MultipartFile file);

    /**
     * Validate file by name and size
     * 
     * @param fileName the file name
     * @param fileSize the file size in bytes
     * @return validation result
     */
    FileValidationResult validateFile(String fileName, long fileSize);

    /**
     * Validate file content for security threats
     * 
     * @param fileName the file name
     * @param inputStream the file content stream
     * @return validation result
     */
    FileValidationResult validateFileContent(String fileName, InputStream inputStream);

    /**
     * Check if file extension is allowed
     * 
     * @param fileName the file name
     * @return true if extension is allowed
     */
    boolean isExtensionAllowed(String fileName);

    /**
     * Check if file size is within limits
     * 
     * @param fileSize the file size in bytes
     * @return true if size is within limits
     */
    boolean isSizeAllowed(long fileSize);

    /**
     * Get allowed file extensions
     * 
     * @return list of allowed extensions
     */
    List<String> getAllowedExtensions();

    /**
     * Get maximum file size
     * 
     * @return maximum file size in bytes
     */
    long getMaxFileSize();

    /**
     * Sanitize file name to prevent security issues
     * 
     * @param fileName the original file name
     * @return sanitized file name
     */
    String sanitizeFileName(String fileName);
}