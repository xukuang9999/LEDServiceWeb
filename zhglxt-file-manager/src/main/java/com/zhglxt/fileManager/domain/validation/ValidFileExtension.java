package com.zhglxt.fileManager.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validation annotation for file extensions
 * 
 * @author zhglxt
 */
@Documented
@Constraint(validatedBy = ValidFileExtensionValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidFileExtension {

    String message() default "Invalid file extension";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Allowed file extensions
     */
    String[] allowed() default {};

    /**
     * Forbidden file extensions
     */
    String[] forbidden() default {};

    /**
     * Whether to ignore case when comparing extensions
     */
    boolean ignoreCase() default true;
}