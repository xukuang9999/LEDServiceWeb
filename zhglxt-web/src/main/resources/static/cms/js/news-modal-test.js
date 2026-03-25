/**
 * News Modal Test - Simple verification
 * Tests if the news menu modal fix is working
 */

(function() {
    'use strict';

    console.log('🧪 News Modal Test loaded');

    // Simple test function
    function testNewsModal() {
        console.log('🧪 Testing news modal fix...');
        
        // Find first news button
        const newsButton = document.querySelector('.news-read-btn, .news-detail-btn, .btn-read-more');
        
        if (newsButton) {
            console.log('🧪 Found news button:', newsButton);
            console.log('🧪 Button data-target:', newsButton.getAttribute('data-target'));
            
            // Simulate click
            newsButton.click();
            
            // Check if independent modal was created
            setTimeout(() => {
                const independentModal = document.querySelector('[id^="independent-modal-"]');
                if (independentModal) {
                    console.log('✅ SUCCESS: Independent modal created!');
                    console.log('✅ Modal element:', independentModal);
                } else {
                    console.log('❌ FAILED: No independent modal found');
                }
            }, 100);
        } else {
            console.log('❌ No news buttons found on this page');
        }
    }

    // Export for console use
    window.testNewsModal = testNewsModal;

    // Auto-test after page loads (only on news pages)
    document.addEventListener('DOMContentLoaded', function() {
        // Check if this is a news page
        if (window.location.pathname.includes('news') || document.querySelector('.news-section')) {
            setTimeout(() => {
                console.log('🧪 Auto-testing news modal on news page...');
                // Don't auto-click, just log status
                const newsButtons = document.querySelectorAll('.news-read-btn, .news-detail-btn, .btn-read-more');
                console.log('🧪 Found', newsButtons.length, 'news buttons on this page');
                
                if (newsButtons.length > 0) {
                    console.log('🧪 Use testNewsModal() in console to test the fix');
                }
            }, 2000);
        }
    });

})();