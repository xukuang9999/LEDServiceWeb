package com.zhglxt.fileManager.domain.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Validator for file extensions
 * 
 * @author zhglxt
 */
public class ValidFileExtensionValidator implements ConstraintValidator<ValidFileExtension, String> {

    private Set<String> allowedExtensions;
    private Set<String> forbiddenExtensions;
    private boolean ignoreCase;

    @Override
    public void initialize(ValidFileExtension constraintAnnotation) {
        this.ignoreCase = constraintAnnotation.ignoreCase();
        
        this.allowedExtensions = Arrays.stream(constraintAnnotation.allowed())
                .map(ext -> ignoreCase ? ext.toLowerCase() : ext)
                .collect(Collectors.toSet());
                
        this.forbiddenExtensions = Arrays.stream(constraintAnnotation.forbidden())
                .map(ext -> ignoreCase ? ext.toLowerCase() : ext)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isValid(String fileName, ConstraintValidatorContext context) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return false;
        }

        String extension = getFileExtension(fileName);
        if (extension == null) {
            return allowedExtensions.isEmpty(); // If no allowed extensions specified, allow files without extension
        }

        if (ignoreCase) {
            extension = extension.toLowerCase();
        }

        // Check forbidden extensions first
        if (!forbiddenExtensions.isEmpty() && forbiddenExtensions.contains(extension)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "File extension '" + extension + "' is not allowed")
                    .addConstraintViolation();
            return false;
        }

        // Check allowed extensions
        if (!allowedExtensions.isEmpty() && !allowedExtensions.contains(extension)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "File extension '" + extension + "' is not in the allowed list: " + allowedExtensions)
                    .addConstraintViolation();
            return false;
        }

        return true;
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return null;
        }
        return fileName.substring(lastDotIndex + 1);
    }
}