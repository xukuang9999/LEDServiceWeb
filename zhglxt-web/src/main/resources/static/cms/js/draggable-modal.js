/**
 * Draggable Modal Enhancement
 * Adds drag functionality and proper scrolling to modals
 */

(function() {
    'use strict';
    
    // Make modal draggable
    window.makeDraggable = function($modal) {
        var $dialog = $modal.find('.modal-dialog');
        var $header = $modal.find('.modal-header');
        
        if (!$dialog.length || !$header.length) return;
        
        var isDragging = false;
        var startX, startY, initialX, initialY;
        
        // Add draggable cursor to header
        $header.css({
            'cursor': 'move',
            'user-select': 'none',
            '-webkit-user-select': 'none',
            '-moz-user-select': 'none',
            '-ms-user-select': 'none'
        });
        
        // Add drag handle indicator
        if (!$header.find('.drag-handle').length) {
            $header.prepend('<div class="drag-handle" style="position: absolute; top: 50%; left: 10px; transform: translateY(-50%); color: rgba(255,255,255,0.7); font-size: 1.2rem;">⋮⋮</div>');
        }
        
        // Mouse events
        $header.on('mousedown', function(e) {
            // Don't drag if clicking on close button or other interactive elements
            if ($(e.target).closest('.modal-close, button, a, input').length) return;
            
            isDragging = true;
            startX = e.clientX;
            startY = e.clientY;
            
            var dialogOffset = $dialog.offset();
            var modalOffset = $modal.offset();
            
            initialX = dialogOffset.left - modalOffset.left;
            initialY = dialogOffset.top - modalOffset.top;
            
            $dialog.css({
                'position': 'absolute',
                'margin': '0',
                'left': initialX + 'px',
                'top': initialY + 'px'
            });
            
            $('body').addClass('dragging-modal');
            e.preventDefault();
        });
        
        $(document).on('mousemove', function(e) {
            if (!isDragging) return;
            
            var deltaX = e.clientX - startX;
            var deltaY = e.clientY - startY;
            
            var newX = initialX + deltaX;
            var newY = initialY + deltaY;
            
            // Constrain to viewport
            var modalWidth = $modal.width();
            var modalHeight = $modal.height();
            var dialogWidth = $dialog.outerWidth();
            var dialogHeight = $dialog.outerHeight();
            
            newX = Math.max(10, Math.min(newX, modalWidth - dialogWidth - 10));
            newY = Math.max(10, Math.min(newY, modalHeight - dialogHeight - 10));
            
            $dialog.css({
                'left': newX + 'px',
                'top': newY + 'px'
            });
        });
        
        $(document).on('mouseup', function() {
            if (isDragging) {
                isDragging = false;
                $('body').removeClass('dragging-modal');
            }
        });
        
        // Touch events for mobile
        $header.on('touchstart', function(e) {
            if ($(e.target).closest('.modal-close, button, a, input').length) return;
            
            var touch = e.originalEvent.touches[0];
            isDragging = true;
            startX = touch.clientX;
            startY = touch.clientY;
            
            var dialogOffset = $dialog.offset();
            var modalOffset = $modal.offset();
            
            initialX = dialogOffset.left - modalOffset.left;
            initialY = dialogOffset.top - modalOffset.top;
            
            $dialog.css({
                'position': 'absolute',
                'margin': '0',
                'left': initialX + 'px',
                'top': initialY + 'px'
            });
            
            e.preventDefault();
        });
        
        $(document).on('touchmove', function(e) {
            if (!isDragging) return;
            
            var touch = e.originalEvent.touches[0];
            var deltaX = touch.clientX - startX;
            var deltaY = touch.clientY - startY;
            
            var newX = initialX + deltaX;
            var newY = initialY + deltaY;
            
            // Constrain to viewport
            var modalWidth = $modal.width();
            var modalHeight = $modal.height();
            var dialogWidth = $dialog.outerWidth();
            var dialogHeight = $dialog.outerHeight();
            
            newX = Math.max(10, Math.min(newX, modalWidth - dialogWidth - 10));
            newY = Math.max(10, Math.min(newY, modalHeight - dialogHeight - 10));
            
            $dialog.css({
                'left': newX + 'px',
                'top': newY + 'px'
            });
            
            e.preventDefault();
        });
        
        $(document).on('touchend', function() {
            if (isDragging) {
                isDragging = false;
            }
        });
        
        console.log('Modal made draggable');
    };
    
    // Add resize handles to modal
    window.makeResizable = function($modal) {
        var $dialog = $modal.find('.modal-dialog');
        var $content = $modal.find('.modal-content');
        
        if (!$dialog.length) return;
        
        // Add resize handle
        if (!$content.find('.resize-handle').length) {
            $content.append('<div class="resize-handle" style="position: absolute; bottom: 0; right: 0; width: 20px; height: 20px; cursor: se-resize; background: linear-gradient(-45deg, transparent 0%, transparent 40%, #ccc 40%, #ccc 60%, transparent 60%); opacity: 0.7;"></div>');
        }
        
        var $handle = $content.find('.resize-handle');
        var isResizing = false;
        var startX, startY, startWidth, startHeight;
        
        $handle.on('mousedown', function(e) {
            isResizing = true;
            startX = e.clientX;
            startY = e.clientY;
            startWidth = $dialog.outerWidth();
            startHeight = $dialog.outerHeight();
            
            $('body').addClass('resizing-modal');
            e.preventDefault();
            e.stopPropagation();
        });
        
        $(document).on('mousemove', function(e) {
            if (!isResizing) return;
            
            var deltaX = e.clientX - startX;
            var deltaY = e.clientY - startY;
            
            var newWidth = Math.max(300, startWidth + deltaX);
            var newHeight = Math.max(200, startHeight + deltaY);
            
            // Constrain to viewport
            var maxWidth = $(window).width() - 40;
            var maxHeight = $(window).height() - 40;
            
            newWidth = Math.min(newWidth, maxWidth);
            newHeight = Math.min(newHeight, maxHeight);
            
            $dialog.css({
                'width': newWidth + 'px',
                'height': newHeight + 'px',
                'max-width': 'none'
            });
            
            // Adjust content height
            var headerHeight = $modal.find('.modal-header').outerHeight() || 0;
            var footerHeight = $modal.find('.modal-footer').outerHeight() || 0;
            var contentHeight = newHeight - headerHeight - footerHeight - 40;
            
            $modal.find('.modal-body').css({
                'max-height': contentHeight + 'px',
                'overflow-y': 'auto'
            });
        });
        
        $(document).on('mouseup', function() {
            if (isResizing) {
                isResizing = false;
                $('body').removeClass('resizing-modal');
            }
        });
        
        console.log('Modal made resizable');
    };
    
    // Enhanced scrolling for modal content
    window.enhanceModalScrolling = function($modal) {
        var $body = $modal.find('.modal-body');
        var $content = $modal.find('.modal-content-text');
        
        // Ensure proper scrolling
        $body.css({
            'overflow-y': 'auto',
            'overflow-x': 'hidden',
            '-webkit-overflow-scrolling': 'touch',
            'scroll-behavior': 'smooth'
        });
        
        $content.css({
            'overflow-wrap': 'break-word',
            'word-wrap': 'break-word'
        });
        
        // Add custom scrollbar styling
        var scrollbarCSS = `
            <style id="modal-scrollbar-${$modal.attr('id')}">
                #${$modal.attr('id')} .modal-body::-webkit-scrollbar {
                    width: 8px;
                }
                #${$modal.attr('id')} .modal-body::-webkit-scrollbar-track {
                    background: #f1f1f1;
                    border-radius: 4px;
                }
                #${$modal.attr('id')} .modal-body::-webkit-scrollbar-thumb {
                    background: #888;
                    border-radius: 4px;
                }
                #${$modal.attr('id')} .modal-body::-webkit-scrollbar-thumb:hover {
                    background: #555;
                }
            </style>
        `;
        
        if (!$('#modal-scrollbar-' + $modal.attr('id')).length) {
            $('head').append(scrollbarCSS);
        }
        
        console.log('Modal scrolling enhanced');
    };
    
    // Position modal optimally in viewport
    window.positionModalOptimally = function($modal) {
        var $dialog = $modal.find('.modal-dialog');
        var windowWidth = $(window).width();
        var windowHeight = $(window).height();
        var dialogWidth = $dialog.outerWidth();
        var dialogHeight = $dialog.outerHeight();
        
        // Calculate optimal position
        var left = Math.max(20, (windowWidth - dialogWidth) / 2);
        var top = Math.max(20, (windowHeight - dialogHeight) / 2);
        
        // If modal is taller than viewport, position at top with some margin
        if (dialogHeight > windowHeight - 40) {
            top = 20;
            
            // Make modal body scrollable
            var headerHeight = $modal.find('.modal-header').outerHeight() || 0;
            var footerHeight = $modal.find('.modal-footer').outerHeight() || 0;
            var maxBodyHeight = windowHeight - headerHeight - footerHeight - 80;
            
            $modal.find('.modal-body').css({
                'max-height': maxBodyHeight + 'px',
                'overflow-y': 'auto'
            });
        }
        
        $dialog.css({
            'position': 'absolute',
            'left': left + 'px',
            'top': top + 'px',
            'margin': '0'
        });
        
        console.log('Modal positioned optimally at', left, top);
    };
    
})();