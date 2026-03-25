package com.zhglxt.fileManager.service.elfinder.command;

import com.zhglxt.fileManager.domain.FileInfo;
import com.zhglxt.fileManager.domain.elfinder.ElFinderFile;
import com.zhglxt.fileManager.domain.elfinder.ElFinderRequest;
import com.zhglxt.fileManager.domain.elfinder.ElFinderResponse;
import com.zhglxt.fileManager.service.FileManagerService;
import com.zhglxt.fileManager.service.elfinder.util.ElFinderHashUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import com.zhglxt.fileManager.service.I18nService;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Locale;

/**
 * Abstract base class for elFinder commands
 * Provides common functionality for all command implementations
 * 
 * @author zhglxt
 */
public abstract class AbstractElFinderCommand implements ElFinderCommand {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected FileManagerService fileManagerService;

    @Autowired
    protected ElFinderHashUtil hashUtil;

    @Autowired
    @Qualifier("fileManagerMessageSource")
    protected MessageSource messageSource;
    
    @Autowired
    protected I18nService i18nService;

    /**
     * Convert FileInfo to ElFinderFile
     * 
     * @param fileInfo file information
     * @param parentHash parent directory hash
     * @return elFinder file
     */
    protected ElFinderFile convertToElFinderFile(FileInfo fileInfo, String parentHash) {
        try {
            String hash = hashUtil.encode(fileInfo.getPath());
            
            ElFinderFile elFile = new ElFinderFile(hash, fileInfo.getName(), fileInfo.isDirectory());
            elFile.setParentHash(parentHash);
            elFile.setSize(fileInfo.getSize());
            elFile.setTimestamp(fileInfo.getLastModified().toEpochSecond(java.time.ZoneOffset.UTC));
            elFile.setMime(fileInfo.getMimeType());
            
            // Set permissions
            elFile.setReadable(fileInfo.isReadable());
            elFile.setWritable(fileInfo.isWritable());
            elFile.setLocked(false); // Default to not locked
            
            // Set thumbnail if available
            if (StringUtils.hasText(fileInfo.getThumbnailPath())) {
                elFile.setThumbnail(fileInfo.getThumbnailPath());
            }
            
            // Set public URL if available
            if (StringUtils.hasText(fileInfo.getPublicUrl())) {
                elFile.setUrl(fileInfo.getPublicUrl());
            }
            
            // Set dimensions for images
            if (fileInfo.isImage() && fileInfo.getMetadata() != null) {
                Object width = fileInfo.getMetadata().get("width");
                Object height = fileInfo.getMetadata().get("height");
                if (width != null && height != null) {
                    elFile.setDimensions(width + "x" + height);
                }
            }
            
            // Set subdirectory indicator for directories
            if (fileInfo.isDirectory()) {
                elFile.setHasSubdirectories(0); // Will be updated by tree command if needed
            }
            
            return elFile;

        } catch (Exception e) {
            logger.error("Error converting FileInfo to ElFinderFile: {}", e.getMessage(), e);
            // Return a basic file representation
            String hash = hashUtil.encode(fileInfo.getPath());
            return new ElFinderFile(hash, fileInfo.getName(), fileInfo.isDirectory());
        }
    }

    /**
     * Convert list of FileInfo to list of ElFinderFile
     * 
     * @param fileInfos list of file information
     * @param parentHash parent directory hash
     * @return list of elFinder files
     */
    protected List<ElFinderFile> convertToElFinderFiles(List<FileInfo> fileInfos, String parentHash) {
        return fileInfos.stream()
            .map(fileInfo -> convertToElFinderFile(fileInfo, parentHash))
            .toList();
    }

    /**
     * Create error response with localized message
     * 
     * @param messageKey message key for localization
     * @param httpRequest HTTP request for locale detection
     * @param args message arguments
     * @return error response
     */
    protected ElFinderResponse createErrorResponse(String messageKey, HttpServletRequest httpRequest, Object... args) {
        try {
            Locale locale = getLocale(httpRequest);
            String message = messageSource.getMessage(messageKey, args, messageKey, locale);
            return ElFinderResponse.error(message);
        } catch (Exception e) {
            logger.warn("Error creating localized error message for key: {}", messageKey, e);
            return ElFinderResponse.error("An error occurred: " + messageKey);
        }
    }

    /**
     * Create success response
     * 
     * @return success response
     */
    protected ElFinderResponse createSuccessResponse() {
        return ElFinderResponse.success();
    }

    /**
     * Get locale from request
     * 
     * @param httpRequest HTTP servlet request
     * @return locale
     */
    protected Locale getLocale(HttpServletRequest httpRequest) {
        // Try to get locale from request
        Locale locale = httpRequest.getLocale();
        if (locale != null) {
            return locale;
        }

        // Try to get from session
        Object sessionLocale = httpRequest.getSession().getAttribute("locale");
        if (sessionLocale instanceof Locale) {
            return (Locale) sessionLocale;
        }

        // Default to English
        return Locale.ENGLISH;
    }

    /**
     * Decode hash to file path
     * 
     * @param hash encoded hash
     * @return file path
     */
    protected String decodePath(String hash) {
        if (!StringUtils.hasText(hash)) {
            return "";
        }
        return hashUtil.decode(hash);
    }

    /**
     * Encode file path to hash
     * 
     * @param path file path
     * @return encoded hash
     */
    protected String encodePath(String path) {
        if (!StringUtils.hasText(path)) {
            return hashUtil.encode("");
        }
        return hashUtil.encode(path);
    }

    /**
     * Get parent directory path
     * 
     * @param filePath file path
     * @return parent directory path
     */
    protected String getParentPath(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return "";
        }
        
        int lastSlash = filePath.lastIndexOf('/');
        if (lastSlash == -1) {
            return "";
        }
        
        return filePath.substring(0, lastSlash);
    }

    /**
     * Get file name from path
     * 
     * @param filePath file path
     * @return file name
     */
    protected String getFileName(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return "";
        }
        
        int lastSlash = filePath.lastIndexOf('/');
        if (lastSlash == -1) {
            return filePath;
        }
        
        return filePath.substring(lastSlash + 1);
    }

    /**
     * Check if path is root
     * 
     * @param path file path
     * @return true if root
     */
    protected boolean isRoot(String path) {
        return !StringUtils.hasText(path) || "/".equals(path);
    }

    /**
     * Normalize path
     * 
     * @param path file path
     * @return normalized path
     */
    protected String normalizePath(String path) {
        if (!StringUtils.hasText(path)) {
            return "";
        }
        
        // Remove leading/trailing slashes and normalize
        path = path.trim();
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        
        // Normalize path separators
        path = path.replace("\\", "/");
        
        return path;
    }
}