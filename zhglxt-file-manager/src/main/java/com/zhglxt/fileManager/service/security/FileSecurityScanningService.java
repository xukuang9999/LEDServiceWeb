package com.zhglxt.fileManager.service.security;

import com.zhglxt.fileManager.config.FileManagerProperties;
import com.zhglxt.fileManager.exception.FileValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * File Security Scanning Service
 * 
 * Provides comprehensive security scanning for uploaded files including
 * malware detection, content validation, and threat analysis.
 * 
 * @author zhglxt
 */
@Service
public class FileSecurityScanningService {

    private static final Logger logger = LoggerFactory.getLogger(FileSecurityScanningService.class);
    
    @Autowired
    private FileManagerProperties properties;
    
    @Autowired
    private SecurityAuditService securityAuditService;
    
    // Known malicious file signatures (simplified examples)
    private static final Map<String, String> MALICIOUS_SIGNATURES = new ConcurrentHashMap<>();
    
    // Suspicious file patterns
    private static final List<Pattern> SUSPICIOUS_PATTERNS = Arrays.asList(
        Pattern.compile("(?i).*\\.(exe|scr|bat|cmd|com|pif|vbs|js|jar|app)$"),
        Pattern.compile("(?i).*\\.(php|jsp|asp|aspx)\\.(txt|jpg|png|gif)$"), // Double extension
        Pattern.compile("(?i).*[<>\"'&].*"), // HTML/Script injection patterns
        Pattern.compile("(?i).*(eval|exec|system|shell_exec|passthru).*") // Code execution patterns
    );
    
    // Known safe file signatures
    private static final Map<String, String> SAFE_SIGNATURES = new ConcurrentHashMap<>();
    
    static {
        // Initialize known malicious signatures (examples)
        MALICIOUS_SIGNATURES.put("4D5A", "PE Executable"); // PE header
        MALICIOUS_SIGNATURES.put("504B0304", "ZIP with suspicious content");
        
        // Initialize safe file signatures
        SAFE_SIGNATURES.put("FFD8FF", "JPEG Image");
        SAFE_SIGNATURES.put("89504E47", "PNG Image");
        SAFE_SIGNATURES.put("47494638", "GIF Image");
        SAFE_SIGNATURES.put("25504446", "PDF Document");
        SAFE_SIGNATURES.put("504B0304", "ZIP Archive");
        SAFE_SIGNATURES.put("D0CF11E0", "Microsoft Office Document");
    }
    
    /**
     * Perform comprehensive security scan on uploaded file
     */
    public SecurityScanResult scanFile(MultipartFile file, String userId, HttpServletRequest request) {
        SecurityScanResult result = new SecurityScanResult();
        result.setFileName(file.getOriginalFilename());
        result.setFileSize(file.getSize());
        result.setScanTimestamp(new Date());
        
        try {
            // Basic file validation
            performBasicValidation(file, result);
            
            // File signature analysis
            performSignatureAnalysis(file, result);
            
            // Content analysis
            performContentAnalysis(file, result);
            
            // Filename analysis
            performFilenameAnalysis(file.getOriginalFilename(), result);
            
            // Size and structure analysis
            performSizeAnalysis(file, result);
            
            // Calculate overall threat level
            calculateThreatLevel(result);
            
            // Log scan results
            logScanResults(result, userId, request);
            
        } catch (Exception e) {
            logger.error("Error during file security scan: {}", e.getMessage(), e);
            result.addThreat("SCAN_ERROR", "Error during security scan: " + e.getMessage(), ThreatLevel.HIGH);
        }
        
        return result;
    }
    
    /**
     * Perform basic file validation
     */
    private void performBasicValidation(MultipartFile file, SecurityScanResult result) {
        // Check if file is empty
        if (file.isEmpty()) {
            result.addThreat("EMPTY_FILE", "File is empty", ThreatLevel.MEDIUM);
            return;
        }
        
        // Check file size limits
        FileManagerProperties.SecurityConfig security = properties.getSecurity();
        if (security != null && security.getScanning() != null) {
            long maxSize = security.getScanning().getMaxFileSizeForScanning();
            if (file.getSize() > maxSize) {
                result.addThreat("FILE_TOO_LARGE", "File too large for security scanning", ThreatLevel.MEDIUM);
                return;
            }
        }
        
        // Check filename
        String filename = file.getOriginalFilename();
        if (filename == null || filename.trim().isEmpty()) {
            result.addThreat("INVALID_FILENAME", "Invalid or missing filename", ThreatLevel.MEDIUM);
        }
    }
    
    /**
     * Perform file signature analysis
     */
    private void performSignatureAnalysis(MultipartFile file, SecurityScanResult result) {
        try {
            byte[] header = new byte[Math.min(1024, (int) file.getSize())];
            try (InputStream is = file.getInputStream()) {
                int bytesRead = is.read(header);
                if (bytesRead > 0) {
                    String signature = bytesToHex(Arrays.copyOf(header, Math.min(8, bytesRead)));
                    
                    // Check against malicious signatures
                    for (Map.Entry<String, String> entry : MALICIOUS_SIGNATURES.entrySet()) {
                        if (signature.startsWith(entry.getKey())) {
                            result.addThreat("MALICIOUS_SIGNATURE", 
                                "File matches known malicious signature: " + entry.getValue(), 
                                ThreatLevel.HIGH);
                            return;
                        }
                    }
                    
                    // Check if signature matches declared file type
                    String declaredType = file.getContentType();
                    if (!isSignatureMatchingType(signature, declaredType)) {
                        result.addThreat("SIGNATURE_MISMATCH", 
                            "File signature doesn't match declared content type", 
                            ThreatLevel.MEDIUM);
                    }
                }
            }
        } catch (IOException e) {
            logger.warn("Error reading file signature: {}", e.getMessage());
            result.addThreat("SIGNATURE_READ_ERROR", "Could not read file signature", ThreatLevel.LOW);
        }
    }
    
    /**
     * Perform content analysis
     */
    private void performContentAnalysis(MultipartFile file, SecurityScanResult result) {
        try {
            // For text files, scan content for suspicious patterns
            if (isTextFile(file)) {
                scanTextContent(file, result);
            }
            
            // For binary files, perform basic structure analysis
            if (isBinaryFile(file)) {
                scanBinaryContent(file, result);
            }
            
        } catch (Exception e) {
            logger.warn("Error during content analysis: {}", e.getMessage());
            result.addThreat("CONTENT_ANALYSIS_ERROR", "Error analyzing file content", ThreatLevel.LOW);
        }
    }
    
    /**
     * Scan text file content for suspicious patterns
     */
    private void scanTextContent(MultipartFile file, SecurityScanResult result) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            int lineNumber = 0;
            
            while ((line = reader.readLine()) != null && lineNumber < 1000) { // Limit scan to first 1000 lines
                lineNumber++;
                
                // Check for suspicious patterns
                for (Pattern pattern : SUSPICIOUS_PATTERNS) {
                    if (pattern.matcher(line).find()) {
                        result.addThreat("SUSPICIOUS_CONTENT", 
                            "Suspicious pattern found at line " + lineNumber + ": " + pattern.pattern(), 
                            ThreatLevel.MEDIUM);
                    }
                }
                
                // Check for script injection attempts
                if (containsScriptInjection(line)) {
                    result.addThreat("SCRIPT_INJECTION", 
                        "Potential script injection found at line " + lineNumber, 
                        ThreatLevel.HIGH);
                }
                
                // Check for SQL injection attempts
                if (containsSqlInjection(line)) {
                    result.addThreat("SQL_INJECTION", 
                        "Potential SQL injection found at line " + lineNumber, 
                        ThreatLevel.HIGH);
                }
            }
            
        } catch (IOException e) {
            logger.warn("Error scanning text content: {}", e.getMessage());
        }
    }
    
    /**
     * Scan binary file content
     */
    private void scanBinaryContent(MultipartFile file, SecurityScanResult result) {
        try {
            // Calculate file hash for integrity checking
            String fileHash = calculateFileHash(file);
            result.setFileHash(fileHash);
            
            // Check against known malicious hashes (would be loaded from threat intelligence)
            if (isKnownMaliciousHash(fileHash)) {
                result.addThreat("MALICIOUS_HASH", 
                    "File hash matches known malicious file", 
                    ThreatLevel.HIGH);
            }
            
            // Basic entropy analysis for packed/encrypted content
            double entropy = calculateEntropy(file);
            if (entropy > 7.5) { // High entropy might indicate packed/encrypted content
                result.addThreat("HIGH_ENTROPY", 
                    "File has high entropy (possible packed/encrypted content)", 
                    ThreatLevel.MEDIUM);
            }
            
        } catch (Exception e) {
            logger.warn("Error scanning binary content: {}", e.getMessage());
        }
    }
    
    /**
     * Perform filename analysis
     */
    private void performFilenameAnalysis(String filename, SecurityScanResult result) {
        if (filename == null) return;
        
        // Check for double extensions
        if (hasDoubleExtension(filename)) {
            result.addThreat("DOUBLE_EXTENSION", 
                "Filename has double extension (possible disguised executable)", 
                ThreatLevel.MEDIUM);
        }
        
        // Check for suspicious characters
        if (containsSuspiciousCharacters(filename)) {
            result.addThreat("SUSPICIOUS_FILENAME", 
                "Filename contains suspicious characters", 
                ThreatLevel.LOW);
        }
        
        // Check for extremely long filename
        if (filename.length() > 255) {
            result.addThreat("LONG_FILENAME", 
                "Filename is extremely long (possible buffer overflow attempt)", 
                ThreatLevel.MEDIUM);
        }
        
        // Check for hidden file patterns
        if (filename.startsWith(".") && !isAllowedHiddenFile(filename)) {
            result.addThreat("HIDDEN_FILE", 
                "Hidden file upload detected", 
                ThreatLevel.LOW);
        }
    }
    
    /**
     * Perform size analysis
     */
    private void performSizeAnalysis(MultipartFile file, SecurityScanResult result) {
        long size = file.getSize();
        
        // Check for suspiciously small files that claim to be complex formats
        String contentType = file.getContentType();
        if (contentType != null) {
            if ((contentType.contains("image") || contentType.contains("document")) && size < 100) {
                result.addThreat("SUSPICIOUS_SIZE", 
                    "File size too small for declared content type", 
                    ThreatLevel.MEDIUM);
            }
        }
        
        // Check for zero-byte files
        if (size == 0) {
            result.addThreat("ZERO_BYTE_FILE", 
                "File has zero bytes", 
                ThreatLevel.LOW);
        }
    }
    
    /**
     * Calculate overall threat level
     */
    private void calculateThreatLevel(SecurityScanResult result) {
        if (result.getThreats().isEmpty()) {
            result.setOverallThreatLevel(ThreatLevel.NONE);
            return;
        }
        
        ThreatLevel maxLevel = ThreatLevel.NONE;
        for (SecurityThreat threat : result.getThreats()) {
            if (threat.getLevel().ordinal() > maxLevel.ordinal()) {
                maxLevel = threat.getLevel();
            }
        }
        
        result.setOverallThreatLevel(maxLevel);
    }
    
    /**
     * Log scan results
     */
    private void logScanResults(SecurityScanResult result, String userId, HttpServletRequest request) {
        Map<String, Object> scanData = new HashMap<>();
        scanData.put("fileName", result.getFileName());
        scanData.put("fileSize", result.getFileSize());
        scanData.put("fileHash", result.getFileHash());
        scanData.put("threatLevel", result.getOverallThreatLevel().toString());
        scanData.put("threatCount", result.getThreats().size());
        scanData.put("threats", result.getThreats());
        
        if (result.getOverallThreatLevel() == ThreatLevel.NONE) {
            securityAuditService.auditFileOperation(userId, "FILE_SCAN_CLEAN", 
                result.getFileName(), true, request, scanData);
        } else {
            securityAuditService.auditSecurityViolation(userId, "FILE_SCAN_THREAT", 
                "File security scan detected threats: " + result.getOverallThreatLevel(), 
                request, scanData);
        }
    }
    
    // Helper methods
    
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02X", b));
        }
        return result.toString();
    }
    
    private boolean isSignatureMatchingType(String signature, String contentType) {
        if (contentType == null) return true; // Can't validate without content type
        
        // Simple signature matching (would be more comprehensive in production)
        if (contentType.contains("jpeg") && signature.startsWith("FFD8FF")) return true;
        if (contentType.contains("png") && signature.startsWith("89504E47")) return true;
        if (contentType.contains("gif") && signature.startsWith("47494638")) return true;
        if (contentType.contains("pdf") && signature.startsWith("25504446")) return true;
        
        return false; // Default to mismatch for unknown types
    }
    
    private boolean isTextFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("text/");
    }
    
    private boolean isBinaryFile(MultipartFile file) {
        return !isTextFile(file);
    }
    
    private boolean containsScriptInjection(String content) {
        String lower = content.toLowerCase();
        return lower.contains("<script") || lower.contains("javascript:") || 
               lower.contains("onload=") || lower.contains("onerror=");
    }
    
    private boolean containsSqlInjection(String content) {
        String lower = content.toLowerCase();
        return lower.contains("union select") || lower.contains("drop table") || 
               lower.contains("'; --") || lower.contains("1=1");
    }
    
    private String calculateFileHash(MultipartFile file) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        try (InputStream is = file.getInputStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                md.update(buffer, 0, bytesRead);
            }
        }
        return bytesToHex(md.digest());
    }
    
    private boolean isKnownMaliciousHash(String hash) {
        // In production, this would check against threat intelligence databases
        return false;
    }
    
    private double calculateEntropy(MultipartFile file) throws IOException {
        int[] frequency = new int[256];
        int totalBytes = 0;
        
        try (InputStream is = file.getInputStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                for (int i = 0; i < bytesRead; i++) {
                    frequency[buffer[i] & 0xFF]++;
                    totalBytes++;
                }
            }
        }
        
        double entropy = 0.0;
        for (int freq : frequency) {
            if (freq > 0) {
                double probability = (double) freq / totalBytes;
                entropy -= probability * (Math.log(probability) / Math.log(2));
            }
        }
        
        return entropy;
    }
    
    private boolean hasDoubleExtension(String filename) {
        String[] parts = filename.split("\\.");
        return parts.length > 2;
    }
    
    private boolean containsSuspiciousCharacters(String filename) {
        return filename.matches(".*[<>:\"|?*\\\\].*");
    }
    
    private boolean isAllowedHiddenFile(String filename) {
        // Allow common hidden files
        return filename.equals(".htaccess") || filename.equals(".gitignore");
    }
    
    /**
     * Security scan result
     */
    public static class SecurityScanResult {
        private String fileName;
        private long fileSize;
        private String fileHash;
        private Date scanTimestamp;
        private ThreatLevel overallThreatLevel = ThreatLevel.NONE;
        private List<SecurityThreat> threats = new ArrayList<>();
        
        public void addThreat(String type, String description, ThreatLevel level) {
            threats.add(new SecurityThreat(type, description, level));
        }
        
        public boolean isClean() {
            return overallThreatLevel == ThreatLevel.NONE;
        }
        
        public boolean isBlocked() {
            return overallThreatLevel == ThreatLevel.HIGH;
        }
        
        // Getters and setters
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        
        public long getFileSize() { return fileSize; }
        public void setFileSize(long fileSize) { this.fileSize = fileSize; }
        
        public String getFileHash() { return fileHash; }
        public void setFileHash(String fileHash) { this.fileHash = fileHash; }
        
        public Date getScanTimestamp() { return scanTimestamp; }
        public void setScanTimestamp(Date scanTimestamp) { this.scanTimestamp = scanTimestamp; }
        
        public ThreatLevel getOverallThreatLevel() { return overallThreatLevel; }
        public void setOverallThreatLevel(ThreatLevel overallThreatLevel) { this.overallThreatLevel = overallThreatLevel; }
        
        public List<SecurityThreat> getThreats() { return threats; }
        public void setThreats(List<SecurityThreat> threats) { this.threats = threats; }
    }
    
    /**
     * Security threat representation
     */
    public static class SecurityThreat {
        private String type;
        private String description;
        private ThreatLevel level;
        
        public SecurityThreat(String type, String description, ThreatLevel level) {
            this.type = type;
            this.description = description;
            this.level = level;
        }
        
        // Getters
        public String getType() { return type; }
        public String getDescription() { return description; }
        public ThreatLevel getLevel() { return level; }
        
        @Override
        public String toString() {
            return String.format("%s: %s (%s)", type, description, level);
        }
    }
    
    /**
     * Threat level enumeration
     */
    public enum ThreatLevel {
        NONE, LOW, MEDIUM, HIGH
    }
}