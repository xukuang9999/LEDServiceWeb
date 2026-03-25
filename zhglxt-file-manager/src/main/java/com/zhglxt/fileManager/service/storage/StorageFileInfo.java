package com.zhglxt.fileManager.service.storage;

import java.time.LocalDateTime;

/**
 * Storage File Information
 * 
 * @author zhglxt
 */
public class StorageFileInfo {
    
    private String name;
    private String path;
    private long size;
    private boolean isDirectory;
    private LocalDateTime lastModified;
    private String mimeType;

    public StorageFileInfo() {
    }

    public StorageFileInfo(String name, String path, long size, boolean isDirectory, 
                          LocalDateTime lastModified, String mimeType) {
        this.name = name;
        this.path = path;
        this.size = size;
        this.isDirectory = isDirectory;
        this.lastModified = lastModified;
        this.mimeType = mimeType;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setDirectory(boolean directory) {
        isDirectory = directory;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}