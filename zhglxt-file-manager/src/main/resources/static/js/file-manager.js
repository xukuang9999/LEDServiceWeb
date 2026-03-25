/**
 * File Manager JavaScript Library
 * Provides utilities for integrating with the file manager
 * 
 * @author zhglxt
 */

(function($) {
    'use strict';
    
    // 命名空间
    window.FileManager = window.FileManager || {};
    
    /**
     * 文件管理器配置
     */
    var config = {
        baseUrl: '/file-manager',
        apiUrl: '/api/file-manager',
        connectorUrl: '/api/file-manager/connector',
        defaultLanguage: 'zh_CN',
        popupWidth: 1000,
        popupHeight: 600,
        browserWidth: 800,
        browserHeight: 500
    };
    
    /**
     * 打开文件管理器弹窗
     * 
     * @param {Object} options 配置选项
     * @param {Function} callback 回调函数
     */
    FileManager.openPopup = function(options, callback) {
        options = $.extend({
            title: '文件管理器',
            width: config.popupWidth,
            height: config.popupHeight,
            mode: 'manage' // manage | select
        }, options);
        
        var url = config.baseUrl + '/popup';
        if (options.mode === 'select') {
            url += '?mode=select';
        }
        
        if (typeof layer !== 'undefined') {
            // 使用layer弹窗
            layer.open({
                type: 2,
                title: options.title,
                shadeClose: false,
                shade: 0.3,
                maxmin: true,
                area: [options.width + 'px', options.height + 'px'],
                content: url,
                success: function(layero, index) {
                    // 设置回调函数
                    if (callback && typeof callback === 'function') {
                        window.onFileSelected = callback;
                    }
                },
                end: function() {
                    // 清理回调函数
                    window.onFileSelected = null;
                }
            });
        } else {
            // 使用原生弹窗
            var popup = window.open(url, 'fileManagerPopup', 
                'width=' + options.width + ',height=' + options.height + 
                ',scrollbars=yes,resizable=yes,toolbar=no,menubar=no,location=no,status=no');
            
            if (popup) {
                popup.focus();
                // 设置回调函数
                if (callback && typeof callback === 'function') {
                    window.onFileSelected = callback;
                }
            } else {
                alert('弹窗被阻止，请允许弹窗后重试');
            }
        }
    };
    
    /**
     * 打开文件浏览器
     * 
     * @param {Object} options 配置选项
     * @param {Function} callback 回调函数
     */
    FileManager.openBrowser = function(options, callback) {
        options = $.extend({
            title: '文件浏览器',
            width: config.browserWidth,
            height: config.browserHeight,
            multiple: true,
            types: [] // 允许的文件类型，如: ['jpg', 'png', 'gif']
        }, options);
        
        var url = config.baseUrl + '/browser';
        var params = [];
        
        if (!options.multiple) {
            params.push('multiple=false');
        }
        
        if (options.types && options.types.length > 0) {
            params.push('types=' + options.types.join(','));
        }
        
        if (params.length > 0) {
            url += '?' + params.join('&');
        }
        
        if (typeof layer !== 'undefined') {
            // 使用layer弹窗
            layer.open({
                type: 2,
                title: options.title,
                shadeClose: false,
                shade: 0.3,
                area: [options.width + 'px', options.height + 'px'],
                content: url,
                success: function(layero, index) {
                    // 设置回调函数
                    if (callback && typeof callback === 'function') {
                        window.onFileBrowserSelection = callback;
                    }
                },
                end: function() {
                    // 清理回调函数
                    window.onFileBrowserSelection = null;
                }
            });
        } else {
            // 使用原生弹窗
            var popup = window.open(url, 'fileBrowserPopup', 
                'width=' + options.width + ',height=' + options.height + 
                ',scrollbars=yes,resizable=yes,toolbar=no,menubar=no,location=no,status=no');
            
            if (popup) {
                popup.focus();
                // 设置回调函数
                if (callback && typeof callback === 'function') {
                    window.onFileBrowserSelection = callback;
                }
            } else {
                alert('弹窗被阻止，请允许弹窗后重试');
            }
        }
    };
    
    /**
     * 选择单个文件
     * 
     * @param {Object} options 配置选项
     * @param {Function} callback 回调函数
     */
    FileManager.selectFile = function(options, callback) {
        options = $.extend({
            title: '选择文件',
            multiple: false
        }, options);
        
        FileManager.openBrowser(options, function(files) {
            if (files && files.length > 0) {
                callback(files[0]);
            }
        });
    };
    
    /**
     * 选择多个文件
     * 
     * @param {Object} options 配置选项
     * @param {Function} callback 回调函数
     */
    FileManager.selectFiles = function(options, callback) {
        options = $.extend({
            title: '选择文件',
            multiple: true
        }, options);
        
        FileManager.openBrowser(options, callback);
    };
    
    /**
     * 选择图片文件
     * 
     * @param {Object} options 配置选项
     * @param {Function} callback 回调函数
     */
    FileManager.selectImage = function(options, callback) {
        options = $.extend({
            title: '选择图片',
            types: ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'],
            multiple: false
        }, options);
        
        FileManager.selectFile(options, callback);
    };
    
    /**
     * 选择多个图片文件
     * 
     * @param {Object} options 配置选项
     * @param {Function} callback 回调函数
     */
    FileManager.selectImages = function(options, callback) {
        options = $.extend({
            title: '选择图片',
            types: ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'],
            multiple: true
        }, options);
        
        FileManager.selectFiles(options, callback);
    };
    
    /**
     * 上传文件
     * 
     * @param {File|FileList} files 文件对象
     * @param {Object} options 配置选项
     * @param {Function} callback 回调函数
     */
    FileManager.uploadFiles = function(files, options, callback) {
        if (typeof options === 'function') {
            callback = options;
            options = {};
        }
        
        options = $.extend({
            directory: '/',
            showProgress: true
        }, options);
        
        var formData = new FormData();
        
        // 添加文件
        if (files instanceof FileList) {
            for (var i = 0; i < files.length; i++) {
                formData.append('files', files[i]);
            }
        } else if (files instanceof File) {
            formData.append('files', files);
        } else {
            if (callback) callback({success: false, message: '无效的文件对象'});
            return;
        }
        
        // 添加目录参数
        formData.append('directory', options.directory);
        
        // 发送请求
        $.ajax({
            url: config.apiUrl + '/upload',
            type: 'POST',
            data: formData,
            processData: false,
            contentType: false,
            xhr: function() {
                var xhr = new window.XMLHttpRequest();
                
                // 上传进度
                if (options.showProgress) {
                    xhr.upload.addEventListener('progress', function(e) {
                        if (e.lengthComputable) {
                            var percentComplete = (e.loaded / e.total) * 100;
                            if (options.onProgress) {
                                options.onProgress(percentComplete);
                            }
                        }
                    }, false);
                }
                
                return xhr;
            },
            success: function(response) {
                if (callback) callback(response);
            },
            error: function(xhr, status, error) {
                if (callback) {
                    callback({
                        success: false,
                        message: '上传失败: ' + error
                    });
                }
            }
        });
    };
    
    /**
     * 获取文件信息
     * 
     * @param {String} filePath 文件路径
     * @param {Function} callback 回调函数
     */
    FileManager.getFileInfo = function(filePath, callback) {
        $.ajax({
            url: config.apiUrl + '/info',
            type: 'GET',
            data: { path: filePath },
            success: function(response) {
                if (callback) callback(response);
            },
            error: function(xhr, status, error) {
                if (callback) {
                    callback({
                        success: false,
                        message: '获取文件信息失败: ' + error
                    });
                }
            }
        });
    };
    
    /**
     * 删除文件
     * 
     * @param {String|Array} filePaths 文件路径
     * @param {Function} callback 回调函数
     */
    FileManager.deleteFiles = function(filePaths, callback) {
        var paths = Array.isArray(filePaths) ? filePaths : [filePaths];
        
        $.ajax({
            url: config.apiUrl + '/delete',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ paths: paths }),
            success: function(response) {
                if (callback) callback(response);
            },
            error: function(xhr, status, error) {
                if (callback) {
                    callback({
                        success: false,
                        message: '删除文件失败: ' + error
                    });
                }
            }
        });
    };
    
    /**
     * 创建文件夹
     * 
     * @param {String} parentPath 父目录路径
     * @param {String} folderName 文件夹名称
     * @param {Function} callback 回调函数
     */
    FileManager.createFolder = function(parentPath, folderName, callback) {
        $.ajax({
            url: config.apiUrl + '/mkdir',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({
                parent: parentPath,
                name: folderName
            }),
            success: function(response) {
                if (callback) callback(response);
            },
            error: function(xhr, status, error) {
                if (callback) {
                    callback({
                        success: false,
                        message: '创建文件夹失败: ' + error
                    });
                }
            }
        });
    };
    
    /**
     * 获取文件下载URL
     * 
     * @param {String} filePath 文件路径
     * @return {String} 下载URL
     */
    FileManager.getDownloadUrl = function(filePath) {
        return config.apiUrl + '/download?path=' + encodeURIComponent(filePath);
    };
    
    /**
     * 获取文件预览URL
     * 
     * @param {String} filePath 文件路径
     * @return {String} 预览URL
     */
    FileManager.getPreviewUrl = function(filePath) {
        return config.apiUrl + '/preview?path=' + encodeURIComponent(filePath);
    };
    
    /**
     * 获取缩略图URL
     * 
     * @param {String} filePath 文件路径
     * @param {String} size 缩略图尺寸 (small|medium|large)
     * @return {String} 缩略图URL
     */
    FileManager.getThumbnailUrl = function(filePath, size) {
        size = size || 'medium';
        return config.apiUrl + '/thumbnail?path=' + encodeURIComponent(filePath) + '&size=' + size;
    };
    
    /**
     * 设置配置
     * 
     * @param {Object} newConfig 新配置
     */
    FileManager.setConfig = function(newConfig) {
        config = $.extend(config, newConfig);
    };
    
    /**
     * 获取配置
     * 
     * @return {Object} 当前配置
     */
    FileManager.getConfig = function() {
        return $.extend({}, config);
    };
    
    // jQuery插件扩展
    $.fn.fileManagerButton = function(options) {
        return this.each(function() {
            var $this = $(this);
            var settings = $.extend({
                type: 'select', // select | upload | manage
                multiple: false,
                fileTypes: [],
                onSelect: null,
                onUpload: null
            }, options);
            
            $this.click(function(e) {
                e.preventDefault();
                
                switch (settings.type) {
                    case 'select':
                        FileManager.openBrowser({
                            multiple: settings.multiple,
                            types: settings.fileTypes
                        }, settings.onSelect);
                        break;
                        
                    case 'upload':
                        // 创建隐藏的文件输入框
                        var $fileInput = $('<input type="file" style="display:none;">');
                        if (settings.multiple) {
                            $fileInput.attr('multiple', 'multiple');
                        }
                        if (settings.fileTypes.length > 0) {
                            $fileInput.attr('accept', '.' + settings.fileTypes.join(',.'));
                        }
                        
                        $fileInput.change(function() {
                            var files = this.files;
                            if (files.length > 0) {
                                FileManager.uploadFiles(files, settings.onUpload);
                            }
                        });
                        
                        $fileInput.click();
                        break;
                        
                    case 'manage':
                        FileManager.openPopup();
                        break;
                }
            });
        });
    };
    
})(jQuery);