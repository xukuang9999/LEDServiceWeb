package com.zhglxt.fileManager.controller;

/**
 * File Manager API Documentation
 * 
 * This class provides comprehensive documentation for all REST API endpoints
 * provided by the File Manager module controllers.
 * 
 * @author zhglxt
 */
public class FileManagerApiDocumentation {

    /**
     * REST API Endpoints Documentation
     * 
     * BASE URL: /api/file-manager
     * 
     * Authentication: All endpoints require authentication via Shiro
     * Authorization: Endpoints require specific permissions as documented
     * 
     * === FILE OPERATIONS ===
     * 
     * POST /upload
     * - Description: Upload a file to the server
     * - Parameters: file (MultipartFile), directory (String, optional)
     * - Permissions: file:upload
     * - Returns: ApiResponse<FileUploadResult>
     * - Status Codes: 200 (success), 400 (validation error), 403 (permission denied), 500 (server error)
     * 
     * GET /download
     * - Description: Download a file from the server
     * - Parameters: path (String, required)
     * - Permissions: file:download
     * - Returns: File content with appropriate headers
     * - Status Codes: 200 (success), 404 (not found), 403 (permission denied), 500 (server error)
     * 
     * DELETE /delete
     * - Description: Delete a file
     * - Parameters: path (String, required)
     * - Permissions: file:delete
     * - Returns: ApiResponse<Boolean>
     * - Status Codes: 200 (success), 404 (not found), 403 (permission denied), 500 (server error)
     * 
     * POST /move
     * - Description: Move a file to a new location
     * - Parameters: source (String, required), target (String, required)
     * - Permissions: file:move
     * - Returns: ApiResponse<Boolean>
     * - Status Codes: 200 (success), 404 (not found), 403 (permission denied), 500 (server error)
     * 
     * POST /copy
     * - Description: Copy a file to a new location
     * - Parameters: source (String, required), target (String, required)
     * - Permissions: file:copy
     * - Returns: ApiResponse<Boolean>
     * - Status Codes: 200 (success), 404 (not found), 403 (permission denied), 500 (server error)
     * 
     * GET /list
     * - Description: List files in a directory
     * - Parameters: directory (String, optional, defaults to root)
     * - Permissions: file:list
     * - Returns: ApiResponse<List<FileInfo>>
     * - Status Codes: 200 (success), 404 (directory not found), 403 (permission denied), 500 (server error)
     * 
     * GET /info
     * - Description: Get file information
     * - Parameters: path (String, required)
     * - Permissions: file:read
     * - Returns: ApiResponse<FileInfo>
     * - Status Codes: 200 (success), 404 (not found), 403 (permission denied), 500 (server error)
     * 
     * GET /preview
     * - Description: Preview a file (text content or preview info)
     * - Parameters: path (String, required)
     * - Permissions: file:read
     * - Returns: File content or ApiResponse<FilePreviewResult>
     * - Status Codes: 200 (success), 404 (not found), 403 (permission denied), 500 (server error)
     * 
     * === DIRECTORY OPERATIONS ===
     * 
     * POST /mkdir
     * - Description: Create a directory
     * - Parameters: path (String, required)
     * - Permissions: file:create
     * - Returns: ApiResponse<Boolean>
     * - Status Codes: 200 (success), 403 (permission denied), 500 (server error)
     * 
     * DELETE /rmdir
     * - Description: Delete a directory
     * - Parameters: path (String, required), recursive (boolean, optional, default false)
     * - Permissions: file:delete
     * - Returns: ApiResponse<Boolean>
     * - Status Codes: 200 (success), 404 (not found), 403 (permission denied), 500 (server error)
     * 
     * POST /rename
     * - Description: Rename a file or directory
     * - Parameters: path (String, required), name (String, required)
     * - Permissions: file:update
     * - Returns: ApiResponse<Boolean>
     * - Status Codes: 200 (success), 404 (not found), 403 (permission denied), 500 (server error)
     * 
     * === UTILITY OPERATIONS ===
     * 
     * GET /search
     * - Description: Search for files by pattern
     * - Parameters: directory (String, optional), pattern (String, required), recursive (boolean, optional, default true)
     * - Permissions: file:read
     * - Returns: ApiResponse<List<FileInfo>>
     * - Status Codes: 200 (success), 403 (permission denied), 500 (server error)
     * 
     * GET /exists
     * - Description: Check if a file exists
     * - Parameters: path (String, required)
     * - Permissions: file:read
     * - Returns: ApiResponse<Boolean>
     * - Status Codes: 200 (success), 500 (server error)
     * 
     * GET /size
     * - Description: Get file size
     * - Parameters: path (String, required)
     * - Permissions: file:read
     * - Returns: ApiResponse<Long>
     * - Status Codes: 200 (success), 404 (not found), 500 (server error)
     */
    
    /**
     * elFinder Connector API Documentation
     * 
     * BASE URL: /api/file-manager/connector
     * 
     * Authentication: All endpoints require authentication via Shiro
     * Authorization: Endpoints require file permissions as appropriate
     * 
     * === CONNECTOR ENDPOINTS ===
     * 
     * GET /
     * - Description: Handle elFinder GET requests (read operations)
     * - Parameters: cmd, target, and other elFinder protocol parameters
     * - Permissions: file:read
     * - Returns: ElFinderResponse (JSON)
     * - Status Codes: 200 (success), 400 (bad request), 500 (server error)
     * 
     * POST /
     * - Description: Handle elFinder POST requests (write operations)
     * - Parameters: cmd, target, upload files, and other elFinder protocol parameters
     * - Permissions: file:write
     * - Returns: ElFinderResponse (JSON)
     * - Status Codes: 200 (success), 400 (bad request), 500 (server error)
     * 
     * GET /init
     * - Description: Initialize elFinder connector
     * - Parameters: None
     * - Permissions: file:read
     * - Returns: ElFinderResponse (JSON)
     * - Status Codes: 200 (success), 400 (bad request), 500 (server error)
     * 
     * GET /file
     * - Description: Serve files for elFinder (preview/download)
     * - Parameters: target (String, required), download (boolean, optional)
     * - Permissions: file:read
     * - Returns: File content or ElFinderResponse
     * - Status Codes: 200 (success), 400 (bad request), 500 (server error)
     * 
     * GET /tmb
     * - Description: Serve thumbnails for elFinder
     * - Parameters: target (String, required)
     * - Permissions: file:read
     * - Returns: Thumbnail content or ElFinderResponse
     * - Status Codes: 200 (success), 400 (bad request), 500 (server error)
     */
    
    /**
     * Web Interface Endpoints Documentation
     * 
     * BASE URL: /file-manager
     * 
     * Authentication: All endpoints require authentication via Shiro
     * Authorization: Endpoints require file permissions
     * 
     * === VIEW ENDPOINTS ===
     * 
     * GET /
     * - Description: Main file manager interface
     * - Parameters: None
     * - Permissions: file:read
     * - Returns: file-manager/index template
     * - Status Codes: 200 (success), 403 (permission denied)
     * 
     * GET /popup
     * - Description: File manager popup interface (for integration)
     * - Parameters: None
     * - Permissions: file:read
     * - Returns: file-manager/popup template
     * - Status Codes: 200 (success), 403 (permission denied)
     * 
     * GET /browser
     * - Description: File browser interface (minimal, for file selection)
     * - Parameters: None
     * - Permissions: file:read
     * - Returns: file-manager/browser template
     * - Status Codes: 200 (success), 403 (permission denied)
     */
    
    /**
     * Error Response Format
     * 
     * All API endpoints return errors in a standardized format:
     * 
     * {
     *   "success": false,
     *   "code": "ERROR_CODE",
     *   "message": "Human readable error message",
     *   "timestamp": 1234567890,
     *   "errors": { ... } // Optional additional error details
     * }
     * 
     * Common Error Codes:
     * - VALIDATION_ERROR: Request validation failed
     * - PERMISSION_DENIED: User lacks required permissions
     * - FILE_NOT_FOUND: Requested file or directory not found
     * - STORAGE_ERROR: Storage backend operation failed
     * - AUTHENTICATION_REQUIRED: User not authenticated
     * - INTERNAL_ERROR: Unexpected server error
     */
    
    /**
     * Success Response Format
     * 
     * All API endpoints return success responses in a standardized format:
     * 
     * {
     *   "success": true,
     *   "data": { ... }, // Response data
     *   "timestamp": 1234567890
     * }
     */
    
    /**
     * Security Integration
     * 
     * The File Manager controllers integrate with the zhglxt-web security framework:
     * 
     * 1. Authentication: All endpoints require user authentication via Shiro
     * 2. Authorization: Endpoints check specific permissions (file:read, file:write, etc.)
     * 3. Session Management: User context is maintained through zhglxt-web sessions
     * 4. CSRF Protection: State-changing operations are protected against CSRF attacks
     * 5. Audit Logging: All file operations are logged for security auditing
     * 
     * User ID Resolution:
     * - Primary: Shiro security context (subject.getPrincipal())
     * - Fallback: HTTP session attribute "userId"
     * - Development: Request parameter "userId" or header "X-User-Id"
     * - Default: "anonymous" (with warning log)
     */
    
    /**
     * Configuration Integration
     * 
     * The controllers automatically inherit configuration from:
     * 
     * 1. zhglxt-web application properties
     * 2. FileManagerProperties configuration
     * 3. Spring Boot auto-configuration
     * 4. Shiro security configuration
     * 
     * Key configuration properties:
     * - file-manager.max-file-size: Maximum upload file size
     * - file-manager.allowed-extensions: Allowed file extensions
     * - file-manager.storage.type: Storage backend type
     * - file-manager.watermark.enabled: Watermark processing enabled
     */

    private FileManagerApiDocumentation() {
        // Documentation class - not meant to be instantiated
    }
}