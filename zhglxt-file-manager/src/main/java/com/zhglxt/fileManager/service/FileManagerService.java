package com.zhglxt.fileManager.service;

import com.zhglxt.fileManager.domain.FileDownloadResult;
import com.zhglxt.fileManager.domain.FileInfo;
import com.zhglxt.fileManager.domain.FilePreviewResult;
import com.zhglxt.fileManager.domain.FileUploadResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * File Manager Service Interface
 * Core business logic for file operations
 * 
 * @author zhglxt
 */
public interface FileManagerService {

    /**
     * Upload a file
     * 
     * @param file the multipart file to upload
     * @param directory target directory (relative to root)
     * @param userId user ID for permission checking
     * @return upload result
     */
    FileUploadResult uploadFile(MultipartFile file, String directory, String userId);

    /**
     * Download a file
     * 
     * @param filePath file path (relative to root)
     * @param userId user ID for permission checking
     * @return download result with input stream
     */
    FileDownloadResult downloadFile(String filePath, String userId);

    /**
     * Delete a file
     * 
     * @param filePath file path (relative to root)
     * @param userId user ID for permission checking
     * @return true if deleted successfully
     */
    boolean deleteFile(String filePath, String userId);

    /**
     * Move a file
     * 
     * @param sourcePath source file path (relative to root)
     * @param targetPath target file path (relative to root)
     * @param userId user ID for permission checking
     * @return true if moved successfully
     */
    boolean moveFile(String sourcePath, String targetPath, String userId);

    /**
     * Copy a file
     * 
     * @param sourcePath source file path (relative to root)
     * @param targetPath target file path (relative to root)
     * @param userId user ID for permission checking
     * @return true if copied successfully
     */
    boolean copyFile(String sourcePath, String targetPath, String userId);

    /**
     * List files in a directory
     * 
     * @param directory directory path (relative to root)
     * @param userId user ID for permission checking
     * @return list of file information
     */
    List<FileInfo> listFiles(String directory, String userId);

    /**
     * Get file information
     * 
     * @param filePath file path (relative to root)
     * @param userId user ID for permission checking
     * @return file information
     */
    FileInfo getFileInfo(String filePath, String userId);

    /**
     * Preview a file
     * 
     * @param filePath file path (relative to root)
     * @param userId user ID for permission checking
     * @return preview result
     */
    FilePreviewResult previewFile(String filePath, String userId);

    /**
     * Create a directory
     * 
     * @param directoryPath directory path (relative to root)
     * @param userId user ID for permission checking
     * @return true if created successfully
     */
    boolean createDirectory(String directoryPath, String userId);

    /**
     * Delete a directory
     * 
     * @param directoryPath directory path (relative to root)
     * @param userId user ID for permission checking
     * @param recursive whether to delete recursively
     * @return true if deleted successfully
     */
    boolean deleteDirectory(String directoryPath, String userId, boolean recursive);

    /**
     * Rename a file or directory
     * 
     * @param oldPath old path (relative to root)
     * @param newName new name
     * @param userId user ID for permission checking
     * @return true if renamed successfully
     */
    boolean rename(String oldPath, String newName, String userId);

    /**
     * Check if file exists
     * 
     * @param filePath file path (relative to root)
     * @param userId user ID for permission checking
     * @return true if file exists and user has access
     */
    boolean fileExists(String filePath, String userId);

    /**
     * Get file size
     * 
     * @param filePath file path (relative to root)
     * @param userId user ID for permission checking
     * @return file size in bytes, -1 if file doesn't exist or no access
     */
    long getFileSize(String filePath, String userId);

    /**
     * Search files by name pattern
     * 
     * @param directory directory to search in (relative to root)
     * @param pattern search pattern (supports wildcards)
     * @param userId user ID for permission checking
     * @param recursive whether to search recursively
     * @return list of matching files
     */
    List<FileInfo> searchFiles(String directory, String pattern, String userId, boolean recursive);
}