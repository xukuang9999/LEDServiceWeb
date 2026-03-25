package com.zhglxt.fileManager.service.async.impl;

import com.zhglxt.fileManager.service.async.AsyncProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * Default implementation of async processing service
 * 
 * @author zhglxt
 */
@Service
public class DefaultAsyncProcessingService implements AsyncProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultAsyncProcessingService.class);

    private final ThreadPoolExecutor executor;
    private final AtomicLong taskIdGenerator = new AtomicLong(0);

    public DefaultAsyncProcessingService(
            @Value("${zhglxt.file-manager.performance.thread-pool-size:10}") int threadPoolSize,
            @Value("${zhglxt.file-manager.performance.queue-size:100}") int queueSize) {
        
        // Create priority-based thread pool
        PriorityBlockingQueue<Runnable> queue = new PriorityBlockingQueue<>(queueSize);
        
        this.executor = new ThreadPoolExecutor(
            threadPoolSize / 2, // core pool size
            threadPoolSize,     // maximum pool size
            60L,               // keep alive time
            TimeUnit.SECONDS,
            queue,
            r -> {
                Thread t = new Thread(r, "async-processing-" + taskIdGenerator.incrementAndGet());
                t.setDaemon(true);
                return t;
            }
        );

        logger.info("Initialized async processing service with {} threads and queue size {}", 
                   threadPoolSize, queueSize);
    }

    @Override
    public <T> CompletableFuture<T> executeAsync(Supplier<T> task) {
        return executeAsync(task, 0);
    }

    @Override
    public <T> CompletableFuture<T> executeAsync(Supplier<T> task, int priority) {
        CompletableFuture<T> future = new CompletableFuture<>();
        
        PriorityTask<T> priorityTask = new PriorityTask<>(task, future, priority);
        executor.execute(priorityTask);
        
        return future;
    }

    @Override
    public CompletableFuture<Void> executeAsync(Runnable task) {
        return executeAsync(task, 0);
    }

    @Override
    public CompletableFuture<Void> executeAsync(Runnable task, int priority) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        PriorityTask<Void> priorityTask = new PriorityTask<>(() -> {
            task.run();
            return null;
        }, future, priority);
        
        executor.execute(priorityTask);
        
        return future;
    }

    @Override
    public int getQueueSize() {
        return executor.getQueue().size();
    }

    @Override
    public int getActiveThreadCount() {
        return executor.getActiveCount();
    }

    @Override
    public long getCompletedTaskCount() {
        return executor.getCompletedTaskCount();
    }

    @Override
    public boolean isShutdown() {
        return executor.isShutdown();
    }

    @Override
    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down async processing service...");
        executor.shutdown();
        
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                logger.warn("Async processing service did not terminate gracefully, forcing shutdown");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.warn("Interrupted while waiting for async processing service shutdown");
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        logger.info("Async processing service shutdown complete");
    }

    @Override
    public void shutdownNow() {
        logger.info("Force shutting down async processing service...");
        executor.shutdownNow();
    }

    /**
     * Priority task wrapper for thread pool execution
     */
    private static class PriorityTask<T> implements Runnable, Comparable<PriorityTask<?>> {
        private final Supplier<T> task;
        private final CompletableFuture<T> future;
        private final int priority;
        private final long creationTime;

        public PriorityTask(Supplier<T> task, CompletableFuture<T> future, int priority) {
            this.task = task;
            this.future = future;
            this.priority = priority;
            this.creationTime = System.nanoTime();
        }

        @Override
        public void run() {
            try {
                T result = task.get();
                future.complete(result);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        }

        @Override
        public int compareTo(PriorityTask<?> other) {
            // Higher priority first
            int priorityComparison = Integer.compare(other.priority, this.priority);
            if (priorityComparison != 0) {
                return priorityComparison;
            }
            
            // If same priority, FIFO (earlier creation time first)
            return Long.compare(this.creationTime, other.creationTime);
        }
    }
}