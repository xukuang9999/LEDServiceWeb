# zhglxt File Manager - User Manual

## Table of Contents
1. [Introduction](#introduction)
2. [Getting Started](#getting-started)
3. [File Operations](#file-operations)
4. [Interface Overview](#interface-overview)
5. [Advanced Features](#advanced-features)
6. [Troubleshooting](#troubleshooting)
7. [FAQ](#faq)

## Introduction

The zhglxt File Manager is a comprehensive web-based file management system that provides an intuitive interface for managing files and directories. Built on the elFinder library, it offers desktop-like functionality with advanced features including thumbnail generation, watermarking, and multi-storage backend support.

### Key Features
- **Intuitive Interface**: Desktop-like file management experience
- **Multiple Storage Backends**: Support for local storage and cloud providers (AWS S3, Alibaba OSS, Tencent COS)
- **Image Processing**: Automatic thumbnail generation and watermarking
- **Security**: Role-based access control and file validation
- **Multi-language**: Support for English and Chinese
- **Mobile Responsive**: Works on desktop, tablet, and mobile devices

## Getting Started

### Accessing the File Manager

1. **Login**: Ensure you are logged into the zhglxt system
2. **Navigate**: Go to the File Manager section in the main menu
3. **Interface**: The file manager will load with a familiar two-pane interface

### First Time Setup

When you first access the file manager, you'll see:
- **Left Panel**: Directory tree navigation
- **Right Panel**: File listing and preview area
- **Toolbar**: Action buttons for file operations
- **Status Bar**: Information about selected files and operations

## File Operations

### Uploading Files

#### Method 1: Drag and Drop
1. Select files from your computer
2. Drag them into the file manager window
3. Drop them in the desired directory
4. Wait for the upload progress to complete

#### Method 2: Upload Button
1. Click the "Upload" button in the toolbar
2. Select files using the file dialog
3. Choose the target directory
4. Click "Upload" to start the process

#### Upload Restrictions
- **File Size**: Maximum 100MB per file (configurable by administrator)
- **File Types**: Only allowed file extensions are accepted
- **Security**: All files are scanned for malicious content

### Downloading Files

#### Single File Download
1. Right-click on the file
2. Select "Download" from the context menu
3. The file will be downloaded to your default download folder

#### Multiple File Download
1. Select multiple files using Ctrl+Click or Shift+Click
2. Right-click on the selection
3. Choose "Download" to create a ZIP archive

### File Navigation

#### Directory Tree
- **Expand/Collapse**: Click the arrow icons to expand or collapse directories
- **Quick Navigation**: Click on any directory name to navigate directly
- **Breadcrumb**: Use the breadcrumb navigation at the top

#### File Listing
- **View Modes**: Switch between list view and icon view
- **Sorting**: Click column headers to sort by name, size, date, or type
- **Search**: Use the search box to find files quickly

### File Management

#### Creating Directories
1. Right-click in an empty area
2. Select "New Folder" from the context menu
3. Enter the folder name
4. Press Enter to create

#### Renaming Files/Folders
1. Right-click on the item
2. Select "Rename" from the context menu
3. Enter the new name
4. Press Enter to confirm

#### Moving Files/Folders
1. Select the items to move
2. Drag them to the destination folder
3. Or use Cut (Ctrl+X) and Paste (Ctrl+V)

#### Copying Files/Folders
1. Select the items to copy
2. Right-click and choose "Copy" (or Ctrl+C)
3. Navigate to the destination
4. Right-click and choose "Paste" (or Ctrl+V)

#### Deleting Files/Folders
1. Select the items to delete
2. Press Delete key or right-click and choose "Delete"
3. Confirm the deletion in the dialog box

### File Preview

#### Image Preview
- **Thumbnails**: Images automatically show thumbnails
- **Quick Look**: Double-click images for full-size preview
- **Slideshow**: Use arrow keys to navigate through images

#### Document Preview
- **Text Files**: Preview content directly in the interface
- **PDF Files**: Basic PDF preview (first page)
- **Office Documents**: Download required for full viewing

## Interface Overview

### Toolbar Buttons

| Button | Function | Shortcut |
|--------|----------|----------|
| Back | Navigate to previous directory | Alt+← |
| Forward | Navigate to next directory | Alt+→ |
| Up | Go to parent directory | Alt+↑ |
| Reload | Refresh current directory | F5 |
| Home | Go to home directory | Alt+Home |
| Upload | Upload files | Ctrl+U |
| New Folder | Create new directory | Ctrl+Shift+N |
| Cut | Cut selected items | Ctrl+X |
| Copy | Copy selected items | Ctrl+C |
| Paste | Paste items | Ctrl+V |
| Delete | Delete selected items | Delete |
| Search | Search files | Ctrl+F |
| View | Change view mode | Ctrl+1/2 |

### Context Menu Options

Right-clicking on files or folders provides these options:
- **Open**: Open file or enter directory
- **Download**: Download file(s)
- **Cut/Copy/Paste**: File operations
- **Rename**: Rename item
- **Delete**: Delete item
- **Properties**: View file information
- **Preview**: Quick preview (images only)

### Status Information

The status bar shows:
- **Selection Info**: Number of selected items and total size
- **Directory Info**: Total files and folders in current directory
- **Upload Progress**: Progress of ongoing uploads
- **Storage Info**: Available storage space

## Advanced Features

### Watermarking

If watermarking is enabled by your administrator:
- **Automatic**: Watermarks are applied to uploaded images automatically
- **Configuration**: Watermark text, position, and opacity are pre-configured
- **File Types**: Only applies to supported image formats (JPG, PNG, GIF)

### Thumbnail Generation

- **Automatic**: Thumbnails are generated automatically for images
- **Caching**: Thumbnails are cached for faster loading
- **Sizes**: Multiple thumbnail sizes are available
- **Fallback**: Default icons are shown for non-image files

### Multi-language Support

To change the interface language:
1. Look for the language selector (usually in the top-right corner)
2. Choose between English and Chinese
3. The interface will update immediately

### Mobile Interface

On mobile devices:
- **Touch Navigation**: Tap to navigate, long-press for context menu
- **Swipe Gestures**: Swipe to navigate between directories
- **Responsive Design**: Interface adapts to screen size
- **Upload**: Use camera or file picker for uploads

## Troubleshooting

### Common Issues

#### Upload Fails
**Problem**: Files won't upload
**Solutions**:
- Check file size (must be under the limit)
- Verify file type is allowed
- Ensure you have write permissions
- Check internet connection

#### Slow Performance
**Problem**: Interface is slow or unresponsive
**Solutions**:
- Clear browser cache
- Reduce number of files in directory
- Check network connection
- Contact administrator about server resources

#### Can't See Files
**Problem**: Expected files are not visible
**Solutions**:
- Check if you have read permissions
- Verify you're in the correct directory
- Try refreshing the page (F5)
- Check if files were moved or deleted

#### Preview Not Working
**Problem**: File previews don't load
**Solutions**:
- Ensure file type supports preview
- Check if thumbnails are enabled
- Try downloading the file instead
- Clear browser cache

### Error Messages

#### "Permission Denied"
- You don't have the required permissions for this operation
- Contact your administrator to request access

#### "File Too Large"
- The file exceeds the maximum allowed size
- Try compressing the file or contact administrator

#### "Invalid File Type"
- The file type is not allowed
- Check the list of allowed extensions with your administrator

#### "Storage Full"
- The storage quota has been reached
- Delete unnecessary files or contact administrator

## FAQ

### General Questions

**Q: What browsers are supported?**
A: Modern browsers including Chrome, Firefox, Safari, and Edge. Internet Explorer is not supported.

**Q: Can I access the file manager on mobile devices?**
A: Yes, the interface is fully responsive and works on smartphones and tablets.

**Q: Is there a file size limit for uploads?**
A: Yes, the default limit is 100MB per file, but this can be configured by your administrator.

**Q: What file types are supported?**
A: This depends on your administrator's configuration. Common types include documents, images, and archives.

### Technical Questions

**Q: Are my files secure?**
A: Yes, all files are protected by the same security system as the main application, with role-based access control.

**Q: Can I share files with other users?**
A: File sharing depends on the permission system configured by your administrator.

**Q: Are files backed up?**
A: Backup policies depend on your organization's configuration. Contact your administrator for details.

**Q: Can I access files offline?**
A: No, the file manager requires an internet connection to function.

### Feature Questions

**Q: Can I edit files directly in the browser?**
A: Basic text file editing may be available. For complex documents, download and use appropriate software.

**Q: Are there keyboard shortcuts?**
A: Yes, see the Interface Overview section for a complete list of shortcuts.

**Q: Can I organize files with tags or labels?**
A: Currently, organization is done through the directory structure. Tags may be added in future versions.

**Q: Is version control available?**
A: Basic version control through file replacement is available. Advanced versioning may require additional configuration.

---

For additional support, contact your system administrator or refer to the Administrator Guide for configuration details.