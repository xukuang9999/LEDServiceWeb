#!/bin/bash

# Docker deployment script for zhglxt File Manager
# This script builds and deploys the application using Docker

set -e

# Configuration
APP_NAME="zhglxt-file-manager"
APP_VERSION="1.0.0"
DOCKER_IMAGE="${APP_NAME}:${APP_VERSION}"
CONTAINER_NAME="${APP_NAME}-container"
DEPLOY_ENV="${1:-production}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

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

# Check Docker installation
check_docker() {
    log "Checking Docker installation..."
    
    if ! command -v docker &> /dev/null; then
        error "Docker is not installed"
    fi
    
    if ! docker info &> /dev/null; then
        error "Docker daemon is not running"
    fi
    
    log "Docker check passed"
}

# Build Docker image
build_image() {
    log "Building Docker image..."
    
    # Create Dockerfile if it doesn't exist
    if [[ ! -f "Dockerfile" ]]; then
        create_dockerfile
    fi
    
    docker build -t ${DOCKER_IMAGE} .
    
    log "Docker image built successfully"
}

# Create Dockerfile
create_dockerfile() {
    log "Creating Dockerfile..."
    
    cat > Dockerfile <<'EOF'
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Install required packages
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Copy application JAR
COPY target/zhglxt-file-manager-*.jar app.jar

# Create directories
RUN mkdir -p /opt/zhglxt/files /opt/zhglxt/logs /opt/zhglxt/config

# Set permissions
RUN useradd -r -s /bin/false zhglxt && \
    chown -R zhglxt:zhglxt /opt/zhglxt /app

# Switch to non-root user
USER zhglxt

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/file-manager/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
EOF

    log "Dockerfile created"
}

# Create docker-compose.yml
create_docker_compose() {
    log "Creating docker-compose.yml..."
    
    cat > docker-compose.yml <<EOF
version: '3.8'

services:
  zhglxt-file-manager:
    image: ${DOCKER_IMAGE}
    container_name: ${CONTAINER_NAME}
    restart: unless-stopped
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=${DEPLOY_ENV}
      - JAVA_OPTS=-Xms512m -Xmx2g -XX:+UseG1GC
    volumes:
      - ./config/application.yml:/opt/zhglxt/config/application.yml:ro
      - zhglxt-files:/opt/zhglxt/files
      - zhglxt-logs:/opt/zhglxt/logs
    networks:
      - zhglxt-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/file-manager/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s

volumes:
  zhglxt-files:
    driver: local
  zhglxt-logs:
    driver: local

networks:
  zhglxt-network:
    driver: bridge
EOF

    log "docker-compose.yml created"
}

# Deploy with Docker Compose
deploy_with_compose() {
    log "Deploying with Docker Compose..."
    
    # Stop existing containers
    if docker-compose ps | grep -q ${CONTAINER_NAME}; then
        log "Stopping existing containers..."
        docker-compose down
    fi
    
    # Start new containers
    docker-compose up -d
    
    log "Containers started successfully"
}

# Deploy with Docker run
deploy_with_docker() {
    log "Deploying with Docker run..."
    
    # Stop and remove existing container
    if docker ps -a | grep -q ${CONTAINER_NAME}; then
        log "Stopping existing container..."
        docker stop ${CONTAINER_NAME} || true
        docker rm ${CONTAINER_NAME} || true
    fi
    
    # Create volumes
    docker volume create zhglxt-files || true
    docker volume create zhglxt-logs || true
    
    # Run new container
    docker run -d \
        --name ${CONTAINER_NAME} \
        --restart unless-stopped \
        -p 8080:8080 \
        -e SPRING_PROFILES_ACTIVE=${DEPLOY_ENV} \
        -e JAVA_OPTS="-Xms512m -Xmx2g -XX:+UseG1GC" \
        -v $(pwd)/config/application.yml:/opt/zhglxt/config/application.yml:ro \
        -v zhglxt-files:/opt/zhglxt/files \
        -v zhglxt-logs:/opt/zhglxt/logs \
        ${DOCKER_IMAGE}
    
    log "Container started successfully"
}

# Wait for application to be ready
wait_for_app() {
    log "Waiting for application to be ready..."
    
    for i in {1..30}; do
        if docker exec ${CONTAINER_NAME} curl -s http://localhost:8080/file-manager/health > /dev/null 2>&1; then
            log "Application is ready"
            return 0
        fi
        sleep 2
    done
    
    error "Application failed to start within 60 seconds"
}

# Verify deployment
verify_deployment() {
    log "Verifying deployment..."
    
    # Check container status
    if ! docker ps | grep -q ${CONTAINER_NAME}; then
        error "Container is not running"
    fi
    
    # Check health endpoint
    if ! docker exec ${CONTAINER_NAME} curl -s http://localhost:8080/file-manager/health | grep -q "UP"; then
        error "Health check failed"
    fi
    
    log "Deployment verification completed successfully"
}

# Show logs
show_logs() {
    log "Showing application logs..."
    docker logs --tail 50 ${CONTAINER_NAME}
}

# Main function
main() {
    log "Starting Docker deployment of ${APP_NAME} v${APP_VERSION}"
    
    check_docker
    
    # Build application first
    if [[ ! -f "target/${APP_NAME}-${APP_VERSION}.jar" ]]; then
        log "Building application..."
        mvn clean package -DskipTests
    fi
    
    build_image
    
    # Choose deployment method
    if [[ -f "docker-compose.yml" ]] || [[ "$2" == "compose" ]]; then
        create_docker_compose
        deploy_with_compose
    else
        deploy_with_docker
    fi
    
    wait_for_app
    verify_deployment
    
    log "Docker deployment completed successfully!"
    log "Application is running at: http://localhost:8080/file-manager"
    log "Container name: ${CONTAINER_NAME}"
    
    echo ""
    echo "Useful Docker commands:"
    echo "  View logs: docker logs -f ${CONTAINER_NAME}"
    echo "  Stop container: docker stop ${CONTAINER_NAME}"
    echo "  Start container: docker start ${CONTAINER_NAME}"
    echo "  Remove container: docker rm ${CONTAINER_NAME}"
    echo "  View container stats: docker stats ${CONTAINER_NAME}"
    
    # Show recent logs
    show_logs
}

# Handle different commands
case "${2:-deploy}" in
    "build")
        check_docker
        build_image
        ;;
    "deploy")
        main "$@"
        ;;
    "logs")
        docker logs -f ${CONTAINER_NAME}
        ;;
    "stop")
        docker stop ${CONTAINER_NAME}
        ;;
    "start")
        docker start ${CONTAINER_NAME}
        ;;
    "restart")
        docker restart ${CONTAINER_NAME}
        ;;
    "remove")
        docker stop ${CONTAINER_NAME} || true
        docker rm ${CONTAINER_NAME} || true
        docker rmi ${DOCKER_IMAGE} || true
        ;;
    *)
        echo "Usage: $0 [environment] [command]"
        echo "Commands: build, deploy, logs, stop, start, restart, remove"
        echo "Environment: development, production (default: production)"
        exit 1
        ;;
esac