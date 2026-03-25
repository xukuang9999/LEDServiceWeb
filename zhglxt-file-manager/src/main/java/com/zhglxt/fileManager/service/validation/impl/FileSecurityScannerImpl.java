package com.zhglxt.fileManager.service.validation.impl;

import com.zhglxt.fileManager.domain.validation.FileValidationResult;
import com.zhglxt.fileManager.service.validation.FileSecurityScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * File Security Scanner Implementation
 * 
 * @author zhglxt
 */
@Service
public class FileSecurityScannerImpl implements FileSecurityScanner {

    private static final Logger logger = LoggerFactory.getLogger(FileSecurityScannerImpl.class);

    // File signatures (magic numbers) for common file types
    private static final Map<String, byte[][]> FILE_SIGNATURES = new HashMap<>();
    
    // Malicious patterns to detect
    private static final List<Pattern> MALICIOUS_PATTERNS = Arrays.asList(
        Pattern.compile("(?i)<script[^>]*>.*?</script>", Pattern.DOTALL),
        Pattern.compile("(?i)javascript:", Pattern.DOTALL),
        Pattern.compile("(?i)vbscript:", Pattern.DOTALL),
        Pattern.compile("(?i)onload\\s*=", Pattern.DOTALL),
        Pattern.compile("(?i)onerror\\s*=", Pattern.DOTALL),
        Pattern.compile("(?i)onclick\\s*=", Pattern.DOTALL),
        Pattern.compile("(?i)eval\\s*\\(", Pattern.DOTALL),
        Pattern.compile("(?i)expression\\s*\\(", Pattern.DOTALL)
    );

    // Executable file extensions
    private static final List<String> EXECUTABLE_EXTENSIONS = Arrays.asList(
        "exe", "bat", "cmd", "com", "pif", "scr", "vbs", "js", "jar", 
        "app", "deb", "pkg", "dmg", "sh", "ps1", "msi", "dll"
    );

    static {
        // Initialize file signatures
        FILE_SIGNATURES.put("jpg", new byte[][]{
            {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}
        });
        FILE_SIGNATURES.put("jpeg", new byte[][]{
            {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}
        });
        FILE_SIGNATURES.put("png", new byte[][]{
            {(byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47, (byte) 0x0D, (byte) 0x0A, (byte) 0x1A, (byte) 0x0A}
        });
        FILE_SIGNATURES.put("gif", new byte[][]{
            {(byte) 0x47, (byte) 0x49, (byte) 0x46, (byte) 0x38, (byte) 0x37, (byte) 0x61},
            {(byte) 0x47, (byte) 0x49, (byte) 0x46, (byte) 0x38, (byte) 0x39, (byte) 0x61}
        });
        FILE_SIGNATURES.put("pdf", new byte[][]{
            {(byte) 0x25, (byte) 0x50, (byte) 0x44, (byte) 0x46}
        });
        FILE_SIGNATURES.put("zip", new byte[][]{
            {(byte) 0x50, (byte) 0x4B, (byte) 0x03, (byte) 0x04},
            {(byte) 0x50, (byte) 0x4B, (byte) 0x05, (byte) 0x06},
            {(byte) 0x50, (byte) 0x4B, (byte) 0x07, (byte) 0x08}
        });
        FILE_SIGNATURES.put("rar", new byte[][]{
            {(byte) 0x52, (byte) 0x61, (byte) 0x72, (byte) 0x21, (byte) 0x1A, (byte) 0x07, (byte) 0x00}
        });
        FILE_SIGNATURES.put("7z", new byte[][]{
            {(byte) 0x37, (byte) 0x7A, (byte) 0xBC, (byte) 0xAF, (byte) 0x27, (byte) 0x1C}
        });
    }

    @Override
    public FileValidationResult scanFile(String fileName, InputStream inputStream) {
        List<String> errors = new ArrayList<>();

        try {
            // Create buffered stream for multiple reads
            BufferedInputStream bufferedStream = new BufferedInputStream(inputStream);
            bufferedStream.mark(8192); // Mark for reset

            // Check for executable content
            if (containsExecutableContent(fileName, bufferedStream)) {
                errors.add("File contains executable content");
            }

            // Reset stream and check for malicious patterns
            bufferedStream.reset();
            if (containsMaliciousPatterns(fileName, bufferedStream)) {
                errors.add("File contains potentially malicious content");
            }

            // Reset stream and validate file signature
            bufferedStream.reset();
            if (!validateFileSignature(fileName, bufferedStream)) {
                errors.add("File signature does not match expected type");
            }

        } catch (IOException e) {
            logger.error("Error scanning file: {}", fileName, e);
            errors.add("Unable to scan file content");
        }

        return errors.isEmpty() ? FileValidationResult.valid() : FileValidationResult.invalid(errors);
    }

    @Override
    public boolean containsExecutableContent(String fileName, InputStream inputStream) {
        // Check file extension
        String extension = getFileExtension(fileName);
        if (extension != null && EXECUTABLE_EXTENSIONS.contains(extension.toLowerCase())) {
            return true;
        }

        try {
            // Check for executable signatures
            byte[] header = new byte[512];
            int bytesRead = inputStream.read(header);
            
            if (bytesRead > 0) {
                String headerString = new String(header, 0, bytesRead);
                
                // Check for common executable patterns
                if (headerString.contains("MZ") || // Windows PE
                    headerString.contains("#!/") || // Unix shebang
                    headerString.contains("PK") || // ZIP-based executables
                    headerString.contains("\u007fELF")) { // ELF executables
                    return true;
                }
            }
        } catch (IOException e) {
            logger.warn("Error checking executable content for file: {}", fileName, e);
        }

        return false;
    }

    @Override
    public boolean containsMaliciousPatterns(String fileName, InputStream inputStream) {
        try {
            // Read file content (limit to prevent memory issues)
            byte[] content = new byte[Math.min(1024 * 1024, inputStream.available())]; // Max 1MB
            int bytesRead = inputStream.read(content);
            
            if (bytesRead > 0) {
                String contentString = new String(content, 0, bytesRead);
                
                // Check for malicious patterns
                for (Pattern pattern : MALICIOUS_PATTERNS) {
                    if (pattern.matcher(contentString).find()) {
                        logger.warn("Malicious pattern detected in file: {}", fileName);
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            logger.warn("Error checking malicious patterns for file: {}", fileName, e);
        }

        return false;
    }

    @Override
    public boolean validateFileSignature(String fileName, InputStream inputStream) {
        String extension = getFileExtension(fileName);
        if (extension == null) {
            return true; // Skip validation for files without extension
        }

        extension = extension.toLowerCase();
        byte[][] signatures = FILE_SIGNATURES.get(extension);
        
        if (signatures == null) {
            return true; // Skip validation for unknown file types
        }

        try {
            // Read enough bytes for signature checking
            byte[] header = new byte[16];
            int bytesRead = inputStream.read(header);
            
            if (bytesRead < 4) {
                return false; // File too small to have valid signature
            }

            // Check if any signature matches
            for (byte[] signature : signatures) {
                if (bytesRead >= signature.length && matchesSignature(header, signature)) {
                    return true;
                }
            }

            logger.warn("File signature validation failed for file: {} (extension: {})", fileName, extension);
            return false;

        } catch (IOException e) {
            logger.warn("Error validating file signature for file: {}", fileName, e);
            return false;
        }
    }

    private boolean matchesSignature(byte[] header, byte[] signature) {
        if (header.length < signature.length) {
            return false;
        }

        for (int i = 0; i < signature.length; i++) {
            if (header[i] != signature[i]) {
                return false;
            }
        }

        return true;
    }

    private String getFileExtension(String fileName) {
        if (fileName == null) {
            return null;
        }
        
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return null;
        }
        
        return fileName.substring(lastDotIndex + 1);
    }
}