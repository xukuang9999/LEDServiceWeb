/**
 * Modal Emergency Fallback
 * Ensures showNewsModal function is always available
 */

(function() {
    'use strict';

    console.log('🚨 Loading Modal Emergency Fallback...');

    // Ensure showNewsModal function exists immediately
    if (!window.showNewsModal) {
        window.showNewsModal = function(newsId) {
            console.log('🚨 Emergency fallback: showNewsModal called with ID:', newsId);
            
            // Try optimized modal first
            if (window.optimizedNewsModal) {
                window.optimizedNewsModal.open(newsId);
                return;
            }
            
            // Try Bootstrap modal fallback
            if (typeof $ !== 'undefined' && $.fn.modal) {
                const modal = document.getElementById('modal-' + newsId);
                if (modal) {
                    $(modal).modal('show');
                    return;
                }
            }
            
            // Final fallback - show alert
            alert('News article loading... Please try again in a moment.');
        };
    }

    // Monitor for modal system availability
    let checkCount = 0;
    const maxChecks = 50; // 5 seconds max
    
    function checkModalSystem() {
        checkCount++;
        
        if (window.optimizedNewsModal) {
            console.log('✅ Optimized modal system detected, updating showNewsModal');
            window.showNewsModal = function(newsId) {
                console.log('🎯 showNewsModal (optimized) called with ID:', newsId);
                window.optimizedNewsModal.open(newsId);
            };
            return;
        }
        
        if (checkCount < maxChecks) {
            setTimeout(checkModalSystem, 100);
        } else {
            console.warn('⚠️ Modal system not detected after 5 seconds, using fallback');
        }
    }
    
    // Start monitoring
    setTimeout(checkModalSystem, 100);

    console.log('✅ Modal Emergency Fallback loaded');

})();