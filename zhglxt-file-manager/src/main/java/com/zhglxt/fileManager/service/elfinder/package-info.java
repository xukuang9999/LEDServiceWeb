/**
 * elFinder Integration Package
 * 
 * This package contains the elFinder connector implementation that bridges
 * the elFinder JavaScript library with the file manager service layer.
 * 
 * Key Components:
 * - ElFinderConnector: Main connector that processes elFinder protocol requests
 * - ElFinderCommandExecutor: Dispatches commands to appropriate handlers
 * - Command implementations: Handle specific elFinder operations
 * - Utilities: Hash encoding/decoding and other helper functions
 * 
 * The implementation follows the elFinder 2.1 protocol specification and
 * provides full compatibility with the elFinder frontend library.
 * 
 * @author zhglxt
 * @version 1.0
 * @since 2024-01-01
 */
package com.zhglxt.fileManager.service.elfinder;