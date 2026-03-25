/**
 * Super Independent Modal
 * Ultra-aggressive approach to ensure modal displays on top of everything
 */

(function() {
    'use strict';

    console.log('🚀 Loading Super Independent Modal...');

    let currentModal = null;
    let isMaximized = false;

    /**
     * Create modal using DOM methods (not innerHTML)
     */
    function createSuperModal(newsData) {
        console.log('🚀 Creating super independent modal...');

        // Remove any existing modal
        if (currentModal) {
            currentModal.remove();
            currentModal = null;
        }

        // Create overlay
        const overlay = document.createElement('div');
        overlay.id = 'super-independent-modal';
        
        // Set styles directly on the element
        const overlayStyles = {
            position: 'fixed',
            top: '0',
            left: '0',
            width: '100vw',
            height: '100vh',
            backgroundColor: 'rgba(0, 0, 0, 0.8)',
            zIndex: '2147483647', // Maximum z-index
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontFamily: 'Arial, sans-serif',
            pointerEvents: 'auto',
            visibility: 'visible',
            opacity: '1'
        };

        Object.assign(overlay.style, overlayStyles);

        // Create modal window
        const modalWindow = document.createElement('div');
        modalWindow.id = 'super-modal-window';
        
        const windowStyles = {
            backgroundColor: 'white',
            borderRadius: '8px',
            boxShadow: '0 20px 60px rgba(0,0,0,0.5)',
            display: 'flex',
            flexDirection: 'column',
            width: '800px',
            height: '600px',
            maxWidth: '95vw',
            maxHeight: '95vh',
            position: 'relative',
            zIndex: '2147483647'
        };

        Object.assign(modalWindow.style, windowStyles);

        // Create header
        const header = document.createElement('div');
        const headerStyles = {
            background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
            color: 'white',
            padding: '15px 20px',
            borderRadius: '8px 8px 0 0',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            flexShrink: '0'
        };
        Object.assign(header.style, headerStyles);

        // Create title
        const title = document.createElement('h3');
        title.textContent = newsData.title || 'News Article';
        title.style.margin = '0';
        title.style.fontSize = '18px';
        title.style.fontWeight = '600';

        // Create controls container
        const controls = document.createElement('div');
        controls.style.display = 'flex';
        controls.style.gap = '10px';

        // Create maximize button
        const maximizeBtn = document.createElement('button');
        maximizeBtn.innerHTML = '<i class="fas fa-window-maximize"></i>';
        maximizeBtn.title = 'Maximize/Restore';
        const maxBtnStyles = {
            background: 'rgba(255,255,255,0.2)',
            border: 'none',
            color: 'white',
            width: '30px',
            height: '30px',
            borderRadius: '4px',
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontSize: '14px'
        };
        Object.assign(maximizeBtn.style, maxBtnStyles);

        // Create close button
        const closeBtn = document.createElement('button');
        closeBtn.innerHTML = '×';
        closeBtn.title = 'Close';
        const closeBtnStyles = {
            background: '#dc3545',
            border: 'none',
            color: 'white',
            width: '30px',
            height: '30px',
            borderRadius: '4px',
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontSize: '16px'
        };
        Object.assign(closeBtn.style, closeBtnStyles);

        // Create body
        const body = document.createElement('div');
        const bodyStyles = {
            flex: '1',
            padding: '20px',
            overflowY: 'auto',
            backgroundColor: '#fafafa'
        };
        Object.assign(body.style, bodyStyles);

        // Create content container
        const contentContainer = document.createElement('div');
        const contentStyles = {
            backgroundColor: 'white',
            padding: '20px',
            borderRadius: '6px',
            boxShadow: '0 2px 8px rgba(0,0,0,0.1)'
        };
        Object.assign(contentContainer.style, contentStyles);

        // Add image if exists
        if (newsData.imageUrl) {
            const img = document.createElement('img');
            img.src = newsData.imageUrl;
            img.alt = 'News Image';
            const imgStyles = {
                width: '100%',
                maxHeight: '300px',
                objectFit: 'cover',
                borderRadius: '6px',
                marginBottom: '20px'
            };
            Object.assign(img.style, imgStyles);
            contentContainer.appendChild(img);
        }

        // Add content
        const contentDiv = document.createElement('div');
        contentDiv.innerHTML = newsData.content || '<p>Loading news content...</p>';
        const contentDivStyles = {
            color: '#333',
            lineHeight: '1.6',
            fontSize: '16px'
        };
        Object.assign(contentDiv.style, contentDivStyles);
        contentContainer.appendChild(contentDiv);

        // Create footer
        const footer = document.createElement('div');
        const footerStyles = {
            padding: '15px 20px',
            borderTop: '1px solid #eee',
            backgroundColor: 'white',
            borderRadius: '0 0 8px 8px',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            flexShrink: '0'
        };
        Object.assign(footer.style, footerStyles);

        // Add date
        const dateSpan = document.createElement('span');
        dateSpan.textContent = formatDate(newsData.createTime);
        dateSpan.style.color = '#666';
        dateSpan.style.fontSize = '14px';

        // Add footer close button
        const footerCloseBtn = document.createElement('button');
        footerCloseBtn.textContent = 'Close';
        const footerCloseBtnStyles = {
            background: '#6c757d',
            color: 'white',
            border: 'none',
            padding: '8px 16px',
            borderRadius: '4px',
            cursor: 'pointer',
            fontSize: '14px'
        };
        Object.assign(footerCloseBtn.style, footerCloseBtnStyles);

        // Assemble the modal
        controls.appendChild(maximizeBtn);
        controls.appendChild(closeBtn);
        header.appendChild(title);
        header.appendChild(controls);
        body.appendChild(contentContainer);
        footer.appendChild(dateSpan);
        footer.appendChild(footerCloseBtn);
        modalWindow.appendChild(header);
        modalWindow.appendChild(body);
        modalWindow.appendChild(footer);
        overlay.appendChild(modalWindow);

        // Add event listeners
        maximizeBtn.addEventListener('click', () => toggleMaximize(modalWindow, maximizeBtn));
        closeBtn.addEventListener('click', closeSuperModal);
        footerCloseBtn.addEventListener('click', closeSuperModal);
        
        // Close on overlay click
        overlay.addEventListener('click', function(e) {
            if (e.target === overlay) {
                closeSuperModal();
            }
        });

        // Add to body with maximum priority
        document.body.appendChild(overlay);
        currentModal = overlay;

        // Prevent body scroll
        document.body.style.overflow = 'hidden';

        // Force styles to ensure visibility
        setTimeout(() => {
            overlay.style.zIndex = '2147483647';
            overlay.style.position = 'fixed';
            overlay.style.display = 'flex';
            overlay.style.visibility = 'visible';
            overlay.style.opacity = '1';
            
            console.log('🚀 Super modal forced to be visible');
            console.log('🚀 Computed z-index:', window.getComputedStyle(overlay).zIndex);
            console.log('🚀 Computed position:', window.getComputedStyle(overlay).position);
            console.log('🚀 Computed display:', window.getComputedStyle(overlay).display);
        }, 10);

        console.log('🚀 Super independent modal created successfully');
        return overlay;
    }

    /**
     * Toggle maximize/restore
     */
    function toggleMaximize(modalWindow, maximizeBtn) {
        const icon = maximizeBtn.querySelector('i');
        
        if (isMaximized) {
            // Restore
            modalWindow.style.width = '800px';
            modalWindow.style.height = '600px';
            modalWindow.style.maxWidth = '95vw';
            modalWindow.style.maxHeight = '95vh';
            
            if (icon) {
                icon.className = 'fas fa-window-maximize';
            }
            maximizeBtn.title = 'Maximize';
            isMaximized = false;
        } else {
            // Maximize
            modalWindow.style.width = '98vw';
            modalWindow.style.height = '98vh';
            modalWindow.style.maxWidth = 'none';
            modalWindow.style.maxHeight = 'none';
            
            if (icon) {
                icon.className = 'fas fa-window-restore';
            }
            maximizeBtn.title = 'Restore';
            isMaximized = true;
        }
        
        console.log('🚀 Modal', isMaximized ? 'maximized' : 'restored');
    }

    /**
     * Close super modal
     */
    function closeSuperModal() {
        if (currentModal) {
            currentModal.remove();
            currentModal = null;
            isMaximized = false;
            document.body.style.overflow = '';
            console.log('🚀 Super modal closed');
        }
    }

    /**
     * Format date
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
     * Get news data
     */
    function getNewsData(newsId) {
        const existingModal = document.getElementById('modal-' + newsId);
        if (existingModal) {
            const title = existingModal.querySelector('.modal-title')?.textContent || 'News Article';
            const content = existingModal.querySelector('.modal-content-text')?.innerHTML || 
                           existingModal.querySelector('.modal-body')?.innerHTML || 
                           '<p>Content not available</p>';
            const date = existingModal.querySelector('.modal-date')?.textContent || new Date().toISOString();
            const imageUrl = existingModal.querySelector('.modal-image img')?.src || null;
            
            return { id: newsId, title, content, createTime: date, imageUrl };
        }
        
        return {
            id: newsId,
            title: 'News Article',
            content: '<p>Unable to load news content. Please try again.</p>',
            createTime: new Date().toISOString(),
            imageUrl: null
        };
    }

    /**
     * Show super independent modal
     */
    function showSuperModal(newsId) {
        console.log('🚀 Showing super independent modal for news ID:', newsId);
        
        const newsData = getNewsData(newsId);
        createSuperModal(newsData);
    }

    /**
     * Set up event listeners
     */
    function setupSuperEventListeners() {
        console.log('🚀 Setting up super modal event listeners...');

        document.addEventListener('click', function(e) {
            const button = e.target.closest('.news-read-btn, .news-detail-btn, .btn-read-more');
            
            if (button) {
                e.preventDefault();
                e.stopPropagation();

                const dataTarget = button.getAttribute('data-target');
                if (dataTarget) {
                    const newsId = dataTarget.replace('#modal-', '');
                    showSuperModal(newsId);
                } else {
                    console.error('🚀 No data-target found on button');
                }

                return false;
            }
        });

        // Close on Escape key
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape' && currentModal) {
                closeSuperModal();
            }
        });

        console.log('🚀 Super modal event listeners set up');
    }

    /**
     * Initialize
     */
    function init() {
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', setupSuperEventListeners);
        } else {
            setupSuperEventListeners();
        }
    }

    // Export for testing
    window.showSuperModal = showSuperModal;
    window.testSuperModal = function() {
        showSuperModal('test');
    };

    // Initialize
    init();

    console.log('✅ Super Independent Modal loaded and ready');

})();