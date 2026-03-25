/**
 * Home Page Independent News Modal Component
 * Completely isolated modal system for home page news articles
 * Does NOT interfere with existing news menu modal systems
 */

(function() {
    'use strict';

    // Unique namespace to avoid conflicts
    const HOME_MODAL_NAMESPACE = 'homeNewsModal';
    
    // Modal state management
    let modalState = {
        isOpen: false,
        isMaximized: false,
        isDragging: false,
        isResizing: false,
        originalPosition: null,
        originalSize: null,
        dragOffset: { x: 0, y: 0 }
    };

    // Configuration
    const config = {
        modalId: 'home-news-modal-container',
        defaultWidth: 800,
        defaultHeight: 600,
        minWidth: 400,
        minHeight: 300,
        zIndex: 10000
    };

    /**
     * Create the modal HTML structure
     */
    function createModalHTML(newsData) {
        return `
            <div id="${config.modalId}" class="home-news-modal-overlay">
                <div class="home-news-modal-dialog" id="home-modal-dialog">
                    <div class="home-news-modal-content">
                        <!-- Header -->
                        <div class="home-news-modal-header" id="home-modal-header">
                            <div class="home-modal-title-section">
                                <div class="home-modal-category">
                                    <i class="fas fa-newspaper"></i>
                                    <span>News Article</span>
                                </div>
                                <h2 class="home-modal-title">${newsData.title || 'News Article'}</h2>
                                <div class="home-modal-meta">
                                    <span class="home-modal-date">${formatDate(newsData.createTime)}</span>
                                    <span class="home-modal-reading-time">
                                        <i class="fas fa-clock"></i>
                                        ${calculateReadingTime(newsData.content)}
                                    </span>
                                </div>
                            </div>
                            <div class="home-modal-controls">
                                <button class="home-modal-btn" id="home-minimize-btn" title="Minimize">
                                    <i class="fas fa-window-minimize"></i>
                                </button>
                                <button class="home-modal-btn" id="home-maximize-btn" title="Maximize">
                                    <i class="fas fa-window-maximize"></i>
                                </button>
                                <button class="home-modal-btn home-modal-close" id="home-close-btn" title="Close">
                                    <i class="fas fa-times"></i>
                                </button>
                            </div>
                            <div class="home-drag-handle" id="home-drag-handle">
                                <i class="fas fa-grip-horizontal"></i>
                            </div>
                        </div>

                        <!-- Body -->
                        <div class="home-news-modal-body" id="home-modal-body">
                            ${newsData.imageUrl ? `
                                <div class="home-modal-image">
                                    <img src="${newsData.imageUrl}" alt="Article Image" class="home-featured-img">
                                </div>
                            ` : ''}
                            
                            <div class="home-article-content">
                                ${newsData.content || '<p>Loading article content...</p>'}
                            </div>
                        </div>

                        <!-- Footer -->
                        <div class="home-news-modal-footer">
                            <div class="home-modal-actions">
                                <button class="home-action-btn home-share-btn" id="home-share-btn">
                                    <i class="fas fa-share-alt"></i>
                                    Share
                                </button>
                                <button class="home-action-btn home-print-btn" id="home-print-btn">
                                    <i class="fas fa-print"></i>
                                    Print
                                </button>
                                <button class="home-action-btn home-close-btn" id="home-footer-close-btn">
                                    <i class="fas fa-times"></i>
                                    Close
                                </button>
                            </div>
                        </div>

                        <!-- Resize Handle -->
                        <div class="home-resize-handle" id="home-resize-handle">
                            <i class="fas fa-grip-lines-vertical"></i>
                        </div>
                    </div>
                </div>
            </div>
        `;
    }

    /**
     * Format date for display
     */
    function formatDate(dateString) {
        if (!dateString) return 'Recent';
        try {
            const date = new Date(dateString);
            return date.toLocaleDateString('en-US', {
                year: 'numeric',
                month: 'long',
                day: 'numeric'
            });
        } catch (e) {
            return 'Recent';
        }
    }

    /**
     * Calculate reading time
     */
    function calculateReadingTime(content) {
        if (!content) return '3 min read';
        const wordsPerMinute = 200;
        const textContent = content.replace(/<[^>]*>/g, '');
        const wordCount = textContent.split(/\s+/).length;
        const readingTime = Math.ceil(wordCount / wordsPerMinute);
        return `${readingTime} min read`;
    }

    /**
     * Initialize modal positioning
     */
    function initializeModal(dialog) {
        dialog.style.width = config.defaultWidth + 'px';
        dialog.style.height = config.defaultHeight + 'px';
        dialog.style.position = 'absolute';
        
        // Center the modal
        const left = Math.max(0, (window.innerWidth - config.defaultWidth) / 2);
        const top = Math.max(0, (window.innerHeight - config.defaultHeight) / 2);
        
        dialog.style.left = left + 'px';
        dialog.style.top = top + 'px';
        
        // Store original position
        modalState.originalPosition = { left, top };
        modalState.originalSize = { width: config.defaultWidth, height: config.defaultHeight };
    }

    /**
     * Initialize drag functionality
     */
    function initializeDrag(dialog) {
        const header = dialog.querySelector('#home-modal-header');
        const dragHandle = dialog.querySelector('#home-drag-handle');
        
        function startDrag(e) {
            if (modalState.isMaximized) return;
            
            modalState.isDragging = true;
            document.body.classList.add('home-modal-dragging');
            
            const rect = dialog.getBoundingClientRect();
            modalState.dragOffset = {
                x: e.clientX - rect.left,
                y: e.clientY - rect.top
            };
            
            document.addEventListener('mousemove', drag);
            document.addEventListener('mouseup', stopDrag);
            e.preventDefault();
        }
        
        function drag(e) {
            if (!modalState.isDragging) return;
            
            const newLeft = Math.max(0, Math.min(window.innerWidth - dialog.offsetWidth, e.clientX - modalState.dragOffset.x));
            const newTop = Math.max(0, Math.min(window.innerHeight - dialog.offsetHeight, e.clientY - modalState.dragOffset.y));
            
            dialog.style.left = newLeft + 'px';
            dialog.style.top = newTop + 'px';
        }
        
        function stopDrag() {
            modalState.isDragging = false;
            document.body.classList.remove('home-modal-dragging');
            document.removeEventListener('mousemove', drag);
            document.removeEventListener('mouseup', stopDrag);
        }
        
        header.addEventListener('mousedown', startDrag);
        dragHandle.addEventListener('mousedown', startDrag);
        
        // Double-click to maximize
        header.addEventListener('dblclick', () => toggleMaximize(dialog));
    }

    /**
     * Initialize resize functionality
     */
    function initializeResize(dialog) {
        const resizeHandle = dialog.querySelector('#home-resize-handle');
        
        function startResize(e) {
            if (modalState.isMaximized) return;
            
            modalState.isResizing = true;
            document.body.classList.add('home-modal-resizing');
            
            document.addEventListener('mousemove', resize);
            document.addEventListener('mouseup', stopResize);
            e.preventDefault();
        }
        
        function resize(e) {
            if (!modalState.isResizing) return;
            
            const rect = dialog.getBoundingClientRect();
            const newWidth = Math.max(config.minWidth, e.clientX - rect.left);
            const newHeight = Math.max(config.minHeight, e.clientY - rect.top);
            
            dialog.style.width = newWidth + 'px';
            dialog.style.height = newHeight + 'px';
        }
        
        function stopResize() {
            modalState.isResizing = false;
            document.body.classList.remove('home-modal-resizing');
            document.removeEventListener('mousemove', resize);
            document.removeEventListener('mouseup', stopResize);
        }
        
        resizeHandle.addEventListener('mousedown', startResize);
    }

    /**
     * Toggle maximize/restore
     */
    function toggleMaximize(dialog) {
        const maximizeBtn = dialog.querySelector('#home-maximize-btn');
        const maximizeIcon = maximizeBtn.querySelector('i');
        
        if (modalState.isMaximized) {
            // Restore
            dialog.style.width = modalState.originalSize.width + 'px';
            dialog.style.height = modalState.originalSize.height + 'px';
            dialog.style.left = modalState.originalPosition.left + 'px';
            dialog.style.top = modalState.originalPosition.top + 'px';
            
            maximizeIcon.className = 'fas fa-window-maximize';
            maximizeBtn.title = 'Maximize';
            modalState.isMaximized = false;
        } else {
            // Store current position
            modalState.originalPosition = {
                left: dialog.offsetLeft,
                top: dialog.offsetTop
            };
            modalState.originalSize = {
                width: dialog.offsetWidth,
                height: dialog.offsetHeight
            };
            
            // Maximize
            dialog.style.width = (window.innerWidth - 40) + 'px';
            dialog.style.height = (window.innerHeight - 40) + 'px';
            dialog.style.left = '20px';
            dialog.style.top = '20px';
            
            maximizeIcon.className = 'fas fa-window-restore';
            maximizeBtn.title = 'Restore';
            modalState.isMaximized = true;
        }
    }

    /**
     * Initialize event listeners
     */
    function initializeEventListeners(dialog, overlay) {
        // Close buttons
        const closeBtns = dialog.querySelectorAll('.home-modal-close, #home-footer-close-btn');
        closeBtns.forEach(btn => {
            btn.addEventListener('click', () => closeModal());
        });
        
        // Maximize button
        const maximizeBtn = dialog.querySelector('#home-maximize-btn');
        maximizeBtn.addEventListener('click', () => toggleMaximize(dialog));
        
        // Minimize button
        const minimizeBtn = dialog.querySelector('#home-minimize-btn');
        minimizeBtn.addEventListener('click', () => {
            overlay.style.display = 'none';
            modalState.isOpen = false;
        });
        
        // Share button
        const shareBtn = dialog.querySelector('#home-share-btn');
        shareBtn.addEventListener('click', () => {
            if (navigator.share) {
                navigator.share({
                    title: dialog.querySelector('.home-modal-title').textContent,
                    url: window.location.href
                });
            } else {
                navigator.clipboard.writeText(window.location.href).then(() => {
                    showNotification('Link copied to clipboard!');
                });
            }
        });
        
        // Print button
        const printBtn = dialog.querySelector('#home-print-btn');
        printBtn.addEventListener('click', () => {
            const content = dialog.querySelector('.home-article-content').innerHTML;
            const title = dialog.querySelector('.home-modal-title').textContent;
            
            const printWindow = window.open('', '_blank');
            printWindow.document.write(`
                <html>
                    <head>
                        <title>${title}</title>
                        <style>
                            body { font-family: Arial, sans-serif; margin: 20px; line-height: 1.6; }
                            img { max-width: 100%; height: auto; }
                            h1, h2, h3 { color: #333; }
                        </style>
                    </head>
                    <body>
                        <h1>${title}</h1>
                        ${content}
                    </body>
                </html>
            `);
            printWindow.document.close();
            printWindow.print();
        });
        
        // Overlay click to close
        overlay.addEventListener('click', (e) => {
            if (e.target === overlay) {
                closeModal();
            }
        });
        
        // Keyboard shortcuts
        document.addEventListener('keydown', handleKeyboard);
    }

    /**
     * Handle keyboard shortcuts
     */
    function handleKeyboard(e) {
        if (!modalState.isOpen) return;
        
        switch(e.key) {
            case 'Escape':
                closeModal();
                break;
            case 'F11':
                e.preventDefault();
                const dialog = document.querySelector('#home-modal-dialog');
                if (dialog) toggleMaximize(dialog);
                break;
        }
    }

    /**
     * Show notification
     */
    function showNotification(message) {
        const notification = document.createElement('div');
        notification.className = 'home-modal-notification';
        notification.textContent = message;
        notification.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            background: #28a745;
            color: white;
            padding: 12px 20px;
            border-radius: 6px;
            z-index: ${config.zIndex + 100};
            font-size: 14px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        `;
        
        document.body.appendChild(notification);
        
        setTimeout(() => {
            notification.remove();
        }, 3000);
    }

    /**
     * Get news data from various sources
     */
    function getNewsData(newsId) {
        // Try embedded JSON data first
        const jsonElement = document.querySelector(`#news-data-${newsId}`);
        if (jsonElement) {
            try {
                return JSON.parse(jsonElement.textContent);
            } catch (e) {
                console.log('Failed to parse embedded JSON:', e);
            }
        }
        
        // Try extracting from existing modal
        const existingModal = document.querySelector(`#modal-${newsId}`);
        if (existingModal) {
            const title = existingModal.querySelector('.modal-title')?.textContent || 'News Article';
            const content = existingModal.querySelector('.modal-content-text')?.innerHTML || '<p>Content not available</p>';
            const date = existingModal.querySelector('.modal-date')?.textContent || new Date().toISOString();
            const imageUrl = existingModal.querySelector('.modal-image img')?.src || null;
            
            return { id: newsId, title, content, createTime: date, imageUrl };
        }
        
        // Fallback data
        return {
            id: newsId,
            title: 'News Article',
            content: '<p>Unable to load article content. Please try again later.</p>',
            createTime: new Date().toISOString()
        };
    }

    /**
     * Close modal
     */
    function closeModal() {
        const overlay = document.querySelector(`#${config.modalId}`);
        if (overlay) {
            // Get stored scroll position
            const scrollTop = parseInt(overlay.getAttribute('data-scroll-top') || '0');
            const scrollLeft = parseInt(overlay.getAttribute('data-scroll-left') || '0');
            
            overlay.style.display = 'none';
            document.body.classList.remove('home-modal-open', 'home-modal-dragging', 'home-modal-resizing');
            document.removeEventListener('keydown', handleKeyboard);
            
            // Restore body styles and scroll position
            document.body.style.overflow = '';
            document.body.style.position = '';
            document.body.style.top = '';
            document.body.style.left = '';
            document.body.style.width = '';
            
            // Restore scroll position
            window.scrollTo(scrollLeft, scrollTop);
            
            setTimeout(() => {
                if (overlay && overlay.parentNode) {
                    overlay.parentNode.removeChild(overlay);
                }
            }, 300);
        }
        
        modalState.isOpen = false;
        modalState.isMaximized = false;
        modalState.isDragging = false;
        modalState.isResizing = false;
    }

    /**
     * Main function to show home news modal
     */
    function showHomeNewsModal(newsId) {
        console.log('🏠 Opening home news modal for ID:', newsId);
        
        // Close any existing modal
        closeModal();
        
        // Get news data
        const newsData = getNewsData(newsId);
        
        // Create modal HTML
        const modalHTML = createModalHTML(newsData);
        
        // Add to DOM
        document.body.insertAdjacentHTML('beforeend', modalHTML);
        
        // Get elements
        const overlay = document.querySelector(`#${config.modalId}`);
        const dialog = document.querySelector('#home-modal-dialog');
        
        // Initialize functionality
        initializeModal(dialog);
        initializeDrag(dialog);
        initializeResize(dialog);
        initializeEventListeners(dialog, overlay);
        
        // Store current scroll position
        const currentScrollTop = window.pageYOffset || document.documentElement.scrollTop;
        const currentScrollLeft = window.pageXOffset || document.documentElement.scrollLeft;
        
        // Show modal
        overlay.style.display = 'flex';
        
        // Prevent body scroll while maintaining position
        document.body.style.overflow = 'hidden';
        document.body.style.position = 'fixed';
        document.body.style.top = `-${currentScrollTop}px`;
        document.body.style.left = `-${currentScrollLeft}px`;
        document.body.style.width = '100%';
        
        // Store scroll position on overlay for restoration
        overlay.setAttribute('data-scroll-top', currentScrollTop);
        overlay.setAttribute('data-scroll-left', currentScrollLeft);
        
        document.body.classList.add('home-modal-open');
        modalState.isOpen = true;
        
        // Animate in
        setTimeout(() => {
            overlay.classList.add('home-modal-show');
            dialog.classList.add('home-modal-show');
        }, 10);
        
        console.log('✅ Home news modal opened successfully');
    }

    /**
     * Test function
     */
    function testHomeModal() {
        const testData = {
            id: 'test-home',
            title: 'Home Page Modal Test',
            content: `
                <p>This is a test of the independent home page news modal system.</p>
                <h3>Features</h3>
                <ul>
                    <li>Completely isolated from other modal systems</li>
                    <li>Draggable and resizable</li>
                    <li>Maximize and restore functionality</li>
                    <li>Keyboard shortcuts</li>
                    <li>Print and share capabilities</li>
                </ul>
                <p>This modal is specifically designed for the home page and will not interfere with any existing news menu modal systems.</p>
            `,
            createTime: new Date().toISOString(),
            imageUrl: null
        };
        
        // Create temporary data element
        const dataElement = document.createElement('script');
        dataElement.type = 'application/json';
        dataElement.id = 'news-data-test-home';
        dataElement.textContent = JSON.stringify(testData);
        document.body.appendChild(dataElement);
        
        showHomeNewsModal('test-home');
    }

    // Export functions to global scope with unique names
    window.showHomeNewsModal = showHomeNewsModal;
    window.testHomeModal = testHomeModal;
    window.closeHomeModal = closeModal;

    console.log('🏠 Home Page Independent News Modal System loaded');

})();