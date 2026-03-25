package com.zhglxt.fileManager.service.storage.impl;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.model.*;
import com.qcloud.cos.region.Region;
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
 * Tencent Cloud COS Storage Service Implementation
 * 
 * @author zhglxt
 */
@Service
@ConditionalOnClass(COSClient.class)
@ConditionalOnProperty(name = "zhglxt.file-manager.storage.type", havingValue = "TENCENT_COS")
public class TencentCosStorageService extends AbstractCloudStorageService {

    private COSClient cosClient;

    public TencentCosStorageService(FileManagerProperties properties) {
        super(properties);
    }

    @Override
    protected void validateSpecificConfiguration() {
        if (!StringUtils.hasText(storageConfig.getRegion())) {
            throw new StorageException("INVALID_CONFIG", "Region is required for Tencent COS storage");
        }
    }

    @Override
    public void initializeClient() {
        try {
            COSCredentials credentials = new BasicCOSCredentials(
                storageConfig.getAccessKey(), 
                storageConfig.getSecretKey()
            );

            ClientConfig clientConfig = new ClientConfig(new Region(storageConfig.getRegion()));
            clientConfig.setConnectionTimeout(storageConfig.getConnectionTimeout());
            clientConfig.setSocketTimeout(storageConfig.getReadTimeout());
            clientConfig.setMaxConnectionsCount(storageConfig.getMaxConnections());
            
            if (storageConfig.isUseHttps()) {
                clientConfig.setHttpProtocol(com.qcloud.cos.http.HttpProtocol.https);
            } else {
                clientConfig.setHttpProtocol(com.qcloud.cos.http.HttpProtocol.http);
            }

            this.cosClient = new COSClient(credentials, clientConfig);

            logger.info("Tencent COS client initialized successfully for bucket: {}", storageConfig.getBucketName());
        } catch (Exception e) {
            throw new StorageException("CLIENT_INIT_FAILED", 
                "Failed to initialize Tencent COS client: " + e.getMessage(), e);
        }
    }

    @Override
    public String getStorageTypeName() {
        return "Tencent COS";
    }

    @Override
    protected boolean performConnectionTest() throws Exception {
        return cosClient.doesBucketExist(storageConfig.getBucketName());
    }

    @Override
    protected Map<String, Object> getSpecificStorageStats() throws Exception {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            BucketVersioningConfiguration versioningConfig = 
                cosClient.getBucketVersioningConfiguration(storageConfig.getBucketName());
            stats.put("versioningStatus", versioningConfig.getStatus());
            
            // Get bucket location
            String location = cosClient.getBucketLocation(storageConfig.getBucketName());
            stats.put("location", location);
        } catch (Exception e) {
            logger.debug("Could not retrieve some COS bucket stats: {}", e.getMessage());
        }
        
        return stats;
    }

    @Override
    protected void performShutdown() throws Exception {
        if (cosClient != null) {
            cosClient.shutdown();
        }
    }

    @Override
    public String storeFile(InputStream inputStream, String fileName, String directory) {
        String objectKey = buildObjectKey(fileName, directory);
        
        return executeWithRetry(() -> {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(determineContentType(fileName));
            
            if (storageConfig.isServerSideEncryption()) {
                metadata.setServerSideEncryption(SSEAlgorithm.AES256.getAlgorithm());
            }
            
            PutObjectRequest request = new PutObjectRequest(
                storageConfig.getBucketName(), objectKey, inputStream, metadata);
            
            if (StringUtils.hasText(storageConfig.getStorageClass())) {
                request.setStorageClass(StorageClass.fromValue(storageConfig.getStorageClass()));
            }
            
            cosClient.putObject(request);
            logger.debug("File stored successfully in COS: {}", objectKey);
            return objectKey;
        }, "storeFile");
    }

    @Override
    public InputStream retrieveFile(String filePath) {
        String objectKey = sanitizeObjectKey(filePath);
        
        return executeWithRetry(() -> {
            GetObjectRequest request = new GetObjectRequest(storageConfig.getBucketName(), objectKey);
            COSObject cosObject = cosClient.getObject(request);
            return cosObject.getObjectContent();
        }, "retrieveFile");
    }

    @Override
    public boolean deleteFile(String filePath) {
        String objectKey = sanitizeObjectKey(filePath);
        
        return executeWithRetry(() -> {
            cosClient.deleteObject(storageConfig.getBucketName(), objectKey);
            logger.debug("File deleted successfully from COS: {}", objectKey);
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
            cosClient.copyObject(copyRequest);
            
            // Delete original
            cosClient.deleteObject(storageConfig.getBucketName(), sourceKey);
            
            logger.debug("File moved successfully in COS: {} -> {}", sourceKey, targetKey);
            return true;
        }, "moveFile");
    }

    @Override
    public List<StorageFileInfo> listFiles(String directory) {
        String prefix = StringUtils.hasText(directory) ? sanitizeObjectKey(directory) + "/" : "";
        
        return executeWithRetry(() -> {
            ListObjectsRequest request = new ListObjectsRequest();
            request.setBucketName(storageConfig.getBucketName());
            request.setPrefix(prefix);
            request.setDelimiter("/");
            
            List<StorageFileInfo> files = new ArrayList<>();
            ObjectListing result;
            
            do {
                result = cosClient.listObjects(request);
                
                // Add directories
                for (String commonPrefix : result.getCommonPrefixes()) {
                    String dirName = commonPrefix.substring(prefix.length());
                    if (dirName.endsWith("/")) {
                        dirName = dirName.substring(0, dirName.length() - 1);
                    }
                    files.add(createDirectoryInfo(dirName));
                }
                
                // Add files
                for (COSObjectSummary summary : result.getObjectSummaries()) {
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
        
        // Generate COS URL
        if (StringUtils.hasText(storageConfig.getCustomDomain())) {
            String protocol = storageConfig.isUseHttps() ? "https" : "http";
            return protocol + "://" + storageConfig.getCustomDomain() + "/" + objectKey;
        }
        
        String protocol = storageConfig.isUseHttps() ? "https" : "http";
        return protocol + "://" + storageConfig.getBucketName() + ".cos." + 
               storageConfig.getRegion() + ".myqcloud.com/" + objectKey;
    }

    @Override
    public boolean fileExists(String filePath) {
        String objectKey = sanitizeObjectKey(filePath);
        
        return executeWithRetry(() -> {
            try {
                cosClient.getObjectMetadata(storageConfig.getBucketName(), objectKey);
                return true;
            } catch (CosClientException e) {
                if (e.getErrorCode() != null && e.getErrorCode().equals("NoSuchKey")) {
                    return false;
                }
                throw e;
            }
        }, "fileExists");
    }

    @Override
    public String generatePresignedUploadUrl(String objectKey, int expirationMinutes) {
        final String finalObjectKey = sanitizeObjectKey(objectKey);
        
        return executeWithRetry(() -> {
            Date expiration = Date.from(Instant.now().plusSeconds(expirationMinutes * 60L));
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(
                storageConfig.getBucketName(), finalObjectKey, HttpMethodName.PUT);
            request.setExpiration(expiration);
            
            URL url = cosClient.generatePresignedUrl(request);
            return url.toString();
        }, "generatePresignedUploadUrl");
    }

    @Override
    public String generatePresignedDownloadUrl(String objectKey, int expirationMinutes) {
        final String finalObjectKey = sanitizeObjectKey(objectKey);
        
        return executeWithRetry(() -> {
            Date expiration = Date.from(Instant.now().plusSeconds(expirationMinutes * 60L));
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(
                storageConfig.getBucketName(), finalObjectKey, HttpMethodName.GET);
            request.setExpiration(expiration);
            
            URL url = cosClient.generatePresignedUrl(request);
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
            cosClient.copyObject(request);
            
            logger.debug("File copied successfully in COS: {} -> {}", finalSourceKey, finalTargetKey);
            return true;
        }, "copyFile");
    }

    @Override
    public Map<String, String> getFileMetadata(String objectKey) {
        final String finalObjectKey = sanitizeObjectKey(objectKey);
        
        return executeWithRetry(() -> {
            ObjectMetadata metadata = cosClient.getObjectMetadata(storageConfig.getBucketName(), finalObjectKey);
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
            ObjectMetadata currentMetadata = cosClient.getObjectMetadata(storageConfig.getBucketName(), finalObjectKey);
            
            // Create new metadata with user metadata
            ObjectMetadata newMetadata = currentMetadata.clone();
            newMetadata.setUserMetadata(metadata);
            
            // Copy object with new metadata
            CopyObjectRequest request = new CopyObjectRequest(
                storageConfig.getBucketName(), finalObjectKey,
                storageConfig.getBucketName(), finalObjectKey);
            request.setNewObjectMetadata(newMetadata);
            // Note: MetadataDirective may not be available in this COS SDK version
            // The copy operation will replace metadata by default
            
            cosClient.copyObject(request);
            
            logger.debug("File metadata updated successfully in COS: {}", finalObjectKey);
            return true;
        }, "setFileMetadata");
    }

    private StorageFileInfo createFileInfo(COSObjectSummary summary) {
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
            ObjectMetadata metadata = cosClient.getObjectMetadata(storageConfig.getBucketName(), objectKey);
            return metadata.getContentLength();
        } catch (Exception e) {
            // Check if it's a "NoSuchKey" error by examining the message
            if (e.getMessage() != null && e.getMessage().contains("NoSuchKey")) {
                return -1; // File not found
            }
            logger.error("Failed to get file size from COS: {}", filePath, e);
            return -1;
        }
    }
}