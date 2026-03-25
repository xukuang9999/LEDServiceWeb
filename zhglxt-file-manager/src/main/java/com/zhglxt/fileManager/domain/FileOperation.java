package com.zhglxt.fileManager.domain;

/**
 * File Operation Enum
 * 
 * @author zhglxt
 */
public enum FileOperation {
    /**
     * Read file content
     */
    READ,

    /**
     * Write/modify file content
     */
    WRITE,

    /**
     * Delete file
     */
    DELETE,

    /**
     * Move/rename file
     */
    MOVE,

    /**
     * Copy file
     */
    COPY,

    /**
     * Create new file
     */
    CREATE,

    /**
     * Upload file
     */
    UPLOAD,

    /**
     * Download file
     */
    DOWNLOAD,

    /**
     * List directory contents
     */
    LIST,

    /**
     * Create directory
     */
    MKDIR,

    /**
     * Remove directory
     */
    RMDIR,

    /**
     * Preview file
     */
    PREVIEW,

    /**
     * Generate thumbnail
     */
    THUMBNAIL,

    /**
     * Apply watermark
     */
    WATERMARK,

    /**
     * Search files
     */
    SEARCH,

    /**
     * Archive files
     */
    ARCHIVE,

    /**
     * Extract archive
     */
    EXTRACT
}