package com.zhglxt.fileManager.domain;

/**
 * Directory Operation Enum
 * 
 * @author zhglxt
 */
public enum DirectoryOperation {
    /**
     * List directory contents
     */
    LIST,

    /**
     * Create directory
     */
    CREATE,

    /**
     * Delete directory
     */
    DELETE,

    /**
     * Move/rename directory
     */
    MOVE,

    /**
     * Copy directory
     */
    COPY,

    /**
     * Access directory
     */
    ACCESS,

    /**
     * Upload files to directory
     */
    UPLOAD,

    /**
     * Search within directory
     */
    SEARCH
}