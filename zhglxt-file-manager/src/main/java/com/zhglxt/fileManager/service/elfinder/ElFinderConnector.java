package com.zhglxt.fileManager.service.elfinder;

import com.zhglxt.fileManager.domain.FileInfo;
import com.zhglxt.fileManager.domain.FileUploadResult;
import com.zhglxt.fileManager.domain.elfinder.ElFinderFile;
import com.zhglxt.fileManager.domain.elfinder.ElFinderRequest;
import com.zhglxt.fileManager.domain.elfinder.ElFinderResponse;
import com.zhglxt.fileManager.service.FileManagerService;
import com.zhglxt.fileManager.service.elfinder.command.ElFinderCommandExecutor;
import com.zhglxt.fileManager.service.elfinder.util.ElFinderHashUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import com.zhglxt.fileManager.service.I18nService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Locale;

/**
 * elFinder Connector Implementation
 * Main connector that processes elFinder protocol requests
 * 
 * @author zhglxt
 */
@Service
public class ElFinderConnector {

    private static final Logger logger = LoggerFactory.getLogger(ElFinderConnector.class);

    @Autowired
    private FileManagerService fileManagerService;

    @Autowired
    private ElFinderCommandExecutor commandExecutor;

    @Autowired
    private ElFinderHashUtil hashUtil;

    @Autowired
    @Qualifier("fileManagerMessageSource")
    private MessageSource messageSource;
    
    @Autowired
    private I18nService i18nService;

    /**
     * Process elFinder command request
     * 
     * @param request elFinder request
     * @param httpRequest HTTP servlet request
     * @return elFinder response
     */
    public ElFinderResponse processCommand(ElFinderRequest request, HttpServletRequest httpRequest) {
        long startTime = System.currentTimeMillis();
        
        try {
            logger.info("Processing elFinder command: {} with target: {}", 
                       request.getCmd(), request.getTarget());

            // Validate request
            if (!StringUtils.hasText(request.getCmd())) {
                return createErrorResponse("error.command.missing", httpRequest);
            }

            // Get user ID from session/security context
            String userId = getCurrentUserId(httpRequest);
            if (!StringUtils.hasText(userId)) {
                return createErrorResponse("error.authentication.required", httpRequest);
            }

            // Execute command
            ElFinderResponse response = commandExecutor.execute(request, userId, httpRequest);
            
            // Add debug information if needed
            if (logger.isDebugEnabled()) {
                long duration = System.currentTimeMillis() - startTime;
                response.addDebug("executionTime", duration + "ms");
                response.addDebug("command", request.getCmd());
                response.addDebug("userId", userId);
            }

            logger.info("elFinder command completed: {} ({}ms)", 
                       request.getCmd(), System.currentTimeMillis() - startTime);
            
            return response;

        } catch (Exception e) {
            logger.error("Error processing elFinder command: {}", e.getMessage(), e);
            return createErrorResponse("error.command.execution", httpRequest, e.getMessage());
        }
    }

    /**
     * Initialize elFinder connector
     * 
     * @param httpRequest HTTP servlet request
     * @return initialization response
     */
    public ElFinderResponse initialize(HttpServletRequest httpRequest) {
        try {
            logger.info("Initializing elFinder connector");

            String userId = getCurrentUserId(httpRequest);
            if (!StringUtils.hasText(userId)) {
                return createErrorResponse("error.authentication.required", httpRequest);
            }

            // Create initialization request
            ElFinderRequest initRequest = new ElFinderRequest("open");
            initRequest.setInit(true);
            initRequest.setTree(true);

            // Process initialization
            return processCommand(initRequest, httpRequest);

        } catch (Exception e) {
            logger.error("Error initializing elFinder connector: {}", e.getMessage(), e);
            return createErrorResponse("error.initialization.failed", httpRequest, e.getMessage());
        }
    }

    /**
     * Convert FileInfo to ElFinderFile
     * 
     * @param fileInfo file information
     * @param parentHash parent directory hash
     * @return elFinder file
     */
    public ElFinderFile convertToElFinderFile(FileInfo fileInfo, String parentHash) {
        try {
            String hash = hashUtil.encode(fileInfo.getPath());
            
            ElFinderFile elFile = new ElFinderFile(hash, fileInfo.getName(), fileInfo.isDirectory());
            elFile.setParentHash(parentHash);
            elFile.setSize(fileInfo.getSize());
            elFile.setTimestamp(fileInfo.getLastModified().toEpochSecond(java.time.ZoneOffset.UTC));
            elFile.setMime(fileInfo.getMimeType());
            
            // Set permissions based on file info
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
    public List<ElFinderFile> convertToElFinderFiles(List<FileInfo> fileInfos, String parentHash) {
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
    public ElFinderResponse createErrorResponse(String messageKey, HttpServletRequest httpRequest, Object... args) {
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
    public ElFinderResponse createSuccessResponse() {
        return ElFinderResponse.success();
    }

    /**
     * Get current user ID from request
     * 
     * @param httpRequest HTTP servlet request
     * @return user ID
     */
    private String getCurrentUserId(HttpServletRequest httpRequest) {
        // Try to get user ID from session
        Object userId = httpRequest.getSession().getAttribute("userId");
        if (userId != null) {
            return userId.toString();
        }

        // Try to get from security context (Shiro)
        try {
            org.apache.shiro.subject.Subject subject = org.apache.shiro.SecurityUtils.getSubject();
            if (subject != null && subject.isAuthenticated()) {
                Object principal = subject.getPrincipal();
                if (principal != null) {
                    return principal.toString();
                }
            }
        } catch (Exception e) {
            logger.debug("Could not get user from Shiro security context: {}", e.getMessage());
        }

        // Fallback to request parameter or header
        String userIdParam = httpRequest.getParameter("userId");
        if (StringUtils.hasText(userIdParam)) {
            return userIdParam;
        }

        String userIdHeader = httpRequest.getHeader("X-User-Id");
        if (StringUtils.hasText(userIdHeader)) {
            return userIdHeader;
        }

        // Default user for development/testing
        logger.warn("No user ID found in request, using default user");
        return "anonymous";
    }

    /**
     * Get locale from request
     * 
     * @param httpRequest HTTP servlet request
     * @return locale
     */
    private Locale getLocale(HttpServletRequest httpRequest) {
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
     * Validate elFinder request
     * 
     * @param request elFinder request
     * @return validation result
     */
    public boolean isValidRequest(ElFinderRequest request) {
        if (request == null) {
            return false;
        }

        if (!StringUtils.hasText(request.getCmd())) {
            return false;
        }

        // Additional validation based on command type
        String cmd = request.getCmd();
        switch (cmd) {
            case "open":
            case "tree":
            case "parents":
                // These commands can work without target
                return true;
            case "file":
            case "get":
            case "put":
            case "rm":
            case "rename":
            case "duplicate":
                // These commands require target
                return StringUtils.hasText(request.getTarget());
            case "paste":
                // Paste requires targets and destination
                return request.getTargets() != null && !request.getTargets().isEmpty() &&
                       StringUtils.hasText(request.getDst());
            case "upload":
                // Upload requires destination
                return StringUtils.hasText(request.getTarget()) || StringUtils.hasText(request.getDst());
            case "mkdir":
                // Mkdir requires parent target and name
                return StringUtils.hasText(request.getTarget()) && StringUtils.hasText(request.getName());
            case "search":
                // Search requires query
                return StringUtils.hasText(request.getQ());
            default:
                return true;
        }
    }

    /**
     * Log request for debugging
     * 
     * @param request elFinder request
     * @param httpRequest HTTP servlet request
     */
    public void logRequest(ElFinderRequest request, HttpServletRequest httpRequest) {
        if (logger.isDebugEnabled()) {
            logger.debug("elFinder Request - Command: {}, Target: {}, Targets: {}, " +
                        "Dst: {}, Name: {}, Query: {}, Init: {}, Tree: {}",
                        request.getCmd(), request.getTarget(), request.getTargets(),
                        request.getDst(), request.getName(), request.getQ(),
                        request.isInit(), request.isTree());
            
            logger.debug("HTTP Request - Method: {}, URI: {}, User-Agent: {}",
                        httpRequest.getMethod(), httpRequest.getRequestURI(),
                        httpRequest.getHeader("User-Agent"));
        }
    }
}