/**
 * Modal Integration Script
 * Ensures all modal systems work together properly
 */

(function() {
    'use strict';

    console.log('🔗 Loading Modal Integration System...');

    // Configuration
    const config = {
        debug: true,
        fallbackToSimple: true,
        autoInit: true,
        retryAttempts: 3,
        retryDelay: 1000
    };

    // State tracking
    let initialized = false;
    let retryCount = 0;

    /**
     * Debug logging
     */
    function log(message, data = null) {
        if (config.debug) {
            console.log('🔗 Modal Integration:', message, data || '');
        }
    }

    /**
     * Check if all required systems are loaded
     */
    function checkSystemsReady() {
        const systems = {
            jquery: typeof $ !== 'undefined',
            bootstrap: typeof $ !== 'undefined' && typeof $.fn.modal !== 'undefined',
            simpleModal: typeof window.showSimpleModal === 'function',
            homeModal: typeof window.showHomeNewsModal === 'function',
            independentModal: typeof window.showIndependentNewsModal === 'function',
            statusChecker: typeof window.checkModalStatus === 'function'
        };

        log('Systems check:', systems);

        // At minimum, we need the simple modal system
        return systems.simpleModal;
    }

    /**
     * Initialize modal integration
     */
    function initializeIntegration() {
        if (initialized) {
            log('Already initialized');
            return true;
        }

        log('Initializing modal integration...');

        if (!checkSystemsReady()) {
            log('Systems not ready, will retry...');
            if (retryCount < config.retryAttempts) {
                retryCount++;
                setTimeout(initializeIntegration, config.retryDelay);
                return false;
            } else {
                log('Failed to initialize after maximum retries');
                return false;
            }
        }

        // Set up unified modal function
        setupUnifiedModalFunction();

        // Set up event handlers
        setupEventHandlers();

        // Override existing functions for compatibility
        setupCompatibilityLayer();

        initialized = true;
        log('Modal integration initialized successfully');
        return true;
    }

    /**
     * Set up unified modal function
     */
    function setupUnifiedModalFunction() {
        window.showModal = function(newsId, options = {}) {
            log('Unified showModal called with:', newsId, options);

            // Try different modal systems in order of preference
            if (options.forceSimple || config.fallbackToSimple) {
                if (window.showSimpleModal) {
                    log('Using simple modal system');
                    return window.showSimpleModal(newsId);
                }
            }

            if (window.showHomeNewsModal) {
                log('Using home modal system');
                return window.showHomeNewsModal(newsId);
            }

            if (window.showIndependentNewsModal) {
                log('Using independent modal system');
                return window.showIndependentNewsModal(newsId);
            }

            // Fallback to simple modal
            if (window.showSimpleModal) {
                log('Falling back to simple modal system');
                return window.showSimpleModal(newsId);
            }

            log('No modal system available');
            alert('Unable to open modal. Please refresh the page and try again.');
        };

        log('Unified modal function created');
    }

    /**
     * Set up event handlers
     */
    function setupEventHandlers() {
        log('Setting up event handlers...');

        // Use event delegation for better compatibility
        document.addEventListener('click', function(e) {
            // Check if clicked element is a news button
            const button = e.target.closest('.news-detail-btn, .news-read-btn, .btn-read-more');
            
            if (button) {
                // Prevent default behavior and stop propagation
                e.preventDefault();
                e.stopPropagation();
                
                // Store current scroll position
                const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
                const scrollLeft = window.pageXOffset || document.documentElement.scrollLeft;

                log('News button clicked:', button);

                // Get news ID from various possible attributes
                let newsId = button.getAttribute('data-news-id') ||
                           button.getAttribute('data-target')?.replace('#modal-', '') ||
                           button.getAttribute('data-id') ||
                           'unknown';

                log('Extracted news ID:', newsId);

                // Show modal using unified function
                window.showModal(newsId);
                
                // Ensure scroll position is maintained after modal opens
                setTimeout(() => {
                    window.scrollTo(scrollLeft, scrollTop);
                }, 50);
            }
        });

        log('Event handlers set up');
    }

    /**
     * Set up compatibility layer
     */
    function setupCompatibilityLayer() {
        log('Setting up compatibility layer...');

        // Override common modal function names
        const originalShowModalById = window.showModalById;
        window.showModalById = function(newsId) {
            log('showModalById called (compatibility):', newsId);
            return window.showModal(newsId);
        };

        const originalOpenModal = window.openModal;
        window.openModal = function(newsId) {
            log('openModal called (compatibility):', newsId);
            return window.showModal(newsId);
        };

        // Create test functions
        window.testModalIntegration = function() {
            log('Testing modal integration...');
            window.showModal('integration-test');
        };

        window.testAllModalSystems = function() {
            log('Testing all modal systems...');
            
            setTimeout(() => {
                if (window.showSimpleModal) {
                    log('Testing simple modal...');
                    window.showSimpleModal('simple-test');
                }
            }, 500);

            setTimeout(() => {
                if (window.showHomeNewsModal) {
                    log('Testing home modal...');
                    window.showHomeNewsModal('home-test');
                }
            }, 1500);

            setTimeout(() => {
                if (window.showIndependentNewsModal) {
                    log('Testing independent modal...');
                    window.showIndependentNewsModal('independent-test');
                }
            }, 2500);
        };

        log('Compatibility layer set up');
    }

    /**
     * Create integration status indicator
     */
    function createStatusIndicator() {
        // Remove existing indicator
        const existing = document.getElementById('modal-integration-status');
        if (existing) {
            existing.remove();
        }

        const indicator = document.createElement('div');
        indicator.id = 'modal-integration-status';
        indicator.style.cssText = `
            position: fixed !important;
            bottom: 10px !important;
            left: 10px !important;
            background: ${initialized ? '#28a745' : '#dc3545'} !important;
            color: white !important;
            padding: 8px 12px !important;
            border-radius: 6px !important;
            z-index: 999997 !important;
            font-family: monospace !important;
            font-size: 11px !important;
            cursor: pointer !important;
            box-shadow: 0 2px 8px rgba(0,0,0,0.2) !important;
        `;

        indicator.innerHTML = `
            🔗 Modal Integration: ${initialized ? 'Ready' : 'Failed'}
        `;

        indicator.addEventListener('click', function() {
            if (window.checkModalStatus) {
                window.checkModalStatus();
            } else {
                console.log('Modal Integration Status:', {
                    initialized,
                    retryCount,
                    systemsReady: checkSystemsReady()
                });
            }
        });

        document.body.appendChild(indicator);

        // Auto-hide after 5 seconds if successful
        if (initialized) {
            setTimeout(() => {
                if (indicator && indicator.parentNode) {
                    indicator.style.opacity = '0.3';
                }
            }, 5000);
        }
    }

    /**
     * Initialize when DOM is ready
     */
    function init() {
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', function() {
                setTimeout(initializeIntegration, 500);
            });
        } else {
            setTimeout(initializeIntegration, 500);
        }

        // Create status indicator after initialization
        setTimeout(() => {
            createStatusIndicator();
        }, 2000);
    }

    // Start initialization if auto-init is enabled
    if (config.autoInit) {
        init();
    }

    // Export functions for manual initialization
    window.initModalIntegration = initializeIntegration;
    window.checkModalIntegration = function() {
        return {
            initialized,
            retryCount,
            systemsReady: checkSystemsReady(),
            config
        };
    };

    log('Modal Integration System loaded');

})();