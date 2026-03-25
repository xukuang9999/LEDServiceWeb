/**
 * Page Restore Script
 * Restores basic page functionality by removing any problematic elements
 */

(function() {
    'use strict';

    console.log('🔧 Page Restore Script loading...');

    /**
     * Force restore page functionality
     */
    function restorePageFunctionality() {
        console.log('🔧 Restoring page functionality...');
        
        // Remove ALL modal elements
        const allModals = document.querySelectorAll('[id*="modal"], [class*="modal"], [id*="Modal"], [class*="Modal"]');
        allModals.forEach(modal => {
            if (modal.id && (modal.id.includes('independent') || modal.id.includes('emergency'))) {
                console.log('🔧 Removing problematic modal:', modal.id);
                modal.remove();
            }
        });
        
        // Force restore body styles
        document.body.style.overflow = '';
        document.body.style.position = '';
        document.body.style.top = '';
        document.body.style.left = '';
        document.body.style.width = '';
        document.body.style.height = '';
        document.body.style.transform = '';
        document.body.style.margin = '';
        document.body.style.padding = '';
        
        // Remove modal-related classes
        document.body.classList.remove('modal-open', 'modal-locked', 'no-scroll');
        
        // Force enable scrolling on html element too
        document.documentElement.style.overflow = '';
        document.documentElement.style.position = '';
        document.documentElement.style.height = '';
        
        console.log('🔧 Page functionality restored');
    }

    /**
     * Simple news modal that just uses Bootstrap (no custom positioning)
     */
    function showSimpleBootstrapModal(newsId) {
        console.log('🔧 Showing simple Bootstrap modal for:', newsId);
        
        // Find the existing Bootstrap modal
        const bootstrapModal = document.getElementById('modal-' + newsId);
        if (bootstrapModal && window.$ && $.fn.modal) {
            // Use standard Bootstrap modal
            $(bootstrapModal).modal('show');
            console.log('🔧 Bootstrap modal shown');
        } else {
            console.log('🔧 Bootstrap modal not found or jQuery not available');
        }
    }

    /**
     * Override any problematic modal functions
     */
    function overrideModalFunctions() {
        // Override any global modal functions that might cause issues
        window.showIndependentNewsModal = showSimpleBootstrapModal;
        window.createIndependentModal = showSimpleBootstrapModal;
        window.showSuperModal = showSimpleBootstrapModal;
        
        console.log('🔧 Modal functions overridden with safe Bootstrap versions');
    }

    /**
     * Initialize page restoration
     */
    function init() {
        // Immediate restoration
        restorePageFunctionality();
        
        // Override modal functions
        overrideModalFunctions();
        
        // Set up periodic cleanup (in case something tries to break the page again)
        setInterval(restorePageFunctionality, 5000);
        
        console.log('🔧 Page restoration initialized');
    }

    // Run immediately
    init();

    // Also run when DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    }

    // Make restore function globally available
    window.restorePageFunctionality = restorePageFunctionality;

    console.log('✅ Page Restore Script loaded');

})();