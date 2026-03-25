# zhglxt File Manager - Deployment Guide

## Table of Contents
1. [Quick Start](#quick-start)
2. [Traditional Deployment](#traditional-deployment)
3. [Docker Deployment](#docker-deployment)
4. [Configuration](#configuration)
5. [Monitoring Setup](#monitoring-setup)
6. [Security Considerations](#security-considerations)
7. [Troubleshooting](#troubleshooting)

## Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- zhglxt-web parent application

### Basic Deployment
```bash
# Clone the repository
git clone https://github.com/zhglxt/zhglxt-springBoot3.git
cd zhglxt-springBoot3

# Build the application
mvn clean package -DskipTests

# Run with default configuration
java -jar zhglxt-web/target/zhglxt-web-*.jar
```

The file manager will be available at: `http://localhost:8080/file-manager`

## Traditional Deployment

### Using the Deployment Script

The provided deployment script automates the entire deployment process:

```bash
# Make the script executable
chmod +x zhglxt-file-manager/scripts/deploy.sh

# Deploy to production
./zhglxt-file-manager/scripts/deploy.sh production

# Deploy to development
./zhglxt-file-manager/scripts/deploy.sh development
```

### Manual Deployment Steps

1. **Prepare the Environment**
```bash
# Create application user
sudo useradd -r -s /bin/false zhglxt

# Create directory structure
sudo mkdir -p /opt/zhglxt/{app,config,logs,files,backup}
sudo chown -R zhglxt:zhglxt /opt/zhglxt
```

2. **Build the Application**
```bash
mvn clean package -DskipTests -Dspring.profiles.active=production
```

3. **Deploy Application Files**
```bash
# Copy JAR file
cp target/zhglxt-file-manager-*.jar /opt/zhglxt/app/zhglxt-file-manager.jar

# Copy configuration
cp config/application-production.yml /opt/zhglxt/config/application.yml
```

4. **Create Systemd Service**
```bash
sudo cp scripts/zhglxt-file-manager.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable zhglxt-file-manager
sudo systemctl start zhglxt-file-manager
```

5. **Configure Web Server (Nginx)**
```bash
sudo cp config/nginx.conf /etc/nginx/sites-available/zhglxt-file-manager
sudo ln -s /etc/nginx/sites-available/zhglxt-file-manager /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

## Docker Deployment

### Using Docker Compose (Recommended)

1. **Create docker-compose.yml**
```yaml
version: '3.8'

services:
  zhglxt-file-manager:
    image: zhglxt-file-manager:1.0.0
    container_name: zhglxt-file-manager
    restart: unless-stopped
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=production
      - JAVA_OPTS=-Xms512m -Xmx2g -XX:+UseG1GC
    volumes:
      - ./config/application.yml:/opt/zhglxt/config/application.yml:ro
      - zhglxt-files:/opt/zhglxt/files
      - zhglxt-logs:/opt/zhglxt/logs
    networks:
      - zhglxt-network

volumes:
  zhglxt-files:
  zhglxt-logs:

networks:
  zhglxt-network:
```

2. **Deploy with Docker Compose**
```bash
# Build and deploy
./zhglxt-file-manager/scripts/docker-deploy.sh production compose

# Or manually
docker-compose up -d
```

### Using Docker Run

```bash
# Build the image
docker build -t zhglxt-file-manager:1.0.0 .

# Run the container
docker run -d \
  --name zhglxt-file-manager \
  --restart unless-stopped \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=production \
  -v $(pwd)/config/application.yml:/opt/zhglxt/config/application.yml:ro \
  -v zhglxt-files:/opt/zhglxt/files \
  -v zhglxt-logs:/opt/zhglxt/logs \
  zhglxt-file-manager:1.0.0
```

### Docker Commands Reference

```bash
# View logs
docker logs -f zhglxt-file-manager

# Stop container
docker stop zhglxt-file-manager

# Start container
docker start zhglxt-file-manager

# Restart container
docker restart zhglxt-file-manager

# Remove container and image
docker stop zhglxt-file-manager
docker rm zhglxt-file-manager
docker rmi zhglxt-file-manager:1.0.0
```

## Configuration

### Environment Variables

Key environment variables for deployment:

```bash
# Database
export DB_USERNAME=zhglxt
export DB_PASSWORD=your_password

# Redis (for caching)
export REDIS_HOST=localhost
export REDIS_PORT=6379
export REDIS_PASSWORD=your_redis_password

# Storage Backend
export STORAGE_TYPE=local  # or aws-s3, alibaba-oss, tencent-cos

# AWS S3 (if using)
export AWS_ACCESS_KEY=your_access_key
export AWS_SECRET_KEY=your_secret_key
export AWS_S3_BUCKET=zhglxt-files
export AWS_S3_REGION=us-east-1

# Watermark
export WATERMARK_ENABLED=true
export WATERMARK_TEXT="© Your Company"

# Cache
export CACHE_TYPE=redis  # or memory
```

### Configuration Files

#### Production Configuration
Location: `/opt/zhglxt/config/application.yml`

Key settings to review:
- Database connection
- Storage backend configuration
- Security settings
- Performance tuning
- Logging configuration

#### Development Configuration
Use `application-development.yml` for local development with relaxed security and debugging enabled.

### SSL/TLS Configuration

For production deployments, configure SSL/TLS:

1. **Obtain SSL Certificate**
```bash
# Using Let's Encrypt
sudo certbot --nginx -d your-domain.com
```

2. **Update Nginx Configuration**
```nginx
server {
    listen 443 ssl http2;
    server_name your-domain.com;
    
    ssl_certificate /etc/letsencrypt/live/your-domain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/your-domain.com/privkey.pem;
    
    # ... rest of configuration
}
```

## Monitoring Setup

### Health Checks

The application provides several health check endpoints:

- `/file-manager/health` - Overall health status
- `/file-manager/ready` - Readiness check
- `/file-manager/live` - Liveness check
- `/actuator/health` - Spring Boot health

### Prometheus Metrics

Enable Prometheus metrics in configuration:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

Access metrics at: `/actuator/prometheus`

### Log Monitoring

Configure log aggregation:

```yaml
logging:
  file:
    name: "/opt/zhglxt/logs/file-manager.log"
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

### Alerting

Set up monitoring alerts for:
- Service availability
- Disk space usage
- Memory usage
- Error rates
- Response times

Example Prometheus alert rules:

```yaml
groups:
- name: file-manager
  rules:
  - alert: FileManagerDown
    expr: up{job="file-manager"} == 0
    for: 1m
    labels:
      severity: critical
    annotations:
      summary: "File Manager is down"
      
  - alert: HighDiskUsage
    expr: file_manager_disk_usage_percent > 85
    for: 5m
    labels:
      severity: warning
    annotations:
      summary: "High disk usage detected"
```

## Security Considerations

### Network Security

1. **Firewall Configuration**
```bash
# Allow only necessary ports
sudo ufw allow 22/tcp    # SSH
sudo ufw allow 80/tcp    # HTTP
sudo ufw allow 443/tcp   # HTTPS
sudo ufw enable
```

2. **Reverse Proxy**
Always use a reverse proxy (Nginx/Apache) in production:
- SSL termination
- Rate limiting
- Request filtering
- Static file serving

### Application Security

1. **File Upload Security**
- Enable file type validation
- Set file size limits
- Enable virus scanning
- Sanitize file names

2. **Access Control**
- Configure proper user permissions
- Enable CSRF protection
- Set up rate limiting
- Use strong authentication

3. **Data Protection**
- Enable encryption at rest
- Use secure connections
- Regular security updates
- Audit logging

### Configuration Security

```yaml
zhglxt:
  file-manager:
    security:
      scan-uploads: true
      rate-limiting:
        enabled: true
        requests-per-minute: 60
      csrf-protection:
        enabled: true
      validation:
        check-file-headers: true
        sanitize-filenames: true
```

## Troubleshooting

### Common Issues

#### Service Won't Start

1. **Check logs**
```bash
sudo journalctl -u zhglxt-file-manager -f
```

2. **Verify configuration**
```bash
java -jar /opt/zhglxt/app/zhglxt-file-manager.jar --spring.config.location=/opt/zhglxt/config/application.yml --debug
```

3. **Check permissions**
```bash
ls -la /opt/zhglxt/
sudo chown -R zhglxt:zhglxt /opt/zhglxt/
```

#### File Upload Issues

1. **Check disk space**
```bash
df -h /opt/zhglxt/files
```

2. **Verify permissions**
```bash
ls -la /opt/zhglxt/files/
sudo chmod 755 /opt/zhglxt/files/
```

3. **Check file size limits**
```bash
# In nginx.conf
client_max_body_size 100M;

# In application.yml
spring:
  servlet:
    multipart:
      max-file-size: 100MB
      max-request-size: 100MB
```

#### Performance Issues

1. **Monitor resources**
```bash
top
free -h
iostat -x 1
```

2. **Check application metrics**
```bash
curl http://localhost:8080/actuator/metrics
```

3. **Optimize configuration**
- Increase JVM heap size
- Enable caching
- Use connection pooling
- Enable async processing

### Log Analysis

Important log locations:
- Application logs: `/opt/zhglxt/logs/file-manager.log`
- System logs: `/var/log/syslog`
- Nginx logs: `/var/log/nginx/`

Common log patterns to watch:
- `ERROR` - Application errors
- `OutOfMemoryError` - Memory issues
- `Connection refused` - Network issues
- `Permission denied` - File system issues

### Performance Tuning

#### JVM Tuning
```bash
export JAVA_OPTS="-Xms1g -Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
```

#### Database Tuning
- Connection pooling
- Query optimization
- Index optimization

#### Storage Optimization
- Use SSD for better I/O
- Enable compression
- Implement caching
- Use CDN for static files

### Backup and Recovery

#### Backup Strategy
```bash
#!/bin/bash
# Backup script
BACKUP_DIR="/opt/zhglxt/backup/$(date +%Y%m%d)"
mkdir -p $BACKUP_DIR

# Backup files
tar -czf $BACKUP_DIR/files.tar.gz /opt/zhglxt/files/

# Backup configuration
cp /opt/zhglxt/config/application.yml $BACKUP_DIR/

# Backup database (if applicable)
mysqldump -u zhglxt -p zhglxt > $BACKUP_DIR/database.sql
```

#### Recovery Process
1. Stop the service
2. Restore files from backup
3. Restore configuration
4. Restore database
5. Start the service
6. Verify functionality

---

For additional support, refer to the Administrator Guide or contact the development team.