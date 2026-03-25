package com.zhglxt.fileManager.domain.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator for file size
 * 
 * @author zhglxt
 */
public class ValidFileSizeValidator implements ConstraintValidator<ValidFileSize, Long> {

    private long maxSize;
    private long minSize;
    private String maxReadable;
    private String minReadable;

    @Override
    public void initialize(ValidFileSize constraintAnnotation) {
        this.maxSize = constraintAnnotation.max();
        this.minSize = constraintAnnotation.min();
        this.maxReadable = constraintAnnotation.maxReadable();
        this.minReadable = constraintAnnotation.minReadable();
    }

    @Override
    public boolean isValid(Long fileSize, ConstraintValidatorContext context) {
        if (fileSize == null) {
            return false;
        }

        if (fileSize < minSize) {
            context.disableDefaultConstraintViolation();
            String message = "File size is too small. Minimum size is " + 
                    (minReadable.isEmpty() ? formatBytes(minSize) : minReadable);
            context.buildConstraintViolationWithTemplate(message)
                    .addConstraintViolation();
            return false;
        }

        if (fileSize > maxSize) {
            context.disableDefaultConstraintViolation();
            String message = "File size is too large. Maximum size is " + 
                    (maxReadable.isEmpty() ? formatBytes(maxSize) : maxReadable);
            context.buildConstraintViolationWithTemplate(message)
                    .addConstraintViolation();
            return false;
        }

        return true;
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