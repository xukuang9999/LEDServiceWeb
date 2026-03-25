package com.zhglxt.fileManager.domain.elfinder;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * elFinder Request Model
 * 
 * @author zhglxt
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ElFinderRequest {

    /**
     * elFinder command
     */
    @NotBlank(message = "Command cannot be blank")
    private String cmd;

    /**
     * Target hash (file or directory identifier)
     */
    private String target;

    /**
     * Source targets for operations like move, copy
     */
    private List<String> targets = new ArrayList<>();

    /**
     * Destination target for move/copy operations
     */
    private String dst;

    /**
     * New name for rename operations
     */
    private String name;

    /**
     * Directory name for mkdir operations
     */
    private String dirs;

    /**
     * Search query
     */
    private String q;

    /**
     * MIME type filter
     */
    private String mimes;

    /**
     * Whether to include subdirectories in operations
     */
    private boolean tree = false;

    /**
     * Whether to initialize the connector
     */
    private boolean init = false;

    /**
     * Uploaded files
     */
    private List<MultipartFile> upload = new ArrayList<>();

    /**
     * Additional request parameters
     */
    private Map<String, String> parameters = new HashMap<>();

    /**
     * Request width for image operations
     */
    private Integer width;

    /**
     * Request height for image operations
     */
    private Integer height;

    /**
     * Quality for image operations
     */
    private Float quality;

    /**
     * Background color for image operations
     */
    private String bg;

    /**
     * Rotation degree for image operations
     */
    private Integer degree;

    /**
     * Content for text file operations
     */
    private String content;

    /**
     * Encoding for text operations
     */
    private String encoding = "UTF-8";

    // Constructors
    public ElFinderRequest() {
    }

    public ElFinderRequest(String cmd) {
        this.cmd = cmd;
    }

    // Getters and Setters
    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public List<String> getTargets() {
        return targets;
    }

    public void setTargets(List<String> targets) {
        this.targets = targets;
    }

    public String getDst() {
        return dst;
    }

    public void setDst(String dst) {
        this.dst = dst;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDirs() {
        return dirs;
    }

    public void setDirs(String dirs) {
        this.dirs = dirs;
    }

    public String getQ() {
        return q;
    }

    public void setQ(String q) {
        this.q = q;
    }

    public String getMimes() {
        return mimes;
    }

    public void setMimes(String mimes) {
        this.mimes = mimes;
    }

    public boolean isTree() {
        return tree;
    }

    public void setTree(boolean tree) {
        this.tree = tree;
    }

    public boolean isInit() {
        return init;
    }

    public void setInit(boolean init) {
        this.init = init;
    }

    public List<MultipartFile> getUpload() {
        return upload;
    }

    public void setUpload(List<MultipartFile> upload) {
        this.upload = upload;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Float getQuality() {
        return quality;
    }

    public void setQuality(Float quality) {
        this.quality = quality;
    }

    public String getBg() {
        return bg;
    }

    public void setBg(String bg) {
        this.bg = bg;
    }

    public Integer getDegree() {
        return degree;
    }

    public void setDegree(Integer degree) {
        this.degree = degree;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Add a parameter
     */
    public void addParameter(String key, String value) {
        this.parameters.put(key, value);
    }

    /**
     * Get parameter value
     */
    public String getParameter(String key) {
        return this.parameters.get(key);
    }

    /**
     * Add a target
     */
    public void addTarget(String target) {
        this.targets.add(target);
    }

    /**
     * Check if request has uploads
     */
    public boolean hasUploads() {
        return upload != null && !upload.isEmpty();
    }

    /**
     * Check if request is for multiple targets
     */
    public boolean hasMultipleTargets() {
        return targets != null && targets.size() > 1;
    }

    @Override
    public String toString() {
        return "ElFinderRequest{" +
                "cmd='" + cmd + '\'' +
                ", target='" + target + '\'' +
                ", targets=" + targets +
                ", dst='" + dst + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}