/**
 * NEWS PAGE INDEPENDENT MODAL SYSTEM
 * Separate modal functionality for the dedicated news page
 * Does not interfere with home page modals
 */

(function() {
    'use strict';

    console.log('📰 NEWS PAGE MODAL: Loading independent modal system...');

    // Modal state for news page
    let newsModalState = {
        isMaximized: false,
        originalSize: { width: '90%', maxWidth: '900px', maxHeight: '85vh' }
    };

    // Create news page modal function
    function createNewsPageModal(newsId, title, content) {
        console.log('📰 NEWS PAGE: Creating modal for news ID:', newsId);

        // Remove existing news modal
        const existing = document.getElementById('news-page-modal');
        if (existing) existing.remove();

        // Reset state
        newsModalState.isMaximized = false;

        // Create modal HTML
        const modalHTML = `
            <div id="news-page-modal" style="
                position: fixed !important;
                top: 0 !important;
                left: 0 !important;
                width: 100vw !important;
                height: 100vh !important;
                background: rgba(255, 255, 255, 0.95) !important;
                z-index: 999999 !important;
                display: flex !important;
                align-items: center !important;
                justify-content: center !important;
                font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Arial, sans-serif !important;
                backdrop-filter: blur(4px) !important;
                animation: newsModalFadeIn 0.3s ease-out !important;
            ">
                <div id="news-modal-dialog" style="
                    background: white !important;
                    border-radius: 20px !important;
                    box-shadow: 0 30px 100px rgba(0, 0, 0, 0.5) !important;
                    width: 90% !important;
                    max-width: 900px !important;
                    max-height: 85vh !important;
                    display: flex !important;
                    flex-direction: column !important;
                    overflow: hidden !important;
                    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
                    animation: newsModalSlideIn 0.3s ease-out !important;
                ">
                    <div id="news-modal-header" style="
                        background: linear-gradient(135deg, #1e40af 0%, #3b82f6 50%, #60a5fa 100%) !important;
                        color: white !important;
                        padding: 24px 28px !important;
                        display: flex !important;
                        justify-content: space-between !important;
                        align-items: center !important;
                        border-radius: 20px 20px 0 0 !important;
                        cursor: move !important;
                        user-select: none !important;
                        position: relative !important;
                        overflow: hidden !important;
                    ">

                        <div style="flex: 1 !important; min-width: 0 !important; position: relative !important; z-index: 1 !important;">
                            <div style="
                                font-size: 13px !important;
                                opacity: 0.9 !important;
                                margin-bottom: 8px !important;
                                display: flex !important;
                                align-items: center !important;
                                gap: 8px !important;
                                font-weight: 500 !important;
                            ">
                                <i class="fas fa-newspaper"></i>
                                <span>News Article</span>
                            </div>
                            <h3 id="news-modal-title" style="
                                margin: 0 !important;
                                font-size: 22px !important;
                                font-weight: 700 !important;
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
                            gap: 10px !important;
                            flex-shrink: 0 !important;
                            margin-left: 24px !important;
                            position: relative !important;
                            z-index: 1 !important;
                        ">
                            <button id="news-maximize-btn" onclick="toggleNewsMaximize()" style="
                                background: rgba(255, 255, 255, 0.2) !important;
                                border: none !important;
                                color: white !important;
                                width: 36px !important;
                                height: 36px !important;
                                border-radius: 8px !important;
                                cursor: pointer !important;
                                font-size: 15px !important;
                                display: flex !important;
                                align-items: center !important;
                                justify-content: center !important;
                                transition: all 0.2s ease !important;
                                backdrop-filter: blur(10px) !important;
                            " title="Maximize">
                                <i class="fas fa-expand"></i>
                            </button>
                            <button onclick="closeNewsPageModal()" style="
                                background: rgba(239, 68, 68, 0.9) !important;
                                color: white !important;
                                border: none !important;
                                width: 36px !important;
                                height: 36px !important;
                                border-radius: 8px !important;
                                cursor: pointer !important;
                                font-size: 15px !important;
                                display: flex !important;
                                align-items: center !important;
                                justify-content: center !important;
                                transition: all 0.2s ease !important;
                                backdrop-filter: blur(10px) !important;
                            " title="Close">
                                <i class="fas fa-times"></i>
                            </button>
                        </div>
                    </div>
                    <div id="news-modal-content" style="
                        padding: 0 !important;
                        overflow-y: auto !important;
                        overflow-x: hidden !important;
                        flex: 1 !important;
                        background: #fafbfc !important;
                    ">
                        <div style="
                            background: white !important;
                            margin: 32px !important;
                            padding: 40px !important;
                            border-radius: 16px !important;
                            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08) !important;
                            line-height: 1.8 !important;
                            color: #374151 !important;
                            font-size: 16px !important;
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
        setupNewsModalEventListeners();

        console.log('✅ NEWS PAGE: Modal created and displayed');
    }

    // Setup news modal event listeners
    function setupNewsModalEventListeners() {
        const modal = document.getElementById('news-page-modal');
        const header = document.getElementById('news-modal-header');

        // Close on overlay click
        modal.addEventListener('click', function(e) {
            if (e.target === modal) {
                closeNewsPageModal();
            }
        });

        // Close on Escape key
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape') {
                closeNewsPageModal();
            } else if (e.key === 'F11') {
                e.preventDefault();
                toggleNewsMaximize();
            }
        });

        // Double-click header to maximize
        header.addEventListener('dblclick', function() {
            toggleNewsMaximize();
        });

        // Button hover effects
        const maximizeBtn = document.getElementById('news-maximize-btn');
        maximizeBtn.addEventListener('mouseenter', function() {
            this.style.background = 'rgba(255, 255, 255, 0.3)';
            this.style.transform = 'translateY(-1px)';
        });
        maximizeBtn.addEventListener('mouseleave', function() {
            this.style.background = 'rgba(255, 255, 255, 0.2)';
            this.style.transform = 'translateY(0)';
        });
    }

    // Toggle maximize/restore function for news
    window.toggleNewsMaximize = function() {
        const dialog = document.getElementById('news-modal-dialog');
        const maximizeBtn = document.getElementById('news-maximize-btn');
        const icon = maximizeBtn.querySelector('i');

        if (newsModalState.isMaximized) {
            // Restore to original size
            dialog.style.width = newsModalState.originalSize.width;
            dialog.style.maxWidth = newsModalState.originalSize.maxWidth;
            dialog.style.maxHeight = newsModalState.originalSize.maxHeight;
            dialog.style.height = 'auto';
            
            icon.className = 'fas fa-expand';
            maximizeBtn.title = 'Maximize';
            newsModalState.isMaximized = false;
            
            console.log('📰 NEWS PAGE: Modal restored to original size');
        } else {
            // Maximize to full screen
            dialog.style.width = '98vw';
            dialog.style.maxWidth = 'none';
            dialog.style.height = '95vh';
            dialog.style.maxHeight = 'none';
            
            icon.className = 'fas fa-compress';
            maximizeBtn.title = 'Restore';
            newsModalState.isMaximized = true;
            
            console.log('📰 NEWS PAGE: Modal maximized');
        }
    };

    // Close news modal function
    window.closeNewsPageModal = function() {
        const modal = document.getElementById('news-page-modal');
        if (modal) {
            const dialog = document.getElementById('news-modal-dialog');
            
            // Animate out
            dialog.style.transform = 'scale(0.95) translateY(-20px)';
            dialog.style.opacity = '0';
            modal.style.opacity = '0';
            
            setTimeout(() => {
                modal.remove();
                document.body.style.overflow = '';
                newsModalState.isMaximized = false;
            }, 250);
        }
        console.log('✅ NEWS PAGE: Modal closed');
    };

    // Main show news modal function for news page
    window.showNewsPageModal = function(newsId) {
        console.log('📰 NEWS PAGE: showNewsPageModal called with ID:', newsId);

        if (!newsId) {
            console.error('❌ NEWS PAGE: No news ID provided');
            createNewsPageModal('error', 'Error', '<p>Unable to load news content. Missing news ID.</p>');
            return;
        }

        // Get news content from existing modal or card
        const newsData = getNewsPageContent(newsId);
        createNewsPageModal(newsId, newsData.title, newsData.content);
    };

    // Get news content - only show news.imageUrl and news.description
    function getNewsPageContent(newsId) {
        let title = 'News Article';
        let content = '<p>Loading news content...</p>';

        // Try to find existing modal first
        const existingModal = document.getElementById('modal-' + newsId);
        if (existingModal) {
            const titleElement = existingModal.querySelector('.modal-title, h2, h3, h4');
            const contentElement = existingModal.querySelector('.modal-content-text, .modal-body, .modal-content, .news-excerpt');
            const imageElement = existingModal.querySelector('img');

            if (titleElement) title = titleElement.textContent.trim();
            
            // Extract image
            let imageHtml = '';
            if (imageElement && imageElement.src) {
                imageHtml = `
                    <img src="${imageElement.src}" 
                         alt="News Image" 
                         style="
                            width: 100%;
                            max-height: 400px;
                            object-fit: cover;
                            border-radius: 12px;
                            margin-bottom: 24px;
                            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
                         ">
                `;
            }

            // Extract only description text (news.description)
            if (contentElement) {
                // Get only the clean text content, no HTML
                let textContent = contentElement.textContent || contentElement.innerText || '';
                textContent = textContent.trim();
                
                /*// Remove redundant modal elements that might be duplicated
                textContent = textContent.replace(/<div[^>]*modal-image[^>]*>.*?<\/div>/gi, '');
                textContent = textContent.replace(/<div[^>]*modal-category[^>]*>.*?<\/div>/gi, '');
                textContent = textContent.replace(/<h[1-6][^>]*modal-title[^>]*>.*?<\/h[1-6]>/gi, '');
                textContent = textContent.replace(/<div[^>]*modal-meta[^>]*>.*?<\/div>/gi, '');
                textContent = textContent.replace(/<span[^>]*modal-date[^>]*>.*?<\/span>/gi, '');
                textContent = textContent.replace(/<span[^>]*modal-reading-time[^>]*>.*?<\/span>/gi, '');
                
                // Remove specific unwanted text
                textContent = textContent.replace(/Company News/gi, '');
                textContent = textContent.replace(/3 min read/gi, '');
                textContent = textContent.replace(/\d+\s*min\s*read/gi, ''); // Remove any "X min read" pattern
                textContent = textContent.replace(/Technology Innovation Company/gi, '');
                
                // Remove elements containing these texts
                textContent = textContent.replace(/<[^>]*>[^<]*Company News[^<]*<\/[^>]*>/gi, '');
                textContent = textContent.replace(/<[^>]*>[^<]*\d+\s*min\s*read[^<]*<\/[^>]*>/gi, '');
                textContent = textContent.replace(/<[^>]*>[^<]*Technology Innovation Company[^<]*<\/[^>]*>/gi, '');
                
                // Remove modal footer and close buttons
                textContent = textContent.replace(/<div[^>]*modal-footer[^>]*>.*?<\/div>/gi, '');
                textContent = textContent.replace(/<button[^>]*modal-close[^>]*>.*?<\/button>/gi, '');
                textContent = textContent.replace(/<button[^>]*btn-modal-close[^>]*>.*?<\/button>/gi, '');
                textContent = textContent.replace(/<button[^>]*data-dismiss[^>]*>.*?<\/button>/gi, '');
                
                // Clean up classes and inline styles
                textContent = textContent.replace(/class="[^"]*"/g, '');
                textContent = textContent.replace(/style="[^"]*"/g, '');
                
                // Remove modal tags and footer elements
                textContent = textContent.replace(/<div[^>]*modal-tags[^>]*>.*?<\/div>/gi, '');
                textContent = textContent.replace(/<div[^>]*modal-actions[^>]*>.*?<\/div>/gi, '');
                textContent = textContent.replace(/<span[^>]*tag[^>]*>.*?<\/span>/gi, '');
                
                // Remove action containers and button wrappers
                textContent = textContent.replace(/<div[^>]*actions[^>]*>.*?<\/div>/gi, '');
                textContent = textContent.replace(/<div[^>]*buttons[^>]*>.*?<\/div>/gi, '');
                textContent = textContent.replace(/<div[^>]*controls[^>]*>.*?<\/div>/gi, '');
                
                // Remove any remaining close/action buttons
                textContent = textContent.replace(/<button[^>]*>.*?Close.*?<\/button>/gi, '');
                textContent = textContent.replace(/<button[^>]*>.*?<i[^>]*fa-times[^>]*><\/i>.*?<\/button>/gi, '');
                
                // Remove all buttons from content area
                textContent = textContent.replace(/<button[^>]*>.*?<\/button>/gi, '');
                textContent = textContent.replace(/<input[^>]*type="button"[^>]*>/gi, '');
                textContent = textContent.replace(/<input[^>]*type="submit"[^>]*>/gi, '');
                
                // Remove button-like elements that might appear after images
                textContent = textContent.replace(/<a[^>]*class="[^"]*btn[^"]*"[^>]*>.*?<\/a>/gi, '');
                textContent = textContent.replace(/<div[^>]*class="[^"]*button[^"]*"[^>]*>.*?<\/div>/gi, '');
                
                // Remove empty divs and spans
                textContent = textContent.replace(/<div[^>]*>\s*<\/div>/gi, '');
                textContent = textContent.replace(/<span[^>]*>\s*<\/span>/gi, '');*/
                
                content = imageHtml + textContent;
            }
        } else {
            // Try to find news card
            const newsCard = document.querySelector(`[data-news-id="${newsId}"]`)?.closest('.news-card, .news-item');
            if (newsCard) {
                const titleElement = newsCard.querySelector('h3, h4, .news-title, .card-title');
                const descElement = newsCard.querySelector('.news-description, .news-content, .card-text, p');
                const imageElement = newsCard.querySelector('img');

                if (titleElement) title = titleElement.textContent.trim();
                
                // Extract image
                let imageHtml = '';
                if (imageElement && imageElement.src) {
                    imageHtml = `
                        <img src="${imageElement.src}" 
                             alt="News Image" 
                             style="
                                width: 100%;
                                max-height: 400px;
                                object-fit: cover;
                                border-radius: 12px;
                                margin-bottom: 24px;
                                box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
                             ">
                    `;
                }

                // Extract content only and clean unwanted text
                let descContent = descElement ? descElement.innerHTML : '<p>News content is being prepared.</p>';
                
                // Remove unwanted text from card content
                /*descContent = descContent.replace(/Company News/gi, '');
                descContent = descContent.replace(/3 min read/gi, '');
                descContent = descContent.replace(/\d+\s*min\s*read/gi, ''); // Remove any "X min read" pattern
                descContent = descContent.replace(/Technology Innovation Company/gi, '');
                
                // Remove elements containing these texts
                descContent = descContent.replace(/<[^>]*>[^<]*Company News[^<]*<\/[^>]*>/gi, '');
                descContent = descContent.replace(/<[^>]*>[^<]*\d+\s*min\s*read[^<]*<\/[^>]*>/gi, '');
                descContent = descContent.replace(/<[^>]*>[^<]*Technology Innovation Company[^<]*<\/[^>]*>/gi, '');
                
                // Remove modal footer and close buttons
                descContent = descContent.replace(/<div[^>]*modal-footer[^>]*>.*?<\/div>/gi, '');
                descContent = descContent.replace(/<button[^>]*modal-close[^>]*>.*?<\/button>/gi, '');
                descContent = descContent.replace(/<button[^>]*btn-modal-close[^>]*>.*?<\/button>/gi, '');
                descContent = descContent.replace(/<button[^>]*data-dismiss[^>]*>.*?<\/button>/gi, '');
                
                // Remove all buttons from content area
                descContent = descContent.replace(/<button[^>]*>.*?<\/button>/gi, '');
                descContent = descContent.replace(/<input[^>]*type="button"[^>]*>/gi, '');
                descContent = descContent.replace(/<input[^>]*type="submit"[^>]*>/gi, '');
                
                // Remove button-like elements that might appear after images
                descContent = descContent.replace(/<a[^>]*class="[^"]*btn[^"]*"[^>]*>.*?<\/a>/gi, '');
                descContent = descContent.replace(/<div[^>]*class="[^"]*button[^"]*"[^>]*>.*?<\/div>/gi, '');*/
                
                content = imageHtml + descContent;
            }
        }

        return { title, content };
    }

    // Set up news page handlers
    function setupNewsPageHandlers() {
        console.log('📰 NEWS PAGE: Setting up independent modal handlers...');

        // Wait for DOM to be ready
        setTimeout(() => {
            // Find news buttons on the news page
            const newsButtons = document.querySelectorAll('.news-detail-btn, .news-read-btn, .btn-read-more, [data-news-id]');
            
            console.log(`📰 NEWS PAGE: Found ${newsButtons.length} news buttons`);

            newsButtons.forEach((button, index) => {
                // Get ID from various attributes
                const newsId = button.getAttribute('data-news-id') || 
                              button.getAttribute('data-target')?.replace('#modal-', '') ||
                              button.closest('[data-news-id]')?.getAttribute('data-news-id');

                if (newsId) {
                    // Remove existing onclick
                    button.removeAttribute('onclick');

                    // Add new click handler for news page
                    button.addEventListener('click', function(e) {
                        e.preventDefault();
                        e.stopPropagation();
                        e.stopImmediatePropagation();

                        console.log(`📰 NEWS PAGE: News button ${index + 1} clicked, ID: ${newsId}`);
                        window.showNewsPageModal(newsId);

                        return false;
                    }, true);

                    console.log(`✅ NEWS PAGE: Handler set for news button ${index + 1}, ID: ${newsId}`);
                } else {
                    console.log(`⚠️ NEWS PAGE: No ID found for button ${index + 1}`);
                }
            });

            console.log('✅ NEWS PAGE: All news handlers set up successfully');
        }, 1000);
    }

    // Test function for news page
    window.testNewsPageModal = function() {
        console.log('🧪 NEWS PAGE: Testing modal...');
        window.showNewsPageModal('test');
    };

    // Initialize only if we're on the news page
    if (document.body.classList.contains('news-page') || window.location.pathname.includes('news')) {
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', setupNewsPageHandlers);
        } else {
            setupNewsPageHandlers();
        }
        console.log('✅ NEWS PAGE MODAL: Loaded and ready for news page');
    } else {
        console.log('📰 NEWS PAGE MODAL: Not on news page, skipping initialization');
    }

})();

// CSS animations for news modal
const newsModalCSS = `
<style>
@keyframes newsModalFadeIn {
    from { opacity: 0; }
    to { opacity: 1; }
}

@keyframes newsModalSlideIn {
    from {
        transform: scale(0.9) translateY(-30px);
        opacity: 0;
    }
    to {
        transform: scale(1) translateY(0);
        opacity: 1;
    }
}

#news-page-modal * {
    box-sizing: border-box !important;
}

#news-modal-content::-webkit-scrollbar {
    width: 8px !important;
}

#news-modal-content::-webkit-scrollbar-track {
    background: #f1f5f9 !important;
    border-radius: 4px !important;
}

#news-modal-content::-webkit-scrollbar-thumb {
    background: #cbd5e1 !important;
    border-radius: 4px !important;
}

#news-modal-content::-webkit-scrollbar-thumb:hover {
    background: #94a3b8 !important;
}
</style>
`;

document.head.insertAdjacentHTML('beforeend', newsModalCSS);