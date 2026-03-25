package com.zhglxt.fileManager.domain.elfinder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.HashMap;
import java.util.Map;

/**
 * elFinder File Model
 * 
 * @author zhglxt
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ElFinderFile {

    /**
     * File hash (unique identifier)
     */
    @NotBlank(message = "Hash cannot be blank")
    private String hash;

    /**
     * Parent directory hash
     */
    @JsonProperty("phash")
    private String parentHash;

    /**
     * File name
     */
    @NotBlank(message = "Name cannot be blank")
    private String name;

    /**
     * File size in bytes
     */
    @PositiveOrZero(message = "Size must be zero or positive")
    private long size;

    /**
     * Last modified timestamp
     */
    @JsonProperty("ts")
    private long timestamp;

    /**
     * MIME type
     */
    private String mime;

    /**
     * Whether this is a directory
     */
    @JsonProperty("dirs")
    private Integer hasSubdirectories;

    /**
     * File permissions
     */
    private String perms;

    /**
     * Whether file is readable
     */
    @JsonProperty("read")
    private boolean readable = true;

    /**
     * Whether file is writable
     */
    @JsonProperty("write")
    private boolean writable = true;

    /**
     * Whether file is locked
     */
    @JsonProperty("locked")
    private boolean locked = false;

    /**
     * Thumbnail URL
     */
    @JsonProperty("tmb")
    private String thumbnail;

    /**
     * File URL
     */
    private String url;

    /**
     * Volume ID
     */
    @JsonProperty("volumeid")
    private String volumeId;

    /**
     * File dimensions (for images)
     */
    @JsonProperty("dim")
    private String dimensions;

    /**
     * File owner
     */
    private String owner;

    /**
     * File group
     */
    private String group;

    /**
     * File mode
     */
    private String mode;

    /**
     * File alias (for links)
     */
    private String alias;

    /**
     * Link target (for symbolic links)
     */
    private String target;

    /**
     * Additional options
     */
    private Map<String, Object> options = new HashMap<>();

    // Constructors
    public ElFinderFile() {
    }

    public ElFinderFile(String hash, String name, boolean isDirectory) {
        this.hash = hash;
        this.name = name;
        this.mime = isDirectory ? "directory" : "application/octet-stream";
        this.hasSubdirectories = isDirectory ? 0 : null;
    }

    // Static factory methods
    public static ElFinderFile directory(String hash, String name, String parentHash) {
        ElFinderFile file = new ElFinderFile(hash, name, true);
        file.setParentHash(parentHash);
        file.setHasSubdirectories(0);
        return file;
    }

    public static ElFinderFile file(String hash, String name, String parentHash, long size, String mime) {
        ElFinderFile file = new ElFinderFile(hash, name, false);
        file.setParentHash(parentHash);
        file.setSize(size);
        file.setMime(mime);
        return file;
    }

    // Getters and Setters
    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getParentHash() {
        return parentHash;
    }

    public void setParentHash(String parentHash) {
        this.parentHash = parentHash;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMime() {
        return mime;
    }

    public void setMime(String mime) {
        this.mime = mime;
    }

    public Integer getHasSubdirectories() {
        return hasSubdirectories;
    }

    public void setHasSubdirectories(Integer hasSubdirectories) {
        this.hasSubdirectories = hasSubdirectories;
    }

    public String getPerms() {
        return perms;
    }

    public void setPerms(String perms) {
        this.perms = perms;
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

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getVolumeId() {
        return volumeId;
    }

    public void setVolumeId(String volumeId) {
        this.volumeId = volumeId;
    }

    public String getDimensions() {
        return dimensions;
    }

    public void setDimensions(String dimensions) {
        this.dimensions = dimensions;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public Map<String, Object> getOptions() {
        return options;
    }

    public void setOptions(Map<String, Object> options) {
        this.options = options;
    }

    /**
     * Add an option
     */
    public void addOption(String key, Object value) {
        this.options.put(key, value);
    }

    /**
     * Check if this is a directory
     */
    public boolean isDirectory() {
        return "directory".equals(mime);
    }

    /**
     * Check if this is an image
     */
    public boolean isImage() {
        return mime != null && mime.startsWith("image/");
    }

    /**
     * Check if this is a text file
     */
    public boolean isText() {
        return mime != null && (mime.startsWith("text/") || 
               mime.equals("application/json") ||
               mime.equals("application/xml"));
    }

    /**
     * Get formatted file size
     */
    public String getFormattedSize() {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", size / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }

    @Override
    public String toString() {
        return "ElFinderFile{" +
                "hash='" + hash + '\'' +
                ", name='" + name + '\'' +
                ", size=" + size +
                ", mime='" + mime + '\'' +
                ", isDirectory=" + isDirectory() +
                '}';
    }
}