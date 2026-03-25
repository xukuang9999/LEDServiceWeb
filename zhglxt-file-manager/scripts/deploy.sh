#!/bin/bash

# zhglxt File Manager Deployment Script
# This script handles the deployment of the file manager module

set -e

# Configuration
APP_NAME="zhglxt-file-manager"
APP_VERSION="1.0.0"
DEPLOY_ENV="${1:-production}"
BASE_DIR="/opt/zhglxt"
APP_DIR="${BASE_DIR}/app"
CONFIG_DIR="${BASE_DIR}/config"
LOG_DIR="${BASE_DIR}/logs"
FILES_DIR="${BASE_DIR}/files"
BACKUP_DIR="${BASE_DIR}/backup"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Logging function
log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')] $1${NC}"
}

warn() {
    echo -e "${YELLOW}[$(date +'%Y-%m-%d %H:%M:%S')] WARNING: $1${NC}"
}

error() {
    echo -e "${RED}[$(date +'%Y-%m-%d %H:%M:%S')] ERROR: $1${NC}"
    exit 1
}

# Check if running as root
check_root() {
    if [[ $EUID -eq 0 ]]; then
        error "This script should not be run as root for security reasons"
    fi
}

# Check prerequisites
check_prerequisites() {
    log "Checking prerequisites..."
    
    # Check Java version
    if ! command -v java &> /dev/null; then
        error "Java is not installed"
    fi
    
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
    if [[ $JAVA_VERSION -lt 17 ]]; then
        error "Java 17 or higher is required. Current version: $JAVA_VERSION"
    fi
    
    # Check Maven
    if ! command -v mvn &> /dev/null; then
        error "Maven is not installed"
    fi
    
    # Check disk space (require at least 2GB free)
    AVAILABLE_SPACE=$(df ${BASE_DIR} | awk 'NR==2 {print $4}')
    if [[ $AVAILABLE_SPACE -lt 2097152 ]]; then
        error "Insufficient disk space. At least 2GB required."
    fi
    
    log "Prerequisites check passed"
}

# Create directory structure
create_directories() {
    log "Creating directory structure..."
    
    sudo mkdir -p ${APP_DIR}
    sudo mkdir -p ${CONFIG_DIR}
    sudo mkdir -p ${LOG_DIR}
    sudo mkdir -p ${FILES_DIR}
    sudo mkdir -p ${BACKUP_DIR}
    sudo mkdir -p ${FILES_DIR}/thumbnails
    sudo mkdir -p ${FILES_DIR}/temp
    
    # Set permissions
    sudo chown -R $(whoami):$(whoami) ${BASE_DIR}
    chmod 755 ${BASE_DIR}
    chmod 755 ${APP_DIR}
    chmod 755 ${CONFIG_DIR}
    chmod 755 ${LOG_DIR}
    chmod 755 ${FILES_DIR}
    chmod 755 ${BACKUP_DIR}
    
    log "Directory structure created"
}

# Build application
build_application() {
    log "Building application..."
    
    if [[ ! -f "pom.xml" ]]; then
        error "pom.xml not found. Please run this script from the project root directory."
    fi
    
    # Clean and build
    mvn clean package -DskipTests -Dspring.profiles.active=${DEPLOY_ENV}
    
    if [[ ! -f "target/${APP_NAME}-${APP_VERSION}.jar" ]]; then
        error "Build failed. JAR file not found."
    fi
    
    log "Application built successfully"
}

# Deploy application
deploy_application() {
    log "Deploying application..."
    
    # Backup existing application if it exists
    if [[ -f "${APP_DIR}/${APP_NAME}.jar" ]]; then
        log "Backing up existing application..."
        cp ${APP_DIR}/${APP_NAME}.jar ${BACKUP_DIR}/${APP_NAME}-$(date +%Y%m%d-%H%M%S).jar
    fi
    
    # Copy new application
    cp target/${APP_NAME}-${APP_VERSION}.jar ${APP_DIR}/${APP_NAME}.jar
    chmod 755 ${APP_DIR}/${APP_NAME}.jar
    
    log "Application deployed successfully"
}

# Deploy configuration
deploy_configuration() {
    log "Deploying configuration..."
    
    # Copy configuration files
    if [[ -f "src/main/resources/application-${DEPLOY_ENV}.yml" ]]; then
        cp src/main/resources/application-${DEPLOY_ENV}.yml ${CONFIG_DIR}/application.yml
    else
        warn "Environment-specific configuration not found. Using default."
        cp src/main/resources/application-file-manager.yml ${CONFIG_DIR}/application.yml
    fi
    
    # Update configuration with environment-specific values
    sed -i "s|/tmp/zhglxt/files|${FILES_DIR}|g" ${CONFIG_DIR}/application.yml
    sed -i "s|/tmp/zhglxt/logs|${LOG_DIR}|g" ${CONFIG_DIR}/application.yml
    
    log "Configuration deployed successfully"
}

# Create systemd service
create_service() {
    log "Creating systemd service..."
    
    sudo tee /etc/systemd/system/${APP_NAME}.service > /dev/null <<EOF
[Unit]
Description=zhglxt File Manager Service
After=network.target

[Service]
Type=simple
User=$(whoami)
Group=$(whoami)
WorkingDirectory=${APP_DIR}
ExecStart=/usr/bin/java -jar ${APP_DIR}/${APP_NAME}.jar --spring.config.location=${CONFIG_DIR}/application.yml
ExecStop=/bin/kill -TERM \$MAINPID
Restart=on-failure
RestartSec=10
StandardOutput=journal
StandardError=journal
SyslogIdentifier=${APP_NAME}

# Environment variables
Environment=JAVA_OPTS="-Xms512m -Xmx2g -XX:+UseG1GC"
Environment=SPRING_PROFILES_ACTIVE=${DEPLOY_ENV}
Environment=LOG_PATH=${LOG_DIR}

# Security settings
NoNewPrivileges=true
PrivateTmp=true
ProtectSystem=strict
ProtectHome=true
ReadWritePaths=${BASE_DIR}

[Install]
WantedBy=multi-user.target
EOF

    sudo systemctl daemon-reload
    sudo systemctl enable ${APP_NAME}
    
    log "Systemd service created and enabled"
}

# Setup log rotation
setup_log_rotation() {
    log "Setting up log rotation..."
    
    sudo tee /etc/logrotate.d/${APP_NAME} > /dev/null <<EOF
${LOG_DIR}/*.log {
    daily
    missingok
    rotate 30
    compress
    delaycompress
    notifempty
    create 644 $(whoami) $(whoami)
    postrotate
        systemctl reload ${APP_NAME} > /dev/null 2>&1 || true
    endscript
}
EOF

    log "Log rotation configured"
}

# Setup monitoring
setup_monitoring() {
    log "Setting up monitoring..."
    
    # Create health check script
    tee ${BASE_DIR}/health-check.sh > /dev/null <<'EOF'
#!/bin/bash

HEALTH_URL="http://localhost:8080/file-manager/health"
TIMEOUT=10

response=$(curl -s -w "%{http_code}" -o /dev/null --max-time $TIMEOUT $HEALTH_URL)

if [[ $response -eq 200 ]]; then
    echo "OK: File Manager is healthy"
    exit 0
else
    echo "CRITICAL: File Manager health check failed (HTTP $response)"
    exit 2
fi
EOF

    chmod +x ${BASE_DIR}/health-check.sh
    
    # Create monitoring cron job
    (crontab -l 2>/dev/null; echo "*/5 * * * * ${BASE_DIR}/health-check.sh >> ${LOG_DIR}/health-check.log 2>&1") | crontab -
    
    log "Monitoring configured"
}

# Start application
start_application() {
    log "Starting application..."
    
    sudo systemctl start ${APP_NAME}
    
    # Wait for application to start
    log "Waiting for application to start..."
    for i in {1..30}; do
        if curl -s http://localhost:8080/file-manager/health > /dev/null; then
            log "Application started successfully"
            return 0
        fi
        sleep 2
    done
    
    error "Application failed to start within 60 seconds"
}

# Verify deployment
verify_deployment() {
    log "Verifying deployment..."
    
    # Check service status
    if ! sudo systemctl is-active --quiet ${APP_NAME}; then
        error "Service is not running"
    fi
    
    # Check health endpoint
    if ! curl -s http://localhost:8080/file-manager/health | grep -q "UP"; then
        error "Health check failed"
    fi
    
    # Check file operations
    if ! curl -s http://localhost:8080/file-manager/api/files/list > /dev/null; then
        warn "File operations endpoint not responding"
    fi
    
    log "Deployment verification completed successfully"
}

# Cleanup function
cleanup() {
    log "Cleaning up temporary files..."
    rm -rf target/
    log "Cleanup completed"
}

# Main deployment function
main() {
    log "Starting deployment of ${APP_NAME} v${APP_VERSION} for ${DEPLOY_ENV} environment"
    
    check_root
    check_prerequisites
    create_directories
    build_application
    deploy_application
    deploy_configuration
    create_service
    setup_log_rotation
    setup_monitoring
    start_application
    verify_deployment
    cleanup
    
    log "Deployment completed successfully!"
    log "Application is running at: http://localhost:8080/file-manager"
    log "Health check: http://localhost:8080/file-manager/health"
    log "Logs location: ${LOG_DIR}"
    log "Files location: ${FILES_DIR}"
    
    echo ""
    echo "Next steps:"
    echo "1. Configure your web server (nginx/apache) to proxy requests"
    echo "2. Set up SSL certificates"
    echo "3. Configure backup procedures"
    echo "4. Review and adjust configuration in ${CONFIG_DIR}/application.yml"
    echo "5. Set up monitoring alerts"
}

# Handle script interruption
trap cleanup EXIT

# Run main function
main "$@"