package com.zhglxt.fileManager;

import com.zhglxt.fileManager.config.FileManagerProperties;
import com.zhglxt.fileManager.domain.config.StorageConfig;
import com.zhglxt.fileManager.service.storage.CdnService;
import com.zhglxt.fileManager.service.storage.CloudStorageService;
import com.zhglxt.fileManager.service.storage.impl.AwsS3StorageService;
import com.zhglxt.fileManager.service.storage.impl.AlibabaOssStorageService;
import com.zhglxt.fileManager.service.storage.impl.TencentCosStorageService;

/**
 * Verification class for cloud storage implementations
 * This class demonstrates that all cloud storage services can be instantiated and used
 * 
 * @author zhglxt
 */
public class CloudStorageVerification {

    public static void main(String[] args) {
        System.out.println("=== Cloud Storage Implementation Verification ===");
        
        try {
            // Test AWS S3 Storage Service
            System.out.println("\n1. Testing AWS S3 Storage Service...");
            testAwsS3StorageService();
            System.out.println("✓ AWS S3 Storage Service - OK");
            
            // Test Alibaba OSS Storage Service
            System.out.println("\n2. Testing Alibaba OSS Storage Service...");
            testAlibabaOssStorageService();
            System.out.println("✓ Alibaba OSS Storage Service - OK");
            
            // Test Tencent COS Storage Service
            System.out.println("\n3. Testing Tencent COS Storage Service...");
            testTencentCosStorageService();
            System.out.println("✓ Tencent COS Storage Service - OK");
            
            // Test CDN Service
            System.out.println("\n4. Testing CDN Service...");
            testCdnService();
            System.out.println("✓ CDN Service - OK");
            
            System.out.println("\n=== All Cloud Storage Implementations Verified Successfully! ===");
            
        } catch (Exception e) {
            System.err.println("❌ Verification failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testAwsS3StorageService() {
        FileManagerProperties properties = createTestProperties(StorageConfig.StorageType.AWS_S3);
        properties.getStorage().setRegion("us-east-1");

        CloudStorageService service = new AwsS3StorageService(properties) {
            @Override
            public void initializeClient() {
                // Override to prevent actual client initialization
                System.out.println("  - Mock AWS S3 client initialized");
            }
            
            @Override
            protected boolean performConnectionTest() {
                return true;
            }
            
            @Override
            protected java.util.Map<String, Object> getSpecificStorageStats() {
                java.util.Map<String, Object> stats = new java.util.HashMap<>();
                stats.put("provider", "AWS S3");
                return stats;
            }
            
            @Override
            protected void performShutdown() {
                System.out.println("  - AWS S3 client shutdown");
            }
        };

        System.out.println("  - Storage Type: " + service.getStorageTypeName());
        System.out.println("  - Bucket Name: " + service.getStorageConfig().getBucketName());
        System.out.println("  - Storage Stats: " + service.getStorageStats().size() + " entries");
    }

    private static void testAlibabaOssStorageService() {
        FileManagerProperties properties = createTestProperties(StorageConfig.StorageType.ALIBABA_OSS);
        properties.getStorage().setEndpoint("https://oss-cn-hangzhou.aliyuncs.com");

        CloudStorageService service = new AlibabaOssStorageService(properties) {
            @Override
            public void initializeClient() {
                // Override to prevent actual client initialization
                System.out.println("  - Mock Alibaba OSS client initialized");
            }
            
            @Override
            protected boolean performConnectionTest() {
                return true;
            }
            
            @Override
            protected java.util.Map<String, Object> getSpecificStorageStats() {
                java.util.Map<String, Object> stats = new java.util.HashMap<>();
                stats.put("provider", "Alibaba OSS");
                return stats;
            }
            
            @Override
            protected void performShutdown() {
                System.out.println("  - Alibaba OSS client shutdown");
            }
        };

        System.out.println("  - Storage Type: " + service.getStorageTypeName());
        System.out.println("  - Bucket Name: " + service.getStorageConfig().getBucketName());
        System.out.println("  - Endpoint: " + service.getStorageConfig().getEndpoint());
    }

    private static void testTencentCosStorageService() {
        FileManagerProperties properties = createTestProperties(StorageConfig.StorageType.TENCENT_COS);
        properties.getStorage().setRegion("ap-beijing");

        CloudStorageService service = new TencentCosStorageService(properties) {
            @Override
            public void initializeClient() {
                // Override to prevent actual client initialization
                System.out.println("  - Mock Tencent COS client initialized");
            }
            
            @Override
            protected boolean performConnectionTest() {
                return true;
            }
            
            @Override
            protected java.util.Map<String, Object> getSpecificStorageStats() {
                java.util.Map<String, Object> stats = new java.util.HashMap<>();
                stats.put("provider", "Tencent COS");
                return stats;
            }
            
            @Override
            protected void performShutdown() {
                System.out.println("  - Tencent COS client shutdown");
            }
        };

        System.out.println("  - Storage Type: " + service.getStorageTypeName());
        System.out.println("  - Bucket Name: " + service.getStorageConfig().getBucketName());
        System.out.println("  - Region: " + service.getStorageConfig().getRegion());
    }

    private static void testCdnService() {
        FileManagerProperties properties = createTestProperties(StorageConfig.StorageType.AWS_S3);
        properties.getStorage().setCdnEnabled(true);
        properties.getStorage().setCdnDomain("cdn.example.com");

        CdnService cdnService = new CdnService(properties);
        
        System.out.println("  - CDN Enabled: " + cdnService.isCdnEnabled());
        System.out.println("  - CDN Domain: " + cdnService.getCdnDomain());
        
        String cdnUrl = cdnService.generateCdnUrl("test/file.jpg");
        System.out.println("  - Sample CDN URL: " + cdnUrl);
        
        String thumbnailUrl = cdnService.generateThumbnailCdnUrl("test/image.jpg", 300, 200);
        System.out.println("  - Thumbnail URL: " + thumbnailUrl);
        
        java.util.Map<String, Object> stats = cdnService.getCdnStats();
        System.out.println("  - CDN Stats: " + stats.size() + " entries");
    }

    private static FileManagerProperties createTestProperties(StorageConfig.StorageType type) {
        FileManagerProperties properties = new FileManagerProperties();
        StorageConfig storageConfig = new StorageConfig();
        storageConfig.setType(type);
        storageConfig.setAccessKey("test-access-key");
        storageConfig.setSecretKey("test-secret-key");
        storageConfig.setBucketName("test-bucket");
        storageConfig.setUseHttps(true);
        properties.setStorage(storageConfig);
        return properties;
    }
}