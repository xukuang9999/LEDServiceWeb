package com.zhglxt.fileManager.domain.validation;

import com.zhglxt.fileManager.service.validation.FileValidationService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

/**
 * Comprehensive file validator
 * 
 * @author zhglxt
 */
public class ValidFileValidator implements ConstraintValidator<ValidFile, MultipartFile> {

    @Autowired
    private FileValidationService fileValidationService;

    private boolean validateSize;
    private boolean validateExtension;
    private boolean validateSecurity;
    private boolean allowEmpty;
    private long maxSize;
    private String[] allowedExtensions;

    @Override
    public void initialize(ValidFile constraintAnnotation) {
        this.validateSize = constraintAnnotation.validateSize();
        this.validateExtension = constraintAnnotation.validateExtension();
        this.validateSecurity = constraintAnnotation.validateSecurity();
        this.allowEmpty = constraintAnnotation.allowEmpty();
        this.maxSize = constraintAnnotation.maxSize();
        this.allowedExtensions = constraintAnnotation.allowedExtensions();
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        if (file == null) {
            return allowEmpty;
        }

        if (file.isEmpty() && !allowEmpty) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("File cannot be empty")
                    .addConstraintViolation();
            return false;
        }

        if (file.isEmpty() && allowEmpty) {
            return true;
        }

        // Use the file validation service for comprehensive validation
        FileValidationResult result = fileValidationService.validateFile(file);
        
        if (!result.isValid()) {
            context.disableDefaultConstraintViolation();
            for (String error : result.getErrors()) {
                context.buildConstraintViolationWithTemplate(error)
                        .addConstraintViolation();
            }
            return false;
        }

        return true;
    }
}