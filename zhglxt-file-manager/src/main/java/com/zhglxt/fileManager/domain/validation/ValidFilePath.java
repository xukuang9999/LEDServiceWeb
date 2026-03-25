package com.zhglxt.fileManager.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * File path validation annotation
 * Validates file paths for security and format
 * 
 * @author zhglxt
 */
@Documented
@Constraint(validatedBy = ValidFilePathValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidFilePath {

    String message() default "Invalid file path";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Whether to allow absolute paths
     */
    boolean allowAbsolute() default false;

    /**
     * Whether to allow path traversal (../)
     */
    boolean allowTraversal() default false;

    /**
     * Maximum path length
     */
    int maxLength() default 255;
}