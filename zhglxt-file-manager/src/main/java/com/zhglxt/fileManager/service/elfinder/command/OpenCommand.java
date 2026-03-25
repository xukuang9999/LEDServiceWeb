package com.zhglxt.fileManager.service.elfinder.command;

import com.zhglxt.fileManager.domain.FileInfo;
import com.zhglxt.fileManager.domain.elfinder.ElFinderFile;
import com.zhglxt.fileManager.domain.elfinder.ElFinderRequest;
import com.zhglxt.fileManager.domain.elfinder.ElFinderResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * elFinder Open Command
 * Opens a directory and returns its contents
 * 
 * @author zhglxt
 */
@Component
public class OpenCommand extends AbstractElFinderCommand {

    @Override
    public String getCommandName() {
        return "open";
    }

    @Override
    public boolean isValidRequest(ElFinderRequest request) {
        // Open command can work without target (opens root)
        return true;
    }

    @Override
    public ElFinderResponse execute(ElFinderRequest request, String userId, HttpServletRequest httpRequest) {
        try {
            logger.info("Executing open command for user: {}", userId);

            ElFinderResponse response = createSuccessResponse();

            // Determine target directory
            String targetHash = request.getTarget();
            String targetPath = "";
            
            if (StringUtils.hasText(targetHash)) {
                targetPath = decodePath(targetHash);
            }

            logger.debug("Opening directory: '{}' (hash: '{}')", targetPath, targetHash);

            // Get directory contents
            List<FileInfo> fileInfos = fileManagerService.listFiles(targetPath, userId);
            
            // Convert to elFinder files
            List<ElFinderFile> elFinderFiles = convertToElFinderFiles(fileInfos, targetHash);
            
            // Set current working directory
            FileInfo currentDirInfo = getCurrentDirectoryInfo(targetPath, userId);
            if (currentDirInfo != null) {
                String parentHash = getParentHash(targetPath);
                ElFinderFile cwd = convertToElFinderFile(currentDirInfo, parentHash);
                response.setCwd(cwd);
            } else {
                // Create root directory info
                ElFinderFile rootDir = createRootDirectory();
                response.setCwd(rootDir);
            }

            // Set files
            response.setFiles(elFinderFiles);

            // If this is initialization, add options
            if (request.isInit()) {
                addInitializationOptions(response);
            }

            // If tree is requested, add tree structure
            if (request.isTree()) {
                addTreeStructure(response, targetPath, userId);
            }

            logger.info("Open command completed successfully. Directory: '{}', Files: {}", 
                       targetPath, elFinderFiles.size());

            return response;

        } catch (Exception e) {
            logger.error("Error executing open command: {}", e.getMessage(), e);
            return createErrorResponse("error.open.failed", httpRequest, e.getMessage());
        }
    }

    /**
     * Get current directory information
     * 
     * @param directoryPath directory path
     * @param userId user ID
     * @return directory file info
     */
    private FileInfo getCurrentDirectoryInfo(String directoryPath, String userId) {
        if (!StringUtils.hasText(directoryPath)) {
            // Return root directory info
            return createRootDirectoryInfo();
        }

        return fileManagerService.getFileInfo(directoryPath, userId);
    }

    /**
     * Create root directory info
     * 
     * @return root directory file info
     */
    private FileInfo createRootDirectoryInfo() {
        FileInfo rootInfo = new FileInfo();
        rootInfo.setName("Root");
        rootInfo.setPath("");
        rootInfo.setDirectory(true);
        rootInfo.setSize(0);
        rootInfo.setLastModified(java.time.LocalDateTime.now());
        rootInfo.setMimeType("directory");
        rootInfo.setReadable(true);
        rootInfo.setWritable(true);
        return rootInfo;
    }

    /**
     * Create root directory elFinder file
     * 
     * @return root directory elFinder file
     */
    private ElFinderFile createRootDirectory() {
        String rootHash = hashUtil.getRootHash();
        ElFinderFile rootDir = new ElFinderFile(rootHash, "Root", true);
        rootDir.setParentHash(null);
        rootDir.setSize(0);
        rootDir.setTimestamp(System.currentTimeMillis() / 1000);
        rootDir.setMime("directory");
        rootDir.setReadable(true);
        rootDir.setWritable(true);
        rootDir.setLocked(false);
        rootDir.setHasSubdirectories(0);
        rootDir.setVolumeId("l1_");
        return rootDir;
    }

    /**
     * Get parent hash for a path
     * 
     * @param path file path
     * @return parent hash
     */
    private String getParentHash(String path) {
        if (!StringUtils.hasText(path)) {
            return null; // Root has no parent
        }

        String parentPath = getParentPath(path);
        return encodePath(parentPath);
    }

    /**
     * Add initialization options to response
     * 
     * @param response elFinder response
     */
    private void addInitializationOptions(ElFinderResponse response) {
        // Set API version
        response.setApiVersion("2.1");

        // Add connector options
        response.addOption("path", "");
        response.addOption("disabled", new String[]{});
        response.addOption("separator", "/");
        response.addOption("copyOverwrite", 1);
        response.addOption("uploadOverwrite", 1);
        response.addOption("uploadMaxSize", "100M");
        response.addOption("uploadMaxConn", 3);
        response.addOption("uploadMime", new String[]{
            "image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp",
            "text/plain", "text/html", "text/css", "text/javascript",
            "application/pdf", "application/zip", "application/json"
        });
        response.addOption("jpgQuality", 100);
        response.addOption("tmbSize", 48);
        response.addOption("tmbCrop", false);
        response.addOption("tmbBgColor", "transparent");

        // Add UI options
        response.addOption("uiOptions", new java.util.HashMap<String, Object>() {{
            put("toolbar", new String[][]{
                {"back", "forward"},
                {"reload"},
                {"home", "up"},
                {"mkdir", "mkfile", "upload"},
                {"open", "download", "getfile"},
                {"info"},
                {"quicklook"},
                {"copy", "cut", "paste"},
                {"rm"},
                {"duplicate", "rename", "edit", "resize"},
                {"extract", "archive"},
                {"search"},
                {"view", "sort"},
                {"help"}
            });
            put("tree", new java.util.HashMap<String, Object>() {{
                put("openRootOnLoad", true);
                put("syncTree", true);
            }});
            put("navbar", new java.util.HashMap<String, Object>() {{
                put("minWidth", 150);
                put("maxWidth", 500);
            }});
        }});

        logger.debug("Added initialization options to response");
    }

    /**
     * Add tree structure to response
     * 
     * @param response elFinder response
     * @param currentPath current path
     * @param userId user ID
     */
    private void addTreeStructure(ElFinderResponse response, String currentPath, String userId) {
        try {
            // For now, just mark directories that have subdirectories
            // A full tree implementation would recursively load directory structure
            List<ElFinderFile> files = response.getFiles();
            
            for (ElFinderFile file : files) {
                if (file.isDirectory()) {
                    String dirPath = decodePath(file.getHash());
                    List<FileInfo> subFiles = fileManagerService.listFiles(dirPath, userId);
                    
                    // Check if directory has subdirectories
                    boolean hasSubdirs = subFiles.stream().anyMatch(FileInfo::isDirectory);
                    file.setHasSubdirectories(hasSubdirs ? 1 : 0);
                }
            }

            logger.debug("Added tree structure information");

        } catch (Exception e) {
            logger.warn("Error adding tree structure: {}", e.getMessage(), e);
        }
    }
}