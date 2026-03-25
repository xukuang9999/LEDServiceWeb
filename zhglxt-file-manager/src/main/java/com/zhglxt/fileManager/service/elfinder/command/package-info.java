/**
 * elFinder Command Implementations Package
 * 
 * This package contains all the command implementations for the elFinder protocol.
 * Each command handles a specific elFinder operation and follows a consistent
 * pattern for request validation, execution, and response formatting.
 * 
 * Standard elFinder Commands:
 * - open: Opens directories and returns contents
 * - tree: Returns directory tree structure
 * - parents: Returns parent directories for navigation
 * - file: Serves file content for download/preview
 * - upload: Handles file uploads
 * - mkdir: Creates new directories
 * - rm: Deletes files and directories
 * - rename: Renames files and directories
 * - duplicate: Duplicates files and directories
 * - paste: Moves or copies files to destinations
 * - get: Gets file content for editing
 * - put: Saves file content after editing
 * - search: Searches for files by name pattern
 * - info: Returns detailed file information
 * - dim: Returns image dimensions
 * - resize: Resizes images (placeholder)
 * - tmb: Generates thumbnails for images
 * 
 * All commands extend AbstractElFinderCommand which provides common
 * functionality for file conversion, error handling, and response creation.
 * 
 * @author zhglxt
 * @version 1.0
 * @since 2024-01-01
 */
package com.zhglxt.fileManager.service.elfinder.command;