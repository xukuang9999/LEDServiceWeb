package com.zhglxt.fileManager.exception;

/**
 * Thumbnail Exception
 * Thrown when thumbnail operations fail
 * 
 * @author zhglxt
 */
public class ThumbnailException extends FileManagerException {

    /**
     * The image path that caused the exception
     */
    private final String imagePath;

    /**
     * The thumbnail dimensions
     */
    private final String dimensions;

    public ThumbnailException(String message) {
        super(message);
        this.imagePath = null;
        this.dimensions = null;
    }

    public ThumbnailException(String message, Throwable cause) {
        super(message, cause);
        this.imagePath = null;
        this.dimensions = null;
    }

    public ThumbnailException(String message, String imagePath) {
        super(message);
        this.imagePath = imagePath;
        this.dimensions = null;
    }

    public ThumbnailException(String message, String imagePath, String dimensions) {
        super(message);
        this.imagePath = imagePath;
        this.dimensions = dimensions;
    }

    public ThumbnailException(String message, String imagePath, String dimensions, Throwable cause) {
        super(message, cause);
        this.imagePath = imagePath;
        this.dimensions = dimensions;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getDimensions() {
        return dimensions;
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder(super.getMessage());
        
        if (imagePath != null) {
            sb.append(" [Image: ").append(imagePath).append("]");
        }
        
        if (dimensions != null) {
            sb.append(" [Dimensions: ").append(dimensions).append("]");
        }
        
        return sb.toString();
    }
}