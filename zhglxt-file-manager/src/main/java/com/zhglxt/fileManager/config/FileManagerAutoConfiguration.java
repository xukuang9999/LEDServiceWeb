package com.zhglxt.fileManager.config;

import com.zhglxt.fileManager.integration.ZhglxtWebIntegrationConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * File Manager Auto Configuration
 * 
 * Configures all necessary components for the file manager module including
 * configuration management, validation, dynamic updates, and zhglxt-web integration.
 * This auto-configuration is automatically loaded when the file manager module
 * is present on the classpath and integrates seamlessly with zhglxt-web.
 * 
 * @author zhglxt
 */
@AutoConfiguration
@ComponentScan(basePackages = "com.zhglxt.fileManager")
@Import({
    I18nConfiguration.class, 
    StorageConfiguration.class,
    ZhglxtWebIntegrationConfiguration.class
})
public class FileManagerAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(FileManagerAutoConfiguration.class);

    public FileManagerAutoConfiguration() {
        logger.info("FileManagerAutoConfiguration is being loaded");
    }

    /**
     * Configuration validator bean
     */
    @Bean
    public FileManagerConfigurationValidator fileManagerConfigurationValidator() {
        return new FileManagerConfigurationValidator();
    }

    /**
     * Configuration manager bean
     */
    @Bean
    public FileManagerConfigurationManager fileManagerConfigurationManager(
            FileManagerProperties properties,
            FileManagerConfigurationValidator validator,
            org.springframework.core.env.ConfigurableEnvironment environment,
            org.springframework.context.ApplicationEventPublisher eventPublisher) {
        return new FileManagerConfigurationManager(properties, validator, environment, eventPublisher);
    }
}