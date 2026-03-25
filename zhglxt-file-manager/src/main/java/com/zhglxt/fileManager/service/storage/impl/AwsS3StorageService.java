package com.zhglxt.fileManager.service.storage.impl;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.HttpMethod;
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
import java.util.stream.Collectors;

/**
 * AWS S3 Storage Service Implementation
 * 
 * @author zhglxt
 */
@Service
@ConditionalOnClass(AmazonS3.class)
@ConditionalOnProperty(name = "zhglxt.file-manager.storage.type", havingValue = "AWS_S3")
public class AwsS3StorageService extends AbstractCloudStorageService {

    private AmazonS3 s3Client;

    public AwsS3StorageService(FileManagerProperties properties) {
        super(properties);
    }

    @Override
    protected void validateSpecificConfiguration() {
        if (!StringUtils.hasText(storageConfig.getRegion())) {
            throw new StorageException("INVALID_CONFIG", "Region is required for AWS S3 storage");
        }
    }

    @Override
    public void initializeClient() {
        try {
            BasicAWSCredentials credentials = new BasicAWSCredentials(
                storageConfig.getAccessKey(), 
                storageConfig.getSecretKey()
            );

            ClientConfiguration clientConfig = new ClientConfiguration();
            clientConfig.setConnectionTimeout(storageConfig.getConnectionTimeout());
            clientConfig.setSocketTimeout(storageConfig.getReadTimeout());
            clientConfig.setMaxConnections(storageConfig.getMaxConnections());

            AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withClientConfiguration(clientConfig);

            if (StringUtils.hasText(storageConfig.getEndpoint())) {
                // Custom endpoint (e.g., for MinIO or other S3-compatible services)
                builder.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
                    storageConfig.getEndpoint(), storageConfig.getRegion()));
                builder.withPathStyleAccessEnabled(storageConfig.isPathStyleAccess());
            } else {
                // Standard AWS S3
                builder.withRegion(storageConfig.getRegion());
            }

            this.s3Client = builder.build();

            logger.info("AWS S3 client initialized successfully for bucket: {}", storageConfig.getBucketName());
        } catch (Exception e) {
            throw new StorageException("CLIENT_INIT_FAILED", 
                "Failed to initialize AWS S3 client: " + e.getMessage(), e);
        }
    }

    @Override
    public String getStorageTypeName() {
        return "AWS S3";
    }

    @Override
    protected boolean performConnectionTest() throws Exception {
        return s3Client.doesBucketExistV2(storageConfig.getBucketName());
    }

    @Override
    protected Map<String, Object> getSpecificStorageStats() throws Exception {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            BucketVersioningConfiguration versioningConfig = 
                s3Client.getBucketVersioningConfiguration(storageConfig.getBucketName());
            stats.put("versioningStatus", versioningConfig.getStatus());
            
            // Note: BucketEncryption requires newer AWS SDK version
            // For now, just set a default value
            stats.put("encryptionEnabled", storageConfig.isServerSideEncryption());
        } catch (Exception e) {
            logger.debug("Could not retrieve some S3 bucket stats: {}", e.getMessage());
        }
        
        return stats;
    }

    @Override
    protected void performShutdown() throws Exception {
        if (s3Client != null) {
            s3Client.shutdown();
        }
    }

    @Override
    public String storeFile(InputStream inputStream, String fileName, String directory) {
        String objectKey = buildObjectKey(fileName, directory);
        
        return executeWithRetry(() -> {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(determineContentType(fileName));
            
            if (storageConfig.isServerSideEncryption()) {
                metadata.setSSEAlgorithm(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION);
            }
            
            PutObjectRequest request = new PutObjectRequest(
                storageConfig.getBucketName(), objectKey, inputStream, metadata);
            
            if (StringUtils.hasText(storageConfig.getStorageClass())) {
                request.setStorageClass(StorageClass.fromValue(storageConfig.getStorageClass()));
            }
            
            s3Client.putObject(request);
            logger.debug("File stored successfully in S3: {}", objectKey);
            return objectKey;
        }, "storeFile");
    }

    @Override
    public InputStream retrieveFile(String filePath) {
        String objectKey = sanitizeObjectKey(filePath);
        
        return executeWithRetry(() -> {
            GetObjectRequest request = new GetObjectRequest(storageConfig.getBucketName(), objectKey);
            S3Object s3Object = s3Client.getObject(request);
            return s3Object.getObjectContent();
        }, "retrieveFile");
    }

    @Override
    public boolean deleteFile(String filePath) {
        String objectKey = sanitizeObjectKey(filePath);
        
        return executeWithRetry(() -> {
            s3Client.deleteObject(storageConfig.getBucketName(), objectKey);
            logger.debug("File deleted successfully from S3: {}", objectKey);
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
            s3Client.copyObject(copyRequest);
            
            // Delete original
            s3Client.deleteObject(storageConfig.getBucketName(), sourceKey);
            
            logger.debug("File moved successfully in S3: {} -> {}", sourceKey, targetKey);
            return true;
        }, "moveFile");
    }

    @Override
    public List<StorageFileInfo> listFiles(String directory) {
        String prefix = StringUtils.hasText(directory) ? sanitizeObjectKey(directory) + "/" : "";
        
        return executeWithRetry(() -> {
            ListObjectsV2Request request = new ListObjectsV2Request()
                .withBucketName(storageConfig.getBucketName())
                .withPrefix(prefix)
                .withDelimiter("/");
            
            List<StorageFileInfo> files = new ArrayList<>();
            ListObjectsV2Result result;
            
            do {
                result = s3Client.listObjectsV2(request);
                
                // Add directories
                for (String commonPrefix : result.getCommonPrefixes()) {
                    String dirName = commonPrefix.substring(prefix.length());
                    if (dirName.endsWith("/")) {
                        dirName = dirName.substring(0, dirName.length() - 1);
                    }
                    files.add(createDirectoryInfo(dirName));
                }
                
                // Add files
                for (S3ObjectSummary summary : result.getObjectSummaries()) {
                    if (!summary.getKey().equals(prefix)) { // Skip the directory itself
                        String fileName = summary.getKey().substring(prefix.length());
                        if (!fileName.contains("/")) { // Only direct children
                            files.add(createFileInfo(summary));
                        }
                    }
                }
                
                request.setContinuationToken(result.getNextContinuationToken());
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
        
        // Generate S3 URL
        if (StringUtils.hasText(storageConfig.getCustomDomain())) {
            String protocol = storageConfig.isUseHttps() ? "https" : "http";
            return protocol + "://" + storageConfig.getCustomDomain() + "/" + objectKey;
        }
        
        return s3Client.getUrl(storageConfig.getBucketName(), objectKey).toString();
    }

    @Override
    public boolean fileExists(String filePath) {
        String objectKey = sanitizeObjectKey(filePath);
        
        return executeWithRetry(() -> {
            try {
                s3Client.getObjectMetadata(storageConfig.getBucketName(), objectKey);
                return true;
            } catch (AmazonS3Exception e) {
                if (e.getStatusCode() == 404) {
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
                storageConfig.getBucketName(), finalObjectKey, HttpMethod.PUT);
            request.setExpiration(expiration);
            
            URL url = s3Client.generatePresignedUrl(request);
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
            
            URL url = s3Client.generatePresignedUrl(request);
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
            s3Client.copyObject(request);
            
            logger.debug("File copied successfully in S3: {} -> {}", finalSourceKey, finalTargetKey);
            return true;
        }, "copyFile");
    }

    @Override
    public Map<String, String> getFileMetadata(String objectKey) {
        final String finalObjectKey = sanitizeObjectKey(objectKey);
        
        return executeWithRetry(() -> {
            ObjectMetadata metadata = s3Client.getObjectMetadata(storageConfig.getBucketName(), finalObjectKey);
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
            ObjectMetadata currentMetadata = s3Client.getObjectMetadata(storageConfig.getBucketName(), finalObjectKey);
            
            // Create new metadata with user metadata
            ObjectMetadata newMetadata = currentMetadata.clone();
            newMetadata.setUserMetadata(metadata);
            
            // Copy object with new metadata
            CopyObjectRequest request = new CopyObjectRequest(
                storageConfig.getBucketName(), finalObjectKey,
                storageConfig.getBucketName(), finalObjectKey);
            request.setNewObjectMetadata(newMetadata);
            request.withMetadataDirective(MetadataDirective.REPLACE);
            
            s3Client.copyObject(request);
            
            logger.debug("File metadata updated successfully in S3: {}", finalObjectKey);
            return true;
        }, "setFileMetadata");
    }

    private StorageFileInfo createFileInfo(S3ObjectSummary summary) {
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
            ObjectMetadata metadata = s3Client.getObjectMetadata(storageConfig.getBucketName(), objectKey);
            return metadata.getContentLength();
        } catch (AmazonS3Exception e) {
            if (e.getStatusCode() == 404) {
                return -1; // File not found
            }
            logger.error("Failed to get file size from S3: {}", filePath, e);
            return -1;
        } catch (Exception e) {
            logger.error("Failed to get file size from S3: {}", filePath, e);
            return -1;
        }
    }
}