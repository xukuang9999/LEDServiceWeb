package com.zhglxt.fileManager.service.validation.impl;

import com.zhglxt.fileManager.config.FileManagerProperties;
import com.zhglxt.fileManager.domain.validation.FileValidationResult;
import com.zhglxt.fileManager.service.validation.FileValidationService;
import com.zhglxt.fileManager.service.validation.FileSecurityScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * File Validation Service Implementation
 * 
 * @author zhglxt
 */
@Service
public class FileValidationServiceImpl implements FileValidationService {

    private static final Logger logger = LoggerFactory.getLogger(FileValidationServiceImpl.class);

    // Pattern for dangerous file names
    private static final Pattern DANGEROUS_FILENAME_PATTERN = Pattern.compile(
        ".*[<>:\"/\\\\|?*].*|^(CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])$", 
        Pattern.CASE_INSENSITIVE
    );

    // Pattern for safe file names (alphanumeric, dots, dashes, underscores)
    private static final Pattern SAFE_FILENAME_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._-]+$"
    );

    private final FileManagerProperties properties;
    private final FileSecurityScanner securityScanner;

    @Autowired
    public FileValidationServiceImpl(FileManagerProperties properties, FileSecurityScanner securityScanner) {
        this.properties = properties;
        this.securityScanner = securityScanner;
    }

    @Override
    public FileValidationResult validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return FileValidationResult.invalid("File is empty or null");
        }

        List<String> errors = new ArrayList<>();

        // Validate file name
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.trim().isEmpty()) {
            errors.add("File name is required");
        } else {
            // Check for dangerous file names
            if (DANGEROUS_FILENAME_PATTERN.matcher(fileName).matches()) {
                errors.add("File name contains invalid characters");
            }

            // Validate extension
            if (!isExtensionAllowed(fileName)) {
                errors.add("File extension is not allowed. Allowed extensions: " + getAllowedExtensions());
            }
        }

        // Validate file size
        long fileSize = file.getSize();
        if (!isSizeAllowed(fileSize)) {
            errors.add("File size exceeds maximum allowed size of " + formatBytes(getMaxFileSize()));
        }

        // Validate content if basic validation passes
        if (errors.isEmpty()) {
            try (InputStream inputStream = file.getInputStream()) {
                FileValidationResult contentResult = validateFileContent(fileName, inputStream);
                if (!contentResult.isValid()) {
                    errors.addAll(contentResult.getErrors());
                }
            } catch (IOException e) {
                logger.error("Error reading file content for validation: {}", fileName, e);
                errors.add("Unable to read file content for validation");
            }
        }

        return errors.isEmpty() ? FileValidationResult.valid() : FileValidationResult.invalid(errors);
    }

    @Override
    public FileValidationResult validateFile(String fileName, long fileSize) {
        List<String> errors = new ArrayList<>();

        // Validate file name
        if (fileName == null || fileName.trim().isEmpty()) {
            errors.add("File name is required");
        } else {
            // Check for dangerous file names
            if (DANGEROUS_FILENAME_PATTERN.matcher(fileName).matches()) {
                errors.add("File name contains invalid characters");
            }

            // Validate extension
            if (!isExtensionAllowed(fileName)) {
                errors.add("File extension is not allowed. Allowed extensions: " + getAllowedExtensions());
            }
        }

        // Validate file size
        if (!isSizeAllowed(fileSize)) {
            errors.add("File size exceeds maximum allowed size of " + formatBytes(getMaxFileSize()));
        }

        return errors.isEmpty() ? FileValidationResult.valid() : FileValidationResult.invalid(errors);
    }

    @Override
    public FileValidationResult validateFileContent(String fileName, InputStream inputStream) {
        try {
            return securityScanner.scanFile(fileName, inputStream);
        } catch (Exception e) {
            logger.error("Error scanning file content: {}", fileName, e);
            return FileValidationResult.invalid("Unable to scan file content for security threats");
        }
    }

    @Override
    public boolean isExtensionAllowed(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return false;
        }

        String extension = getFileExtension(fileName);
        if (extension == null) {
            return false; // Files without extension are not allowed
        }

        List<String> allowedExtensions = getAllowedExtensions();
        return allowedExtensions.stream()
                .anyMatch(allowed -> allowed.equalsIgnoreCase(extension));
    }

    @Override
    public boolean isSizeAllowed(long fileSize) {
        return fileSize > 0 && fileSize <= getMaxFileSize();
    }

    @Override
    public List<String> getAllowedExtensions() {
        return properties.getAllowedExtensions();
    }

    @Override
    public long getMaxFileSize() {
        return properties.getMaxFileSize();
    }

    @Override
    public String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return "unnamed_file";
        }

        // Remove path separators and dangerous characters
        String sanitized = fileName.replaceAll("[<>:\"/\\\\|?*]", "_");
        
        // Remove control characters
        sanitized = sanitized.replaceAll("[\\x00-\\x1f\\x7f]", "");
        
        // Trim whitespace
        sanitized = sanitized.trim();
        
        // Ensure it's not a reserved name
        if (DANGEROUS_FILENAME_PATTERN.matcher(sanitized).matches()) {
            sanitized = "safe_" + sanitized;
        }
        
        // Ensure it's not empty after sanitization
        if (sanitized.isEmpty()) {
            sanitized = "unnamed_file";
        }
        
        // Limit length to prevent filesystem issues
        if (sanitized.length() > 255) {
            String extension = getFileExtension(sanitized);
            String nameWithoutExt = sanitized.substring(0, sanitized.lastIndexOf('.'));
            int maxNameLength = 255 - (extension != null ? extension.length() + 1 : 0);
            sanitized = nameWithoutExt.substring(0, Math.min(nameWithoutExt.length(), maxNameLength));
            if (extension != null) {
                sanitized += "." + extension;
            }
        }
        
        return sanitized;
    }

    private String getFileExtension(String fileName) {
        if (fileName == null) {
            return null;
        }
        
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return null;
        }
        
        return fileName.substring(lastDotIndex + 1);
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
}