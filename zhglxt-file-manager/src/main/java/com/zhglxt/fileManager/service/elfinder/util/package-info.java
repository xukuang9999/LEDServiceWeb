/**
 * elFinder Utility Classes Package
 * 
 * This package contains utility classes that support the elFinder connector
 * implementation. These utilities handle common tasks like path encoding/decoding,
 * hash generation, and other helper functions.
 * 
 * Key Utilities:
 * - ElFinderHashUtil: Handles encoding and decoding of file paths to/from elFinder hashes
 * 
 * The hash utility is particularly important as it provides the mapping between
 * internal file paths and the hash-based identifiers used by the elFinder protocol.
 * This abstraction allows for secure path handling and prevents direct file system
 * access through the web interface.
 * 
 * @author zhglxt
 * @version 1.0
 * @since 2024-01-01
 */
package com.zhglxt.fileManager.service.elfinder.util;