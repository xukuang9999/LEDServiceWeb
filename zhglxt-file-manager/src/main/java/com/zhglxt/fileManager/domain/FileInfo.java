package com.zhglxt.fileManager.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * File Information Model
 * 
 * @author zhglxt
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileInfo {

    /**
     * File name
     */
    @NotBlank(message = "File name cannot be blank")
    private String name;

    /**
     * File path relative to root directory
     */
    @NotBlank(message = "File path cannot be blank")
    private String path;

    /**
     * File type (file or directory)
     */
    @NotNull(message = "File type cannot be null")
    private String type;

    /**
     * File size in bytes
     */
    @PositiveOrZero(message = "File size must be zero or positive")
    private long size;

    /**
     * Last modified timestamp
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastModified;

    /**
     * MIME type of the file
     */
    private String mimeType;

    /**
     * Whether this is a directory
     */
    private boolean isDirectory;

    /**
     * Thumbnail path (if available)
     */
    private String thumbnailPath;

    /**
     * File extension
     */
    private String extension;

    /**
     * Whether the file is readable
     */
    private boolean readable = true;

    /**
     * Whether the file is writable
     */
    private boolean writable = true;

    /**
     * Additional metadata
     */
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * File hash (for integrity checking)
     */
    private String hash;

    /**
     * Public URL (for cloud storage)
     */
    private String publicUrl;

    // Constructors
    public FileInfo() {
    }

    public FileInfo(String name, String path, boolean isDirectory) {
        this.name = name;
        this.path = path;
        this.isDirectory = isDirectory;
        this.type = isDirectory ? "directory" : "file";
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
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

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setDirectory(boolean directory) {
        isDirectory = directory;
        this.type = directory ? "directory" : "file";
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public boolean isReadable() {
        return readable;
    }

    public void setReadable(boolean readable) {
        this.readable = readable;
    }

    public boolean isWritable() {
        return writable;
    }

    public void setWritable(boolean writable) {
        this.writable = writable;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getPublicUrl() {
        return publicUrl;
    }

    public void setPublicUrl(String publicUrl) {
        this.publicUrl = publicUrl;
    }

    /**
     * Add metadata entry
     */
    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }

    /**
     * Get metadata value
     */
    public Object getMetadata(String key) {
        return this.metadata.get(key);
    }

    /**
     * Check if file is an image
     */
    public boolean isImage() {
        return mimeType != null && mimeType.startsWith("image/");
    }

    /**
     * Check if file is a document
     */
    public boolean isDocument() {
        return mimeType != null && (
            mimeType.startsWith("application/") || 
            mimeType.equals("text/plain") ||
            mimeType.equals("text/markdown")
        );
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", type='" + type + '\'' +
                ", size=" + size +
                ", isDirectory=" + isDirectory +
                '}';
    }
}