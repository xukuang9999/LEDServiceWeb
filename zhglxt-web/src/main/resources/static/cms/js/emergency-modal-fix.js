/**
 * EMERGENCY MODAL FIX
 * Last resort solution that ensures modal works 100% of the time
 */

(function() {
    'use strict';

    console.log('🚨 EMERGENCY MODAL FIX: Loading...');

    // Ensure showNewsModal exists immediately
    if (!window.showNewsModal) {
        window.showNewsModal = function(newsId) {
            console.log('🚨 EMERGENCY: showNewsModal called with ID:', newsId);
            
            // Create emergency modal immediately
            createEmergencyModal(newsId);
        };
    }

    function createEmergencyModal(newsId) {
        console.log('🚨 EMERGENCY: Creating emergency modal for ID:', newsId);

        // Remove any existing emergency modal
        const existing = document.getElementById('emergency-modal');
        if (existing) existing.remove();

        // Create emergency modal HTML
        const modalHTML = `
            <div id="emergency-modal" style="
                position: fixed !important;
                top: 0 !important;
                left: 0 !important;
                width: 100vw !important;
                height: 100vh !important;
                background: rgba(0, 0, 0, 0.8) !important;
                z-index: 2147483647 !important;
                display: flex !important;
                align-items: center !important;
                justify-content: center !important;
                font-family: Arial, sans-serif !important;
            ">
                <div style="
                    background: white !important;
                    border-radius: 12px !important;
                    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.4) !important;
                    width: 90% !important;
                    max-width: 800px !important;
                    max-height: 80vh !important;
                    display: flex !important;
                    flex-direction: column !important;
                    overflow: hidden !important;
                ">
                    <div style="
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
                        color: white !important;
                        padding: 20px !important;
                        display: flex !important;
                        justify-content: space-between !important;
                        align-items: center !important;
                    ">
                        <h3 id="emergency-title" style="margin: 0 !important; font-size: 20px !important;">
                            News Article
                        </h3>
                        <button onclick="closeEmergencyModal()" style="
                            background: #dc3545 !important;
                            color: white !important;
                            border: none !important;
                            width: 30px !important;
                            height: 30px !important;
                            border-radius: 50% !important;
                            cursor: pointer !important;
                            font-size: 16px !important;
                        ">×</button>
                    </div>
                    <div id="emergency-content" style="
                        padding: 30px !important;
                        overflow-y: auto !important;
                        flex: 1 !important;
                        line-height: 1.6 !important;
                        color: #333 !important;
                    ">
                        Loading news content...
                    </div>
                </div>
            </div>
        `;

        document.body.insertAdjacentHTML('beforeend', modalHTML);

        // Get content from existing modal
        const existingModal = document.getElementById('modal-' + newsId);
        if (existingModal) {
            const title = existingModal.querySelector('.modal-title')?.textContent || 'News Article';
            const content = existingModal.querySelector('.modal-content-text, .modal-body')?.innerHTML || 'Content not available';
            
            document.getElementById('emergency-title').textContent = title;
            document.getElementById('emergency-content').innerHTML = content;
        } else {
            document.getElementById('emergency-content').innerHTML = `
                <h3>News Article ${newsId}</h3>
                <p>This is an emergency modal display. The news content system is loading...</p>
                <p>Please try refreshing the page if this persists.</p>
            `;
        }

        // Prevent body scroll
        document.body.style.overflow = 'hidden';

        console.log('✅ EMERGENCY: Emergency modal created and displayed');
    }

    // Close function
    window.closeEmergencyModal = function() {
        const modal = document.getElementById('emergency-modal');
        if (modal) {
            modal.remove();
            document.body.style.overflow = '';
        }
        console.log('✅ EMERGENCY: Emergency modal closed');
    };

    // Override any existing showNewsModal after a delay
    setTimeout(() => {
        if (!window.showNewsModal || typeof window.showNewsModal !== 'function') {
            console.log('🚨 EMERGENCY: showNewsModal not found, creating emergency version');
            window.showNewsModal = function(newsId) {
                console.log('🚨 EMERGENCY: Emergency showNewsModal called with ID:', newsId);
                createEmergencyModal(newsId);
            };
        }
    }, 3000);

    // Set up emergency click handlers
    setTimeout(() => {
        const buttons = document.querySelectorAll('.news-detail-btn, .news-read-btn, .btn-read-more');
        console.log(`🚨 EMERGENCY: Found ${buttons.length} news buttons, setting up emergency handlers`);

        buttons.forEach((button, index) => {
            const newsId = button.getAttribute('data-news-id');
            if (newsId) {
                // Add emergency click handler
                button.addEventListener('click', function(e) {
                    console.log(`🚨 EMERGENCY: Button ${index + 1} clicked, news ID: ${newsId}`);
                    
                    if (window.showNewsModal) {
                        window.showNewsModal(newsId);
                    } else {
                        createEmergencyModal(newsId);
                    }
                }, true);
            }
        });
    }, 1000);

    console.log('✅ EMERGENCY MODAL FIX: Loaded and ready');

})();