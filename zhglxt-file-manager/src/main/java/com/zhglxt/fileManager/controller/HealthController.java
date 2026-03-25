package com.zhglxt.fileManager.controller;

import com.zhglxt.fileManager.service.monitoring.HealthCheckService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check controller for file manager monitoring
 * 
 * @author zhglxt
 */
@RestController
@RequestMapping("/file-manager")
@Tag(name = "Health Check", description = "Health monitoring endpoints")
public class HealthController {

    @Autowired
    private HealthCheckService healthCheckService;

    @Operation(summary = "Health check", description = "Check the health status of the file manager service")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Service is healthy"),
        @ApiResponse(responseCode = "503", description = "Service is unhealthy")
    })
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        
        try {
            Map<String, Object> healthStatus = healthCheckService.checkHealth();
            boolean isHealthy = (Boolean) healthStatus.get("healthy");
            
            response.put("status", isHealthy ? "UP" : "DOWN");
            response.put("details", healthStatus.get("details"));
            
            return ResponseEntity.status(isHealthy ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE)
                    .body(response);
        } catch (Exception e) {
            response.put("status", "DOWN");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        }
    }

    @Operation(summary = "Readiness check", description = "Check if the service is ready to accept requests")
    @GetMapping("/ready")
    public ResponseEntity<Map<String, Object>> ready() {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        
        try {
            boolean isReady = healthCheckService.isReady();
            response.put("status", isReady ? "READY" : "NOT_READY");
            
            return ResponseEntity.status(isReady ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE)
                    .body(response);
        } catch (Exception e) {
            response.put("status", "NOT_READY");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        }
    }

    @Operation(summary = "Liveness check", description = "Check if the service is alive")
    @GetMapping("/live")
    public ResponseEntity<Map<String, Object>> live() {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", "ALIVE");
        response.put("uptime", healthCheckService.getUptime());
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Service info", description = "Get service information and statistics")
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "zhglxt-file-manager");
        response.put("version", "1.0.0");
        response.put("info", healthCheckService.getServiceInfo());
        
        return ResponseEntity.ok(response);
    }
}