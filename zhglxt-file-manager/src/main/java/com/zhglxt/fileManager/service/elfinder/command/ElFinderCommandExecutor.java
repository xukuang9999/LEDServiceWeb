package com.zhglxt.fileManager.service.elfinder.command;

import com.zhglxt.fileManager.domain.elfinder.ElFinderRequest;
import com.zhglxt.fileManager.domain.elfinder.ElFinderResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * elFinder Command Executor
 * Dispatches elFinder commands to appropriate handlers
 * 
 * @author zhglxt
 */
@Service
public class ElFinderCommandExecutor {

    private static final Logger logger = LoggerFactory.getLogger(ElFinderCommandExecutor.class);

    private final Map<String, ElFinderCommand> commands = new HashMap<>();

    @Autowired
    public ElFinderCommandExecutor(
            OpenCommand openCommand,
            TreeCommand treeCommand,
            ParentsCommand parentsCommand,
            FileCommand fileCommand,
            UploadCommand uploadCommand,
            MkdirCommand mkdirCommand,
            RmCommand rmCommand,
            RenameCommand renameCommand,
            DuplicateCommand duplicateCommand,
            PasteCommand pasteCommand,
            GetCommand getCommand,
            PutCommand putCommand,
            SearchCommand searchCommand,
            InfoCommand infoCommand,
            DimCommand dimCommand,
            ResizeCommand resizeCommand,
            TmbCommand tmbCommand) {
        
        // Register all commands
        registerCommand("open", openCommand);
        registerCommand("tree", treeCommand);
        registerCommand("parents", parentsCommand);
        registerCommand("file", fileCommand);
        registerCommand("upload", uploadCommand);
        registerCommand("mkdir", mkdirCommand);
        registerCommand("rm", rmCommand);
        registerCommand("rename", renameCommand);
        registerCommand("duplicate", duplicateCommand);
        registerCommand("paste", pasteCommand);
        registerCommand("get", getCommand);
        registerCommand("put", putCommand);
        registerCommand("search", searchCommand);
        registerCommand("info", infoCommand);
        registerCommand("dim", dimCommand);
        registerCommand("resize", resizeCommand);
        registerCommand("tmb", tmbCommand);
        
        logger.info("Registered {} elFinder commands", commands.size());
    }

    /**
     * Execute elFinder command
     * 
     * @param request elFinder request
     * @param userId user ID
     * @param httpRequest HTTP servlet request
     * @return elFinder response
     */
    public ElFinderResponse execute(ElFinderRequest request, String userId, HttpServletRequest httpRequest) {
        try {
            String commandName = request.getCmd();
            
            logger.debug("Executing elFinder command: {} for user: {}", commandName, userId);

            // Get command handler
            ElFinderCommand command = commands.get(commandName);
            if (command == null) {
                logger.warn("Unknown elFinder command: {}", commandName);
                return ElFinderResponse.error("Unknown command: " + commandName);
            }

            // Validate request for this command
            if (!command.isValidRequest(request)) {
                logger.warn("Invalid request for command: {}", commandName);
                return ElFinderResponse.error("Invalid request for command: " + commandName);
            }

            // Execute command
            ElFinderResponse response = command.execute(request, userId, httpRequest);
            
            // Log execution result
            if (response.hasErrors()) {
                logger.warn("Command {} failed with errors: {}", commandName, response.getError());
            } else {
                logger.debug("Command {} executed successfully", commandName);
            }

            return response;

        } catch (Exception e) {
            logger.error("Error executing elFinder command: {}", e.getMessage(), e);
            return ElFinderResponse.error("Command execution failed: " + e.getMessage());
        }
    }

    /**
     * Register a command handler
     * 
     * @param commandName command name
     * @param command command handler
     */
    private void registerCommand(String commandName, ElFinderCommand command) {
        commands.put(commandName, command);
        logger.debug("Registered elFinder command: {}", commandName);
    }

    /**
     * Get all registered command names
     * 
     * @return set of command names
     */
    public java.util.Set<String> getRegisteredCommands() {
        return commands.keySet();
    }

    /**
     * Check if command is supported
     * 
     * @param commandName command name
     * @return true if supported
     */
    public boolean isCommandSupported(String commandName) {
        return commands.containsKey(commandName);
    }
}