package com.zhglxt.fileManager.service.impl;

import com.zhglxt.fileManager.config.FileManagerProperties;
import com.zhglxt.fileManager.domain.config.WatermarkConfig;
import com.zhglxt.fileManager.exception.WatermarkException;
import com.zhglxt.fileManager.service.WatermarkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * Watermark Service Implementation
 * 
 * Provides image watermarking functionality with configurable text watermarks,
 * opacity, positioning, and conditional watermarking based on file types.
 * 
 * @author zhglxt
 */
@Service
public class WatermarkServiceImpl implements WatermarkService {

    private static final Logger logger = LoggerFactory.getLogger(WatermarkServiceImpl.class);

    /**
     * Supported image formats for watermarking
     */
    private static final Set<String> SUPPORTED_FORMATS = Set.of(
        "jpg", "jpeg", "png", "gif", "bmp", "webp"
    );

    /**
     * Default image format for output
     */
    private static final String DEFAULT_OUTPUT_FORMAT = "jpg";

    @Autowired
    private FileManagerProperties fileManagerProperties;

    @Override
    public InputStream applyWatermark(InputStream imageInputStream, String fileName) {
        return applyWatermark(imageInputStream, fileName, getWatermarkConfig());
    }

    @Override
    public InputStream applyWatermark(InputStream imageInputStream, String fileName, WatermarkConfig config) {
        if (!isWatermarkEnabled() || config == null || !config.isEnabled()) {
            logger.debug("Watermarking is disabled, returning original image");
            return imageInputStream;
        }

        if (!supportsWatermarking(fileName)) {
            logger.debug("File type not supported for watermarking: {}", fileName);
            return imageInputStream;
        }

        validateConfig(config);

        try {
            // Read the original image
            BufferedImage originalImage = ImageIO.read(imageInputStream);
            if (originalImage == null) {
                throw new WatermarkException("Failed to read image: " + fileName);
            }

            // Check minimum size requirements
            if (!meetsMinimumSize(originalImage, config)) {
                logger.debug("Image does not meet minimum size requirements: {}x{}", 
                    originalImage.getWidth(), originalImage.getHeight());
                return convertImageToInputStream(originalImage, getOutputFormat(fileName));
            }

            // Apply watermark
            BufferedImage watermarkedImage = processWatermark(originalImage, config);

            // Convert back to InputStream
            return convertImageToInputStream(watermarkedImage, getOutputFormat(fileName));

        } catch (IOException e) {
            throw new WatermarkException("Failed to process watermark for file: " + fileName, e);
        }
    }

    @Override
    public boolean isWatermarkEnabled() {
        return fileManagerProperties.getWatermark().isEnabled();
    }

    @Override
    public WatermarkConfig getWatermarkConfig() {
        return fileManagerProperties.getWatermark();
    }

    @Override
    public boolean supportsWatermarking(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return false;
        }

        String extension = getFileExtension(fileName).toLowerCase();
        return SUPPORTED_FORMATS.contains(extension);
    }

    @Override
    public boolean meetsMinimumSize(InputStream imageInputStream, String fileName) {
        try {
            BufferedImage image = ImageIO.read(imageInputStream);
            if (image == null) {
                return false;
            }
            return meetsMinimumSize(image, getWatermarkConfig());
        } catch (IOException e) {
            logger.warn("Failed to read image for size check: {}", fileName, e);
            return false;
        }
    }

    @Override
    public void validateConfig(WatermarkConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Watermark configuration cannot be null");
        }

        if (config.isEnabled()) {
            if (config.isTextWatermark() && !StringUtils.hasText(config.getText())) {
                throw new IllegalArgumentException("Watermark text cannot be empty when text watermark is enabled");
            }

            if (config.isImageWatermark() && !StringUtils.hasText(config.getImagePath())) {
                throw new IllegalArgumentException("Watermark image path cannot be empty when image watermark is enabled");
            }

            if (config.getOpacity() < 0.0f || config.getOpacity() > 1.0f) {
                throw new IllegalArgumentException("Watermark opacity must be between 0.0 and 1.0");
            }

            if (config.getFontSize() <= 0) {
                throw new IllegalArgumentException("Font size must be positive");
            }

            if (config.getMargin() < 0) {
                throw new IllegalArgumentException("Margin cannot be negative");
            }
        }
    }

    /**
     * Process watermark on the image
     */
    private BufferedImage processWatermark(BufferedImage originalImage, WatermarkConfig config) {
        // Create a copy of the original image
        BufferedImage watermarkedImage = new BufferedImage(
            originalImage.getWidth(), 
            originalImage.getHeight(), 
            BufferedImage.TYPE_INT_RGB
        );

        Graphics2D g2d = watermarkedImage.createGraphics();
        try {
            // Draw the original image
            g2d.drawImage(originalImage, 0, 0, null);

            // Configure rendering hints for better quality
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            if (config.isTextWatermark()) {
                applyTextWatermark(g2d, originalImage, config);
            } else if (config.isImageWatermark()) {
                applyImageWatermark(g2d, originalImage, config);
            }

        } finally {
            g2d.dispose();
        }

        return watermarkedImage;
    }

    /**
     * Apply text watermark
     */
    private void applyTextWatermark(Graphics2D g2d, BufferedImage image, WatermarkConfig config) {
        // Set font
        Font font = new Font(
            config.getFontFamily(), 
            config.getFontStyle().getValue(), 
            config.getFontSize()
        );
        g2d.setFont(font);

        // Set color with opacity
        Color color = parseColor(config.getColor());
        g2d.setColor(new Color(
            color.getRed(), 
            color.getGreen(), 
            color.getBlue(), 
            (int) (255 * config.getOpacity())
        ));

        // Calculate text dimensions
        FontMetrics fontMetrics = g2d.getFontMetrics();
        String text = config.getText();
        int textWidth = fontMetrics.stringWidth(text);
        int textHeight = fontMetrics.getHeight();

        // Calculate position
        Point position = calculateTextPosition(
            image.getWidth(), 
            image.getHeight(), 
            textWidth, 
            textHeight, 
            config.getPosition(), 
            config.getMargin()
        );

        // Apply rotation if specified
        if (config.getRotation() != 0) {
            g2d.rotate(Math.toRadians(config.getRotation()), 
                position.x + textWidth / 2.0, 
                position.y + textHeight / 2.0);
        }

        // Draw the text
        g2d.drawString(text, position.x, position.y + fontMetrics.getAscent());
    }

    /**
     * Apply image watermark
     */
    private void applyImageWatermark(Graphics2D g2d, BufferedImage image, WatermarkConfig config) {
        try {
            // Load watermark image
            BufferedImage watermarkImage = ImageIO.read(new java.io.File(config.getImagePath()));
            if (watermarkImage == null) {
                logger.warn("Failed to load watermark image: {}", config.getImagePath());
                return;
            }

            // Scale watermark image
            int scaledWidth = (int) (watermarkImage.getWidth() * config.getImageScale());
            int scaledHeight = (int) (watermarkImage.getHeight() * config.getImageScale());

            // Calculate position
            Point position = calculateImagePosition(
                image.getWidth(), 
                image.getHeight(), 
                scaledWidth, 
                scaledHeight, 
                config.getPosition(), 
                config.getMargin()
            );

            // Set opacity
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, config.getOpacity()));

            // Draw the watermark image
            g2d.drawImage(watermarkImage, position.x, position.y, scaledWidth, scaledHeight, null);

        } catch (IOException e) {
            logger.error("Failed to apply image watermark: {}", config.getImagePath(), e);
        }
    }

    /**
     * Calculate text position based on watermark position setting
     */
    private Point calculateTextPosition(int imageWidth, int imageHeight, int textWidth, int textHeight, 
                                      WatermarkConfig.WatermarkPosition position, int margin) {
        int x, y;

        switch (position) {
            case TOP_LEFT:
                x = margin;
                y = margin;
                break;
            case TOP_CENTER:
                x = (imageWidth - textWidth) / 2;
                y = margin;
                break;
            case TOP_RIGHT:
                x = imageWidth - textWidth - margin;
                y = margin;
                break;
            case MIDDLE_LEFT:
                x = margin;
                y = (imageHeight - textHeight) / 2;
                break;
            case MIDDLE_CENTER:
                x = (imageWidth - textWidth) / 2;
                y = (imageHeight - textHeight) / 2;
                break;
            case MIDDLE_RIGHT:
                x = imageWidth - textWidth - margin;
                y = (imageHeight - textHeight) / 2;
                break;
            case BOTTOM_LEFT:
                x = margin;
                y = imageHeight - textHeight - margin;
                break;
            case BOTTOM_CENTER:
                x = (imageWidth - textWidth) / 2;
                y = imageHeight - textHeight - margin;
                break;
            case BOTTOM_RIGHT:
            default:
                x = imageWidth - textWidth - margin;
                y = imageHeight - textHeight - margin;
                break;
        }

        return new Point(x, y);
    }

    /**
     * Calculate image position based on watermark position setting
     */
    private Point calculateImagePosition(int imageWidth, int imageHeight, int watermarkWidth, int watermarkHeight, 
                                       WatermarkConfig.WatermarkPosition position, int margin) {
        int x, y;

        switch (position) {
            case TOP_LEFT:
                x = margin;
                y = margin;
                break;
            case TOP_CENTER:
                x = (imageWidth - watermarkWidth) / 2;
                y = margin;
                break;
            case TOP_RIGHT:
                x = imageWidth - watermarkWidth - margin;
                y = margin;
                break;
            case MIDDLE_LEFT:
                x = margin;
                y = (imageHeight - watermarkHeight) / 2;
                break;
            case MIDDLE_CENTER:
                x = (imageWidth - watermarkWidth) / 2;
                y = (imageHeight - watermarkHeight) / 2;
                break;
            case MIDDLE_RIGHT:
                x = imageWidth - watermarkWidth - margin;
                y = (imageHeight - watermarkHeight) / 2;
                break;
            case BOTTOM_LEFT:
                x = margin;
                y = imageHeight - watermarkHeight - margin;
                break;
            case BOTTOM_CENTER:
                x = (imageWidth - watermarkWidth) / 2;
                y = imageHeight - watermarkHeight - margin;
                break;
            case BOTTOM_RIGHT:
            default:
                x = imageWidth - watermarkWidth - margin;
                y = imageHeight - watermarkHeight - margin;
                break;
        }

        return new Point(x, y);
    }

    /**
     * Check if image meets minimum size requirements
     */
    private boolean meetsMinimumSize(BufferedImage image, WatermarkConfig config) {
        return image.getWidth() >= config.getMinImageWidth() && 
               image.getHeight() >= config.getMinImageHeight();
    }

    /**
     * Parse color string to Color object
     */
    private Color parseColor(String colorStr) {
        try {
            if (colorStr.startsWith("#")) {
                return Color.decode(colorStr);
            } else {
                return Color.decode("#" + colorStr);
            }
        } catch (NumberFormatException e) {
            logger.warn("Invalid color format: {}, using white as default", colorStr);
            return Color.WHITE;
        }
    }

    /**
     * Get file extension from filename
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1);
        }
        return "";
    }

    /**
     * Get output format based on input filename
     */
    private String getOutputFormat(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();
        return SUPPORTED_FORMATS.contains(extension) ? extension : DEFAULT_OUTPUT_FORMAT;
    }

    /**
     * Convert BufferedImage to InputStream
     */
    private InputStream convertImageToInputStream(BufferedImage image, String format) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, format, baos);
        return new ByteArrayInputStream(baos.toByteArray());
    }
}