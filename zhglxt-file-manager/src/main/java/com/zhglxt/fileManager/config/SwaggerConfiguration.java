package com.zhglxt.fileManager.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI configuration for zhglxt-file-manager
 * 
 * @author zhglxt
 */
@Configuration
@ConditionalOnProperty(name = "zhglxt.file-manager.swagger.enabled", havingValue = "true", matchIfMissing = false)
public class SwaggerConfiguration {

    @Bean
    public OpenAPI fileManagerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("zhglxt File Manager API")
                        .description("Comprehensive file management API with elFinder integration, " +
                                   "supporting local and cloud storage backends, thumbnail generation, " +
                                   "watermarking, and advanced security features.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("zhglxt Development Team")
                                .email("support@zhglxt.com")
                                .url("https://github.com/zhglxt/zhglxt"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server()
                                .url("/")
                                .description("Default server")))
                .addSecurityItem(new SecurityRequirement().addList("shiroAuth"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("shiroAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .description("Shiro-based authentication token")));
    }
}