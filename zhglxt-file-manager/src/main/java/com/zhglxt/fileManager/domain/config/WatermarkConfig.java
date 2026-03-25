package com.zhglxt.fileManager.domain.config;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * Watermark Configuration Model
 * 
 * @author zhglxt
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WatermarkConfig {

    /**
     * Whether watermark is enabled
     */
    private boolean enabled = false;

    /**
     * Watermark text
     */
    @NotBlank(message = "Watermark text cannot be blank")
    private String text = "zhglxt";

    /**
     * Watermark opacity (0.0 to 1.0)
     */
    @DecimalMin(value = "0.0", message = "Opacity must be at least 0.0")
    @DecimalMax(value = "1.0", message = "Opacity must be at most 1.0")
    private float opacity = 0.5f;

    /**
     * Font size for watermark text
     */
    @Positive(message = "Font size must be positive")
    private int fontSize = 24;

    /**
     * Watermark color in hex format
     */
    private String color = "#FFFFFF";

    /**
     * Watermark position
     */
    private WatermarkPosition position = WatermarkPosition.BOTTOM_RIGHT;

    /**
     * Font family for watermark text
     */
    private String fontFamily = "Arial";

    /**
     * Font style (PLAIN, BOLD, ITALIC, BOLD_ITALIC)
     */
    private FontStyle fontStyle = FontStyle.PLAIN;

    /**
     * Margin from edges in pixels
     */
    @Positive(message = "Margin must be positive")
    private int margin = 10;

    /**
     * Rotation angle in degrees
     */
    private int rotation = 0;

    /**
     * Whether to apply watermark to thumbnails
     */
    private boolean applyToThumbnails = false;

    /**
     * Minimum image width to apply watermark
     */
    @Positive(message = "Min width must be positive")
    private int minImageWidth = 200;

    /**
     * Minimum image height to apply watermark
     */
    @Positive(message = "Min height must be positive")
    private int minImageHeight = 200;

    /**
     * Image path for image watermark (alternative to text)
     */
    private String imagePath;

    /**
     * Scale factor for image watermark (0.0 to 1.0)
     */
    @DecimalMin(value = "0.0", message = "Scale factor must be at least 0.0")
    @DecimalMax(value = "1.0", message = "Scale factor must be at most 1.0")
    private float imageScale = 0.2f;

    // Constructors
    public WatermarkConfig() {
    }

    public WatermarkConfig(boolean enabled, String text, float opacity, WatermarkPosition position) {
        this.enabled = enabled;
        this.text = text;
        this.opacity = opacity;
        this.position = position;
    }

    // Getters and Setters
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public float getOpacity() {
        return opacity;
    }

    public void setOpacity(float opacity) {
        this.opacity = opacity;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public WatermarkPosition getPosition() {
        return position;
    }

    public void setPosition(WatermarkPosition position) {
        this.position = position;
    }

    public String getFontFamily() {
        return fontFamily;
    }

    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
    }

    public FontStyle getFontStyle() {
        return fontStyle;
    }

    public void setFontStyle(FontStyle fontStyle) {
        this.fontStyle = fontStyle;
    }

    public int getMargin() {
        return margin;
    }

    public void setMargin(int margin) {
        this.margin = margin;
    }

    public int getRotation() {
        return rotation;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

    public boolean isApplyToThumbnails() {
        return applyToThumbnails;
    }

    public void setApplyToThumbnails(boolean applyToThumbnails) {
        this.applyToThumbnails = applyToThumbnails;
    }

    public int getMinImageWidth() {
        return minImageWidth;
    }

    public void setMinImageWidth(int minImageWidth) {
        this.minImageWidth = minImageWidth;
    }

    public int getMinImageHeight() {
        return minImageHeight;
    }

    public void setMinImageHeight(int minImageHeight) {
        this.minImageHeight = minImageHeight;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public float getImageScale() {
        return imageScale;
    }

    public void setImageScale(float imageScale) {
        this.imageScale = imageScale;
    }

    /**
     * Check if using image watermark
     */
    public boolean isImageWatermark() {
        return imagePath != null && !imagePath.trim().isEmpty();
    }

    /**
     * Check if using text watermark
     */
    public boolean isTextWatermark() {
        return !isImageWatermark() && text != null && !text.trim().isEmpty();
    }

    /**
     * Watermark Position Enum
     */
    public enum WatermarkPosition {
        TOP_LEFT, TOP_CENTER, TOP_RIGHT,
        MIDDLE_LEFT, MIDDLE_CENTER, MIDDLE_RIGHT,
        BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT
    }

    /**
     * Font Style Enum
     */
    public enum FontStyle {
        PLAIN(0),
        BOLD(1),
        ITALIC(2),
        BOLD_ITALIC(3);

        private final int value;

        FontStyle(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    @Override
    public String toString() {
        return "WatermarkConfig{" +
                "enabled=" + enabled +
                ", text='" + text + '\'' +
                ", opacity=" + opacity +
                ", fontSize=" + fontSize +
                ", position=" + position +
                '}';
    }
}