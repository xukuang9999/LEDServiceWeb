package com.zhglxt.fileManager.exception;

/**
 * Watermark Exception
 * 
 * Thrown when watermark processing operations fail.
 * 
 * @author zhglxt
 */
public class WatermarkException extends FileManagerException {

    /**
     * Constructs a new WatermarkException with the specified detail message.
     * 
     * @param message the detail message
     */
    public WatermarkException(String message) {
        super(message);
    }

    /**
     * Constructs a new WatermarkException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause
     */
    public WatermarkException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new WatermarkException with the specified cause.
     * 
     * @param cause the cause
     */
    public WatermarkException(Throwable cause) {
        super(cause);
    }
}