package com.zhglxt.fileManager.exception;

/**
 * Configuration Exception
 * 
 * Thrown when file manager configuration is invalid or missing.
 * 
 * @author zhglxt
 */
public class ConfigurationException extends FileManagerException {

    private final String configurationKey;
    private final String configurationValue;

    public ConfigurationException(String message) {
        super("CONFIGURATION_ERROR", message);
        this.configurationKey = null;
        this.configurationValue = null;
    }

    public ConfigurationException(String message, Throwable cause) {
        super("CONFIGURATION_ERROR", message, cause);
        this.configurationKey = null;
        this.configurationValue = null;
    }

    public ConfigurationException(String configurationKey, String message) {
        super("CONFIGURATION_ERROR", message);
        this.configurationKey = configurationKey;
        this.configurationValue = null;
        withContext("configurationKey", configurationKey);
    }

    public ConfigurationException(String configurationKey, String configurationValue, String message) {
        super("CONFIGURATION_ERROR", message);
        this.configurationKey = configurationKey;
        this.configurationValue = configurationValue;
        withContext("configurationKey", configurationKey)
            .withContext("configurationValue", configurationValue);
    }

    public ConfigurationException(String configurationKey, String configurationValue, String message, Throwable cause) {
        super("CONFIGURATION_ERROR", message, cause);
        this.configurationKey = configurationKey;
        this.configurationValue = configurationValue;
        withContext("configurationKey", configurationKey)
            .withContext("configurationValue", configurationValue);
    }

    public String getConfigurationKey() {
        return configurationKey;
    }

    public String getConfigurationValue() {
        return configurationValue;
    }
}