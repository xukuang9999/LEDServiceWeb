package com.zhglxt.fileManager.domain.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.regex.Pattern;

/**
 * File path validator
 * 
 * @author zhglxt
 */
public class ValidFilePathValidator implements ConstraintValidator<ValidFilePath, String> {

    private static final Pattern DANGEROUS_PATH_PATTERN = Pattern.compile(
        ".*[<>:\"|?*].*", Pattern.CASE_INSENSITIVE
    );

    private boolean allowAbsolute;
    private boolean allowTraversal;
    private int maxLength;

    @Override
    public void initialize(ValidFilePath constraintAnnotation) {
        this.allowAbsolute = constraintAnnotation.allowAbsolute();
        this.allowTraversal = constraintAnnotation.allowTraversal();
        this.maxLength = constraintAnnotation.maxLength();
    }

    @Override
    public boolean isValid(String path, ConstraintValidatorContext context) {
        if (path == null || path.trim().isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Path cannot be empty")
                    .addConstraintViolation();
            return false;
        }

        // Check path length
        if (path.length() > maxLength) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Path length exceeds maximum allowed length of " + maxLength)
                    .addConstraintViolation();
            return false;
        }

        // Check for dangerous characters
        if (DANGEROUS_PATH_PATTERN.matcher(path).matches()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Path contains invalid characters")
                    .addConstraintViolation();
            return false;
        }

        // Check for absolute paths
        if (!allowAbsolute && (path.startsWith("/") || path.matches("^[a-zA-Z]:\\\\.*"))) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Absolute paths are not allowed")
                    .addConstraintViolation();
            return false;
        }

        // Check for path traversal
        if (!allowTraversal && path.contains("..")) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Path traversal is not allowed")
                    .addConstraintViolation();
            return false;
        }

        // Validate path format
        try {
            Paths.get(path);
        } catch (InvalidPathException e) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Invalid path format")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}