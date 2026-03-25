package com.zhglxt.fileManager.controller;

import com.zhglxt.fileManager.domain.elfinder.ElFinderRequest;
import com.zhglxt.fileManager.domain.elfinder.ElFinderResponse;
import com.zhglxt.fileManager.service.elfinder.ElFinderConnector;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * elFinder Connector Controller
 * Handles elFinder protocol requests
 * 
 * @author zhglxt
 */
@RestController
@RequestMapping("/api/file-manager/connector")
@RequiresAuthentication
public class ElFinderController {

    private static final Logger logger = LoggerFactory.getLogger(ElFinderController.class);

    @Autowired
    private ElFinderConnector elFinderConnector;

    /**
     * Handle elFinder connector requests (GET)
     * 
     * @param cmd command
     * @param target target hash
     * @param targets target hashes (for multi-target operations)
     * @param dst destination hash
     * @param name name parameter
     * @param dirs directory names
     * @param q search query
     * @param mimes MIME type filter
     * @param tree tree flag
     * @param init initialization flag
     * @param width image width
     * @param height image height
     * @param quality image quality
     * @param bg background color
     * @param degree rotation degree
     * @param content text content
     * @param encoding text encoding
     * @param allParams all request parameters
     * @param request HTTP request
     * @return elFinder response
     */
    @GetMapping
    @RequiresPermissions("file:read")
    public ResponseEntity<ElFinderResponse> handleGet(
            @RequestParam(value = "cmd", required = false) String cmd,
            @RequestParam(value = "target", required = false) String target,
            @RequestParam(value = "targets[]", required = false) String[] targets,
            @RequestParam(value = "dst", required = false) String dst,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "dirs[]", required = false) String[] dirs,
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "mimes[]", required = false) String[] mimes,
            @RequestParam(value = "tree", required = false, defaultValue = "false") boolean tree,
            @RequestParam(value = "init", required = false, defaultValue = "false") boolean init,
            @RequestParam(value = "width", required = false) Integer width,
            @RequestParam(value = "height", required = false) Integer height,
            @RequestParam(value = "quality", required = false) Float quality,
            @RequestParam(value = "bg", required = false) String bg,
            @RequestParam(value = "degree", required = false) Integer degree,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "encoding", required = false) String encoding,
            @RequestParam Map<String, String> allParams,
            HttpServletRequest request) {

        try {
            // Create elFinder request
            ElFinderRequest elFinderRequest = createElFinderRequest(
                cmd, target, targets, dst, name, dirs, q, mimes, tree, init,
                width, height, quality, bg, degree, content, encoding, allParams, null);

            // Log request for debugging
            elFinderConnector.logRequest(elFinderRequest, request);

            // Validate request
            if (!elFinderConnector.isValidRequest(elFinderRequest)) {
                logger.warn("Invalid elFinder request: {}", elFinderRequest);
                return ResponseEntity.badRequest()
                    .body(elFinderConnector.createErrorResponse("error.request.invalid", request));
            }

            // Process request
            ElFinderResponse response = elFinderConnector.processCommand(elFinderRequest, request);

            // Return response with appropriate status
            HttpStatus status = response.hasErrors() ? HttpStatus.BAD_REQUEST : HttpStatus.OK;
            return ResponseEntity.status(status).body(response);

        } catch (Exception e) {
            logger.error("Error handling elFinder GET request: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(elFinderConnector.createErrorResponse("error.request.processing", request, e.getMessage()));
        }
    }

    /**
     * Handle elFinder connector requests (POST)
     * 
     * @param cmd command
     * @param target target hash
     * @param targets target hashes (for multi-target operations)
     * @param dst destination hash
     * @param name name parameter
     * @param dirs directory names
     * @param q search query
     * @param mimes MIME type filter
     * @param tree tree flag
     * @param init initialization flag
     * @param width image width
     * @param height image height
     * @param quality image quality
     * @param bg background color
     * @param degree rotation degree
     * @param content text content
     * @param encoding text encoding
     * @param upload uploaded files
     * @param allParams all request parameters
     * @param request HTTP request
     * @return elFinder response
     */
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    @RequiresPermissions("file:write")
    public ResponseEntity<ElFinderResponse> handlePost(
            @RequestParam(value = "cmd", required = false) String cmd,
            @RequestParam(value = "target", required = false) String target,
            @RequestParam(value = "targets[]", required = false) String[] targets,
            @RequestParam(value = "dst", required = false) String dst,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "dirs[]", required = false) String[] dirs,
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "mimes[]", required = false) String[] mimes,
            @RequestParam(value = "tree", required = false, defaultValue = "false") boolean tree,
            @RequestParam(value = "init", required = false, defaultValue = "false") boolean init,
            @RequestParam(value = "width", required = false) Integer width,
            @RequestParam(value = "height", required = false) Integer height,
            @RequestParam(value = "quality", required = false) Float quality,
            @RequestParam(value = "bg", required = false) String bg,
            @RequestParam(value = "degree", required = false) Integer degree,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "encoding", required = false) String encoding,
            @RequestParam(value = "upload[]", required = false) MultipartFile[] upload,
            @RequestParam Map<String, String> allParams,
            HttpServletRequest request) {

        try {
            // Create elFinder request
            ElFinderRequest elFinderRequest = createElFinderRequest(
                cmd, target, targets, dst, name, dirs, q, mimes, tree, init,
                width, height, quality, bg, degree, content, encoding, allParams, upload);

            // Log request for debugging
            elFinderConnector.logRequest(elFinderRequest, request);

            // Validate request
            if (!elFinderConnector.isValidRequest(elFinderRequest)) {
                logger.warn("Invalid elFinder request: {}", elFinderRequest);
                return ResponseEntity.badRequest()
                    .body(elFinderConnector.createErrorResponse("error.request.invalid", request));
            }

            // Process request
            ElFinderResponse response = elFinderConnector.processCommand(elFinderRequest, request);

            // Return response with appropriate status
            HttpStatus status = response.hasErrors() ? HttpStatus.BAD_REQUEST : HttpStatus.OK;
            return ResponseEntity.status(status).body(response);

        } catch (Exception e) {
            logger.error("Error handling elFinder POST request: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(elFinderConnector.createErrorResponse("error.request.processing", request, e.getMessage()));
        }
    }

    /**
     * Initialize elFinder connector
     * 
     * @param request HTTP request
     * @return initialization response
     */
    @GetMapping("/init")
    @RequiresPermissions("file:read")
    public ResponseEntity<ElFinderResponse> initialize(HttpServletRequest request) {
        try {
            logger.info("elFinder initialization request");

            ElFinderResponse response = elFinderConnector.initialize(request);

            HttpStatus status = response.hasErrors() ? HttpStatus.BAD_REQUEST : HttpStatus.OK;
            return ResponseEntity.status(status).body(response);

        } catch (Exception e) {
            logger.error("Error initializing elFinder connector: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(elFinderConnector.createErrorResponse("error.initialization.failed", request, e.getMessage()));
        }
    }

    /**
     * Handle file serving for elFinder
     * This endpoint serves files directly for elFinder preview and download
     * 
     * @param target target hash
     * @param download download flag
     * @param request HTTP request
     * @return file response
     */
    @GetMapping("/file")
    @RequiresPermissions("file:read")
    public ResponseEntity<?> serveFile(
            @RequestParam("target") String target,
            @RequestParam(value = "download", required = false, defaultValue = "false") boolean download,
            HttpServletRequest request) {

        try {
            logger.debug("elFinder file serve request - target: {}, download: {}", target, download);

            // Create file command request
            ElFinderRequest elFinderRequest = new ElFinderRequest("file");
            elFinderRequest.setTarget(target);
            elFinderRequest.addParameter("download", String.valueOf(download));

            // Process request
            ElFinderResponse response = elFinderConnector.processCommand(elFinderRequest, request);

            if (response.hasErrors()) {
                return ResponseEntity.badRequest().body(response);
            }

            // The file command should return the file content directly
            // This is handled by the FileCommand implementation
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error serving elFinder file: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(elFinderConnector.createErrorResponse("error.file.serve", request, e.getMessage()));
        }
    }

    /**
     * Handle thumbnail requests for elFinder
     * 
     * @param target target hash
     * @param request HTTP request
     * @return thumbnail response
     */
    @GetMapping("/tmb")
    @RequiresPermissions("file:read")
    public ResponseEntity<?> getThumbnail(
            @RequestParam("target") String target,
            HttpServletRequest request) {

        try {
            logger.debug("elFinder thumbnail request - target: {}", target);

            // Create thumbnail command request
            ElFinderRequest elFinderRequest = new ElFinderRequest("tmb");
            elFinderRequest.setTarget(target);

            // Process request
            ElFinderResponse response = elFinderConnector.processCommand(elFinderRequest, request);

            if (response.hasErrors()) {
                return ResponseEntity.badRequest().body(response);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting elFinder thumbnail: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(elFinderConnector.createErrorResponse("error.thumbnail.get", request, e.getMessage()));
        }
    }

    /**
     * Create ElFinderRequest from parameters
     */
    private ElFinderRequest createElFinderRequest(
            String cmd, String target, String[] targets, String dst, String name, String[] dirs,
            String q, String[] mimes, boolean tree, boolean init, Integer width, Integer height,
            Float quality, String bg, Integer degree, String content, String encoding,
            Map<String, String> allParams, MultipartFile[] upload) {

        ElFinderRequest request = new ElFinderRequest();
        
        // Set basic parameters
        request.setCmd(cmd);
        request.setTarget(target);
        request.setDst(dst);
        request.setName(name);
        request.setQ(q);
        request.setTree(tree);
        request.setInit(init);
        
        // Set image parameters
        request.setWidth(width);
        request.setHeight(height);
        request.setQuality(quality);
        request.setBg(bg);
        request.setDegree(degree);
        
        // Set text parameters
        request.setContent(content);
        if (StringUtils.hasText(encoding)) {
            request.setEncoding(encoding);
        }
        
        // Set array parameters
        if (targets != null && targets.length > 0) {
            request.setTargets(Arrays.asList(targets));
        }
        
        if (dirs != null && dirs.length > 0) {
            request.setDirs(String.join(",", dirs));
        }
        
        if (mimes != null && mimes.length > 0) {
            request.setMimes(String.join(",", mimes));
        }
        
        // Set uploaded files
        if (upload != null && upload.length > 0) {
            request.setUpload(Arrays.asList(upload));
        }
        
        // Set additional parameters
        if (allParams != null) {
            for (Map.Entry<String, String> entry : allParams.entrySet()) {
                String key = entry.getKey();
                // Skip parameters we've already handled
                if (!isHandledParameter(key)) {
                    request.addParameter(key, entry.getValue());
                }
            }
        }
        
        return request;
    }

    /**
     * Check if parameter is already handled
     */
    private boolean isHandledParameter(String key) {
        return key.equals("cmd") || key.equals("target") || key.startsWith("targets") ||
               key.equals("dst") || key.equals("name") || key.startsWith("dirs") ||
               key.equals("q") || key.startsWith("mimes") || key.equals("tree") ||
               key.equals("init") || key.equals("width") || key.equals("height") ||
               key.equals("quality") || key.equals("bg") || key.equals("degree") ||
               key.equals("content") || key.equals("encoding") || key.startsWith("upload");
    }
}