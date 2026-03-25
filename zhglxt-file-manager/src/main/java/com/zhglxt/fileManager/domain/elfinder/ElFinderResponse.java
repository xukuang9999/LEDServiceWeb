package com.zhglxt.fileManager.domain.elfinder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * elFinder Response Model
 * 
 * @author zhglxt
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ElFinderResponse {

    /**
     * Response data
     */
    private Map<String, Object> data = new HashMap<>();

    /**
     * Error messages
     */
    private List<String> error = new ArrayList<>();

    /**
     * Warning messages
     */
    private List<String> warning = new ArrayList<>();

    /**
     * Debug information
     */
    private Map<String, Object> debug = new HashMap<>();

    /**
     * Files and directories
     */
    private List<ElFinderFile> files = new ArrayList<>();

    /**
     * Current working directory
     */
    private ElFinderFile cwd;

    /**
     * Upload results
     */
    private List<ElFinderFile> added = new ArrayList<>();

    /**
     * Removed files
     */
    private List<String> removed = new ArrayList<>();

    /**
     * Changed files
     */
    private List<ElFinderFile> changed = new ArrayList<>();

    /**
     * API version
     */
    @JsonProperty("api")
    private String apiVersion = "2.1";

    /**
     * Options for the connector
     */
    private Map<String, Object> options = new HashMap<>();

    /**
     * Net mount options
     */
    private Map<String, Object> netDrivers = new HashMap<>();

    // Constructors
    public ElFinderResponse() {
    }

    // Static factory methods
    public static ElFinderResponse success() {
        return new ElFinderResponse();
    }

    public static ElFinderResponse error(String errorMessage) {
        ElFinderResponse response = new ElFinderResponse();
        response.addError(errorMessage);
        return response;
    }

    public static ElFinderResponse error(List<String> errorMessages) {
        ElFinderResponse response = new ElFinderResponse();
        response.setError(errorMessages);
        return response;
    }

    // Getters and Setters
    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public List<String> getError() {
        return error;
    }

    public void setError(List<String> error) {
        this.error = error;
    }

    public List<String> getWarning() {
        return warning;
    }

    public void setWarning(List<String> warning) {
        this.warning = warning;
    }

    public Map<String, Object> getDebug() {
        return debug;
    }

    public void setDebug(Map<String, Object> debug) {
        this.debug = debug;
    }

    public List<ElFinderFile> getFiles() {
        return files;
    }

    public void setFiles(List<ElFinderFile> files) {
        this.files = files;
    }

    public ElFinderFile getCwd() {
        return cwd;
    }

    public void setCwd(ElFinderFile cwd) {
        this.cwd = cwd;
    }

    public List<ElFinderFile> getAdded() {
        return added;
    }

    public void setAdded(List<ElFinderFile> added) {
        this.added = added;
    }

    public List<String> getRemoved() {
        return removed;
    }

    public void setRemoved(List<String> removed) {
        this.removed = removed;
    }

    public List<ElFinderFile> getChanged() {
        return changed;
    }

    public void setChanged(List<ElFinderFile> changed) {
        this.changed = changed;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public Map<String, Object> getOptions() {
        return options;
    }

    public void setOptions(Map<String, Object> options) {
        this.options = options;
    }

    public Map<String, Object> getNetDrivers() {
        return netDrivers;
    }

    public void setNetDrivers(Map<String, Object> netDrivers) {
        this.netDrivers = netDrivers;
    }

    // Utility methods
    public void addData(String key, Object value) {
        this.data.put(key, value);
    }

    public void addError(String errorMessage) {
        this.error.add(errorMessage);
    }

    public void addWarning(String warningMessage) {
        this.warning.add(warningMessage);
    }

    public void addDebug(String key, Object value) {
        this.debug.put(key, value);
    }

    public void addFile(ElFinderFile file) {
        this.files.add(file);
    }

    public void addAdded(ElFinderFile file) {
        this.added.add(file);
    }

    public void addRemoved(String hash) {
        this.removed.add(hash);
    }

    public void addChanged(ElFinderFile file) {
        this.changed.add(file);
    }

    public void addOption(String key, Object value) {
        this.options.put(key, value);
    }

    /**
     * Check if response has errors
     */
    public boolean hasErrors() {
        return error != null && !error.isEmpty();
    }

    /**
     * Check if response has warnings
     */
    public boolean hasWarnings() {
        return warning != null && !warning.isEmpty();
    }

    /**
     * Check if response is successful
     */
    public boolean isSuccess() {
        return !hasErrors();
    }

    /**
     * Clear all data
     */
    public void clear() {
        data.clear();
        error.clear();
        warning.clear();
        debug.clear();
        files.clear();
        added.clear();
        removed.clear();
        changed.clear();
        options.clear();
        cwd = null;
    }

    @Override
    public String toString() {
        return "ElFinderResponse{" +
                "hasErrors=" + hasErrors() +
                ", filesCount=" + (files != null ? files.size() : 0) +
                ", addedCount=" + (added != null ? added.size() : 0) +
                ", removedCount=" + (removed != null ? removed.size() : 0) +
                '}';
    }
}