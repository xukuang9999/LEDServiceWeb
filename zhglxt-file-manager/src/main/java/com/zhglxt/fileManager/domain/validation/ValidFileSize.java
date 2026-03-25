package com.zhglxt.fileManager.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validation annotation for file size
 * 
 * @author zhglxt
 */
@Documented
@Constraint(validatedBy = ValidFileSizeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidFileSize {

    String message() default "Invalid file size";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Maximum file size in bytes
     */
    long max() default Long.MAX_VALUE;

    /**
     * Minimum file size in bytes
     */
    long min() default 0;

    /**
     * Human readable max size for error message (e.g., "10MB")
     */
    String maxReadable() default "";

    /**
     * Human readable min size for error message (e.g., "1KB")
     */
    String minReadable() default "";
}