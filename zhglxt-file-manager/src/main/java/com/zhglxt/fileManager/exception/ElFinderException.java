package com.zhglxt.fileManager.exception;

/**
 * elFinder Exception
 * 
 * Thrown when elFinder connector operations fail.
 * 
 * @author zhglxt
 */
public class ElFinderException extends FileManagerException {

    private final String command;
    private final String target;

    public ElFinderException(String command, String message) {
        super("ELFINDER_ERROR", message);
        this.command = command;
        this.target = null;
        withOperation("ELFINDER_" + command);
    }

    public ElFinderException(String command, String message, Throwable cause) {
        super("ELFINDER_ERROR", message, cause);
        this.command = command;
        this.target = null;
        withOperation("ELFINDER_" + command);
    }

    public ElFinderException(String command, String target, String message) {
        super("ELFINDER_ERROR", message);
        this.command = command;
        this.target = target;
        withOperation("ELFINDER_" + command).withFilePath(target);
    }

    public ElFinderException(String command, String target, String message, Throwable cause) {
        super("ELFINDER_ERROR", message, cause);
        this.command = command;
        this.target = target;
        withOperation("ELFINDER_" + command).withFilePath(target);
    }

    public String getCommand() {
        return command;
    }

    public String getTarget() {
        return target;
    }
}