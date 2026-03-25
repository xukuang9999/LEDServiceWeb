/**
 * News Menu Interference Fix
 * Specifically handles Bootstrap modal interference on news menu pages
 */

(function() {
    'use strict';

    console.log('🛠️ Loading News Menu Interference Fix...');

    let isNewsMenuPage = false;
    let interferenceFixed = false;

    /**
     * Detect if we're on a news menu page
     */
    function detectNewsMenuPage() {
        const indicators = [
            '.news-grid',
            '.news-section',
            '.news-card',
            '[class*="news-"]'
        ];

        for (const indicator of indicators) {
            if (document.querySelector(indicator)) {
                isNewsMenuPage = true;
                break;
            }
        }

        // Also check URL
        if (window.location.pathname.includes('news') || 
            window.location.pathname.includes('article')) {
            isNewsMenuPage = true;
        }

        console.log('📍 News menu page detected:', isNewsMenuPage);
        return isNewsMenuPage;
    }

    /**
     * Completely disable Bootstrap modal system for news buttons
     */
    function disableBootstrapModalSystem() {
        console.log('🚫 Disabling Bootstrap modal system for news buttons...');

        // Override jQuery modal function if available
        if (typeof $ !== 'undefined' && $.fn.modal) {
            const originalModal = $.fn.modal;
            
            $.fn.modal = function(action) {
                const element = this[0];
                const modalId = element ? element.id : '';
                
                // Block all news-related modals
                if (modalId && modalId.startsWith('modal-')) {
                    console.log('🚫 Blocked Bootstrap modal:', modalId);
                    return this; // Return jQuery object but do nothing
                }
                
                // Allow other modals to work normally
                return originalModal.apply(this, arguments);
            };
        }

        // Remove Bootstrap modal attributes from news buttons
        const newsButtons = document.querySelectorAll(
            '.news-detail-btn, .news-read-btn, .btn-read-more, [data-toggle="modal"][data-target*="modal-"]'
        );

        newsButtons.forEach(button => {
            const dataTarget = button.getAttribute('data-target');
            
            // Only process buttons that target news modals
            if (dataTarget && dataTarget.includes('modal-')) {
                // Store original attributes
                button.setAttribute('data-original-toggle', button.getAttribute('data-toggle') || '');
                button.setAttribute('data-original-target', dataTarget);
                
                // Remove Bootstrap attributes
                button.removeAttribute('data-toggle');
                button.removeAttribute('data-target');
                
                // Add our identifier
                button.classList.add('interference-fixed');
                
                console.log('🔧 Fixed button interference:', button);
            }
        });

        console.log('✅ Bootstrap modal system disabled for news buttons');
    }

    /**
     * Hide all Bootstrap news modals
     */
    function hideBootstrapNewsModals() {
        console.log('👻 Hiding Bootstrap news modals...');

        const newsModals = document.querySelectorAll('.modal[id^="modal-"]');
        
        newsModals.forEach(modal => {
            // Hide the modal completely
            modal.style.display = 'none';
            modal.style.visibility = 'hidden';
            modal.style.opacity = '0';
            modal.style.pointerEvents = 'none';
            modal.style.position = 'absolute';
            modal.style.left = '-9999px';
            modal.style.top = '-9999px';
            modal.setAttribute('aria-hidden', 'true');
            
            // Remove Bootstrap classes that might interfere
            modal.classList.remove('show', 'fade');
            
            console.log('👻 Hidden modal:', modal.id);
        });

        // Also hide any modal backdrops
        const backdrops = document.querySelectorAll('.modal-backdrop');
        backdrops.forEach(backdrop => {
            backdrop.style.display = 'none';
            backdrop.style.visibility = 'hidden';
            backdrop.style.opacity = '0';
            backdrop.style.pointerEvents = 'none';
            backdrop.remove();
        });

        console.log('✅ Bootstrap news modals hidden');
    }

    /**
     * Override Bootstrap modal events
     */
    function overrideBootstrapEvents() {
        console.log('🔄 Overriding Bootstrap modal events...');

        // Prevent Bootstrap modal events from firing
        document.addEventListener('show.bs.modal', function(e) {
            const modalId = e.target.id;
            if (modalId && modalId.startsWith('modal-')) {
                console.log('🚫 Prevented Bootstrap modal show:', modalId);
                e.preventDefault();
                e.stopPropagation();
                e.stopImmediatePropagation();
                return false;
            }
        }, true);

        document.addEventListener('shown.bs.modal', function(e) {
            const modalId = e.target.id;
            if (modalId && modalId.startsWith('modal-')) {
                console.log('🚫 Prevented Bootstrap modal shown:', modalId);
                e.preventDefault();
                e.stopPropagation();
                e.stopImmediatePropagation();
                return false;
            }
        }, true);

        // Override click events on modal triggers
        document.addEventListener('click', function(e) {
            const button = e.target.closest('[data-original-target*="modal-"]');
            
            if (button) {
                console.log('🎯 Intercepted news button click:', button);
                e.preventDefault();
                e.stopPropagation();
                e.stopImmediatePropagation();
                
                // Extract news ID and trigger our modal
                const originalTarget = button.getAttribute('data-original-target');
                const newsId = originalTarget ? originalTarget.replace('#modal-', '') : 
                              button.getAttribute('data-news-id');
                
                if (newsId && window.optimizedNewsModal) {
                    console.log('🚀 Opening optimized modal for:', newsId);
                    window.optimizedNewsModal.open(newsId);
                } else if (window.showModal) {
                    window.showModal(newsId);
                }
                
                return false;
            }
        }, true);

        console.log('✅ Bootstrap modal events overridden');
    }

    /**
     * Apply comprehensive interference fix
     */
    function applyInterferenceFix() {
        if (interferenceFixed) return;

        console.log('🔧 Applying comprehensive interference fix...');

        disableBootstrapModalSystem();
        hideBootstrapNewsModals();
        overrideBootstrapEvents();

        // Add CSS to ensure our modals take precedence
        const style = document.createElement('style');
        style.textContent = `
            /* Force hide Bootstrap news modals */
            .modal[id^="modal-"] {
                display: none !important;
                visibility: hidden !important;
                opacity: 0 !important;
                pointer-events: none !important;
                position: absolute !important;
                left: -9999px !important;
                top: -9999px !important;
            }
            
            /* Hide modal backdrops */
            .modal-backdrop {
                display: none !important;
                visibility: hidden !important;
                opacity: 0 !important;
                pointer-events: none !important;
            }
            
            /* Ensure our buttons work */
            .interference-fixed {
                position: relative !important;
                z-index: 1000 !important;
                pointer-events: auto !important;
            }
            
            /* Prevent body scroll lock from Bootstrap */
            body.modal-open {
                overflow: auto !important;
                padding-right: 0 !important;
            }
        `;
        document.head.appendChild(style);

        interferenceFixed = true;
        console.log('✅ Comprehensive interference fix applied');
    }

    /**
     * Monitor for dynamically added content
     */
    function setupMutationObserver() {
        const observer = new MutationObserver(function(mutations) {
            let needsRefix = false;
            
            mutations.forEach(function(mutation) {
                mutation.addedNodes.forEach(function(node) {
                    if (node.nodeType === 1) { // Element node
                        // Check if new news buttons were added
                        if (node.matches && node.matches('.news-detail-btn, .news-read-btn, .btn-read-more')) {
                            needsRefix = true;
                        }
                        
                        // Check if new modals were added
                        if (node.matches && node.matches('.modal[id^="modal-"]')) {
                            needsRefix = true;
                        }
                        
                        // Check child elements
                        if (node.querySelector) {
                            if (node.querySelector('.news-detail-btn, .news-read-btn, .btn-read-more') ||
                                node.querySelector('.modal[id^="modal-"]')) {
                                needsRefix = true;
                            }
                        }
                    }
                });
            });
            
            if (needsRefix) {
                console.log('🔄 New content detected, reapplying interference fix...');
                setTimeout(applyInterferenceFix, 100);
            }
        });
        
        observer.observe(document.body, {
            childList: true,
            subtree: true
        });
        
        console.log('👁️ Mutation observer set up for dynamic content');
    }

    /**
     * Initialize the interference fix
     */
    function init() {
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', function() {
                setTimeout(() => {
                    if (detectNewsMenuPage()) {
                        applyInterferenceFix();
                        setupMutationObserver();
                    }
                }, 200);
            });
        } else {
            setTimeout(() => {
                if (detectNewsMenuPage()) {
                    applyInterferenceFix();
                    setupMutationObserver();
                }
            }, 200);
        }
    }

    // Export for debugging
    window.fixNewsMenuInterference = applyInterferenceFix;
    window.checkNewsMenuInterference = function() {
        return {
            isNewsMenuPage,
            interferenceFixed,
            newsButtons: document.querySelectorAll('.news-detail-btn, .news-read-btn, .btn-read-more').length,
            fixedButtons: document.querySelectorAll('.interference-fixed').length,
            hiddenModals: document.querySelectorAll('.modal[id^="modal-"]').length
        };
    };

    // Initialize
    init();

    console.log('✅ News Menu Interference Fix loaded');

})();