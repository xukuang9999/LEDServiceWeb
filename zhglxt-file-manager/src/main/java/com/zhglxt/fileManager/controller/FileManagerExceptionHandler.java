package com.zhglxt.fileManager.controller;

import com.zhglxt.fileManager.exception.*;
import com.zhglxt.fileManager.service.monitoring.ErrorMonitoringService;
import com.zhglxt.fileManager.service.monitoring.FileOperationAuditService;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthenticatedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import com.zhglxt.fileManager.service.I18nService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Global Exception Handler for File Manager Controllers
 * Provides centralized error handling with proper HTTP status codes and response formatting
 * 
 * @author zhglxt
 */
@RestControllerAdvice(basePackages = "com.zhglxt.fileManager.controller")
public class FileManagerExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(FileManagerExceptionHandler.class);

    @Autowired
    @Qualifier("fileManagerMessageSource")
    private MessageSource messageSource;
    
    @Autowired
    private I18nService i18nService;
    
    @Autowired
    private ErrorMonitoringService errorMonitoringService;
    
    @Autowired
    private FileOperationAuditService auditService;

    /**
     * Handle file not found exceptions
     */
    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleFileNotFoundException(
            FileNotFoundException ex, HttpServletRequest request) {
        
        String userId = getCurrentUserId(request);
        String operation = getCurrentOperation(request);
        String filePath = extractFilePathFromException(ex);
        
        logger.warn("File not found: {}", ex.getMessage());
        
        // Record error for monitoring
        errorMonitoringService.recordError(ex, userId, operation, filePath);
        
        ErrorResponse error = ErrorResponse.builder()
            .code("FILE_NOT_FOUND")
            .message(getLocalizedMessage("error.file.not.found", request, filePath))
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .correlationId(generateCorrelationId())
            .details(buildErrorDetails(ex))
            .build();
            
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Handle file permission exceptions
     */
    @ExceptionHandler(FilePermissionException.class)
    public ResponseEntity<ErrorResponse> handleFilePermissionException(
            FilePermissionException ex, HttpServletRequest request) {
        
        String userId = getCurrentUserId(request);
        String operation = getCurrentOperation(request);
        String filePath = ex.getFilePath();
        
        logger.warn("File permission denied: {}", ex.getMessage());
        
        // Record error for monitoring
        errorMonitoringService.recordError(ex, userId, operation, filePath);
        
        // Log security event
        auditService.logPermissionDenied(ex.getUserId(), ex.getOperation(), ex.getFilePath(), ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .code("PERMISSION_DENIED")
            .message(getLocalizedMessage("error.permission.denied", request, filePath))
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .correlationId(generateCorrelationId())
            .details(buildErrorDetails(ex))
            .build();
            
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /**
     * Handle file validation exceptions
     */
    @ExceptionHandler(FileValidationException.class)
    public ResponseEntity<ErrorResponse> handleFileValidationException(
            FileValidationException ex, HttpServletRequest request) {
        
        String userId = getCurrentUserId(request);
        String operation = getCurrentOperation(request);
        String filePath = extractFilePathFromException(ex);
        
        logger.warn("File validation failed: {}", ex.getMessage());
        
        // Record error for monitoring
        errorMonitoringService.recordError(ex, userId, operation, filePath);
        
        Map<String, Object> details = buildErrorDetails(ex);
        details.put("validationErrors", ex.getValidationErrors());
        
        ErrorResponse error = ErrorResponse.builder()
            .code("VALIDATION_ERROR")
            .message(getLocalizedMessage("error.validation.failed", request, ex.getMessage()))
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .correlationId(generateCorrelationId())
            .details(details)
            .build();
            
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handle file security exceptions
     */
    @ExceptionHandler(FileSecurityException.class)
    public ResponseEntity<ErrorResponse> handleFileSecurityException(
            FileSecurityException ex, HttpServletRequest request) {
        
        String userId = getCurrentUserId(request);
        String operation = getCurrentOperation(request);
        String filePath = ex.getFileName();
        
        logger.warn("File security violation: {}", ex.getMessage());
        
        // Record error for monitoring
        errorMonitoringService.recordError(ex, userId, operation, filePath);
        
        // Log security event
        auditService.logSecurityEvent(userId, "SECURITY_THREAT", filePath, ex.getThreatType(), 
            Map.of("threatType", ex.getThreatType(), "fileName", ex.getFileName()));
        
        ErrorResponse error = ErrorResponse.builder()
            .code("SECURITY_VIOLATION")
            .message(getLocalizedMessage("error.security.violation", request, ex.getThreatType()))
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .correlationId(generateCorrelationId())
            .details(buildErrorDetails(ex))
            .build();
            
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /**
     * Handle storage exceptions
     */
    @ExceptionHandler(StorageException.class)
    public ResponseEntity<ErrorResponse> handleStorageException(
            StorageException ex, HttpServletRequest request) {
        
        String userId = getCurrentUserId(request);
        String operation = getCurrentOperation(request);
        String filePath = extractFilePathFromException(ex);
        
        logger.error("Storage operation failed: {}", ex.getMessage(), ex);
        
        // Record error for monitoring
        errorMonitoringService.recordError(ex, userId, operation, filePath);
        
        ErrorResponse error = ErrorResponse.builder()
            .code("STORAGE_ERROR")
            .message(getLocalizedMessage("error.storage.operation", request, ex.getMessage()))
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .correlationId(generateCorrelationId())
            .details(buildErrorDetails(ex))
            .build();
            
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Handle thumbnail exceptions
     */
    @ExceptionHandler(ThumbnailException.class)
    public ResponseEntity<ErrorResponse> handleThumbnailException(
            ThumbnailException ex, HttpServletRequest request) {
        
        String userId = getCurrentUserId(request);
        String operation = getCurrentOperation(request);
        String filePath = ex.getImagePath();
        
        logger.warn("Thumbnail operation failed: {}", ex.getMessage());
        
        // Record error for monitoring
        errorMonitoringService.recordError(ex, userId, operation, filePath);
        
        ErrorResponse error = ErrorResponse.builder()
            .code("THUMBNAIL_ERROR")
            .message(getLocalizedMessage("error.thumbnail.operation", request, ex.getMessage()))
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .correlationId(generateCorrelationId())
            .details(buildErrorDetails(ex))
            .build();
            
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Handle watermark exceptions
     */
    @ExceptionHandler(WatermarkException.class)
    public ResponseEntity<ErrorResponse> handleWatermarkException(
            WatermarkException ex, HttpServletRequest request) {
        
        String userId = getCurrentUserId(request);
        String operation = getCurrentOperation(request);
        String filePath = extractFilePathFromException(ex);
        
        logger.warn("Watermark operation failed: {}", ex.getMessage());
        
        // Record error for monitoring
        errorMonitoringService.recordError(ex, userId, operation, filePath);
        
        ErrorResponse error = ErrorResponse.builder()
            .code("WATERMARK_ERROR")
            .message(getLocalizedMessage("error.watermark.operation", request, ex.getMessage()))
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .correlationId(generateCorrelationId())
            .details(buildErrorDetails(ex))
            .build();
            
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Handle general file manager exceptions
     */
    @ExceptionHandler(FileManagerException.class)
    public ResponseEntity<ErrorResponse> handleFileManagerException(
            FileManagerException ex, HttpServletRequest request) {
        
        String userId = getCurrentUserId(request);
        String operation = getCurrentOperation(request);
        String filePath = ex.getFilePath();
        
        logger.error("File manager operation failed: {}", ex.getFormattedMessage(), ex);
        
        // Record error for monitoring
        errorMonitoringService.recordError(ex, userId, operation, filePath);
        
        ErrorResponse error = ErrorResponse.builder()
            .code("FILE_MANAGER_ERROR")
            .message(getLocalizedMessage("error.file.manager.operation", request, ex.getMessage()))
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .correlationId(generateCorrelationId())
            .details(buildErrorDetails(ex))
            .build();
            
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Handle Shiro authentication exceptions
     */
    @ExceptionHandler(UnauthenticatedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthenticatedException(
            UnauthenticatedException ex, HttpServletRequest request) {
        
        String userId = getCurrentUserId(request);
        String operation = getCurrentOperation(request);
        
        logger.warn("Authentication required: {}", ex.getMessage());
        
        // Record error for monitoring
        errorMonitoringService.recordError(ex, userId, operation, null);
        
        // Log authentication failure
        auditService.logAuthenticationFailure(userId, operation, ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .code("AUTHENTICATION_REQUIRED")
            .message(getLocalizedMessage("error.authentication.required", request))
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .correlationId(generateCorrelationId())
            .details(Map.of("errorType", ex.getClass().getSimpleName()))
            .build();
            
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * Handle Shiro authorization exceptions
     */
    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationException(
            AuthorizationException ex, HttpServletRequest request) {
        
        String userId = getCurrentUserId(request);
        String operation = getCurrentOperation(request);
        
        logger.warn("Authorization failed: {}", ex.getMessage());
        
        // Record error for monitoring
        errorMonitoringService.recordError(ex, userId, operation, null);
        
        // Log permission denied
        auditService.logPermissionDenied(userId, operation, null, ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .code("AUTHORIZATION_FAILED")
            .message(getLocalizedMessage("error.authorization.failed", request))
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .correlationId(generateCorrelationId())
            .details(Map.of("errorType", ex.getClass().getSimpleName()))
            .build();
            
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /**
     * Handle validation exceptions
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        String userId = getCurrentUserId(request);
        String operation = getCurrentOperation(request);
        
        logger.warn("Method argument validation failed: {}", ex.getMessage());
        
        // Record error for monitoring
        errorMonitoringService.recordError(ex, userId, operation, null);
        
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        
        Map<String, Object> details = new HashMap<>();
        details.put("fieldErrors", fieldErrors);
        details.put("errorType", ex.getClass().getSimpleName());
        
        ErrorResponse error = ErrorResponse.builder()
            .code("VALIDATION_ERROR")
            .message(getLocalizedMessage("error.validation.method.argument", request))
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .correlationId(generateCorrelationId())
            .details(details)
            .build();
            
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handle bind exceptions
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(
            BindException ex, HttpServletRequest request) {
        
        String userId = getCurrentUserId(request);
        String operation = getCurrentOperation(request);
        
        logger.warn("Bind validation failed: {}", ex.getMessage());
        
        // Record error for monitoring
        errorMonitoringService.recordError(ex, userId, operation, null);
        
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        
        Map<String, Object> details = new HashMap<>();
        details.put("fieldErrors", fieldErrors);
        details.put("errorType", ex.getClass().getSimpleName());
        
        ErrorResponse error = ErrorResponse.builder()
            .code("VALIDATION_ERROR")
            .message(getLocalizedMessage("error.validation.bind", request))
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .correlationId(generateCorrelationId())
            .details(details)
            .build();
            
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handle constraint violation exceptions
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex, HttpServletRequest request) {
        
        String userId = getCurrentUserId(request);
        String operation = getCurrentOperation(request);
        
        logger.warn("Constraint validation failed: {}", ex.getMessage());
        
        // Record error for monitoring
        errorMonitoringService.recordError(ex, userId, operation, null);
        
        List<String> violations = ex.getConstraintViolations().stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.toList());
        
        Map<String, Object> details = new HashMap<>();
        details.put("violations", violations);
        details.put("errorType", ex.getClass().getSimpleName());
        
        ErrorResponse error = ErrorResponse.builder()
            .code("VALIDATION_ERROR")
            .message(getLocalizedMessage("error.validation.constraint", request))
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .correlationId(generateCorrelationId())
            .details(details)
            .build();
            
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handle missing request parameter exceptions
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        
        String userId = getCurrentUserId(request);
        String operation = getCurrentOperation(request);
        
        logger.warn("Missing request parameter: {}", ex.getMessage());
        
        // Record error for monitoring
        errorMonitoringService.recordError(ex, userId, operation, null);
        
        Map<String, Object> details = new HashMap<>();
        details.put("parameterName", ex.getParameterName());
        details.put("parameterType", ex.getParameterType());
        details.put("errorType", ex.getClass().getSimpleName());
        
        ErrorResponse error = ErrorResponse.builder()
            .code("MISSING_PARAMETER")
            .message(getLocalizedMessage("error.parameter.missing", request, ex.getParameterName()))
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .correlationId(generateCorrelationId())
            .details(details)
            .build();
            
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handle max upload size exceeded exceptions
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex, HttpServletRequest request) {
        
        String userId = getCurrentUserId(request);
        String operation = getCurrentOperation(request);
        
        logger.warn("Upload size exceeded: {}", ex.getMessage());
        
        // Record error for monitoring
        errorMonitoringService.recordError(ex, userId, operation, null);
        
        Map<String, Object> details = new HashMap<>();
        details.put("maxUploadSize", ex.getMaxUploadSize());
        details.put("errorType", ex.getClass().getSimpleName());
        
        ErrorResponse error = ErrorResponse.builder()
            .code("UPLOAD_SIZE_EXCEEDED")
            .message(getLocalizedMessage("error.upload.size.exceeded", request))
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .correlationId(generateCorrelationId())
            .details(details)
            .build();
            
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(error);
    }

    /**
     * Handle file operation exceptions
     */
    @ExceptionHandler(FileOperationException.class)
    public ResponseEntity<ErrorResponse> handleFileOperationException(
            FileOperationException ex, HttpServletRequest request) {
        
        String userId = getCurrentUserId(request);
        String operation = ex.getOperationType().name();
        String filePath = ex.getFilePath();
        
        logger.error("File operation failed: {}", ex.getFormattedMessage(), ex);
        
        // Record error for monitoring
        errorMonitoringService.recordError(ex, userId, operation, filePath);
        
        ErrorResponse error = ErrorResponse.builder()
            .code("FILE_OPERATION_ERROR")
            .message(getLocalizedMessage("error.file.operation", request, operation))
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .correlationId(generateCorrelationId())
            .details(buildErrorDetails(ex))
            .build();
            
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Handle configuration exceptions
     */
    @ExceptionHandler(ConfigurationException.class)
    public ResponseEntity<ErrorResponse> handleConfigurationException(
            ConfigurationException ex, HttpServletRequest request) {
        
        String userId = getCurrentUserId(request);
        String operation = "CONFIGURATION";
        
        logger.error("Configuration error: {}", ex.getFormattedMessage(), ex);
        
        // Record error for monitoring
        errorMonitoringService.recordError(ex, userId, operation, null);
        
        ErrorResponse error = ErrorResponse.builder()
            .code("CONFIGURATION_ERROR")
            .message(getLocalizedMessage("error.configuration", request, ex.getConfigurationKey()))
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .correlationId(generateCorrelationId())
            .details(buildErrorDetails(ex))
            .build();
            
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Handle elFinder exceptions
     */
    @ExceptionHandler(ElFinderException.class)
    public ResponseEntity<ErrorResponse> handleElFinderException(
            ElFinderException ex, HttpServletRequest request) {
        
        String userId = getCurrentUserId(request);
        String operation = "ELFINDER_" + ex.getCommand();
        String filePath = ex.getTarget();
        
        logger.error("elFinder operation failed: {}", ex.getFormattedMessage(), ex);
        
        // Record error for monitoring
        errorMonitoringService.recordError(ex, userId, operation, filePath);
        
        ErrorResponse error = ErrorResponse.builder()
            .code("ELFINDER_ERROR")
            .message(getLocalizedMessage("error.elfinder.operation", request, ex.getCommand()))
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .correlationId(generateCorrelationId())
            .details(buildErrorDetails(ex))
            .build();
            
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        
        String userId = getCurrentUserId(request);
        String operation = getCurrentOperation(request);
        
        logger.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        
        // Record error for monitoring
        errorMonitoringService.recordError(ex, userId, operation, null);
        
        ErrorResponse error = ErrorResponse.builder()
            .code("INTERNAL_ERROR")
            .message(getLocalizedMessage("error.internal", request))
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .correlationId(generateCorrelationId())
            .details(Map.of("errorType", ex.getClass().getSimpleName()))
            .build();
            
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Get localized message
     */
    private String getLocalizedMessage(String key, HttpServletRequest request, Object... args) {
        try {
            Locale locale = getLocale(request);
            return messageSource.getMessage(key, args, key, locale);
        } catch (Exception e) {
            logger.warn("Failed to get localized message for key: {}", key, e);
            return key;
        }
    }

    /**
     * Get locale from request
     */
    private Locale getLocale(HttpServletRequest request) {
        // Try to get locale from request
        Locale locale = request.getLocale();
        if (locale != null) {
            return locale;
        }

        // Try to get from session
        Object sessionLocale = request.getSession().getAttribute("locale");
        if (sessionLocale instanceof Locale) {
            return (Locale) sessionLocale;
        }

        // Default to English
        return Locale.ENGLISH;
    }

    /**
     * Get current user ID from request
     */
    private String getCurrentUserId(HttpServletRequest request) {
        // Try to get from session or security context
        Object userId = request.getSession().getAttribute("userId");
        if (userId != null) {
            return userId.toString();
        }
        
        // Try to get from request parameter
        String userParam = request.getParameter("userId");
        if (userParam != null) {
            return userParam;
        }
        
        return "anonymous";
    }

    /**
     * Get current operation from request
     */
    private String getCurrentOperation(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        
        // Extract operation from URI and method
        if (uri.contains("/upload")) {
            return "UPLOAD";
        } else if (uri.contains("/download")) {
            return "DOWNLOAD";
        } else if (uri.contains("/delete") || "DELETE".equals(method)) {
            return "DELETE";
        } else if (uri.contains("/move")) {
            return "MOVE";
        } else if (uri.contains("/copy")) {
            return "COPY";
        } else if (uri.contains("/connector")) {
            String cmd = request.getParameter("cmd");
            return cmd != null ? "ELFINDER_" + cmd.toUpperCase() : "ELFINDER";
        }
        
        return method;
    }

    /**
     * Extract file path from exception
     */
    private String extractFilePathFromException(Exception ex) {
        if (ex instanceof FileManagerException) {
            FileManagerException fme = (FileManagerException) ex;
            return fme.getFilePath();
        }
        return null;
    }

    /**
     * Build error details from exception
     */
    private Map<String, Object> buildErrorDetails(Exception ex) {
        Map<String, Object> details = new HashMap<>();
        
        if (ex instanceof FileManagerException) {
            FileManagerException fme = (FileManagerException) ex;
            details.put("errorCode", fme.getErrorCode());
            details.put("timestamp", fme.getTimestamp());
            details.putAll(fme.getContext());
            
            if (fme.getUserId() != null) {
                details.put("userId", fme.getUserId());
            }
            if (fme.getOperation() != null) {
                details.put("operation", fme.getOperation());
            }
            if (fme.getFilePath() != null) {
                details.put("filePath", fme.getFilePath());
            }
        }
        
        details.put("exceptionType", ex.getClass().getSimpleName());
        return details;
    }

    /**
     * Generate correlation ID for error tracking
     */
    private String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Error Response Model
     */
    public static class ErrorResponse {
        private String code;
        private String message;
        private LocalDateTime timestamp;
        private String path;
        private String correlationId;
        private Map<String, Object> details;

        // Constructors
        public ErrorResponse() {}

        public ErrorResponse(String code, String message, LocalDateTime timestamp, String path) {
            this.code = code;
            this.message = message;
            this.timestamp = timestamp;
            this.path = path;
        }

        // Builder pattern
        public static ErrorResponseBuilder builder() {
            return new ErrorResponseBuilder();
        }

        // Getters and setters
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }

        public String getCorrelationId() { return correlationId; }
        public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

        public Map<String, Object> getDetails() { return details; }
        public void setDetails(Map<String, Object> details) { this.details = details; }

        // Builder class
        public static class ErrorResponseBuilder {
            private String code;
            private String message;
            private LocalDateTime timestamp;
            private String path;
            private String correlationId;
            private Map<String, Object> details;

            public ErrorResponseBuilder code(String code) {
                this.code = code;
                return this;
            }

            public ErrorResponseBuilder message(String message) {
                this.message = message;
                return this;
            }

            public ErrorResponseBuilder timestamp(LocalDateTime timestamp) {
                this.timestamp = timestamp;
                return this;
            }

            public ErrorResponseBuilder path(String path) {
                this.path = path;
                return this;
            }

            public ErrorResponseBuilder correlationId(String correlationId) {
                this.correlationId = correlationId;
                return this;
            }

            public ErrorResponseBuilder details(Map<String, Object> details) {
                this.details = details;
                return this;
            }

            public ErrorResponse build() {
                ErrorResponse response = new ErrorResponse(code, message, timestamp, path);
                response.setCorrelationId(correlationId);
                response.setDetails(details);
                return response;
            }
        }
    }
}