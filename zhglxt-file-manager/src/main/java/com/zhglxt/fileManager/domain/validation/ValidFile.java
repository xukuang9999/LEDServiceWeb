package com.zhglxt.fileManager.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Comprehensive file validation annotation
 * Validates file size, extension, and security
 * 
 * @author zhglxt
 */
@Documented
@Constraint(validatedBy = ValidFileValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidFile {

    String message() default "Invalid file";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Whether to validate file size
     */
    boolean validateSize() default true;

    /**
     * Whether to validate file extension
     */
    boolean validateExtension() default true;

    /**
     * Whether to validate file security
     */
    boolean validateSecurity() default true;

    /**
     * Whether to allow empty files
     */
    boolean allowEmpty() default false;

    /**
     * Custom maximum file size (overrides configuration)
     */
    long maxSize() default -1;

    /**
     * Custom allowed extensions (overrides configuration)
     */
    String[] allowedExtensions() default {};
}