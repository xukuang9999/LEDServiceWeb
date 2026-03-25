/**
 * Modal System Status Checker
 * Helps debug modal system issues
 */

(function() {
    'use strict';

    console.log('🔍 Loading Modal Status Checker...');

    /**
     * Check modal system status
     */
    function checkModalStatus() {
        console.log('🔍 === MODAL SYSTEM STATUS CHECK ===');
        
        // Check if jQuery is loaded
        console.log('jQuery loaded:', typeof $ !== 'undefined' ? '✅ Yes' : '❌ No');
        if (typeof $ !== 'undefined') {
            console.log('jQuery version:', $.fn.jquery);
        }
        
        // Check if Bootstrap is loaded
        console.log('Bootstrap modal:', typeof $.fn.modal !== 'undefined' ? '✅ Yes' : '❌ No');
        
        // Check modal functions
        console.log('showHomeNewsModal:', typeof window.showHomeNewsModal !== 'undefined' ? '✅ Available' : '❌ Missing');
        console.log('showIndependentNewsModal:', typeof window.showIndependentNewsModal !== 'undefined' ? '✅ Available' : '❌ Missing');
        console.log('showSimpleModal:', typeof window.showSimpleModal !== 'undefined' ? '✅ Available' : '❌ Missing');
        console.log('testSimpleModal:', typeof window.testSimpleModal !== 'undefined' ? '✅ Available' : '❌ Missing');
        
        // Check for news buttons
        const newsButtons = document.querySelectorAll('.news-detail-btn, .news-read-btn, .independent-modal-trigger');
        console.log('News buttons found:', newsButtons.length);
        
        if (newsButtons.length > 0) {
            console.log('First button details:');
            const firstBtn = newsButtons[0];
            console.log('  - Classes:', firstBtn.className);
            console.log('  - data-news-id:', firstBtn.getAttribute('data-news-id'));
            console.log('  - data-target:', firstBtn.getAttribute('data-target'));
            console.log('  - data-toggle:', firstBtn.getAttribute('data-toggle'));
        }
        
        // Check for existing modals
        const existingModals = document.querySelectorAll('.modal, .independent-modal-overlay, .home-news-modal-overlay');
        console.log('Existing modals found:', existingModals.length);
        
        // Check CSS files
        const cssFiles = Array.from(document.querySelectorAll('link[rel="stylesheet"]'))
            .map(link => link.href)
            .filter(href => href.includes('modal'));
        console.log('Modal CSS files loaded:', cssFiles.length);
        cssFiles.forEach(file => console.log('  -', file));
        
        // Check JS files
        const jsFiles = Array.from(document.querySelectorAll('script[src]'))
            .map(script => script.src)
            .filter(src => src.includes('modal'));
        console.log('Modal JS files loaded:', jsFiles.length);
        jsFiles.forEach(file => console.log('  -', file));
        
        console.log('🔍 === END STATUS CHECK ===');
        
        return {
            jquery: typeof $ !== 'undefined',
            bootstrap: typeof $.fn.modal !== 'undefined',
            homeModal: typeof window.showHomeNewsModal !== 'undefined',
            independentModal: typeof window.showIndependentNewsModal !== 'undefined',
            simpleModal: typeof window.showSimpleModal !== 'undefined',
            newsButtons: newsButtons.length,
            existingModals: existingModals.length
        };
    }

    /**
     * Test all available modal systems
     */
    function testAllModals() {
        console.log('🧪 Testing all available modal systems...');
        
        const status = checkModalStatus();
        
        if (status.simpleModal) {
            console.log('🧪 Testing simple modal...');
            setTimeout(() => window.testSimpleModal(), 500);
        } else if (status.homeModal) {
            console.log('🧪 Testing home modal...');
            setTimeout(() => window.showHomeNewsModal('test'), 500);
        } else if (status.independentModal) {
            console.log('🧪 Testing independent modal...');
            setTimeout(() => window.showIndependentNewsModal('test'), 500);
        } else {
            console.log('❌ No modal systems available to test');
        }
    }

    /**
     * Force trigger modal for first news item
     */
    function triggerFirstNewsModal() {
        console.log('🎯 Attempting to trigger first news modal...');
        
        const newsButtons = document.querySelectorAll('.news-detail-btn, .news-read-btn, .independent-modal-trigger');
        
        if (newsButtons.length > 0) {
            const firstBtn = newsButtons[0];
            console.log('🎯 Clicking first news button:', firstBtn);
            
            // Try to get news ID
            let newsId = firstBtn.getAttribute('data-news-id');
            if (!newsId) {
                const dataTarget = firstBtn.getAttribute('data-target');
                if (dataTarget) {
                    newsId = dataTarget.replace('#modal-', '');
                }
            }
            
            console.log('🎯 News ID:', newsId);
            
            if (newsId) {
                // Try different modal functions
                if (window.showSimpleModal) {
                    window.showSimpleModal(newsId);
                } else if (window.showHomeNewsModal) {
                    window.showHomeNewsModal(newsId);
                } else if (window.showIndependentNewsModal) {
                    window.showIndependentNewsModal(newsId);
                } else {
                    console.log('🎯 Simulating button click...');
                    firstBtn.click();
                }
            } else {
                console.log('🎯 Simulating button click without ID...');
                firstBtn.click();
            }
        } else {
            console.log('❌ No news buttons found to trigger');
        }
    }

    /**
     * Create debug panel
     */
    function createDebugPanel() {
        // Remove existing panel
        const existing = document.getElementById('modal-debug-panel');
        if (existing) {
            existing.remove();
        }

        const panel = document.createElement('div');
        panel.id = 'modal-debug-panel';
        panel.style.cssText = `
            position: fixed !important;
            top: 10px !important;
            right: 10px !important;
            background: #333 !important;
            color: white !important;
            padding: 15px !important;
            border-radius: 8px !important;
            z-index: 999998 !important;
            font-family: monospace !important;
            font-size: 12px !important;
            max-width: 300px !important;
            box-shadow: 0 4px 12px rgba(0,0,0,0.3) !important;
        `;

        panel.innerHTML = `
            <div style="font-weight: bold; margin-bottom: 10px; color: #4CAF50;">Modal Debug Panel</div>
            <button onclick="checkModalStatus()" style="margin: 2px; padding: 5px 8px; font-size: 11px; background: #2196F3; color: white; border: none; border-radius: 3px; cursor: pointer;">Check Status</button>
            <button onclick="testAllModals()" style="margin: 2px; padding: 5px 8px; font-size: 11px; background: #FF9800; color: white; border: none; border-radius: 3px; cursor: pointer;">Test Modals</button>
            <button onclick="triggerFirstNewsModal()" style="margin: 2px; padding: 5px 8px; font-size: 11px; background: #4CAF50; color: white; border: none; border-radius: 3px; cursor: pointer;">Trigger News</button>
            <button onclick="testSimpleModal()" style="margin: 2px; padding: 5px 8px; font-size: 11px; background: #9C27B0; color: white; border: none; border-radius: 3px; cursor: pointer;">Simple Test</button>
            <button onclick="document.getElementById('modal-debug-panel').remove()" style="margin: 2px; padding: 5px 8px; font-size: 11px; background: #F44336; color: white; border: none; border-radius: 3px; cursor: pointer;">Close</button>
        `;

        document.body.appendChild(panel);
        console.log('🔧 Debug panel created');
    }

    // Make functions globally available
    window.checkModalStatus = checkModalStatus;
    window.testAllModals = testAllModals;
    window.triggerFirstNewsModal = triggerFirstNewsModal;
    window.createDebugPanel = createDebugPanel;

    // Auto-create debug panel after page loads
    document.addEventListener('DOMContentLoaded', function() {
        setTimeout(() => {
            createDebugPanel();
            checkModalStatus();
        }, 2000);
    });

    console.log('✅ Modal Status Checker loaded');
    console.log('🔧 Functions available: checkModalStatus(), testAllModals(), triggerFirstNewsModal(), createDebugPanel()');

})();