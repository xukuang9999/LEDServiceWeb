/**
 * Independent News Modal
 * Completely independent modal with maximize/restore functionality only
 */

(function() {
    'use strict';

    console.log('📰 Loading Independent News Modal...');

    let currentModal = null;
    let isMaximized = false;
    let originalSize = { width: 800, height: 600 };

    /**
     * Create independent modal HTML
     */
    function createModalHTML(newsData) {
        return `
            <div id="independent-news-modal" style="
                position: fixed !important;
                top: 0 !important;
                left: 0 !important;
                width: 100vw !important;
                height: 100vh !important;
                background: rgba(0, 0, 0, 0.7) !important;
                z-index: 2147483647 !important;
                display: flex !important;
                align-items: center !important;
                justify-content: center !important;
                font-family: Arial, sans-serif !important;
            ">
                <div id="modal-window" style="
                    background: white !important;
                    border-radius: 8px !important;
                    box-shadow: 0 10px 30px rgba(0,0,0,0.3) !important;
                    display: flex !important;
                    flex-direction: column !important;
                    width: 800px !important;
                    height: 600px !important;
                    max-width: 95vw !important;
                    max-height: 95vh !important;
                    position: relative !important;
                ">
                    <!-- Header -->
                    <div style="
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
                        color: white !important;
                        padding: 15px 20px !important;
                        border-radius: 8px 8px 0 0 !important;
                        display: flex !important;
                        justify-content: space-between !important;
                        align-items: center !important;
                        flex-shrink: 0 !important;
                    ">
                        <h3 style="margin: 0 !important; font-size: 18px !important; font-weight: 600 !important;">
                            ${newsData.title || 'News Article'}
                        </h3>
                        <div style="display: flex !important; gap: 10px !important;">
                            <button id="maximize-btn" style="
                                background: rgba(255,255,255,0.2) !important;
                                border: none !important;
                                color: white !important;
                                width: 30px !important;
                                height: 30px !important;
                                border-radius: 4px !important;
                                cursor: pointer !important;
                                display: flex !important;
                                align-items: center !important;
                                justify-content: center !important;
                                font-size: 14px !important;
                            " title="Maximize/Restore">
                                <i class="fas fa-window-maximize"></i>
                            </button>
                            <button id="close-btn" style="
                                background: #dc3545 !important;
                                border: none !important;
                                color: white !important;
                                width: 30px !important;
                                height: 30px !important;
                                border-radius: 4px !important;
                                cursor: pointer !important;
                                display: flex !important;
                                align-items: center !important;
                                justify-content: center !important;
                                font-size: 16px !important;
                            " title="Close">×</button>
                        </div>
                    </div>

                    <!-- Body -->
                    <div style="
                        flex: 1 !important;
                        padding: 20px !important;
                        overflow-y: auto !important;
                        background: #fafafa !important;
                    ">
                        <div style="
                            background: white !important;
                            padding: 20px !important;
                            border-radius: 6px !important;
                            box-shadow: 0 2px 8px rgba(0,0,0,0.1) !important;
                        ">
                            ${newsData.imageUrl ? `
                                <img src="${newsData.imageUrl}" alt="News Image" style="
                                    width: 100% !important;
                                    max-height: 300px !important;
                                    object-fit: cover !important;
                                    border-radius: 6px !important;
                                    margin-bottom: 20px !important;
                                ">
                            ` : ''}
                            
                            <div style="
                                color: #333 !important;
                                line-height: 1.6 !important;
                                font-size: 16px !important;
                            ">
                                ${newsData.content || '<p>Loading news content...</p>'}
                            </div>
                        </div>
                    </div>

                    <!-- Footer -->
                    <div style="
                        padding: 15px 20px !important;
                        border-top: 1px solid #eee !important;
                        background: white !important;
                        border-radius: 0 0 8px 8px !important;
                        display: flex !important;
                        justify-content: space-between !important;
                        align-items: center !important;
                        flex-shrink: 0 !important;
                    ">
                        <div style="color: #666 !important; font-size: 14px !important;">
                            ${formatDate(newsData.createTime)}
                        </div>
                        <button id="footer-close-btn" style="
                            background: #6c757d !important;
                            color: white !important;
                            border: none !important;
                            padding: 8px 16px !important;
                            border-radius: 4px !important;
                            cursor: pointer !important;
                            font-size: 14px !important;
                        ">Close</button>
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
     * Toggle maximize/restore
     */
    function toggleMaximize() {
        const modalWindow = document.getElementById('modal-window');
        const maximizeBtn = document.getElementById('maximize-btn');
        const icon = maximizeBtn.querySelector('i');

        if (isMaximized) {
            // Restore to original size
            modalWindow.style.width = originalSize.width + 'px';
            modalWindow.style.height = originalSize.height + 'px';
            modalWindow.style.maxWidth = '95vw';
            modalWindow.style.maxHeight = '95vh';
            
            icon.className = 'fas fa-window-maximize';
            maximizeBtn.title = 'Maximize';
            isMaximized = false;
            
            console.log('📰 Modal restored to original size');
        } else {
            // Maximize to full screen
            modalWindow.style.width = '98vw';
            modalWindow.style.height = '98vh';
            modalWindow.style.maxWidth = 'none';
            modalWindow.style.maxHeight = 'none';
            
            icon.className = 'fas fa-window-restore';
            maximizeBtn.title = 'Restore';
            isMaximized = true;
            
            console.log('📰 Modal maximized');
        }
    }

    /**
     * Close modal
     */
    function closeModal() {
        if (currentModal) {
            currentModal.remove();
            currentModal = null;
            isMaximized = false;
            
            // Restore body scroll
            document.body.style.overflow = '';
            
            console.log('📰 Modal closed');
        }
    }

    /**
     * Get news data from existing modal or create fallback
     */
    function getNewsData(newsId) {
        // Try to get data from existing Bootstrap modal
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
        
        // Fallback data
        return {
            id: newsId,
            title: 'News Article',
            content: '<p>Unable to load news content. Please try again.</p>',
            createTime: new Date().toISOString(),
            imageUrl: null
        };
    }

    /**
     * Show independent modal
     */
    function showIndependentModal(newsId) {
        console.log('📰 Showing independent modal for news ID:', newsId);

        // Close any existing modal
        if (currentModal) {
            closeModal();
        }

        // Get news data
        const newsData = getNewsData(newsId);
        
        // Create modal HTML
        const modalHTML = createModalHTML(newsData);
        
        // Remove any existing modals first
        const existingModal = document.getElementById('independent-news-modal');
        if (existingModal) {
            existingModal.remove();
        }

        // Add to DOM - directly to body
        document.body.insertAdjacentHTML('beforeend', modalHTML);
        currentModal = document.getElementById('independent-news-modal');
        
        // Debug: Check if modal was created
        console.log('📰 Modal created:', currentModal);
        console.log('📰 Modal z-index:', window.getComputedStyle(currentModal).zIndex);
        console.log('📰 Modal position:', window.getComputedStyle(currentModal).position);
        
        // Prevent body scroll
        document.body.style.overflow = 'hidden';
        
        // Set up event listeners
        document.getElementById('maximize-btn').addEventListener('click', toggleMaximize);
        document.getElementById('close-btn').addEventListener('click', closeModal);
        document.getElementById('footer-close-btn').addEventListener('click', closeModal);
        
        // Close on overlay click
        currentModal.addEventListener('click', function(e) {
            if (e.target === currentModal) {
                closeModal();
            }
        });
        
        // Close on Escape key
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape' && currentModal) {
                closeModal();
            }
        });
        
        console.log('📰 Independent modal displayed successfully');
    }

    /**
     * Set up event listeners for news buttons
     */
    function setupEventListeners() {
        console.log('📰 Setting up independent modal event listeners...');

        document.addEventListener('click', function(e) {
            const button = e.target.closest('.news-read-btn, .news-detail-btn, .btn-read-more');
            
            if (button) {
                e.preventDefault();
                e.stopPropagation();

                // Get news ID from data-target attribute
                const dataTarget = button.getAttribute('data-target');
                if (dataTarget) {
                    const newsId = dataTarget.replace('#modal-', '');
                    showIndependentModal(newsId);
                } else {
                    console.error('📰 No data-target found on button');
                }

                return false;
            }
        });

        console.log('📰 Event listeners set up successfully');
    }

    /**
     * Initialize when DOM is ready
     */
    function init() {
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', setupEventListeners);
        } else {
            setupEventListeners();
        }
    }

    // Export for testing
    window.showIndependentModal = showIndependentModal;
    window.testIndependentModal = function() {
        showIndependentModal('test');
    };

    // Initialize
    init();

    console.log('✅ Independent News Modal loaded and ready');

})();