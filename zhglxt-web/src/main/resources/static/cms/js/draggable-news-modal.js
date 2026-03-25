/**
 * Draggable News Modal
 * Provides drag, maximize/restore, and centered modal functionality
 */

(function() {
    'use strict';

    console.log('🎯 Loading Draggable News Modal...');

    let isDragging = false;
    let isMaximized = false;
    let dragOffset = { x: 0, y: 0 };
    let originalPosition = null;
    let originalSize = null;

    /**
     * Initialize draggable functionality for all news modals
     */
    function initializeDraggableModals() {
        // Wait for DOM to be ready
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', setupDraggableModals);
        } else {
            setupDraggableModals();
        }
    }

    /**
     * Setup draggable functionality for all news modals
     */
    function setupDraggableModals() {
        console.log('🔧 Setting up draggable news modals...');

        // Find all news modals
        const newsModals = document.querySelectorAll('.modal[id^="modal-"], .news-modal');
        
        newsModals.forEach(modal => {
            setupModalDragAndResize(modal);
        });

        // Setup mutation observer for dynamically added modals
        setupMutationObserver();

        console.log(`✅ Initialized ${newsModals.length} draggable news modals`);
    }

    /**
     * Setup drag and resize functionality for a specific modal
     */
    function setupModalDragAndResize(modal) {
        const modalDialog = modal.querySelector('.modal-dialog');
        const modalHeader = modal.querySelector('.modal-header');
        
        if (!modalDialog || !modalHeader) {
            console.warn('⚠️ Modal structure incomplete, skipping:', modal.id);
            return;
        }

        // Add draggable class
        modalHeader.classList.add('draggable-header');
        modalHeader.style.cursor = 'move';

        // Add maximize/restore button if not exists
        addMaximizeButton(modal);

        // Setup drag events
        modalHeader.addEventListener('mousedown', (e) => startDrag(e, modalDialog));
        
        // Setup double-click to maximize
        modalHeader.addEventListener('dblclick', () => toggleMaximize(modal));

        // Prevent text selection during drag
        modalHeader.addEventListener('selectstart', (e) => e.preventDefault());

        console.log('🎯 Setup complete for modal:', modal.id);
    }

    /**
     * Add maximize/restore button to modal header
     */
    function addMaximizeButton(modal) {
        const modalHeader = modal.querySelector('.modal-header');
        const existingClose = modalHeader.querySelector('.close, .btn-close, [data-dismiss="modal"]');
        
        if (modalHeader.querySelector('.maximize-btn')) {
            return; // Button already exists
        }

        const maximizeBtn = document.createElement('button');
        maximizeBtn.type = 'button';
        maximizeBtn.className = 'maximize-btn';
        maximizeBtn.innerHTML = '<i class="fas fa-window-maximize"></i>';
        maximizeBtn.title = 'Maximize';
        maximizeBtn.style.cssText = `
            background: none;
            border: none;
            color: #6c757d;
            font-size: 1.2rem;
            cursor: pointer;
            padding: 0.25rem;
            margin-right: 0.5rem;
            border-radius: 0.25rem;
            transition: all 0.2s ease;
        `;

        maximizeBtn.addEventListener('mouseenter', () => {
            maximizeBtn.style.background = 'rgba(0, 0, 0, 0.1)';
        });

        maximizeBtn.addEventListener('mouseleave', () => {
            maximizeBtn.style.background = 'none';
        });

        maximizeBtn.addEventListener('click', (e) => {
            e.preventDefault();
            e.stopPropagation();
            toggleMaximize(modal);
        });

        // Insert before close button
        if (existingClose) {
            modalHeader.insertBefore(maximizeBtn, existingClose);
        } else {
            modalHeader.appendChild(maximizeBtn);
        }
    }

    /**
     * Start dragging the modal
     */
    function startDrag(e, modalDialog) {
        if (isMaximized) return;

        isDragging = true;
        document.body.style.userSelect = 'none';
        
        const rect = modalDialog.getBoundingClientRect();
        dragOffset.x = e.clientX - rect.left;
        dragOffset.y = e.clientY - rect.top;

        // Store original position if not already stored
        if (!originalPosition) {
            originalPosition = {
                top: modalDialog.style.top || 'auto',
                left: modalDialog.style.left || 'auto',
                transform: modalDialog.style.transform || ''
            };
        }

        document.addEventListener('mousemove', handleDrag);
        document.addEventListener('mouseup', stopDrag);

        e.preventDefault();
    }

    /**
     * Handle mouse move during drag
     */
    function handleDrag(e) {
        if (!isDragging) return;

        const modalDialog = document.querySelector('.modal.show .modal-dialog');
        if (!modalDialog) return;

        const x = Math.max(0, Math.min(window.innerWidth - modalDialog.offsetWidth, e.clientX - dragOffset.x));
        const y = Math.max(0, Math.min(window.innerHeight - modalDialog.offsetHeight, e.clientY - dragOffset.y));

        modalDialog.style.position = 'fixed';
        modalDialog.style.top = y + 'px';
        modalDialog.style.left = x + 'px';
        modalDialog.style.transform = 'none';
        modalDialog.style.margin = '0';
    }

    /**
     * Stop dragging
     */
    function stopDrag() {
        isDragging = false;
        document.body.style.userSelect = '';
        document.removeEventListener('mousemove', handleDrag);
        document.removeEventListener('mouseup', stopDrag);
    }

    /**
     * Toggle maximize/restore state
     */
    function toggleMaximize(modal) {
        const modalDialog = modal.querySelector('.modal-dialog');
        const maximizeBtn = modal.querySelector('.maximize-btn');
        const icon = maximizeBtn ? maximizeBtn.querySelector('i') : null;

        if (isMaximized) {
            // Restore
            if (originalSize) {
                modalDialog.style.width = originalSize.width;
                modalDialog.style.height = originalSize.height;
                modalDialog.style.maxWidth = originalSize.maxWidth;
            }

            if (originalPosition) {
                modalDialog.style.top = originalPosition.top;
                modalDialog.style.left = originalPosition.left;
                modalDialog.style.transform = originalPosition.transform;
                modalDialog.style.margin = '';
                modalDialog.style.position = '';
            }

            if (icon) {
                icon.className = 'fas fa-window-maximize';
                maximizeBtn.title = 'Maximize';
            }

            isMaximized = false;
            console.log('📐 Modal restored');
        } else {
            // Store current state
            originalSize = {
                width: modalDialog.style.width || modalDialog.offsetWidth + 'px',
                height: modalDialog.style.height || modalDialog.offsetHeight + 'px',
                maxWidth: modalDialog.style.maxWidth || ''
            };

            originalPosition = {
                top: modalDialog.style.top || 'auto',
                left: modalDialog.style.left || 'auto',
                transform: modalDialog.style.transform || '',
                margin: modalDialog.style.margin || '',
                position: modalDialog.style.position || ''
            };

            // Maximize
            modalDialog.style.position = 'fixed';
            modalDialog.style.top = '20px';
            modalDialog.style.left = '20px';
            modalDialog.style.width = 'calc(100vw - 40px)';
            modalDialog.style.height = 'calc(100vh - 40px)';
            modalDialog.style.maxWidth = 'none';
            modalDialog.style.transform = 'none';
            modalDialog.style.margin = '0';

            if (icon) {
                icon.className = 'fas fa-window-restore';
                maximizeBtn.title = 'Restore';
            }

            isMaximized = true;
            console.log('🔍 Modal maximized');
        }
    }

    /**
     * Setup mutation observer for dynamically added modals
     */
    function setupMutationObserver() {
        const observer = new MutationObserver((mutations) => {
            mutations.forEach((mutation) => {
                mutation.addedNodes.forEach((node) => {
                    if (node.nodeType === 1) { // Element node
                        if (node.matches && node.matches('.modal[id^="modal-"], .news-modal')) {
                            setupModalDragAndResize(node);
                        }
                        
                        // Check child elements
                        if (node.querySelector) {
                            const childModals = node.querySelectorAll('.modal[id^="modal-"], .news-modal');
                            childModals.forEach(setupModalDragAndResize);
                        }
                    }
                });
            });
        });

        observer.observe(document.body, {
            childList: true,
            subtree: true
        });

        console.log('👁️ Mutation observer setup for dynamic modals');
    }

    /**
     * Reset modal state when modal is hidden
     */
    function resetModalState(modal) {
        const modalDialog = modal.querySelector('.modal-dialog');
        const maximizeBtn = modal.querySelector('.maximize-btn');
        const icon = maximizeBtn ? maximizeBtn.querySelector('i') : null;

        if (originalPosition) {
            modalDialog.style.top = originalPosition.top;
            modalDialog.style.left = originalPosition.left;
            modalDialog.style.transform = originalPosition.transform;
            modalDialog.style.margin = '';
            modalDialog.style.position = '';
        }

        if (originalSize) {
            modalDialog.style.width = originalSize.width;
            modalDialog.style.height = originalSize.height;
            modalDialog.style.maxWidth = originalSize.maxWidth;
        }

        if (icon) {
            icon.className = 'fas fa-window-maximize';
            maximizeBtn.title = 'Maximize';
        }

        isMaximized = false;
        originalPosition = null;
        originalSize = null;
    }

    /**
     * Setup Bootstrap modal event listeners
     */
    function setupBootstrapEvents() {
        // Listen for Bootstrap modal events
        document.addEventListener('hidden.bs.modal', (e) => {
            if (e.target.matches('.modal[id^="modal-"], .news-modal')) {
                resetModalState(e.target);
            }
        });

        // jQuery events if available
        if (typeof $ !== 'undefined') {
            $(document).on('hidden.bs.modal', '.modal[id^="modal-"], .news-modal', function() {
                resetModalState(this);
            });
        }
    }

    /**
     * Add CSS styles for draggable functionality
     */
    function addDraggableStyles() {
        const style = document.createElement('style');
        style.textContent = `
            .draggable-header {
                cursor: move !important;
                user-select: none !important;
            }
            
            .draggable-header:hover {
                background-color: rgba(0, 0, 0, 0.05) !important;
            }
            
            .maximize-btn:hover {
                background-color: rgba(0, 0, 0, 0.1) !important;
            }
            
            .modal-dialog {
                transition: all 0.3s ease !important;
            }
            
            /* Prevent text selection during drag */
            body.dragging {
                user-select: none !important;
                -webkit-user-select: none !important;
                -moz-user-select: none !important;
                -ms-user-select: none !important;
            }
        `;
        document.head.appendChild(style);
    }

    // Export functions for testing
    window.initializeDraggableModals = initializeDraggableModals;
    window.testDraggableModal = function() {
        const testModal = document.querySelector('.modal[id^="modal-"], .news-modal');
        if (testModal) {
            if (typeof $ !== 'undefined') {
                $(testModal).modal('show');
            } else {
                testModal.style.display = 'block';
                testModal.classList.add('show');
            }
        } else {
            console.warn('No news modal found for testing');
        }
    };

    // Initialize when script loads
    addDraggableStyles();
    setupBootstrapEvents();
    initializeDraggableModals();

    console.log('✅ Draggable News Modal system loaded');

})();