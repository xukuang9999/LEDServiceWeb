package com.zhglxt.fileManager.actuator;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Custom metrics for file manager operations
 * 
 * @author zhglxt
 */
@Component
public class FileManagerMetrics {

    @Autowired
    private MeterRegistry meterRegistry;

    // Counters
    private Counter uploadCounter;
    private Counter downloadCounter;
    private Counter deleteCounter;
    private Counter errorCounter;

    // Timers
    private Timer uploadTimer;
    private Timer downloadTimer;
    private Timer thumbnailTimer;

    // Gauges
    private AtomicLong activeUploads = new AtomicLong(0);
    private AtomicLong activeDownloads = new AtomicLong(0);
    private AtomicLong totalFiles = new AtomicLong(0);
    private AtomicLong totalStorageUsed = new AtomicLong(0);

    @PostConstruct
    public void init() {
        // Initialize counters
        uploadCounter = Counter.builder("file_manager_uploads_total")
                .description("Total number of file uploads")
                .register(meterRegistry);

        downloadCounter = Counter.builder("file_manager_downloads_total")
                .description("Total number of file downloads")
                .register(meterRegistry);

        deleteCounter = Counter.builder("file_manager_deletes_total")
                .description("Total number of file deletions")
                .register(meterRegistry);

        errorCounter = Counter.builder("file_manager_errors_total")
                .description("Total number of errors")
                .register(meterRegistry);

        // Initialize timers
        uploadTimer = Timer.builder("file_manager_upload_duration")
                .description("File upload duration")
                .register(meterRegistry);

        downloadTimer = Timer.builder("file_manager_download_duration")
                .description("File download duration")
                .register(meterRegistry);

        thumbnailTimer = Timer.builder("file_manager_thumbnail_duration")
                .description("Thumbnail generation duration")
                .register(meterRegistry);

        // Initialize gauges
        Gauge.builder("file_manager_active_uploads", activeUploads, AtomicLong::doubleValue)
                .description("Number of active uploads")
                .register(meterRegistry);

        Gauge.builder("file_manager_active_downloads", activeDownloads, AtomicLong::doubleValue)
                .description("Number of active downloads")
                .register(meterRegistry);

        Gauge.builder("file_manager_total_files", totalFiles, AtomicLong::doubleValue)
                .description("Total number of files")
                .register(meterRegistry);

        Gauge.builder("file_manager_storage_usage_bytes", totalStorageUsed, AtomicLong::doubleValue)
                .description("Total storage usage in bytes")
                .register(meterRegistry);

        // JVM and system metrics
        Gauge.builder("file_manager_jvm_memory_used", this, FileManagerMetrics::getJvmMemoryUsed)
                .description("JVM memory usage")
                .register(meterRegistry);

        Gauge.builder("file_manager_disk_space_free", this, FileManagerMetrics::getFreeDiskSpace)
                .description("Free disk space")
                .register(meterRegistry);
    }

    // Counter methods
    public void incrementUploadCounter() {
        uploadCounter.increment();
    }

    public void incrementDownloadCounter() {
        downloadCounter.increment();
    }

    public void incrementDeleteCounter() {
        deleteCounter.increment();
    }

    public void incrementErrorCounter() {
        errorCounter.increment();
    }

    public void incrementErrorCounter(String errorType) {
        Counter.builder("file_manager_errors_total")
                .tag("type", errorType)
                .register(meterRegistry)
                .increment();
    }

    // Timer methods
    public Timer.Sample startUploadTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordUploadTime(Timer.Sample sample) {
        sample.stop(uploadTimer);
    }

    public Timer.Sample startDownloadTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordDownloadTime(Timer.Sample sample) {
        sample.stop(downloadTimer);
    }

    public Timer.Sample startThumbnailTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordThumbnailTime(Timer.Sample sample) {
        sample.stop(thumbnailTimer);
    }

    // Gauge methods
    public void incrementActiveUploads() {
        activeUploads.incrementAndGet();
    }

    public void decrementActiveUploads() {
        activeUploads.decrementAndGet();
    }

    public void incrementActiveDownloads() {
        activeDownloads.incrementAndGet();
    }

    public void decrementActiveDownloads() {
        activeDownloads.decrementAndGet();
    }

    public void updateTotalFiles(long count) {
        totalFiles.set(count);
    }

    public void updateStorageUsage(long bytes) {
        totalStorageUsed.set(bytes);
    }

    // Helper methods for gauges
    private double getJvmMemoryUsed() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    private double getFreeDiskSpace() {
        try {
            File root = new File(".");
            return root.getFreeSpace();
        } catch (Exception e) {
            return -1;
        }
    }

    // Custom metrics for specific operations
    public void recordFileSize(long size) {
        Gauge.builder("file_manager_last_upload_size", () -> (double) size)
                .description("Size of last uploaded file")
                .register(meterRegistry);
    }

    public void recordOperationDuration(String operation, long durationMs) {
        Timer.builder("file_manager_operation_duration")
                .tag("operation", operation)
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    // Getters for current values
    public long getActiveUploads() {
        return activeUploads.get();
    }

    public long getActiveDownloads() {
        return activeDownloads.get();
    }

    public long getTotalFiles() {
        return totalFiles.get();
    }

    public long getTotalStorageUsed() {
        return totalStorageUsed.get();
    }
}