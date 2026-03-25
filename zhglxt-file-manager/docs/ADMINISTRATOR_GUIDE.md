# zhglxt File Manager - Administrator Guide

## Table of Contents
1. [Installation and Setup](#installation-and-setup)
2. [Configuration](#configuration)
3. [Storage Backends](#storage-backends)
4. [Security Configuration](#security-configuration)
5. [Performance Tuning](#performance-tuning)
6. [Monitoring and Maintenance](#monitoring-and-maintenance)
7. [Troubleshooting](#troubleshooting)
8. [API Reference](#api-reference)

## Installation and Setup

### Prerequisites

- Java 17 or higher
- Spring Boot 3.x
- zhglxt-web parent module
- Maven 3.6+

### Module Integration

The zhglxt-file-manager is designed as a sub-module of the zhglxt-web application:

1. **Add Dependency**: Include the file manager module in your parent POM:
```xml
<dependency>
    <groupId>com.zhglxt</groupId>
    <artifactId>zhglxt-file-manager</artifactId>
    <version>1.0.0</version>
</dependency>
```

2. **Auto-Configuration**: The module uses Spring Boot auto-configuration and will be automatically detected.

3. **Component Scan**: Ensure your main application includes the file manager package:
```java
@SpringBootApplication
@ComponentScan(basePackages = {"com.zhglxt.web", "com.zhglxt.fileManager"})
public class ZhglxtApplication {
    public static void main(String[] args) {
        SpringApplication.run(ZhglxtApplication.class, args);
    }
}
```

### Initial Configuration

Create or update your `application.yml` with basic file manager configuration:

```yaml
zhglxt:
  file-manager:
    enabled: true
    root-directory: "/opt/zhglxt/files"
    max-file-size: 104857600  # 100MB
    allowed-extensions:
      - jpg
      - jpeg
      - png
      - gif
      - pdf
      - doc
      - docx
      - txt
    storage:
      type: local
    watermark:
      enabled: false
    thumbnail:
      enabled: true
      cache-enabled: true
```

## Configuration

### Core Configuration Properties

#### Basic Settings
```yaml
zhglxt:
  file-manager:
    # Enable/disable the file manager module
    enabled: true
    
    # Root directory for file storage (local storage only)
    root-directory: "/opt/zhglxt/files"
    
    # Maximum file size in bytes (default: 100MB)
    max-file-size: 104857600
    
    # Maximum files per upload request
    max-files-per-request: 10
    
    # Allowed file extensions
    allowed-extensions:
      - jpg
      - jpeg
      - png
      - gif
      - bmp
      - pdf
      - doc
      - docx
      - xls
      - xlsx
      - ppt
      - pptx
      - txt
      - zip
      - rar
    
    # Blocked file extensions (takes precedence over allowed)
    blocked-extensions:
      - exe
      - bat
      - sh
      - php
      - jsp
```

#### Storage Configuration
```yaml
zhglxt:
  file-manager:
    storage:
      # Storage type: local, aws-s3, alibaba-oss, tencent-cos
      type: local
      
      # Local storage settings
      local:
        root-path: "/opt/zhglxt/files"
        create-directories: true
        
      # AWS S3 settings
      aws-s3:
        endpoint: "https://s3.amazonaws.com"
        region: "us-east-1"
        bucket-name: "zhglxt-files"
        access-key: "${AWS_ACCESS_KEY}"
        secret-key: "${AWS_SECRET_KEY}"
        cdn:
          enabled: false
          domain: "https://cdn.example.com"
          
      # Alibaba OSS settings
      alibaba-oss:
        endpoint: "https://oss-cn-hangzhou.aliyuncs.com"
        bucket-name: "zhglxt-files"
        access-key: "${ALIBABA_ACCESS_KEY}"
        secret-key: "${ALIBABA_SECRET_KEY}"
        cdn:
          enabled: false
          domain: "https://cdn.example.com"
          
      # Tencent COS settings
      tencent-cos:
        region: "ap-beijing"
        bucket-name: "zhglxt-files"
        secret-id: "${TENCENT_SECRET_ID}"
        secret-key: "${TENCENT_SECRET_KEY}"
        cdn:
          enabled: false
          domain: "https://cdn.example.com"
```

#### Watermark Configuration
```yaml
zhglxt:
  file-manager:
    watermark:
      enabled: true
      text: "© zhglxt"
      font-size: 24
      color: "#FFFFFF"
      opacity: 0.5
      position: bottom-right  # top-left, top-right, bottom-left, bottom-right, center
      margin: 10
      # Apply watermark only to these image types
      image-types:
        - jpg
        - jpeg
        - png
```

#### Thumbnail Configuration
```yaml
zhglxt:
  file-manager:
    thumbnail:
      enabled: true
      cache-enabled: true
      cache-directory: "/opt/zhglxt/thumbnails"
      sizes:
        small: 64
        medium: 128
        large: 256
      quality: 0.8
      format: "jpg"  # jpg, png, webp
```

#### Security Configuration
```yaml
zhglxt:
  file-manager:
    security:
      # Enable file content scanning
      scan-uploads: true
      
      # Rate limiting
      rate-limiting:
        enabled: true
        requests-per-minute: 60
        requests-per-hour: 1000
        
      # CSRF protection
      csrf-protection:
        enabled: true
        
      # File validation
      validation:
        check-file-headers: true
        max-filename-length: 255
        sanitize-filenames: true
```

#### Performance Configuration
```yaml
zhglxt:
  file-manager:
    performance:
      # Async processing
      async:
        enabled: true
        thread-pool-size: 10
        
      # Caching
      cache:
        type: memory  # memory, redis
        ttl: 3600  # seconds
        max-size: 1000
        
      # Connection pooling (for cloud storage)
      connection-pool:
        max-connections: 50
        connection-timeout: 30000
        read-timeout: 60000
```

### Environment-Specific Configuration

#### Development Environment
```yaml
# application-dev.yml
zhglxt:
  file-manager:
    root-directory: "./dev-files"
    max-file-size: 10485760  # 10MB for development
    watermark:
      enabled: false
    thumbnail:
      cache-enabled: false
    security:
      scan-uploads: false
    swagger:
      enabled: true
```

#### Production Environment
```yaml
# application-prod.yml
zhglxt:
  file-manager:
    root-directory: "/opt/zhglxt/files"
    max-file-size: 104857600  # 100MB
    storage:
      type: aws-s3  # Use cloud storage in production
    watermark:
      enabled: true
    thumbnail:
      cache-enabled: true
    security:
      scan-uploads: true
      rate-limiting:
        enabled: true
    swagger:
      enabled: false
```

## Storage Backends

### Local File System

The default storage backend stores files on the local file system:

```yaml
zhglxt:
  file-manager:
    storage:
      type: local
      local:
        root-path: "/opt/zhglxt/files"
        create-directories: true
        permissions: "755"
```

**Advantages:**
- Simple setup
- No external dependencies
- Fast access
- Full control

**Disadvantages:**
- Limited scalability
- No built-in redundancy
- Server-dependent

### AWS S3

Configure AWS S3 for cloud storage:

```yaml
zhglxt:
  file-manager:
    storage:
      type: aws-s3
      aws-s3:
        endpoint: "https://s3.amazonaws.com"
        region: "us-east-1"
        bucket-name: "zhglxt-files"
        access-key: "${AWS_ACCESS_KEY}"
        secret-key: "${AWS_SECRET_KEY}"
        path-style-access: false
        cdn:
          enabled: true
          domain: "https://d123456789.cloudfront.net"
```

**Setup Steps:**
1. Create S3 bucket
2. Configure IAM user with appropriate permissions
3. Set up CloudFront distribution (optional)
4. Configure environment variables

**Required IAM Permissions:**
```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "s3:GetObject",
                "s3:PutObject",
                "s3:DeleteObject",
                "s3:ListBucket"
            ],
            "Resource": [
                "arn:aws:s3:::zhglxt-files",
                "arn:aws:s3:::zhglxt-files/*"
            ]
        }
    ]
}
```

### Alibaba Cloud OSS

Configure Alibaba Cloud Object Storage Service:

```yaml
zhglxt:
  file-manager:
    storage:
      type: alibaba-oss
      alibaba-oss:
        endpoint: "https://oss-cn-hangzhou.aliyuncs.com"
        bucket-name: "zhglxt-files"
        access-key: "${ALIBABA_ACCESS_KEY}"
        secret-key: "${ALIBABA_SECRET_KEY}"
        cdn:
          enabled: true
          domain: "https://zhglxt-files.oss-cn-hangzhou.aliyuncs.com"
```

### Tencent Cloud COS

Configure Tencent Cloud Object Storage:

```yaml
zhglxt:
  file-manager:
    storage:
      type: tencent-cos
      tencent-cos:
        region: "ap-beijing"
        bucket-name: "zhglxt-files"
        secret-id: "${TENCENT_SECRET_ID}"
        secret-key: "${TENCENT_SECRET_KEY}"
        cdn:
          enabled: true
          domain: "https://zhglxt-files.cos.ap-beijing.myqcloud.com"
```

## Security Configuration

### Authentication Integration

The file manager integrates with zhglxt-web's Shiro authentication system:

```java
@Configuration
public class FileManagerSecurityConfig {
    
    @Bean
    public FilePermissionEvaluator filePermissionEvaluator() {
        return new ShiroFilePermissionEvaluator();
    }
    
    @Bean
    public SecurityInterceptor securityInterceptor() {
        return new SecurityInterceptor();
    }
}
```

### Permission Configuration

Configure role-based permissions:

```yaml
zhglxt:
  file-manager:
    permissions:
      # Default permissions for authenticated users
      default:
        read: true
        write: true
        delete: false
        
      # Role-specific permissions
      roles:
        admin:
          read: true
          write: true
          delete: true
          manage: true
        user:
          read: true
          write: true
          delete: false
        guest:
          read: true
          write: false
          delete: false
```

### File Validation

Configure file validation rules:

```yaml
zhglxt:
  file-manager:
    validation:
      # Check file headers to verify file type
      check-file-headers: true
      
      # Maximum filename length
      max-filename-length: 255
      
      # Sanitize filenames (remove special characters)
      sanitize-filenames: true
      
      # Scan files for malicious content
      virus-scanning:
        enabled: true
        engine: "clamav"  # clamav, custom
        
      # Content type validation
      mime-type-validation:
        enabled: true
        strict: true
```

### Rate Limiting

Configure rate limiting to prevent abuse:

```yaml
zhglxt:
  file-manager:
    rate-limiting:
      enabled: true
      
      # Per-user limits
      per-user:
        requests-per-minute: 60
        requests-per-hour: 1000
        upload-mb-per-hour: 1000
        
      # Global limits
      global:
        requests-per-minute: 1000
        concurrent-uploads: 50
        
      # IP-based limits
      per-ip:
        requests-per-minute: 100
        requests-per-hour: 2000
```

## Performance Tuning

### Caching Configuration

#### Memory Cache
```yaml
zhglxt:
  file-manager:
    cache:
      type: memory
      memory:
        max-size: 1000
        ttl: 3600
        cleanup-interval: 300
```

#### Redis Cache
```yaml
zhglxt:
  file-manager:
    cache:
      type: redis
      redis:
        host: localhost
        port: 6379
        database: 0
        ttl: 3600
        key-prefix: "file-manager:"
```

### Async Processing

Configure asynchronous processing for heavy operations:

```yaml
zhglxt:
  file-manager:
    async:
      enabled: true
      
      # Thread pool configuration
      thread-pool:
        core-size: 5
        max-size: 20
        queue-capacity: 100
        keep-alive: 60
        
      # Operations to process asynchronously
      operations:
        thumbnail-generation: true
        watermark-application: true
        file-scanning: true
        large-file-uploads: true
```

### Connection Pooling

For cloud storage backends:

```yaml
zhglxt:
  file-manager:
    connection-pool:
      # Maximum number of connections
      max-connections: 50
      
      # Connection timeout in milliseconds
      connection-timeout: 30000
      
      # Read timeout in milliseconds
      read-timeout: 60000
      
      # Maximum idle time in milliseconds
      max-idle-time: 300000
      
      # Connection validation
      validate-connections: true
      validation-query-timeout: 5000
```

### File Streaming Optimization

```yaml
zhglxt:
  file-manager:
    streaming:
      # Buffer size for file streaming
      buffer-size: 8192
      
      # Enable range requests for large files
      range-requests: true
      
      # Compression for text files
      compression:
        enabled: true
        min-size: 1024
        types:
          - text/plain
          - text/html
          - text/css
          - application/javascript
```

## Monitoring and Maintenance

### Health Checks

The file manager provides health check endpoints:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always
      
zhglxt:
  file-manager:
    health:
      storage-check: true
      cache-check: true
      disk-space-check: true
      disk-space-threshold: 85  # percentage
```

Access health information at:
- `/actuator/health` - Overall health status
- `/file-manager/health` - File manager specific health

### Metrics and Monitoring

Configure metrics collection:

```yaml
zhglxt:
  file-manager:
    metrics:
      enabled: true
      
      # Metrics to collect
      collect:
        file-operations: true
        storage-usage: true
        performance-stats: true
        error-rates: true
        
      # Export to monitoring systems
      export:
        prometheus: true
        jmx: true
```

Available metrics:
- `file_manager_uploads_total` - Total number of uploads
- `file_manager_downloads_total` - Total number of downloads
- `file_manager_storage_usage_bytes` - Storage usage in bytes
- `file_manager_operation_duration_seconds` - Operation duration
- `file_manager_errors_total` - Total number of errors

### Logging Configuration

Configure detailed logging:

```yaml
logging:
  level:
    com.zhglxt.fileManager: INFO
    com.zhglxt.fileManager.service: DEBUG
    com.zhglxt.fileManager.security: WARN
    
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    
zhglxt:
  file-manager:
    logging:
      # Log file operations
      audit-logging: true
      
      # Log security events
      security-logging: true
      
      # Log performance metrics
      performance-logging: true
      
      # Separate log files
      separate-log-files: true
      log-directory: "/var/log/zhglxt"
```

### Maintenance Tasks

#### Cleanup Tasks
```yaml
zhglxt:
  file-manager:
    maintenance:
      # Cleanup orphaned thumbnails
      thumbnail-cleanup:
        enabled: true
        schedule: "0 2 * * *"  # Daily at 2 AM
        
      # Cleanup temporary files
      temp-file-cleanup:
        enabled: true
        schedule: "0 */6 * * *"  # Every 6 hours
        max-age: 24  # hours
        
      # Storage usage reports
      usage-reports:
        enabled: true
        schedule: "0 0 * * 0"  # Weekly on Sunday
        recipients:
          - admin@example.com
```

#### Database Maintenance
```sql
-- Clean up old audit logs (run monthly)
DELETE FROM file_operation_audit 
WHERE created_date < DATE_SUB(NOW(), INTERVAL 6 MONTH);

-- Clean up old error logs (run weekly)
DELETE FROM file_manager_errors 
WHERE created_date < DATE_SUB(NOW(), INTERVAL 1 MONTH);

-- Update storage statistics (run daily)
CALL update_storage_statistics();
```

## Troubleshooting

### Common Issues

#### File Upload Failures

**Symptoms:**
- Files fail to upload
- Error messages about file size or type
- Upload progress stops

**Diagnosis:**
```bash
# Check disk space
df -h /opt/zhglxt/files

# Check file permissions
ls -la /opt/zhglxt/files

# Check application logs
tail -f /var/log/zhglxt/file-manager.log

# Test storage backend connectivity
curl -I https://s3.amazonaws.com/zhglxt-files/
```

**Solutions:**
1. Verify file size limits in configuration
2. Check allowed file extensions
3. Ensure sufficient disk space
4. Verify storage backend connectivity
5. Check user permissions

#### Performance Issues

**Symptoms:**
- Slow file operations
- High memory usage
- Timeout errors

**Diagnosis:**
```bash
# Check system resources
top
free -h
iostat -x 1

# Check application metrics
curl http://localhost:8080/actuator/metrics/file_manager_operation_duration_seconds

# Check database performance
SHOW PROCESSLIST;
EXPLAIN SELECT * FROM file_operation_audit WHERE user_id = 'user123';
```

**Solutions:**
1. Enable caching
2. Increase thread pool sizes
3. Optimize database queries
4. Use CDN for cloud storage
5. Enable async processing

#### Storage Backend Issues

**Symptoms:**
- Cannot connect to cloud storage
- Authentication errors
- Slow cloud operations

**Diagnosis:**
```bash
# Test AWS S3 connectivity
aws s3 ls s3://zhglxt-files/

# Test Alibaba OSS connectivity
ossutil ls oss://zhglxt-files/

# Check network connectivity
ping s3.amazonaws.com
traceroute s3.amazonaws.com
```

**Solutions:**
1. Verify credentials and permissions
2. Check network connectivity
3. Verify bucket configuration
4. Enable connection pooling
5. Configure retry policies

### Error Codes and Solutions

| Error Code | Description | Solution |
|------------|-------------|----------|
| FM001 | File too large | Increase max-file-size or compress file |
| FM002 | Invalid file type | Add extension to allowed-extensions |
| FM003 | Permission denied | Check user permissions and roles |
| FM004 | Storage full | Clean up files or increase storage |
| FM005 | Storage backend error | Check storage configuration and connectivity |
| FM006 | Thumbnail generation failed | Check image processing libraries |
| FM007 | Watermark application failed | Verify watermark configuration |
| FM008 | Rate limit exceeded | Reduce request frequency or increase limits |
| FM009 | File not found | Verify file path and existence |
| FM010 | Configuration error | Check application configuration |

### Debug Mode

Enable debug mode for detailed troubleshooting:

```yaml
zhglxt:
  file-manager:
    debug:
      enabled: true
      log-requests: true
      log-responses: true
      log-storage-operations: true
      log-security-checks: true
      
logging:
  level:
    com.zhglxt.fileManager: DEBUG
    org.springframework.web: DEBUG
```

## API Reference

### REST API Endpoints

#### File Operations
- `POST /file-manager/api/files/upload` - Upload files
- `GET /file-manager/api/files/download` - Download file
- `GET /file-manager/api/files/list` - List files
- `DELETE /file-manager/api/files/delete` - Delete files
- `POST /file-manager/api/files/move` - Move files
- `GET /file-manager/api/files/preview` - Preview file

#### elFinder Connector
- `GET /file-manager/connector` - elFinder GET requests
- `POST /file-manager/connector` - elFinder POST requests

#### Monitoring
- `GET /file-manager/health` - Health check
- `GET /actuator/health` - Spring Boot health
- `GET /actuator/metrics` - Application metrics

### Configuration API

For dynamic configuration updates:

```java
@RestController
@RequestMapping("/file-manager/admin")
public class FileManagerAdminController {
    
    @PostMapping("/config/reload")
    public ResponseEntity<String> reloadConfiguration() {
        // Reload configuration
    }
    
    @GetMapping("/config/current")
    public ResponseEntity<FileManagerProperties> getCurrentConfiguration() {
        // Return current configuration
    }
    
    @PostMapping("/maintenance/cleanup")
    public ResponseEntity<String> runCleanup() {
        // Run maintenance cleanup
    }
}
```

### Integration Examples

#### Custom Storage Backend
```java
@Component
public class CustomStorageService implements StorageService {
    
    @Override
    public String storeFile(InputStream inputStream, String fileName, String directory) {
        // Custom storage implementation
    }
    
    // Implement other methods...
}
```

#### Custom Permission Evaluator
```java
@Component
public class CustomPermissionEvaluator implements FilePermissionEvaluator {
    
    @Override
    public boolean hasFilePermission(String userId, String filePath, FileOperation operation) {
        // Custom permission logic
    }
}
```

---

For additional support or advanced configuration options, refer to the source code documentation or contact the development team.