package com.zhglxt.config;

import com.zhglxt.fileManager.config.FileManagerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * File Manager Configuration in main application
 * This ensures FileManagerProperties bean is created
 */
@Configuration
public class FileManagerConfig {

    private static final Logger logger = LoggerFactory.getLogger(FileManagerConfig.class);

    @Bean
    @ConfigurationProperties(prefix = "zhglxt.file-manager")
    public FileManagerProperties fileManagerProperties() {
        logger.info("Creating FileManagerProperties bean in main application");
        return new FileManagerProperties();
    }
}