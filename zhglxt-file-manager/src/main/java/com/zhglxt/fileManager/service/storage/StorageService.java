package com.zhglxt.fileManager.service.storage;

import java.io.InputStream;
import java.util.List;

/**
 * Storage Service Interface
 * Abstract storage operations across different backends
 * 
 * @author zhglxt
 */
public interface StorageService {

    /**
     * Store a file
     * 
     * @param inputStream file input stream
     * @param fileName file name
     * @param directory target directory
     * @return stored file path
     */
    String storeFile(InputStream inputStream, String fileName, String directory);

    /**
     * Retrieve a file
     * 
     * @param filePath file path
     * @return file input stream
     */
    InputStream retrieveFile(String filePath);

    /**
     * Delete a file
     * 
     * @param filePath file path
     * @return true if deleted successfully
     */
    boolean deleteFile(String filePath);

    /**
     * Move a file
     * 
     * @param sourcePath source file path
     * @param targetPath target file path
     * @return true if moved successfully
     */
    boolean moveFile(String sourcePath, String targetPath);

    /**
     * List files in a directory
     * 
     * @param directory directory path
     * @return list of file information
     */
    List<StorageFileInfo> listFiles(String directory);

    /**
     * Generate public URL for a file
     * 
     * @param filePath file path
     * @return public URL
     */
    String generatePublicUrl(String filePath);

    /**
     * Check if file exists
     * 
     * @param filePath file path
     * @return true if file exists
     */
    boolean fileExists(String filePath);

    /**
     * Get file size
     * 
     * @param filePath file path
     * @return file size in bytes, -1 if file doesn't exist
     */
    long getFileSize(String filePath);
}