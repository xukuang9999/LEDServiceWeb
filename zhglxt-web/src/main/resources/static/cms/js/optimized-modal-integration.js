/**
 * Optimized Modal Integration
 * Ensures the optimized modal system works properly with all news buttons
 */

(function() {
    'use strict';

    console.log('🚀 Loading Optimized Modal Integration...');

    let isInitialized = false;
    let modalSystem = null;

    /**
     * Initialize the modal integration system
     */
    function initializeModalIntegration() {
        if (isInitialized) return;

        console.log('🔧 Initializing optimized modal integration...');

        // Wait for the optimized modal system to be available
        waitForModalSystem();
    }

    /**
     * Wait for the optimized modal system to load
     */
    function waitForModalSystem() {
        if (window.optimizedNewsModal) {
            modalSystem = window.optimizedNewsModal;
            setupEventHandlers();
            setupCompatibilityLayer();
            isInitialized = true;
            console.log('✅ Optimized modal integration ready');
        } else {
            setTimeout(waitForModalSystem, 100);
        }
    }

    /**
     * Setup event handlers for news buttons
     */
    function setupEventHandlers() {
        console.log('🎯 Setting up optimized modal event handlers...');

        // Disable Bootstrap modal functionality for our buttons
        disableBootstrapModalInterference();

        // Use event delegation to handle all news buttons with high priority
        document.addEventListener('click', function(e) {
            const button = e.target.closest('.news-detail-btn, .news-read-btn, .btn-read-more');
            
            if (button) {
                // Immediately stop all event propagation to prevent Bootstrap interference
                e.preventDefault();
                e.stopPropagation();
                e.stopImmediatePropagation();

                // Get news ID from various possible attributes
                let newsId = button.getAttribute('data-news-id') ||
                           button.getAttribute('data-target')?.replace('#modal-', '') ||
                           button.getAttribute('data-id');

                if (!newsId) {
                    // Try to extract from onclick attribute
                    const onclick = button.getAttribute('onclick');
                    if (onclick) {
                        const match = onclick.match(/['"]([^'"]+)['"]/);
                        if (match) newsId = match[1];
                    }
                }

                if (newsId) {
                    console.log('🎯 Opening optimized modal for news ID:', newsId);
                    
                    // Small delay to ensure all other handlers are bypassed
                    setTimeout(() => {
                        modalSystem.open(newsId);
                    }, 10);
                } else {
                    console.error('❌ No news ID found on button:', button);
                    showErrorMessage('Unable to load news article. Missing article ID.');
                }

                return false;
            }
        }, true); // Use capture phase to intercept before Bootstrap

        console.log('✅ Event handlers set up successfully');
    }

    /**
     * Disable Bootstrap modal interference
     */
    function disableBootstrapModalInterference() {
        console.log('🚫 Disabling Bootstrap modal interference...');

        // Override Bootstrap modal show function for our modals
        if (typeof $ !== 'undefined' && $.fn.modal) {
            const originalModal = $.fn.modal;
            
            $.fn.modal = function(action) {
                // Check if this is one of our news modals
                const modalId = this.attr('id');
                if (modalId && modalId.startsWith('modal-')) {
                    console.log('🚫 Intercepted Bootstrap modal call for:', modalId);
                    
                    // Extract news ID and use our system instead
                    const newsId = modalId.replace('modal-', '');
                    if (modalSystem) {
                        modalSystem.open(newsId);
                    }
                    return this; // Return jQuery object for chaining
                }
                
                // For non-news modals, use original Bootstrap functionality
                return originalModal.apply(this, arguments);
            };
        }

        // Remove data-toggle and data-target attributes that cause Bootstrap interference
        setTimeout(() => {
            const newsButtons = document.querySelectorAll('.news-detail-btn, .news-read-btn, .btn-read-more');
            newsButtons.forEach(button => {
                if (button.getAttribute('data-news-id')) {
                    // Store original attributes for reference
                    const originalToggle = button.getAttribute('data-toggle');
                    const originalTarget = button.getAttribute('data-target');
                    
                    if (originalToggle) {
                        button.setAttribute('data-original-toggle', originalToggle);
                        button.removeAttribute('data-toggle');
                    }
                    
                    if (originalTarget) {
                        button.setAttribute('data-original-target', originalTarget);
                        button.removeAttribute('data-target');
                    }
                    
                    // Add our custom class for identification
                    button.classList.add('optimized-modal-trigger');
                    
                    console.log('🔧 Cleaned Bootstrap attributes from button:', button);
                }
            });
        }, 500);

        console.log('✅ Bootstrap interference disabled');
    }

    /**
     * Setup compatibility layer for existing modal functions
     */
    function setupCompatibilityLayer() {
        console.log('🔗 Setting up compatibility layer...');

        // Override existing modal functions
        const originalFunctions = {};

        // Store original functions if they exist
        if (window.showModal) originalFunctions.showModal = window.showModal;
        if (window.showNewsModal) originalFunctions.showNewsModal = window.showNewsModal;
        if (window.openNewsModal) originalFunctions.openNewsModal = window.openNewsModal;
        if (window.showIndependentModal) originalFunctions.showIndependentModal = window.showIndependentModal;

        // Create unified modal function
        window.showModal = function(newsId, title, content) {
            console.log('📢 showModal called with:', { newsId, title, content });
            
            if (modalSystem) {
                if (title && content) {
                    modalSystem.open({ id: newsId, title, content, createTime: new Date().toLocaleDateString() });
                } else {
                    modalSystem.open(newsId);
                }
            } else {
                console.error('❌ Modal system not available');
                showErrorMessage('Modal system not available. Please refresh the page.');
            }
        };

        // Override other modal functions to use optimized system
        window.showNewsModal = window.showModal;
        window.openNewsModal = function(title, content) {
            window.showModal(null, title, content);
        };
        window.showOptimizedModal = window.showModal;
        window.showIndependentModal = window.showModal;

        // Create test function
        window.testOptimizedModal = function() {
            const testData = {
                id: 'test-optimized',
                title: 'Optimized Modal Test',
                content: `
                    <h3>🚀 Optimized Modal System Test</h3>
                    <p>This is a test of the optimized modal system with the following features:</p>
                    <ul>
                        <li><strong>Drag and Drop:</strong> Click and drag the header to move the modal</li>
                        <li><strong>Maximize/Restore:</strong> Click the maximize button or double-click the header</li>
                        <li><strong>Keyboard Shortcuts:</strong> 
                            <ul>
                                <li>ESC - Close modal</li>
                                <li>F11 - Toggle maximize</li>
                                <li>F5 - Minimize</li>
                            </ul>
                        </li>
                        <li><strong>Share Function:</strong> Click share to copy link or use native sharing</li>
                        <li><strong>Print Function:</strong> Click print to open print-friendly version</li>
                        <li><strong>Responsive Design:</strong> Adapts to different screen sizes</li>
                    </ul>
                    <p>The modal is optimized for performance and user experience with smooth animations and proper event handling.</p>
                    <h4>Technical Features:</h4>
                    <ul>
                        <li>Event delegation for reliable button handling</li>
                        <li>Compatibility layer for existing modal functions</li>
                        <li>Automatic data extraction from existing modals</li>
                        <li>Fallback error handling</li>
                        <li>Memory leak prevention</li>
                    </ul>
                `,
                createTime: new Date().toLocaleDateString(),
                imageUrl: null
            };
            
            modalSystem.open(testData);
        };

        console.log('✅ Compatibility layer established');
    }

    /**
     * Show error message to user
     */
    function showErrorMessage(message) {
        // Create a simple error notification
        const notification = document.createElement('div');
        notification.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            background: #dc3545;
            color: white;
            padding: 12px 20px;
            border-radius: 6px;
            z-index: 10002;
            font-size: 14px;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
            max-width: 300px;
        `;
        notification.textContent = message;
        
        document.body.appendChild(notification);
        
        setTimeout(() => {
            notification.remove();
        }, 5000);
    }

    /**
     * Create debug panel for testing
     */
    function createDebugPanel() {
        const panel = document.createElement('div');
        panel.id = 'optimized-modal-debug';
        panel.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            background: white;
            border: 2px solid #007bff;
            border-radius: 8px;
            padding: 15px;
            z-index: 9999;
            font-family: Arial, sans-serif;
            font-size: 14px;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
            max-width: 250px;
        `;

        panel.innerHTML = `
            <div style="font-weight: bold; margin-bottom: 10px; color: #007bff;">
                🚀 Optimized Modal Debug
            </div>
            <div style="margin-bottom: 8px;">
                Status: <span style="color: ${isInitialized ? 'green' : 'red'};">
                    ${isInitialized ? '✅ Ready' : '❌ Not Ready'}
                </span>
            </div>
            <div style="margin-bottom: 12px;">
                Buttons: <span id="button-count">Scanning...</span>
            </div>
            <button onclick="testOptimizedModal()" style="
                background: #007bff;
                color: white;
                border: none;
                padding: 6px 12px;
                border-radius: 4px;
                cursor: pointer;
                margin-right: 5px;
                font-size: 12px;
            ">Test Modal</button>
            <button onclick="document.getElementById('optimized-modal-debug').remove()" style="
                background: #6c757d;
                color: white;
                border: none;
                padding: 6px 12px;
                border-radius: 4px;
                cursor: pointer;
                font-size: 12px;
            ">Close</button>
        `;

        document.body.appendChild(panel);

        // Update button count
        setTimeout(() => {
            const buttons = document.querySelectorAll('.news-detail-btn, .news-read-btn, .btn-read-more');
            const countElement = document.getElementById('button-count');
            if (countElement) {
                countElement.textContent = buttons.length;
                countElement.style.color = buttons.length > 0 ? 'green' : 'red';
            }
        }, 1000);
    }

    /**
     * Check system status
     */
    function checkSystemStatus() {
        const status = {
            initialized: isInitialized,
            modalSystemAvailable: !!window.optimizedNewsModal,
            newsButtons: document.querySelectorAll('.news-detail-btn, .news-read-btn, .btn-read-more').length,
            existingModals: document.querySelectorAll('[id^="modal-"]').length
        };

        console.log('📊 Optimized Modal System Status:', status);
        return status;
    }

    // Export functions for debugging
    window.createOptimizedModalDebugPanel = createDebugPanel;
    window.checkOptimizedModalStatus = checkSystemStatus;
    
    // Global function for direct button calls
    window.showNewsModal = function(newsId) {
        console.log('🎯 showNewsModal called with ID:', newsId);
        if (modalSystem) {
            modalSystem.open(newsId);
        } else {
            console.error('❌ Modal system not available');
            showErrorMessage('Modal system not ready. Please wait a moment and try again.');
        }
    };

    /**
     * Handle news menu page specific interference
     */
    function handleNewsMenuPageInterference() {
        console.log('🔧 Handling news menu page interference...');

        // Detect if we're on a news menu page
        const isNewsMenuPage = document.querySelector('.news-grid') || 
                              document.querySelector('.news-section') ||
                              window.location.pathname.includes('news');

        if (isNewsMenuPage) {
            console.log('📰 News menu page detected, applying interference fixes...');

            // Disable all existing modal event handlers
            const existingModals = document.querySelectorAll('.modal.news-modal');
            existingModals.forEach(modal => {
                // Hide the modal to prevent Bootstrap from showing it
                modal.style.display = 'none !important';
                modal.setAttribute('aria-hidden', 'true');
                
                // Remove Bootstrap event listeners
                if (typeof $ !== 'undefined') {
                    $(modal).off('show.bs.modal shown.bs.modal hide.bs.modal hidden.bs.modal');
                }
            });

            // Override any existing click handlers on news buttons
            const newsButtons = document.querySelectorAll('.news-detail-btn, .news-read-btn, .btn-read-more');
            newsButtons.forEach(button => {
                // Clone the button to remove all existing event listeners
                const newButton = button.cloneNode(true);
                button.parentNode.replaceChild(newButton, button);
                
                // Add our optimized handler
                newButton.addEventListener('click', function(e) {
                    e.preventDefault();
                    e.stopPropagation();
                    e.stopImmediatePropagation();
                    
                    const newsId = this.getAttribute('data-news-id') ||
                                  this.getAttribute('data-target')?.replace('#modal-', '');
                    
                    if (newsId && modalSystem) {
                        console.log('🎯 News menu button clicked, opening modal for:', newsId);
                        modalSystem.open(newsId);
                    }
                    
                    return false;
                }, true);
                
                console.log('🔄 Replaced button with optimized handler:', newButton);
            });

            // Prevent Bootstrap modal backdrop clicks
            document.addEventListener('click', function(e) {
                if (e.target.classList.contains('modal-backdrop')) {
                    e.preventDefault();
                    e.stopPropagation();
                    e.stopImmediatePropagation();
                    return false;
                }
            }, true);
        }

        console.log('✅ News menu page interference handling complete');
    }

    /**
     * Initialize when DOM is ready
     */
    function init() {
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', function() {
                setTimeout(() => {
                    initializeModalIntegration();
                    handleNewsMenuPageInterference();
                }, 100);
            });
        } else {
            setTimeout(() => {
                initializeModalIntegration();
                handleNewsMenuPageInterference();
            }, 100);
        }

        // Auto-create debug panel in development
        if (window.location.hostname === 'localhost' || window.location.hostname.includes('dev')) {
            setTimeout(createDebugPanel, 2000);
        }
    }

    // Start initialization
    init();

    console.log('✅ Optimized Modal Integration script loaded');

})();