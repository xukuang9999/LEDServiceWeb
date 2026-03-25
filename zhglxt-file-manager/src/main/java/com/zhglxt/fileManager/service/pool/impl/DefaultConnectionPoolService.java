package com.zhglxt.fileManager.service.pool.impl;

import com.zhglxt.fileManager.service.pool.ConnectionPoolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * Default implementation of connection pool service
 * 
 * @author zhglxt
 */
@Service
public class DefaultConnectionPoolService implements ConnectionPoolService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultConnectionPoolService.class);

    private final BlockingQueue<PooledConnection> connectionPool;
    private final int maxConnections;
    private final long connectionTimeout;
    
    // Statistics
    private final AtomicInteger activeConnections = new AtomicInteger(0);
    private final AtomicInteger totalConnections = new AtomicInteger(0);
    private final AtomicLong connectionsCreated = new AtomicLong(0);
    private final AtomicLong connectionsDestroyed = new AtomicLong(0);
    private final AtomicLong totalWaitTime = new AtomicLong(0);
    private final AtomicLong totalUsageTime = new AtomicLong(0);
    private final AtomicLong operationCount = new AtomicLong(0);

    private volatile boolean shutdown = false;

    public DefaultConnectionPoolService(
            @Value("${zhglxt.file-manager.storage.max-connections:50}") int maxConnections,
            @Value("${zhglxt.file-manager.storage.connection-timeout:30000}") long connectionTimeout) {
        
        this.maxConnections = maxConnections;
        this.connectionTimeout = connectionTimeout;
        this.connectionPool = new ArrayBlockingQueue<>(maxConnections);
        
        logger.info("Initialized connection pool with {} max connections and {}ms timeout", 
                   maxConnections, connectionTimeout);
    }

    @Override
    public <T> CompletableFuture<T> executeWithConnection(Function<Object, T> operation) {
        return executeWithConnection(operation, connectionTimeout);
    }

    @Override
    public <T> CompletableFuture<T> executeWithConnection(Function<Object, T> operation, long timeoutMs) {
        if (shutdown) {
            return CompletableFuture.failedFuture(new IllegalStateException("Connection pool is shutdown"));
        }

        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            PooledConnection connection = null;
            
            try {
                // Get connection from pool
                connection = getConnection(timeoutMs);
                if (connection == null) {
                    throw new RuntimeException("Failed to acquire connection within timeout");
                }
                
                long waitTime = System.currentTimeMillis() - startTime;
                totalWaitTime.addAndGet(waitTime);
                
                // Execute operation
                long operationStart = System.currentTimeMillis();
                T result = operation.apply(connection.getConnection());
                long operationTime = System.currentTimeMillis() - operationStart;
                
                totalUsageTime.addAndGet(operationTime);
                operationCount.incrementAndGet();
                
                return result;
                
            } catch (Exception e) {
                logger.error("Error executing operation with pooled connection", e);
                throw new RuntimeException("Operation failed", e);
            } finally {
                if (connection != null) {
                    returnConnection(connection);
                }
            }
        });
    }

    @Override
    public PoolStats getPoolStats() {
        return new DefaultPoolStats();
    }

    @Override
    public CompletableFuture<Void> warmUp() {
        return CompletableFuture.runAsync(() -> {
            logger.info("Warming up connection pool...");
            
            int warmupConnections = Math.min(maxConnections / 2, 10);
            for (int i = 0; i < warmupConnections; i++) {
                try {
                    PooledConnection connection = createConnection();
                    connectionPool.offer(connection);
                } catch (Exception e) {
                    logger.warn("Failed to create warmup connection {}", i + 1, e);
                }
            }
            
            logger.info("Connection pool warmup completed with {} connections", connectionPool.size());
        });
    }

    @Override
    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down connection pool...");
        shutdown = true;
        
        // Close all connections
        PooledConnection connection;
        while ((connection = connectionPool.poll()) != null) {
            closeConnection(connection);
        }
        
        logger.info("Connection pool shutdown completed");
    }

    private PooledConnection getConnection(long timeoutMs) {
        try {
            // Try to get existing connection from pool
            PooledConnection connection = connectionPool.poll(timeoutMs, TimeUnit.MILLISECONDS);
            
            if (connection != null) {
                // Validate connection
                if (connection.isValid()) {
                    activeConnections.incrementAndGet();
                    return connection;
                } else {
                    // Connection is invalid, close it and create new one
                    closeConnection(connection);
                }
            }
            
            // Create new connection if pool is not at max capacity
            if (totalConnections.get() < maxConnections) {
                connection = createConnection();
                if (connection != null) {
                    activeConnections.incrementAndGet();
                    return connection;
                }
            }
            
            return null;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    private void returnConnection(PooledConnection connection) {
        activeConnections.decrementAndGet();
        
        if (shutdown || !connection.isValid()) {
            closeConnection(connection);
            return;
        }
        
        // Return connection to pool
        if (!connectionPool.offer(connection)) {
            // Pool is full, close the connection
            closeConnection(connection);
        }
    }

    private PooledConnection createConnection() {
        try {
            // This is a mock implementation - in real scenario, this would create
            // actual cloud storage client connections (S3, OSS, COS, etc.)
            Object mockConnection = new Object();
            
            PooledConnection pooledConnection = new PooledConnection(mockConnection);
            totalConnections.incrementAndGet();
            connectionsCreated.incrementAndGet();
            
            logger.debug("Created new pooled connection, total: {}", totalConnections.get());
            return pooledConnection;
            
        } catch (Exception e) {
            logger.error("Failed to create new connection", e);
            return null;
        }
    }

    private void closeConnection(PooledConnection connection) {
        try {
            connection.close();
            totalConnections.decrementAndGet();
            connectionsDestroyed.incrementAndGet();
            
            logger.debug("Closed pooled connection, total: {}", totalConnections.get());
            
        } catch (Exception e) {
            logger.warn("Error closing connection", e);
        }
    }

    /**
     * Pooled connection wrapper
     */
    private static class PooledConnection {
        private final Object connection;
        private final long createdTime;
        private volatile boolean closed = false;

        public PooledConnection(Object connection) {
            this.connection = connection;
            this.createdTime = System.currentTimeMillis();
        }

        public Object getConnection() {
            return connection;
        }

        public boolean isValid() {
            // Check if connection is still valid (not closed, not expired, etc.)
            if (closed) {
                return false;
            }
            
            // Check if connection is too old (e.g., older than 1 hour)
            long maxAge = 60 * 60 * 1000; // 1 hour
            return (System.currentTimeMillis() - createdTime) < maxAge;
        }

        public void close() {
            closed = true;
            // Close actual connection resources here
        }
    }

    /**
     * Default pool statistics implementation
     */
    private class DefaultPoolStats implements PoolStats {
        @Override
        public int getActiveConnections() {
            return activeConnections.get();
        }

        @Override
        public int getIdleConnections() {
            return connectionPool.size();
        }

        @Override
        public int getTotalConnections() {
            return totalConnections.get();
        }

        @Override
        public int getMaxConnections() {
            return maxConnections;
        }

        @Override
        public long getConnectionsCreated() {
            return connectionsCreated.get();
        }

        @Override
        public long getConnectionsDestroyed() {
            return connectionsDestroyed.get();
        }

        @Override
        public double getAverageWaitTime() {
            long operations = operationCount.get();
            return operations > 0 ? (double) totalWaitTime.get() / operations : 0.0;
        }

        @Override
        public double getAverageUsageTime() {
            long operations = operationCount.get();
            return operations > 0 ? (double) totalUsageTime.get() / operations : 0.0;
        }
    }
}