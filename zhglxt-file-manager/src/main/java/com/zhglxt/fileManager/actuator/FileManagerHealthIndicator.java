package com.zhglxt.fileManager.actuator;

import com.zhglxt.fileManager.service.monitoring.HealthCheckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Spring Boot Actuator health indicator for file manager
 * 
 * @author zhglxt
 */
@Component("fileManager")
public class FileManagerHealthIndicator implements HealthIndicator {

    @Autowired
    private HealthCheckService healthCheckService;

    @Override
    public Health health() {
        try {
            Map<String, Object> healthStatus = healthCheckService.checkHealth();
            boolean isHealthy = (Boolean) healthStatus.get("healthy");
            
            Health.Builder builder = isHealthy ? Health.up() : Health.down();
            
            @SuppressWarnings("unchecked")
            Map<String, Object> details = (Map<String, Object>) healthStatus.get("details");
            if (details != null) {
                details.forEach(builder::withDetail);
            }
            
            builder.withDetail("uptime", healthCheckService.getUptime().toString());
            
            return builder.build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withException(e)
                    .build();
        }
    }
}