package com.zhglxt.fileManager.service.storage.impl;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.model.*;
import com.aliyun.oss.HttpMethod;
import com.zhglxt.fileManager.config.FileManagerProperties;
import com.zhglxt.fileManager.exception.StorageException;
import com.zhglxt.fileManager.service.storage.StorageFileInfo;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Alibaba Cloud OSS Storage Service Implementation
 * 
 * @author zhglxt
 */
@Service
@ConditionalOnClass(OSS.class)
@ConditionalOnProperty(name = "zhglxt.file-manager.storage.type", havingValue = "ALIBABA_OSS")
public class AlibabaOssStorageService extends AbstractCloudStorageService {

    private OSS ossClient;

    public AlibabaOssStorageService(FileManagerProperties properties) {
        super(properties);
    }

    @Override
    protected void validateSpecificConfiguration() {
        if (!StringUtils.hasText(storageConfig.getEndpoint())) {
            throw new StorageException("INVALID_CONFIG", "Endpoint is required for Alibaba OSS storage");
        }
    }

    @Override
    public void initializeClient() {
        try {
            CredentialsProvider credentialsProvider = new DefaultCredentialProvider(
                storageConfig.getAccessKey(), storageConfig.getSecretKey());

            ClientBuilderConfiguration clientConfig = new ClientBuilderConfiguration();
            clientConfig.setConnectionTimeout(storageConfig.getConnectionTimeout());
            clientConfig.setSocketTimeout(storageConfig.getReadTimeout());
            clientConfig.setMaxConnections(storageConfig.getMaxConnections());
            clientConfig.setSupportCname(StringUtils.hasText(storageConfig.getCustomDomain()));

            this.ossClient = new OSSClientBuilder().build(
                storageConfig.getEndpoint(),
                credentialsProvider,
                clientConfig
            );

            logger.info("Alibaba OSS client initialized successfully for bucket: {}", storageConfig.getBucketName());
        } catch (Exception e) {
            throw new StorageException("CLIENT_INIT_FAILED", 
                "Failed to initialize Alibaba OSS client: " + e.getMessage(), e);
        }
    }

    @Override
    public String getStorageTypeName() {
        return "Alibaba OSS";
    }

    @Override
    protected boolean performConnectionTest() throws Exception {
        return ossClient.doesBucketExist(storageConfig.getBucketName());
    }

    @Override
    protected Map<String, Object> getSpecificStorageStats() throws Exception {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            BucketInfo bucketInfo = ossClient.getBucketInfo(storageConfig.getBucketName());
            stats.put("storageClass", bucketInfo.getBucket().getStorageClass());
            stats.put("creationDate", bucketInfo.getBucket().getCreationDate());
            stats.put("location", bucketInfo.getBucket().getLocation());
            
            BucketVersioningConfiguration versioningConfig = 
                ossClient.getBucketVersioning(storageConfig.getBucketName());
            stats.put("versioningStatus", versioningConfig.getStatus());
        } catch (Exception e) {
            logger.debug("Could not retrieve some OSS bucket stats: {}", e.getMessage());
        }
        
        return stats;
    }

    @Override
    protected void performShutdown() throws Exception {
        if (ossClient != null) {
            ossClient.shutdown();
        }
    }

    @Override
    public String storeFile(InputStream inputStream, String fileName, String directory) {
        String objectKey = buildObjectKey(fileName, directory);
        
        return executeWithRetry(() -> {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(determineContentType(fileName));
            
            if (storageConfig.isServerSideEncryption()) {
                metadata.setServerSideEncryption(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION);
            }
            
            PutObjectRequest request = new PutObjectRequest(
                storageConfig.getBucketName(), objectKey, inputStream, metadata);
            
            // Note: Storage class setting may vary by OSS SDK version
            // For now, skip this setting
            
            ossClient.putObject(request);
            logger.debug("File stored successfully in OSS: {}", objectKey);
            return objectKey;
        }, "storeFile");
    }

    @Override
    public InputStream retrieveFile(String filePath) {
        String objectKey = sanitizeObjectKey(filePath);
        
        return executeWithRetry(() -> {
            GetObjectRequest request = new GetObjectRequest(storageConfig.getBucketName(), objectKey);
            OSSObject ossObject = ossClient.getObject(request);
            return ossObject.getObjectContent();
        }, "retrieveFile");
    }

    @Override
    public boolean deleteFile(String filePath) {
        String objectKey = sanitizeObjectKey(filePath);
        
        return executeWithRetry(() -> {
            ossClient.deleteObject(storageConfig.getBucketName(), objectKey);
            logger.debug("File deleted successfully from OSS: {}", objectKey);
            return true;
        }, "deleteFile");
    }

    @Override
    public boolean moveFile(String sourcePath, String targetPath) {
        String sourceKey = sanitizeObjectKey(sourcePath);
        String targetKey = sanitizeObjectKey(targetPath);
        
        return executeWithRetry(() -> {
            // Copy to new location
            CopyObjectRequest copyRequest = new CopyObjectRequest(
                storageConfig.getBucketName(), sourceKey,
                storageConfig.getBucketName(), targetKey);
            ossClient.copyObject(copyRequest);
            
            // Delete original
            ossClient.deleteObject(storageConfig.getBucketName(), sourceKey);
            
            logger.debug("File moved successfully in OSS: {} -> {}", sourceKey, targetKey);
            return true;
        }, "moveFile");
    }

    @Override
    public List<StorageFileInfo> listFiles(String directory) {
        String prefix = StringUtils.hasText(directory) ? sanitizeObjectKey(directory) + "/" : "";
        
        return executeWithRetry(() -> {
            ListObjectsRequest request = new ListObjectsRequest(storageConfig.getBucketName())
                .withPrefix(prefix)
                .withDelimiter("/");
            
            List<StorageFileInfo> files = new ArrayList<>();
            ObjectListing result;
            
            do {
                result = ossClient.listObjects(request);
                
                // Add directories
                for (String commonPrefix : result.getCommonPrefixes()) {
                    String dirName = commonPrefix.substring(prefix.length());
                    if (dirName.endsWith("/")) {
                        dirName = dirName.substring(0, dirName.length() - 1);
                    }
                    files.add(createDirectoryInfo(dirName));
                }
                
                // Add files
                for (OSSObjectSummary summary : result.getObjectSummaries()) {
                    if (!summary.getKey().equals(prefix)) { // Skip the directory itself
                        String fileName = summary.getKey().substring(prefix.length());
                        if (!fileName.contains("/")) { // Only direct children
                            files.add(createFileInfo(summary));
                        }
                    }
                }
                
                request.setMarker(result.getNextMarker());
            } while (result.isTruncated());
            
            return files;
        }, "listFiles");
    }

    @Override
    public String generatePublicUrl(String filePath) {
        String objectKey = sanitizeObjectKey(filePath);
        
        // Check for CDN URL first
        String cdnUrl = generateCdnUrl(objectKey);
        if (cdnUrl != null) {
            return cdnUrl;
        }
        
        // Generate OSS URL
        if (StringUtils.hasText(storageConfig.getCustomDomain())) {
            String protocol = storageConfig.isUseHttps() ? "https" : "http";
            return protocol + "://" + storageConfig.getCustomDomain() + "/" + objectKey;
        }
        
        String protocol = storageConfig.isUseHttps() ? "https" : "http";
        return protocol + "://" + storageConfig.getBucketName() + "." + 
               storageConfig.getEndpoint().replaceFirst("^https?://", "") + "/" + objectKey;
    }

    @Override
    public boolean fileExists(String filePath) {
        String objectKey = sanitizeObjectKey(filePath);
        
        return executeWithRetry(() -> {
            return ossClient.doesObjectExist(storageConfig.getBucketName(), objectKey);
        }, "fileExists");
    }

    @Override
    public String generatePresignedUploadUrl(String objectKey, int expirationMinutes) {
        final String finalObjectKey = sanitizeObjectKey(objectKey);
        
        return executeWithRetry(() -> {
            Date expiration = Date.from(Instant.now().plusSeconds(expirationMinutes * 60L));
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(
                storageConfig.getBucketName(), finalObjectKey, HttpMethod.PUT);
            request.setExpiration(expiration);
            
            URL url = ossClient.generatePresignedUrl(request);
            return url.toString();
        }, "generatePresignedUploadUrl");
    }

    @Override
    public String generatePresignedDownloadUrl(String objectKey, int expirationMinutes) {
        final String finalObjectKey = sanitizeObjectKey(objectKey);
        
        return executeWithRetry(() -> {
            Date expiration = Date.from(Instant.now().plusSeconds(expirationMinutes * 60L));
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(
                storageConfig.getBucketName(), finalObjectKey, HttpMethod.GET);
            request.setExpiration(expiration);
            
            URL url = ossClient.generatePresignedUrl(request);
            return url.toString();
        }, "generatePresignedDownloadUrl");
    }

    @Override
    public boolean copyFile(String sourceKey, String targetKey) {
        final String finalSourceKey = sanitizeObjectKey(sourceKey);
        final String finalTargetKey = sanitizeObjectKey(targetKey);
        
        return executeWithRetry(() -> {
            CopyObjectRequest request = new CopyObjectRequest(
                storageConfig.getBucketName(), finalSourceKey,
                storageConfig.getBucketName(), finalTargetKey);
            ossClient.copyObject(request);
            
            logger.debug("File copied successfully in OSS: {} -> {}", finalSourceKey, finalTargetKey);
            return true;
        }, "copyFile");
    }

    @Override
    public Map<String, String> getFileMetadata(String objectKey) {
        final String finalObjectKey = sanitizeObjectKey(objectKey);
        
        return executeWithRetry(() -> {
            ObjectMetadata metadata = ossClient.getObjectMetadata(storageConfig.getBucketName(), finalObjectKey);
            Map<String, String> result = new HashMap<>();
            
            if (metadata.getUserMetadata() != null) {
                result.putAll(metadata.getUserMetadata());
            }
            
            result.put("contentType", metadata.getContentType());
            result.put("contentLength", String.valueOf(metadata.getContentLength()));
            result.put("lastModified", metadata.getLastModified().toString());
            result.put("etag", metadata.getETag());
            
            return result;
        }, "getFileMetadata");
    }

    @Override
    public boolean setFileMetadata(String objectKey, Map<String, String> metadata) {
        final String finalObjectKey = sanitizeObjectKey(objectKey);
        
        return executeWithRetry(() -> {
            // Get current object metadata
            ObjectMetadata currentMetadata = ossClient.getObjectMetadata(storageConfig.getBucketName(), finalObjectKey);
            
            // Create new metadata with user metadata
            ObjectMetadata newMetadata = new ObjectMetadata();
            newMetadata.setContentType(currentMetadata.getContentType());
            newMetadata.setContentLength(currentMetadata.getContentLength());
            newMetadata.setUserMetadata(metadata);
            
            // Copy object with new metadata
            CopyObjectRequest request = new CopyObjectRequest(
                storageConfig.getBucketName(), finalObjectKey,
                storageConfig.getBucketName(), finalObjectKey);
            request.setNewObjectMetadata(newMetadata);
            
            ossClient.copyObject(request);
            
            logger.debug("File metadata updated successfully in OSS: {}", finalObjectKey);
            return true;
        }, "setFileMetadata");
    }

    private StorageFileInfo createFileInfo(OSSObjectSummary summary) {
        StorageFileInfo info = new StorageFileInfo();
        info.setName(getFileNameFromKey(summary.getKey()));
        info.setPath(summary.getKey());
        info.setSize(summary.getSize());
        info.setLastModified(LocalDateTime.ofInstant(summary.getLastModified().toInstant(), ZoneId.systemDefault()));
        info.setDirectory(false);
        info.setMimeType(determineContentType(summary.getKey()));
        return info;
    }

    private StorageFileInfo createDirectoryInfo(String dirName) {
        StorageFileInfo info = new StorageFileInfo();
        info.setName(dirName);
        info.setPath(dirName);
        info.setSize(0);
        info.setLastModified(LocalDateTime.now());
        info.setDirectory(true);
        info.setMimeType("application/x-directory");
        return info;
    }

    private String getFileNameFromKey(String key) {
        int lastSlash = key.lastIndexOf('/');
        return lastSlash >= 0 ? key.substring(lastSlash + 1) : key;
    }

    private String determineContentType(String fileName) {
        // Simple content type determination based on file extension
        String extension = getFileExtension(fileName).toLowerCase();
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "pdf":
                return "application/pdf";
            case "txt":
                return "text/plain";
            case "html":
                return "text/html";
            case "css":
                return "text/css";
            case "js":
                return "application/javascript";
            case "json":
                return "application/json";
            case "xml":
                return "application/xml";
            case "zip":
                return "application/zip";
            default:
                return "application/octet-stream";
        }
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot >= 0 ? fileName.substring(lastDot + 1) : "";
    }

    @Override
    public long getFileSize(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return -1;
        }

        try {
            String objectKey = sanitizeObjectKey(filePath);
            ObjectMetadata metadata = ossClient.getObjectMetadata(storageConfig.getBucketName(), objectKey);
            return metadata.getContentLength();
        } catch (Exception e) {
            // Check if it's a "NoSuchKey" error by examining the message
            if (e.getMessage() != null && e.getMessage().contains("NoSuchKey")) {
                return -1; // File not found
            }
            logger.error("Failed to get file size from OSS: {}", filePath, e);
            return -1;
        }
    }
}