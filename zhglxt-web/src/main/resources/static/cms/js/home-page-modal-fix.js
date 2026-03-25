/**
 * HOME PAGE MODAL FIX
 * Direct solution for home page news buttons
 */

(function() {
    'use strict';

    console.log('🏠 HOME PAGE MODAL FIX: Loading...');

    // Modal state
    let isMaximized = false;
    let originalSize = { width: '90%', maxWidth: '800px', maxHeight: '85vh' };

    // Create enhanced modal function
    function createSimpleModal(itemId, title, content, type = 'Content') {
        console.log('🏠 Creating enhanced modal for item ID:', itemId, 'Type:', type);

        // Remove existing modal
        const existing = document.getElementById('home-simple-modal');
        if (existing) existing.remove();

        // Reset state
        isMaximized = false;

        // Create modal HTML with maximize/restore functionality
        const modalHTML = `
            <div id="home-simple-modal" style="
                position: fixed !important;
                top: 0 !important;
                left: 0 !important;
                width: 100vw !important;
                height: 100vh !important;
                background: rgba(0, 0, 0, 0.8) !important;
                z-index: 999999 !important;
                display: flex !important;
                align-items: center !important;
                justify-content: center !important;
                font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Arial, sans-serif !important;
                backdrop-filter: blur(3px) !important;
            ">
                <div id="home-modal-dialog" style="
                    background: white !important;
                    border-radius: 16px !important;
                    box-shadow: 0 25px 80px rgba(0, 0, 0, 0.4) !important;
                    width: 90% !important;
                    max-width: 800px !important;
                    max-height: 85vh !important;
                    display: flex !important;
                    flex-direction: column !important;
                    overflow: hidden !important;
                    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
                ">
                    <div id="home-modal-header" style="
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
                        color: white !important;
                        padding: 20px 24px !important;
                        display: flex !important;
                        justify-content: space-between !important;
                        align-items: center !important;
                        border-radius: 16px 16px 0 0 !important;
                        cursor: move !important;
                        user-select: none !important;
                    ">
                        <div style="flex: 1 !important; min-width: 0 !important;">
                            <div style="
                                font-size: 12px !important;
                                opacity: 0.9 !important;
                                margin-bottom: 6px !important;
                                display: flex !important;
                                align-items: center !important;
                                gap: 6px !important;
                            ">
                                <i class="fas ${type === 'Exhibition' ? 'fa-building' : 'fa-newspaper'}"></i>
                                <span>${type}</span>
                            </div>
                            <h3 id="home-modal-title" style="
                                margin: 0 !important;
                                font-size: 20px !important;
                                font-weight: 600 !important;
                                line-height: 1.3 !important;
                                white-space: nowrap !important;
                                overflow: hidden !important;
                                text-overflow: ellipsis !important;
                            ">
                                ${title || 'News Article'}
                            </h3>
                        </div>
                        <div style="
                            display: flex !important;
                            gap: 8px !important;
                            flex-shrink: 0 !important;
                            margin-left: 20px !important;
                        ">
                            <button id="home-maximize-btn" onclick="toggleHomeMaximize()" style="
                                background: rgba(255, 255, 255, 0.2) !important;
                                border: none !important;
                                color: white !important;
                                width: 32px !important;
                                height: 32px !important;
                                border-radius: 6px !important;
                                cursor: pointer !important;
                                font-size: 14px !important;
                                display: flex !important;
                                align-items: center !important;
                                justify-content: center !important;
                                transition: all 0.2s ease !important;
                            " title="Maximize">
                                <i class="fas fa-expand"></i>
                            </button>
                            <button onclick="closeHomeModal()" style="
                                background: #dc3545 !important;
                                color: white !important;
                                border: none !important;
                                width: 32px !important;
                                height: 32px !important;
                                border-radius: 6px !important;
                                cursor: pointer !important;
                                font-size: 14px !important;
                                display: flex !important;
                                align-items: center !important;
                                justify-content: center !important;
                                transition: all 0.2s ease !important;
                            " title="Close">
                                <i class="fas fa-times"></i>
                            </button>
                        </div>
                    </div>
                    <div id="home-modal-content" style="
                        padding: 30px !important;
                        overflow-y: auto !important;
                        overflow-x: hidden !important;
                        flex: 1 !important;
                        line-height: 1.7 !important;
                        color: #333 !important;
                        background: #fafafa !important;
                        word-wrap: break-word !important;
                        word-break: break-word !important;
                    ">
                        <div style="
                            background: white !important;
                            padding: 30px !important;
                            border-radius: 12px !important;
                            box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08) !important;
                            word-wrap: break-word !important;
                            overflow-wrap: break-word !important;
                        ">
                            ${content || '<p>Loading news content...</p>'}
                        </div>
                    </div>

                </div>
            </div>
        `;

        document.body.insertAdjacentHTML('beforeend', modalHTML);
        document.body.style.overflow = 'hidden';

        // Set up event listeners
        setupModalEventListeners();

        console.log('✅ HOME PAGE: Enhanced modal created and displayed');
    }

    // Setup modal event listeners
    function setupModalEventListeners() {
        const modal = document.getElementById('home-simple-modal');
        const header = document.getElementById('home-modal-header');

        // Close on overlay click
        modal.addEventListener('click', function(e) {
            if (e.target === modal) {
                closeHomeModal();
            }
        });

        // Close on Escape key
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape') {
                closeHomeModal();
            } else if (e.key === 'F11') {
                e.preventDefault();
                toggleHomeMaximize();
            }
        });

        // Double-click header to maximize
        header.addEventListener('dblclick', function() {
            toggleHomeMaximize();
        });

        // Button hover effects
        const maximizeBtn = document.getElementById('home-maximize-btn');
        maximizeBtn.addEventListener('mouseenter', function() {
            this.style.background = 'rgba(255, 255, 255, 0.3)';
        });
        maximizeBtn.addEventListener('mouseleave', function() {
            this.style.background = 'rgba(255, 255, 255, 0.2)';
        });
    }

    // Toggle maximize/restore function
    window.toggleHomeMaximize = function() {
        const dialog = document.getElementById('home-modal-dialog');
        const maximizeBtn = document.getElementById('home-maximize-btn');
        const icon = maximizeBtn.querySelector('i');

        if (isMaximized) {
            // Restore to original size
            dialog.style.width = originalSize.width;
            dialog.style.maxWidth = originalSize.maxWidth;
            dialog.style.maxHeight = originalSize.maxHeight;
            dialog.style.height = 'auto';
            
            icon.className = 'fas fa-expand';
            maximizeBtn.title = 'Maximize';
            isMaximized = false;
            
            console.log('🏠 HOME PAGE: Modal restored to original size');
        } else {
            // Maximize to full screen
            dialog.style.width = '98vw';
            dialog.style.maxWidth = 'none';
            dialog.style.height = '95vh';
            dialog.style.maxHeight = 'none';
            
            icon.className = 'fas fa-compress';
            maximizeBtn.title = 'Restore';
            isMaximized = true;
            
            console.log('🏠 HOME PAGE: Modal maximized');
        }
    };

    // Close modal function
    window.closeHomeModal = function() {
        const modal = document.getElementById('home-simple-modal');
        if (modal) {
            const dialog = document.getElementById('home-modal-dialog');
            
            // Animate out
            dialog.style.transform = 'scale(0.9)';
            dialog.style.opacity = '0.5';
            
            setTimeout(() => {
                modal.remove();
                document.body.style.overflow = '';
                isMaximized = false;
            }, 200);
        }
        console.log('✅ HOME PAGE: Modal closed');
    };

    // Main show modal function (supports both news and exhibitions)
    window.showNewsModal = function(itemId) {
        console.log('🏠 HOME PAGE: showNewsModal called with ID:', itemId);

        if (!itemId) {
            console.error('❌ HOME PAGE: No item ID provided');
            createSimpleModal('error', 'Error', '<p>Unable to load content. Missing item ID.</p>');
            return;
        }

        // Determine if this is news or exhibition content
        const isExhibition = document.querySelector(`[data-exhibition-id="${itemId}"]`);
        const isNews = document.querySelector(`[data-news-id="${itemId}"]`);

        let title, content, type;

        if (isExhibition) {
            // Handle exhibition content
            type = 'Exhibition';
            const exhibitionData = getExhibitionContent(itemId);
            title = exhibitionData.title;
            content = exhibitionData.content;
        } else if (isNews) {
            // Handle news content
            type = 'News Article';
            const newsData = getNewsContent(itemId);
            title = newsData.title;
            content = newsData.content;
        } else {
            // Try both and see what we find
            const newsData = getNewsContent(itemId);
            const exhibitionData = getExhibitionContent(itemId);
            
            if (newsData.content !== '<p>Loading content...</p>') {
                type = 'News Article';
                title = newsData.title;
                content = newsData.content;
            } else if (exhibitionData.content !== '<p>Loading content...</p>') {
                type = 'Exhibition';
                title = exhibitionData.title;
                content = exhibitionData.content;
            } else {
                type = 'Content';
                title = 'Content Details';
                content = `
                    <h3>Content ${itemId}</h3>
                    <p>This content is loading. Please wait a moment...</p>
                    <p>If this persists, please try refreshing the page.</p>
                `;
            }
        }

        createSimpleModal(itemId, title, content, type);
    };

    // Get news content from existing modal
    function getNewsContent(newsId) {
        const existingModal = document.getElementById('modal-' + newsId);
        let title = 'News Article';
        let content = '<p>Loading content...</p>';

        if (existingModal) {
            const titleElement = existingModal.querySelector('.modal-title, h2, h3');
            const contentElement = existingModal.querySelector('.modal-content-text, .modal-body');

            if (titleElement) title = titleElement.textContent.trim();
            if (contentElement) content = contentElement.innerHTML;
        }

        return { title, content };
    }

    // Get exhibition content from button's parent card
    function getExhibitionContent(exhibitionId) {
        const exhibitionButton = document.querySelector(`[data-exhibition-id="${exhibitionId}"]`);
        let title = 'Exhibition Details';
        let content = '<p>Loading content...</p>';

        if (exhibitionButton) {
            const exhibitionCard = exhibitionButton.closest('.exhibition-preview-card');
            if (exhibitionCard) {
                // Extract title
                const titleElement = exhibitionCard.querySelector('h3, .exhibition-title');
                if (titleElement) title = titleElement.textContent.trim();

                // Extract image
                const imageElement = exhibitionCard.querySelector('.exhibition-image');
                let imageHtml = '';
                if (imageElement && imageElement.src) {
                    imageHtml = `
                        <img src="${imageElement.src}" 
                             alt="Exhibition Image" 
                             style="
                                width: 100%;
                                max-height: 300px;
                                object-fit: cover;
                                border-radius: 12px;
                                margin-bottom: 24px;
                                box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
                             ">
                    `;
                }

                // Extract description
                const descElement = exhibitionCard.querySelector('.exhibition-description');
                const descContent = descElement ? descElement.innerHTML : '<p>Exhibition details are being prepared.</p>';
                
                content = imageHtml + descContent;
            }
        }

        return { title, content };
    }

    // Set up direct click handlers for home page
    function setupHomePageHandlers() {
        console.log('🏠 HOME PAGE: Setting up click handlers...');

        // Wait for DOM to be ready
        setTimeout(() => {
            // Find both news and exhibition buttons
            const newsButtons = document.querySelectorAll('.news-detail-btn, .news-read-btn, .btn-read-more, [class*="news"]');
            const exhibitionButtons = document.querySelectorAll('.exhibition-cta, [data-exhibition-id]');
            const allButtons = [...newsButtons, ...exhibitionButtons];
            
            console.log(`🏠 HOME PAGE: Found ${newsButtons.length} news buttons and ${exhibitionButtons.length} exhibition buttons`);

            allButtons.forEach((button, index) => {
                // Get ID from various attributes
                const newsId = button.getAttribute('data-news-id') || 
                              button.getAttribute('data-target')?.replace('#modal-', '') ||
                              button.closest('[data-news-id]')?.getAttribute('data-news-id');
                
                const exhibitionId = button.getAttribute('data-exhibition-id') ||
                                   button.closest('[data-exhibition-id]')?.getAttribute('data-exhibition-id');

                const itemId = newsId || exhibitionId;
                const itemType = newsId ? 'news' : 'exhibition';

                if (itemId) {
                    // Remove existing onclick
                    button.removeAttribute('onclick');

                    // Add new click handler
                    button.addEventListener('click', function(e) {
                        e.preventDefault();
                        e.stopPropagation();
                        e.stopImmediatePropagation();

                        console.log(`🏠 HOME PAGE: ${itemType} button ${index + 1} clicked, ID: ${itemId}`);
                        window.showNewsModal(itemId);

                        return false;
                    }, true);

                    console.log(`✅ HOME PAGE: Handler set for ${itemType} button ${index + 1}, ID: ${itemId}`);
                } else {
                    console.log(`⚠️ HOME PAGE: No ID found for button ${index + 1}`);
                }
            });

            // Also set up event delegation as backup
            document.addEventListener('click', function(e) {
                const button = e.target.closest('.news-detail-btn, .news-read-btn, .btn-read-more, .exhibition-cta, [data-exhibition-id]');
                if (button) {
                    const newsId = button.getAttribute('data-news-id') || 
                                  button.getAttribute('data-target')?.replace('#modal-', '');
                    const exhibitionId = button.getAttribute('data-exhibition-id');
                    const itemId = newsId || exhibitionId;
                    const itemType = newsId ? 'news' : 'exhibition';
                    
                    if (itemId) {
                        e.preventDefault();
                        e.stopPropagation();
                        e.stopImmediatePropagation();

                        console.log(`🏠 HOME PAGE: Event delegation triggered for ${itemType} ID:`, itemId);
                        window.showNewsModal(itemId);

                        return false;
                    }
                }
            }, true);

            console.log('✅ HOME PAGE: All news and exhibition handlers set up successfully');
        }, 1000);
    }

    // Test function
    window.testHomeModal = function() {
        console.log('🧪 HOME PAGE: Testing modal...');
        window.showNewsModal('test');
    };

    // Initialize
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', setupHomePageHandlers);
    } else {
        setupHomePageHandlers();
    }

    console.log('✅ HOME PAGE MODAL FIX: Loaded and ready');

})();