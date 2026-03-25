package com.zhglxt.fileManager.config;

import com.zhglxt.fileManager.exception.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Configuration Manager for File Manager
 * 
 * Manages dynamic configuration updates and provides configuration
 * change notifications to other components.
 * 
 * @author zhglxt
 */
@Component
public class FileManagerConfigurationManager {

    private static final Logger logger = LoggerFactory.getLogger(FileManagerConfigurationManager.class);

    private final FileManagerProperties properties;
    private final FileManagerConfigurationValidator validator;
    private final ConfigurableEnvironment environment;
    private final ApplicationEventPublisher eventPublisher;

    private final ReadWriteLock configLock = new ReentrantReadWriteLock();
    private final Map<String, Object> runtimeOverrides = new ConcurrentHashMap<>();
    private volatile FileManagerProperties cachedProperties;

    @Autowired
    public FileManagerConfigurationManager(
            FileManagerProperties properties,
            FileManagerConfigurationValidator validator,
            ConfigurableEnvironment environment,
            ApplicationEventPublisher eventPublisher) {
        this.properties = properties;
        this.validator = validator;
        this.environment = environment;
        this.eventPublisher = eventPublisher;
    }

    @PostConstruct
    public void initialize() {
        try {
            // Validate initial configuration
            validator.validateConfiguration(properties);
            
            // Cache the initial configuration
            this.cachedProperties = properties;
            
            // Log configuration warnings
            List<String> warnings = validator.getConfigurationWarnings(properties);
            if (!warnings.isEmpty()) {
                logger.warn("Configuration warnings:\n{}", String.join("\n", warnings));
            }
            
            logger.info("File Manager configuration initialized successfully");
            
        } catch (Exception e) {
            logger.error("Failed to initialize File Manager configuration", e);
            throw new ConfigurationException("Configuration initialization failed", e);
        }
    }

    /**
     * Get current configuration (thread-safe)
     */
    public FileManagerProperties getCurrentConfiguration() {
        configLock.readLock().lock();
        try {
            return cachedProperties;
        } finally {
            configLock.readLock().unlock();
        }
    }

    /**
     * Update configuration dynamically
     */
    public void updateConfiguration(Map<String, Object> updates) {
        configLock.writeLock().lock();
        try {
            logger.info("Updating configuration with {} changes", updates.size());
            
            // Create a copy of current properties for validation
            FileManagerProperties testProperties = createUpdatedProperties(updates);
            
            // Validate the updated configuration
            validator.validateConfiguration(testProperties);
            
            // Apply the updates
            applyConfigurationUpdates(updates);
            
            // Update cached properties
            this.cachedProperties = testProperties;
            
            // Publish configuration change event
            eventPublisher.publishEvent(new ConfigurationChangeEvent(this, updates));
            
            logger.info("Configuration updated successfully");
            
        } catch (Exception e) {
            logger.error("Failed to update configuration", e);
            throw new ConfigurationException("Configuration update failed: " + e.getMessage(), e);
        } finally {
            configLock.writeLock().unlock();
        }
    }

    /**
     * Update a single configuration property
     */
    public void updateProperty(String key, Object value) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(key, value);
        updateConfiguration(updates);
    }

    /**
     * Reset configuration to defaults
     */
    public void resetToDefaults() {
        configLock.writeLock().lock();
        try {
            logger.info("Resetting configuration to defaults");
            
            // Clear runtime overrides
            runtimeOverrides.clear();
            
            // Remove custom property source
            environment.getPropertySources().remove("fileManagerRuntimeOverrides");
            
            // Recreate properties from environment
            FileManagerProperties defaultProperties = Binder.get(environment)
                .bind("zhglxt.file-manager", FileManagerProperties.class)
                .orElse(new FileManagerProperties());
            
            // Validate default configuration
            validator.validateConfiguration(defaultProperties);
            
            // Update cached properties
            this.cachedProperties = defaultProperties;
            
            // Publish reset event
            eventPublisher.publishEvent(new ConfigurationResetEvent(this));
            
            logger.info("Configuration reset to defaults successfully");
            
        } catch (Exception e) {
            logger.error("Failed to reset configuration", e);
            throw new ConfigurationException("Configuration reset failed: " + e.getMessage(), e);
        } finally {
            configLock.writeLock().unlock();
        }
    }

    /**
     * Get configuration property value
     */
    public Object getProperty(String key) {
        configLock.readLock().lock();
        try {
            return runtimeOverrides.getOrDefault(key, getPropertyFromEnvironment(key));
        } finally {
            configLock.readLock().unlock();
        }
    }

    /**
     * Check if configuration is valid
     */
    public boolean isConfigurationValid() {
        configLock.readLock().lock();
        try {
            return validator.isValidConfiguration(cachedProperties);
        } finally {
            configLock.readLock().unlock();
        }
    }

    /**
     * Get configuration validation warnings
     */
    public List<String> getConfigurationWarnings() {
        configLock.readLock().lock();
        try {
            return validator.getConfigurationWarnings(cachedProperties);
        } finally {
            configLock.readLock().unlock();
        }
    }

    /**
     * Get configuration summary
     */
    public Map<String, Object> getConfigurationSummary() {
        configLock.readLock().lock();
        try {
            Map<String, Object> summary = new HashMap<>();
            FileManagerProperties config = cachedProperties;
            
            summary.put("enabled", config.isEnabled());
            summary.put("rootDirectory", config.getRootDirectory());
            summary.put("maxFileSize", config.getMaxFileSize());
            summary.put("maxStorageSize", config.getMaxStorageSize());
            summary.put("allowedExtensions", config.getAllowedExtensions().size());
            summary.put("blockedExtensions", config.getBlockedExtensions().size());
            summary.put("storageType", config.getStorage().getType());
            summary.put("thumbnailEnabled", config.getThumbnail().isEnabled());
            summary.put("watermarkEnabled", config.getWatermark().isEnabled());
            summary.put("virusScanEnabled", config.isVirusScanEnabled());
            summary.put("auditEnabled", config.isAuditEnabled());
            summary.put("cacheEnabled", config.getCache().isEnabled());
            summary.put("runtimeOverrides", runtimeOverrides.size());
            
            return summary;
        } finally {
            configLock.readLock().unlock();
        }
    }

    /**
     * Export current configuration
     */
    public Map<String, Object> exportConfiguration() {
        configLock.readLock().lock();
        try {
            Map<String, Object> export = new HashMap<>();
            
            // Add all current property values
            export.putAll(getAllConfigurationProperties());
            
            // Add runtime overrides
            if (!runtimeOverrides.isEmpty()) {
                export.put("_runtimeOverrides", new HashMap<>(runtimeOverrides));
            }
            
            return export;
        } finally {
            configLock.readLock().unlock();
        }
    }

    /**
     * Import configuration from map
     */
    public void importConfiguration(Map<String, Object> configMap) {
        configLock.writeLock().lock();
        try {
            logger.info("Importing configuration with {} properties", configMap.size());
            
            // Remove runtime overrides marker if present
            Map<String, Object> cleanConfig = new HashMap<>(configMap);
            cleanConfig.remove("_runtimeOverrides");
            
            // Validate imported configuration
            FileManagerProperties testProperties = createPropertiesFromMap(cleanConfig);
            validator.validateConfiguration(testProperties);
            
            // Clear existing overrides
            runtimeOverrides.clear();
            
            // Apply imported configuration
            applyConfigurationUpdates(cleanConfig);
            
            // Update cached properties
            this.cachedProperties = testProperties;
            
            // Publish import event
            eventPublisher.publishEvent(new ConfigurationImportEvent(this, configMap));
            
            logger.info("Configuration imported successfully");
            
        } catch (Exception e) {
            logger.error("Failed to import configuration", e);
            throw new ConfigurationException("Configuration import failed: " + e.getMessage(), e);
        } finally {
            configLock.writeLock().unlock();
        }
    }

    /**
     * Create updated properties for validation
     */
    private FileManagerProperties createUpdatedProperties(Map<String, Object> updates) {
        try {
            // Create a temporary property source with updates
            Map<String, Object> allProperties = new HashMap<>(getAllConfigurationProperties());
            allProperties.putAll(updates);
            
            return createPropertiesFromMap(allProperties);
            
        } catch (Exception e) {
            throw new ConfigurationException("Failed to create updated properties", e);
        }
    }

    /**
     * Create properties object from map
     */
    private FileManagerProperties createPropertiesFromMap(Map<String, Object> propertyMap) {
        try {
            // Create temporary property source
            MapPropertySource tempSource = new MapPropertySource("temp", propertyMap);
            environment.getPropertySources().addFirst(tempSource);
            
            try {
                return Binder.get(environment)
                    .bind("zhglxt.file-manager", FileManagerProperties.class)
                    .orElse(new FileManagerProperties());
            } finally {
                environment.getPropertySources().remove("temp");
            }
            
        } catch (Exception e) {
            throw new ConfigurationException("Failed to create properties from map", e);
        }
    }

    /**
     * Apply configuration updates to environment
     */
    private void applyConfigurationUpdates(Map<String, Object> updates) {
        // Update runtime overrides
        runtimeOverrides.putAll(updates);
        
        // Update environment property source
        MapPropertySource runtimeSource = new MapPropertySource("fileManagerRuntimeOverrides", runtimeOverrides);
        
        // Remove existing runtime source if present
        environment.getPropertySources().remove("fileManagerRuntimeOverrides");
        
        // Add updated runtime source (high priority)
        environment.getPropertySources().addFirst(runtimeSource);
    }

    /**
     * Get all configuration properties as a flat map
     */
    private Map<String, Object> getAllConfigurationProperties() {
        Map<String, Object> properties = new HashMap<>();
        
        // This would need to be implemented based on the specific property structure
        // For now, we'll include the most commonly updated properties
        FileManagerProperties config = cachedProperties;
        
        properties.put("zhglxt.file-manager.enabled", config.isEnabled());
        properties.put("zhglxt.file-manager.root-directory", config.getRootDirectory());
        properties.put("zhglxt.file-manager.max-file-size", config.getMaxFileSize());
        properties.put("zhglxt.file-manager.max-storage-size", config.getMaxStorageSize());
        properties.put("zhglxt.file-manager.virus-scan-enabled", config.isVirusScanEnabled());
        properties.put("zhglxt.file-manager.audit-enabled", config.isAuditEnabled());
        properties.put("zhglxt.file-manager.thumbnail.enabled", config.getThumbnail().isEnabled());
        properties.put("zhglxt.file-manager.watermark.enabled", config.getWatermark().isEnabled());
        properties.put("zhglxt.file-manager.storage.type", config.getStorage().getType());
        
        return properties;
    }

    /**
     * Get property value from environment
     */
    private Object getPropertyFromEnvironment(String key) {
        return environment.getProperty(key);
    }

    /**
     * Configuration change event
     */
    public static class ConfigurationChangeEvent {
        private final Object source;
        private final Map<String, Object> changes;

        public ConfigurationChangeEvent(Object source, Map<String, Object> changes) {
            this.source = source;
            this.changes = changes;
        }

        public Object getSource() {
            return source;
        }

        public Map<String, Object> getChanges() {
            return changes;
        }
    }

    /**
     * Configuration reset event
     */
    public static class ConfigurationResetEvent {
        private final Object source;

        public ConfigurationResetEvent(Object source) {
            this.source = source;
        }

        public Object getSource() {
            return source;
        }
    }

    /**
     * Configuration import event
     */
    public static class ConfigurationImportEvent {
        private final Object source;
        private final Map<String, Object> importedConfig;

        public ConfigurationImportEvent(Object source, Map<String, Object> importedConfig) {
            this.source = source;
            this.importedConfig = importedConfig;
        }

        public Object getSource() {
            return source;
        }

        public Map<String, Object> getImportedConfig() {
            return importedConfig;
        }
    }

    /**
     * Handle configuration change events from other components
     */
    @EventListener
    public void handleConfigurationChange(ConfigurationChangeEvent event) {
        if (event.getSource() != this) {
            logger.debug("Received external configuration change event");
            // Handle external configuration changes if needed
        }
    }

    /**
     * Ensure required directories exist
     */
    public void ensureDirectoriesExist() {
        configLock.readLock().lock();
        try {
            String rootDir = getCurrentConfiguration().getRootDirectory();
            if (StringUtils.hasText(rootDir)) {
                java.nio.file.Path rootPath = java.nio.file.Paths.get(rootDir);
                if (!java.nio.file.Files.exists(rootPath)) {
                    try {
                        java.nio.file.Files.createDirectories(rootPath);
                        logger.info("Created root directory: {}", rootPath);
                    } catch (java.io.IOException e) {
                        logger.error("Failed to create root directory: {}", rootPath, e);
                    }
                }
            }
        } finally {
            configLock.readLock().unlock();
        }
    }

    /**
     * Update watermark text
     */
    public void updateWatermarkText(String text) {
        configLock.writeLock().lock();
        try {
            getCurrentConfiguration().getWatermark().setText(text);
            logger.debug("Updated watermark text to: {}", text);
        } finally {
            configLock.writeLock().unlock();
        }
    }

    /**
     * Apply default values to properties
     */
    public void applyDefaults(FileManagerProperties props) {
        if (!StringUtils.hasText(props.getRootDirectory())) {
            props.setRootDirectory("upload");
        }
        if (props.getMaxFileSize() <= 0) {
            props.setMaxFileSize(100 * 1024 * 1024L); // 100MB
        }
        if (props.getAllowedExtensions() == null || props.getAllowedExtensions().isEmpty()) {
            props.setAllowedExtensions(List.of("jpg", "jpeg", "png", "gif", "pdf", "doc", "docx", "txt"));
        }
    }

    /**
     * Serialize configuration to string
     */
    public String serializeConfiguration(FileManagerProperties props) {
        try {
            // Simple JSON-like serialization (in production, use proper JSON library)
            return String.format("{\"rootDirectory\":\"%s\",\"maxFileSize\":%d}", 
                props.getRootDirectory(), props.getMaxFileSize());
        } catch (Exception e) {
            logger.error("Failed to serialize configuration", e);
            return "{}";
        }
    }

    /**
     * Deserialize configuration from string
     */
    public FileManagerProperties deserializeConfiguration(String serialized) {
        try {
            // Simple deserialization (in production, use proper JSON library)
            FileManagerProperties props = new FileManagerProperties();
            if (serialized.contains("rootDirectory")) {
                // Extract root directory (simplified parsing)
                String rootDir = serialized.replaceAll(".*\"rootDirectory\":\"([^\"]+)\".*", "$1");
                props.setRootDirectory(rootDir);
            }
            return props;
        } catch (Exception e) {
            logger.error("Failed to deserialize configuration", e);
            return new FileManagerProperties();
        }
    }

    /**
     * Check if Spring Boot compatible
     */
    public boolean isSpringBootCompatible() {
        return true; // Always compatible in Spring Boot environment
    }

    /**
     * Check if supports external configuration
     */
    public boolean supportsExternalConfiguration() {
        return true; // Supports external configuration through Spring Boot
    }

    /**
     * Check if supports profile configuration
     */
    public boolean supportsProfileConfiguration() {
        return true; // Supports Spring profiles
    }
}