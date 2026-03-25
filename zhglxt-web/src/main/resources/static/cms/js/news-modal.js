/**
 * Optimized News Modal System
 * Reliable modal with drag, maximize/restore, and proper event handling
 */

class OptimizedNewsModal {
    constructor() {
        this.modal = null;
        this.overlay = null;
        this.isDragging = false;
        this.isMaximized = false;
        this.dragOffset = { x: 0, y: 0 };
        this.originalSize = { width: 900, height: 700 };
        this.originalPosition = null;
        this.isInitialized = false;
        this.currentNewsId = null;
        
        this.init();
    }

    init() {
        if (this.isInitialized) return;
        
        this.createModal();
        this.bindEvents();
        this.setupKeyboardShortcuts();
        this.isInitialized = true;
        
        console.log('✅ Optimized News Modal initialized');
    }

    createModal() {
        // Remove any existing modal
        const existing = document.getElementById('optimized-news-modal');
        if (existing) existing.remove();

        // Create overlay
        this.overlay = document.createElement('div');
        this.overlay.id = 'optimized-news-modal';
        this.overlay.className = 'optimized-news-modal-overlay';
        this.overlay.style.cssText = `
            position: fixed;
            top: 0;
            left: 0;
            width: 100vw;
            height: 100vh;
            background: rgba(255, 255, 255, 0.95);
            z-index: 10000;
            display: none;
            backdrop-filter: blur(3px);
        `;

        // Create modal
        this.modal = document.createElement('div');
        this.modal.className = 'optimized-news-modal';
        this.modal.style.cssText = `
            position: fixed;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            width: ${this.originalSize.width}px;
            height: ${this.originalSize.height}px;
            background: white;
            border-radius: 12px;
            box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
            z-index: 10001;
            display: flex;
            flex-direction: column;
            overflow: hidden;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        `;

        this.modal.innerHTML = `
            <div class="optimized-modal-header" style="
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                color: white;
                padding: 16px 20px;
                display: flex;
                justify-content: space-between;
                align-items: center;
                cursor: move;
                user-select: none;
                border-radius: 12px 12px 0 0;
            ">
                <div class="modal-title-section" style="flex: 1; min-width: 0;">
                    <div class="modal-category" style="
                        font-size: 12px;
                        opacity: 0.9;
                        margin-bottom: 4px;
                        display: flex;
                        align-items: center;
                        gap: 6px;
                    ">
                        <i class="fas fa-newspaper"></i>
                        <span>News Article</span>
                    </div>
                    <h3 class="optimized-modal-title" style="
                        margin: 0;
                        font-size: 18px;
                        font-weight: 600;
                        white-space: nowrap;
                        overflow: hidden;
                        text-overflow: ellipsis;
                    ">新闻详情</h3>
                </div>
                <div class="optimized-modal-controls" style="
                    display: flex;
                    gap: 8px;
                    flex-shrink: 0;
                ">
                    <button class="modal-btn minimize-btn" style="
                        background: rgba(255, 255, 255, 0.2);
                        border: none;
                        color: white;
                        width: 32px;
                        height: 32px;
                        border-radius: 6px;
                        cursor: pointer;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        transition: background 0.2s;
                    " title="Minimize">
                        <i class="fas fa-window-minimize" style="font-size: 12px;"></i>
                    </button>
                    <button class="modal-btn maximize-btn" style="
                        background: rgba(255, 255, 255, 0.2);
                        border: none;
                        color: white;
                        width: 32px;
                        height: 32px;
                        border-radius: 6px;
                        cursor: pointer;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        transition: background 0.2s;
                    " title="Maximize">
                        <i class="fas fa-window-maximize" style="font-size: 12px;"></i>
                    </button>
                    <button class="modal-btn close-btn" style="
                        background: #dc3545;
                        border: none;
                        color: white;
                        width: 32px;
                        height: 32px;
                        border-radius: 6px;
                        cursor: pointer;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        transition: background 0.2s;
                    " title="Close">
                        <i class="fas fa-times" style="font-size: 14px;"></i>
                    </button>
                </div>
            </div>
            <div class="optimized-modal-content" style="
                flex: 1;
                padding: 24px;
                overflow-y: auto;
                background: #fafafa;
            ">
                <div id="optimized-modal-body" style="
                    background: white;
                    padding: 24px;
                    border-radius: 8px;
                    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
                    line-height: 1.6;
                    color: #333;
                "></div>
            </div>
            <div class="optimized-modal-footer" style="
                padding: 16px 20px;
                background: white;
                border-top: 1px solid #e9ecef;
                display: flex;
                justify-content: space-between;
                align-items: center;
                border-radius: 0 0 12px 12px;
            ">
                <div class="modal-meta" style="
                    color: #6c757d;
                    font-size: 14px;
                ">
                    <span class="modal-date"></span>
                </div>
                <div class="modal-actions" style="display: flex; gap: 12px;">
                    <button class="action-btn share-btn" style="
                        background: #28a745;
                        color: white;
                        border: none;
                        padding: 8px 16px;
                        border-radius: 6px;
                        cursor: pointer;
                        font-size: 14px;
                        display: flex;
                        align-items: center;
                        gap: 6px;
                        transition: background 0.2s;
                    ">
                        <i class="fas fa-share-alt"></i>
                        Share
                    </button>
                    <button class="action-btn print-btn" style="
                        background: #17a2b8;
                        color: white;
                        border: none;
                        padding: 8px 16px;
                        border-radius: 6px;
                        cursor: pointer;
                        font-size: 14px;
                        display: flex;
                        align-items: center;
                        gap: 6px;
                        transition: background 0.2s;
                    ">
                        <i class="fas fa-print"></i>
                        Print
                    </button>
                    <button class="action-btn footer-close-btn" style="
                        background: #6c757d;
                        color: white;
                        border: none;
                        padding: 8px 16px;
                        border-radius: 6px;
                        cursor: pointer;
                        font-size: 14px;
                        display: flex;
                        align-items: center;
                        gap: 6px;
                        transition: background 0.2s;
                    ">
                        <i class="fas fa-times"></i>
                        Close
                    </button>
                </div>
            </div>
        `;

        this.overlay.appendChild(this.modal);
        document.body.appendChild(this.overlay);
    }

    bindEvents() {
        const header = this.modal.querySelector('.optimized-modal-header');
        const minimizeBtn = this.modal.querySelector('.minimize-btn');
        const maximizeBtn = this.modal.querySelector('.maximize-btn');
        const closeBtn = this.modal.querySelector('.close-btn');
        const footerCloseBtn = this.modal.querySelector('.footer-close-btn');
        const shareBtn = this.modal.querySelector('.share-btn');
        const printBtn = this.modal.querySelector('.print-btn');

        // Drag functionality
        header.addEventListener('mousedown', this.startDrag.bind(this));
        
        // Double-click header to maximize
        header.addEventListener('dblclick', this.toggleMaximize.bind(this));

        // Button events
        minimizeBtn.addEventListener('click', this.minimize.bind(this));
        maximizeBtn.addEventListener('click', this.toggleMaximize.bind(this));
        closeBtn.addEventListener('click', this.close.bind(this));
        footerCloseBtn.addEventListener('click', this.close.bind(this));
        shareBtn.addEventListener('click', this.shareNews.bind(this));
        printBtn.addEventListener('click', this.printNews.bind(this));

        // Close on overlay click
        this.overlay.addEventListener('click', (e) => {
            if (e.target === this.overlay) {
                this.close();
            }
        });

        // Button hover effects
        this.addHoverEffects();
    }

    addHoverEffects() {
        const buttons = this.modal.querySelectorAll('button');
        buttons.forEach(btn => {
            btn.addEventListener('mouseenter', () => {
                if (btn.classList.contains('close-btn')) {
                    btn.style.background = '#c82333';
                } else if (btn.classList.contains('share-btn')) {
                    btn.style.background = '#218838';
                } else if (btn.classList.contains('print-btn')) {
                    btn.style.background = '#138496';
                } else if (btn.classList.contains('footer-close-btn')) {
                    btn.style.background = '#5a6268';
                } else {
                    btn.style.background = 'rgba(255, 255, 255, 0.3)';
                }
            });
            
            btn.addEventListener('mouseleave', () => {
                if (btn.classList.contains('close-btn')) {
                    btn.style.background = '#dc3545';
                } else if (btn.classList.contains('share-btn')) {
                    btn.style.background = '#28a745';
                } else if (btn.classList.contains('print-btn')) {
                    btn.style.background = '#17a2b8';
                } else if (btn.classList.contains('footer-close-btn')) {
                    btn.style.background = '#6c757d';
                } else {
                    btn.style.background = 'rgba(255, 255, 255, 0.2)';
                }
            });
        });
    }

    setupKeyboardShortcuts() {
        document.addEventListener('keydown', (e) => {
            if (!this.isOpen()) return;
            
            switch(e.key) {
                case 'Escape':
                    this.close();
                    break;
                case 'F11':
                    e.preventDefault();
                    this.toggleMaximize();
                    break;
                case 'F5':
                    e.preventDefault();
                    this.minimize();
                    break;
            }
        });
    }

    startDrag(e) {
        if (this.isMaximized) return;
        
        this.isDragging = true;
        const rect = this.modal.getBoundingClientRect();
        this.dragOffset.x = e.clientX - rect.left;
        this.dragOffset.y = e.clientY - rect.top;
        
        document.addEventListener('mousemove', this.handleDrag.bind(this));
        document.addEventListener('mouseup', this.handleDragEnd.bind(this));
        
        document.body.style.userSelect = 'none';
        e.preventDefault();
    }

    handleDrag(e) {
        if (!this.isDragging || this.isMaximized) return;
        
        const x = Math.max(0, Math.min(window.innerWidth - this.modal.offsetWidth, e.clientX - this.dragOffset.x));
        const y = Math.max(0, Math.min(window.innerHeight - this.modal.offsetHeight, e.clientY - this.dragOffset.y));
        
        this.modal.style.transform = 'none';
        this.modal.style.left = x + 'px';
        this.modal.style.top = y + 'px';
    }

    handleDragEnd() {
        this.isDragging = false;
        document.removeEventListener('mousemove', this.handleDrag.bind(this));
        document.removeEventListener('mouseup', this.handleDragEnd.bind(this));
        document.body.style.userSelect = '';
    }

    toggleMaximize() {
        const maximizeBtn = this.modal.querySelector('.maximize-btn');
        const icon = maximizeBtn.querySelector('i');
        
        if (this.isMaximized) {
            // Restore
            this.modal.style.width = this.originalSize.width + 'px';
            this.modal.style.height = this.originalSize.height + 'px';
            
            if (this.originalPosition) {
                this.modal.style.left = this.originalPosition.left + 'px';
                this.modal.style.top = this.originalPosition.top + 'px';
                this.modal.style.transform = 'none';
            } else {
                this.centerModal();
            }
            
            icon.className = 'fas fa-window-maximize';
            maximizeBtn.title = 'Maximize';
            this.isMaximized = false;
        } else {
            // Store current position
            this.originalPosition = {
                left: this.modal.offsetLeft,
                top: this.modal.offsetTop
            };
            
            // Maximize
            this.modal.style.width = (window.innerWidth - 40) + 'px';
            this.modal.style.height = (window.innerHeight - 40) + 'px';
            this.modal.style.left = '20px';
            this.modal.style.top = '20px';
            this.modal.style.transform = 'none';
            
            icon.className = 'fas fa-window-restore';
            maximizeBtn.title = 'Restore';
            this.isMaximized = true;
        }
    }

    minimize() {
        this.overlay.style.display = 'none';
        // Could add to taskbar or notification area in the future
    }

    centerModal() {
        this.modal.style.top = '50%';
        this.modal.style.left = '50%';
        this.modal.style.transform = 'translate(-50%, -50%)';
    }

    shareNews() {
        const title = this.modal.querySelector('.optimized-modal-title').textContent;
        
        if (navigator.share) {
            navigator.share({
                title: title,
                url: window.location.href
            }).catch(console.error);
        } else {
            // Fallback to clipboard
            navigator.clipboard.writeText(window.location.href).then(() => {
                this.showNotification('Link copied to clipboard!');
            }).catch(() => {
                // Final fallback
                prompt('Copy this link:', window.location.href);
            });
        }
    }

    printNews() {
        const title = this.modal.querySelector('.optimized-modal-title').textContent;
        const content = this.modal.querySelector('#optimized-modal-body').innerHTML;
        
        const printWindow = window.open('', '_blank');
        printWindow.document.write(`
            <!DOCTYPE html>
            <html>
            <head>
                <title>${title}</title>
                <style>
                    body { 
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Arial, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        max-width: 800px;
                        margin: 0 auto;
                        padding: 20px;
                    }
                    img { max-width: 100%; height: auto; }
                    h1, h2, h3 { color: #2c3e50; }
                    @media print {
                        body { margin: 0; padding: 15px; }
                    }
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
    }

    showNotification(message) {
        const notification = document.createElement('div');
        notification.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            background: #28a745;
            color: white;
            padding: 12px 20px;
            border-radius: 6px;
            z-index: 10002;
            font-size: 14px;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
            animation: slideInRight 0.3s ease-out;
        `;
        notification.textContent = message;
        
        document.body.appendChild(notification);
        
        setTimeout(() => {
            notification.style.animation = 'slideOutRight 0.3s ease-in forwards';
            setTimeout(() => notification.remove(), 300);
        }, 3000);
    }

    getNewsData(newsId) {
        // Try to get data from existing Bootstrap modal
        const existingModal = document.querySelector(`#modal-${newsId}`);
        if (existingModal) {
            const title = existingModal.querySelector('.modal-title')?.textContent || 'News Article';
            const content = existingModal.querySelector('.modal-content-text')?.innerHTML || 
                           existingModal.querySelector('.modal-body')?.innerHTML || 
                           '<p>Content not available</p>';
            const date = existingModal.querySelector('.modal-date')?.textContent || 
                        new Date().toLocaleDateString();
            const imageUrl = existingModal.querySelector('.modal-image img')?.src || null;
            
            return { id: newsId, title, content, createTime: date, imageUrl };
        }
        
        // Try embedded JSON data
        const jsonElement = document.querySelector(`#news-data-${newsId}`);
        if (jsonElement) {
            try {
                return JSON.parse(jsonElement.textContent);
            } catch (e) {
                console.warn('Failed to parse embedded JSON:', e);
            }
        }
        
        // Fallback data
        return {
            id: newsId,
            title: 'News Article',
            content: '<p>Unable to load news content. Please try again later.</p>',
            createTime: new Date().toLocaleDateString(),
            imageUrl: null
        };
    }

    open(newsId, title, content) {
        this.currentNewsId = newsId;
        
        // Get news data if not provided
        let newsData;
        if (typeof newsId === 'object') {
            newsData = newsId; // newsId is actually the data object
        } else if (title && content) {
            newsData = { id: newsId, title, content, createTime: new Date().toLocaleDateString() };
        } else {
            newsData = this.getNewsData(newsId);
        }
        
        // Update modal content
        this.modal.querySelector('.optimized-modal-title').textContent = newsData.title;
        this.modal.querySelector('.modal-date').textContent = newsData.createTime;
        
        let bodyContent = '';
        if (newsData.imageUrl) {
            bodyContent += `<img src="${newsData.imageUrl}" alt="News Image" style="width: 100%; max-height: 300px; object-fit: cover; border-radius: 6px; margin-bottom: 20px;">`;
        }
        bodyContent += newsData.content;
        
        this.modal.querySelector('#optimized-modal-body').innerHTML = bodyContent;
        
        // Reset modal state
        this.modal.classList.remove('maximized');
        this.modal.style.width = this.originalSize.width + 'px';
        this.modal.style.height = this.originalSize.height + 'px';
        this.centerModal();
        this.isMaximized = false;
        this.originalPosition = null;
        
        const maximizeBtn = this.modal.querySelector('.maximize-btn');
        const icon = maximizeBtn.querySelector('i');
        icon.className = 'fas fa-window-maximize';
        maximizeBtn.title = 'Maximize';
        
        // Show modal
        this.overlay.style.display = 'block';
        document.body.style.overflow = 'hidden';
        
        // Animate in
        setTimeout(() => {
            this.modal.style.opacity = '1';
            this.modal.style.transform = this.modal.style.transform + ' scale(1)';
        }, 10);
        
        console.log('✅ Optimized modal opened for:', newsData.title);
    }

    close() {
        this.overlay.style.display = 'none';
        document.body.style.overflow = '';
        this.currentNewsId = null;
        
        console.log('✅ Optimized modal closed');
    }

    isOpen() {
        return this.overlay && this.overlay.style.display !== 'none';
    }
}

// Global instance
window.optimizedNewsModal = new OptimizedNewsModal();

// Global functions for compatibility
window.openNewsModal = function(title, content) {
    window.optimizedNewsModal.open(null, title, content);
};

window.showOptimizedModal = function(newsId) {
    window.optimizedNewsModal.open(newsId);
};

// Enhanced modal function that works with various data sources
window.showNewsModal = function(newsId) {
    window.optimizedNewsModal.open(newsId);
};

console.log('✅ Optimized News Modal System loaded');