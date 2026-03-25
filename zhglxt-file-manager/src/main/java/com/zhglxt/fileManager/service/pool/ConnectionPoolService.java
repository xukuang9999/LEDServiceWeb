package com.zhglxt.fileManager.service.pool;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Connection Pool Service Interface
 * Manages connection pooling for cloud storage operations
 * 
 * @author zhglxt
 */
public interface ConnectionPoolService {

    /**
     * Execute operation with pooled connection
     * 
     * @param operation operation to execute
     * @param <T> return type
     * @return future with operation result
     */
    <T> CompletableFuture<T> executeWithConnection(Function<Object, T> operation);

    /**
     * Execute operation with pooled connection and timeout
     * 
     * @param operation operation to execute
     * @param timeoutMs timeout in milliseconds
     * @param <T> return type
     * @return future with operation result
     */
    <T> CompletableFuture<T> executeWithConnection(Function<Object, T> operation, long timeoutMs);

    /**
     * Get pool statistics
     * 
     * @return pool statistics
     */
    PoolStats getPoolStats();

    /**
     * Warm up the connection pool
     * 
     * @return future that completes when warmup is done
     */
    CompletableFuture<Void> warmUp();

    /**
     * Shutdown the connection pool
     */
    void shutdown();

    /**
     * Pool statistics interface
     */
    interface PoolStats {
        int getActiveConnections();
        int getIdleConnections();
        int getTotalConnections();
        int getMaxConnections();
        long getConnectionsCreated();
        long getConnectionsDestroyed();
        double getAverageWaitTime();
        double getAverageUsageTime();
    }
}