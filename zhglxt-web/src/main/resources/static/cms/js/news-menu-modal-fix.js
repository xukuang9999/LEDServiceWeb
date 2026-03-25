/**
 * News Menu Modal Fix
 * Fixes the modal interference issue on the news menu page
 * Creates independent modals that are not affected by parent containers
 */

(function() {
    'use strict';

    console.log('📰 Loading News Menu Modal Fix...');

    /**
     * Create independent modal outside of parent containers
     */
    function createIndependentModal(newsId, newsData) {
        console.log('📰 Creating independent modal for news ID:', newsId);

        // Remove any existing independent modal
        const existingModal = document.getElementById('independent-modal-' + newsId);
        if (existingModal) {
            existingModal.remove();
        }

        // Create modal HTML
        const modalHTML = `
            <div id="independent-modal-${newsId}" class="independent-modal-overlay" style="
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
                <div class="independent-modal-dialog" style="
                    background: white !important;
                    border-radius: 8px !important;
                    box-shadow: 0 20px 60px rgba(0,0,0,0.3) !important;
                    width: 90vw !important;
                    max-width: 800px !important;
                    height: 90vh !important;
                    max-height: 600px !important;
                    display: flex !important;
                    flex-direction: column !important;
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
                            ${newsData.title}
                        </h3>
                        <button onclick="document.getElementById('independent-modal-${newsId}').remove(); document.body.style.overflow = '';" style="
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
                        ">×</button>
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
                                ${newsData.content}
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
                            ${newsData.date}
                        </div>
                        <button onclick="document.getElementById('independent-modal-${newsId}').remove(); document.body.style.overflow = '';" style="
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

        // Add modal directly to body (outside all containers)
        document.body.insertAdjacentHTML('beforeend', modalHTML);
        
        // Prevent body scroll
        document.body.style.overflow = 'hidden';

        console.log('📰 Independent modal created and displayed');
    }

    /**
     * Extract news data from existing Bootstrap modal
     */
    function extractNewsData(newsId) {
        const bootstrapModal = document.getElementById('modal-' + newsId);
        if (bootstrapModal) {
            const title = bootstrapModal.querySelector('.modal-title')?.textContent || 'News Article';
            const content = bootstrapModal.querySelector('.modal-content-text')?.innerHTML || '<p>Content not available</p>';
            const date = bootstrapModal.querySelector('.modal-date')?.textContent || 'Recent';
            const imageUrl = bootstrapModal.querySelector('.modal-image img')?.src || null;
            
            return { title, content, date, imageUrl };
        }
        
        return {
            title: 'News Article',
            content: '<p>Unable to load news content.</p>',
            date: 'Recent',
            imageUrl: null
        };
    }

    /**
     * Handle news button clicks
     */
    function handleNewsButtonClick(e) {
        const button = e.target.closest('.news-read-btn, .news-detail-btn, .btn-read-more');
        
        if (button) {
            // Prevent Bootstrap modal from opening
            e.preventDefault();
            e.stopPropagation();

            // Get news ID from data-target
            const dataTarget = button.getAttribute('data-target');
            if (dataTarget) {
                const newsId = dataTarget.replace('#modal-', '');
                const newsData = extractNewsData(newsId);
                createIndependentModal(newsId, newsData);
            }

            return false;
        }
    }

    /**
     * Initialize the fix
     */
    function init() {
        console.log('📰 Initializing news menu modal fix...');

        // Add event listener to handle all news button clicks
        document.addEventListener('click', handleNewsButtonClick, true);

        // Also handle escape key to close modals
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape') {
                const modals = document.querySelectorAll('[id^="independent-modal-"]');
                modals.forEach(modal => {
                    modal.remove();
                    document.body.style.overflow = '';
                });
            }
        });

        console.log('📰 News menu modal fix initialized');
    }

    // Initialize when DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

    console.log('✅ News Menu Modal Fix loaded');

})();