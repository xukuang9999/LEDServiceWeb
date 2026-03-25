/**
 * Simple Modal System for Testing
 * Minimal implementation to ensure modals work
 */

(function() {
    'use strict';

    console.log('🔧 Loading Simple Modal System...');

    /**
     * Create a simple test modal
     */
    function createSimpleModal(newsId) {
        console.log('🔧 Creating simple modal for ID:', newsId);

        // Remove any existing modal
        const existing = document.getElementById('simple-test-modal');
        if (existing) {
            existing.remove();
        }

        // Create modal HTML
        const modalHTML = `
            <div id="simple-test-modal" style="
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
                visibility: visible !important;
                opacity: 1 !important;
            ">
                <div style="
                    background: white !important;
                    padding: 30px !important;
                    border-radius: 10px !important;
                    max-width: 600px !important;
                    max-height: 80vh !important;
                    overflow-y: auto !important;
                    box-shadow: 0 10px 30px rgba(0,0,0,0.5) !important;
                    position: relative !important;
                ">
                    <button id="simple-modal-close" style="
                        position: absolute !important;
                        top: 10px !important;
                        right: 15px !important;
                        background: #dc3545 !important;
                        color: white !important;
                        border: none !important;
                        width: 30px !important;
                        height: 30px !important;
                        border-radius: 50% !important;
                        cursor: pointer !important;
                        font-size: 16px !important;
                        display: flex !important;
                        align-items: center !important;
                        justify-content: center !important;
                    ">×</button>
                    
                    <h2 style="margin-top: 0; color: #333;">Simple Test Modal</h2>
                    <p style="color: #666; line-height: 1.6;">
                        This is a simple test modal for news ID: <strong>${newsId}</strong>
                    </p>
                    <p style="color: #666; line-height: 1.6;">
                        If you can see this modal, the basic modal system is working correctly.
                        The modal should be:
                    </p>
                    <ul style="color: #666; line-height: 1.6;">
                        <li>Visible and centered on screen</li>
                        <li>Have a dark overlay background</li>
                        <li>Be closeable with the × button</li>
                        <li>Be on top of all other content</li>
                    </ul>
                    
                    <div style="margin-top: 20px; text-align: center;">
                        <button id="simple-modal-ok" style="
                            background: #28a745 !important;
                            color: white !important;
                            border: none !important;
                            padding: 10px 20px !important;
                            border-radius: 5px !important;
                            cursor: pointer !important;
                            font-size: 14px !important;
                        ">OK, Modal Works!</button>
                    </div>
                </div>
            </div>
        `;

        // Add to DOM
        document.body.insertAdjacentHTML('beforeend', modalHTML);

        // Add event listeners
        const modal = document.getElementById('simple-test-modal');
        const closeBtn = document.getElementById('simple-modal-close');
        const okBtn = document.getElementById('simple-modal-ok');

        function closeModal() {
            if (modal) {
                // Get stored scroll position
                const scrollTop = parseInt(modal.getAttribute('data-scroll-top') || '0');
                const scrollLeft = parseInt(modal.getAttribute('data-scroll-left') || '0');
                
                // Remove modal
                modal.remove();
                
                // Restore body styles and scroll position
                document.body.style.overflow = '';
                document.body.style.position = '';
                document.body.style.top = '';
                document.body.style.left = '';
                document.body.style.width = '';
                
                // Restore scroll position
                window.scrollTo(scrollLeft, scrollTop);
            }
        }

        closeBtn.addEventListener('click', closeModal);
        okBtn.addEventListener('click', closeModal);
        
        // Close on overlay click
        modal.addEventListener('click', function(e) {
            if (e.target === modal) {
                closeModal();
            }
        });

        // Close on Escape key
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape') {
                closeModal();
            }
        });

        // Only apply scroll lock if not already applied by news handler
        if (!document.body.style.position || document.body.style.position !== 'fixed') {
            // Store current scroll position before preventing scroll
            const currentScrollTop = window.pageYOffset || document.documentElement.scrollTop;
            const currentScrollLeft = window.pageXOffset || document.documentElement.scrollLeft;
            
            // Prevent body scroll but maintain position
            document.body.style.overflow = 'hidden';
            document.body.style.position = 'fixed';
            document.body.style.top = `-${currentScrollTop}px`;
            document.body.style.left = `-${currentScrollLeft}px`;
            document.body.style.width = '100%';
            
            // Store scroll position for restoration
            modal.setAttribute('data-scroll-top', currentScrollTop);
            modal.setAttribute('data-scroll-left', currentScrollLeft);
        } else {
            // Use existing scroll position from news handler
            const existingTop = document.body.style.top.replace('-', '').replace('px', '') || '0';
            const existingLeft = document.body.style.left.replace('-', '').replace('px', '') || '0';
            modal.setAttribute('data-scroll-top', existingTop);
            modal.setAttribute('data-scroll-left', existingLeft);
        }

        console.log('✅ Simple modal created and displayed');
    }

    /**
     * Show simple modal
     */
    function showSimpleModal(newsId) {
        console.log('🔧 Showing simple modal for:', newsId);
        
        // Immediately prevent any scroll behavior
        const originalScrollBehavior = document.documentElement.style.scrollBehavior;
        document.documentElement.style.scrollBehavior = 'auto';
        
        // Store current scroll position BEFORE any DOM manipulation
        const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
        const scrollLeft = window.pageXOffset || document.documentElement.scrollLeft;
        
        console.log('🔧 Current scroll position:', { scrollTop, scrollLeft });
        
        // Create modal without any scroll restoration (it handles its own)
        createSimpleModal(newsId || 'test');
        
        // Restore original scroll behavior
        document.documentElement.style.scrollBehavior = originalScrollBehavior;
    }

    /**
     * Test function
     */
    function testSimpleModal() {
        console.log('🧪 Testing simple modal system');
        showSimpleModal('simple-test');
    }

    // Make functions globally available
    window.showSimpleModal = showSimpleModal;
    window.testSimpleModal = testSimpleModal;
    window.createSimpleModal = createSimpleModal;

    console.log('✅ Simple Modal System loaded');
    console.log('🧪 Use testSimpleModal() to test the modal');

})();