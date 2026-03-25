package com.zhglxt.fileManager.service.async;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Async Processing Service Interface
 * Handles asynchronous processing of heavy operations
 * 
 * @author zhglxt
 */
public interface AsyncProcessingService {

    /**
     * Execute a task asynchronously
     * 
     * @param task task to execute
     * @param <T> return type
     * @return future with result
     */
    <T> CompletableFuture<T> executeAsync(Supplier<T> task);

    /**
     * Execute a task asynchronously with priority
     * 
     * @param task task to execute
     * @param priority task priority (higher = more priority)
     * @param <T> return type
     * @return future with result
     */
    <T> CompletableFuture<T> executeAsync(Supplier<T> task, int priority);

    /**
     * Execute a runnable task asynchronously
     * 
     * @param task task to execute
     * @return future that completes when task is done
     */
    CompletableFuture<Void> executeAsync(Runnable task);

    /**
     * Execute a runnable task asynchronously with priority
     * 
     * @param task task to execute
     * @param priority task priority (higher = more priority)
     * @return future that completes when task is done
     */
    CompletableFuture<Void> executeAsync(Runnable task, int priority);

    /**
     * Get current queue size
     * 
     * @return number of pending tasks
     */
    int getQueueSize();

    /**
     * Get active thread count
     * 
     * @return number of active threads
     */
    int getActiveThreadCount();

    /**
     * Get completed task count
     * 
     * @return number of completed tasks
     */
    long getCompletedTaskCount();

    /**
     * Check if service is shutdown
     * 
     * @return true if shutdown
     */
    boolean isShutdown();

    /**
     * Shutdown the service gracefully
     */
    void shutdown();

    /**
     * Force shutdown the service
     */
    void shutdownNow();
}