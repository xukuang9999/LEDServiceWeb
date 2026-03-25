package com.zhglxt.fileManager.service.elfinder.command;

import com.zhglxt.fileManager.domain.elfinder.ElFinderRequest;
import com.zhglxt.fileManager.domain.elfinder.ElFinderResponse;

import jakarta.servlet.http.HttpServletRequest;

/**
 * elFinder Command Interface
 * Base interface for all elFinder command handlers
 * 
 * @author zhglxt
 */
public interface ElFinderCommand {

    /**
     * Execute the command
     * 
     * @param request elFinder request
     * @param userId user ID
     * @param httpRequest HTTP servlet request
     * @return elFinder response
     */
    ElFinderResponse execute(ElFinderRequest request, String userId, HttpServletRequest httpRequest);

    /**
     * Validate if the request is valid for this command
     * 
     * @param request elFinder request
     * @return true if valid
     */
    boolean isValidRequest(ElFinderRequest request);

    /**
     * Get command name
     * 
     * @return command name
     */
    String getCommandName();
}