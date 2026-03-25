/**
 * EXHIBITION PAGE MODAL SYSTEM - REDESIGNED
 * Following the news modal pattern for consistency
 * Enhanced with modern design and better functionality
 */

(function() {
    'use strict';

    console.log('🏢 EXHIBITION MODAL: Loading redesigned modal system...');

    // Modal state for exhibition page
    let exhibitionModalState = {
        isMaximized: false,
        originalSize: { width: '90%', maxWidth: '1000px', maxHeight: '85vh' }
    };

    // Create exhibition modal function - redesigned following news modal pattern
    function createExhibitionPageModal(exhibitionId, title, content, imageUrl, features) {
        console.log('🏢 EXHIBITION MODAL: Creating redesigned modal for exhibition ID:', exhibitionId);

        // Remove existing exhibition modal
        const existing = document.getElementById('exhibition-page-modal');
        if (existing) existing.remove();

        // Reset state
        exhibitionModalState.isMaximized = false;

        // Build features HTML if provided
        let featuresHTML = '';
        if (features && features.length > 0) {
            featuresHTML = `
                <div style="
                    margin-top: 32px !important;
                    padding-top: 24px !important;
                    border-top: 1px solid #e5e7eb !important;
                ">
                    <h4 style="
                        color: #374151 !important;
                        font-size: 18px !important;
                        font-weight: 600 !important;
                        margin-bottom: 16px !important;
                        display: flex !important;
                        align-items: center !important;
                        gap: 8px !important;
                    ">
                        <i class="fas fa-star" style="color: #f59e0b !important;"></i>
                        Exhibition Features
                    </h4>
                    <div style="
                        display: flex !important;
                        flex-wrap: wrap !important;
                        gap: 8px !important;
                    ">
                        ${features.map(feature => `
                            <span style="
                                background: linear-gradient(135deg, #3b82f6, #8b5cf6) !important;
                                color: white !important;
                                padding: 6px 12px !important;
                                border-radius: 20px !important;
                                font-size: 14px !important;
                                font-weight: 500 !important;
                                display: inline-block !important;
                            ">${feature.trim()}</span>
                        `).join('')}
                    </div>
                </div>
            `;
        }

        // Build image HTML if provided
        let imageHTML = '';
        if (imageUrl) {
            imageHTML = `
                <img src="${imageUrl}" 
                     alt="Exhibition Image" 
                     style="
                        width: 100% !important;
                        max-height: 400px !important;
                        object-fit: cover !important;
                        border-radius: 12px !important;
                        margin-bottom: 24px !important;
                        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1) !important;
                     ">
            `;
        }

        // Create modal HTML with improved design
        const modalHTML = `
            <div id="exhibition-page-modal" style="
                position: fixed !important;
                top: 0 !important;
                left: 0 !important;
                width: 100vw !important;
                height: 100vh !important;
                background: rgba(255, 255, 255, 0.95) !important;
                z-index: 999999 !important;
                display: flex !important;
                align-items: center !important;
                justify-content: center !important;
                font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Arial, sans-serif !important;
                backdrop-filter: blur(4px) !important;
                animation: exhibitionModalFadeIn 0.4s ease-out !important;
            ">
                <div id="exhibition-modal-dialog" style="
                    background: white !important;
                    border-radius: 24px !important;
                    box-shadow: 0 40px 120px rgba(0, 0, 0, 0.25) !important;
                    width: 90% !important;
                    max-width: 1000px !important;
                    max-height: 85vh !important;
                    display: flex !important;
                    flex-direction: column !important;
                    overflow: hidden !important;
                    transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1) !important;
                    animation: exhibitionModalSlideIn 0.4s ease-out !important;
                    border: 1px solid rgba(255, 255, 255, 0.1) !important;
                ">
                    <div id="exhibition-modal-header" style="
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
                        color: white !important;
                        padding: 28px 32px !important;
                        display: flex !important;
                        justify-content: space-between !important;
                        align-items: center !important;
                        border-radius: 24px 24px 0 0 !important;
                        cursor: move !important;
                        user-select: none !important;
                        position: relative !important;
                        overflow: hidden !important;
                    ">
                        <!-- Header background pattern -->
                        <div style="
                            position: absolute !important;
                            top: 0 !important;
                            left: 0 !important;
                            right: 0 !important;
                            bottom: 0 !important;
                            background: url('data:image/svg+xml,<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 100 100\"><defs><pattern id=\"grain\" width=\"100\" height=\"100\" patternUnits=\"userSpaceOnUse\"><circle cx=\"50\" cy=\"50\" r=\"1\" fill=\"white\" opacity=\"0.1\"/></pattern></defs><rect width=\"100\" height=\"100\" fill=\"url(%23grain)\"/></svg></div>

                        <div style="flex: 1 !important; min-width: 0 !important; position: relative !important; z-index: 1 !important;">
                            <div style="
                                font-size: 13px !important;
                                opacity: 0.9 !important;
                                margin-bottom: 8px !important;
                                display: flex !important;
                                align-items: center !important;
                                gap: 8px !important;
                                font-weight: 500 !important;
                            ">
                                <i class="fas fa-building" style="font-size: 14px !important;"></i>
                                <span>Exhibition Details</span>
                            </div>
                            <h3 id="exhibition-modal-title" style="
                                margin: 0 !important;
                                font-size: 22px !important;
                                font-weight: 700 !important;
                                line-height: 1.3 !important;
                                white-space: nowrap !important;
                                overflow: hidden !important;
                                text-overflow: ellipsis !important;
                                text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1) !important;
                            ">
                                ${title || 'Exhibition Details'}
                            </h3>
                        </div>
                        <div style="
                            display: flex !important;
                            gap: 12px !important;
                            flex-shrink: 0 !important;
                            margin-left: 24px !important;
                            position: relative !important;
                            z-index: 1 !important;
                        ">
                            <button id="exhibition-maximize-btn" onclick="toggleExhibitionMaximize()" style="
                                background: rgba(255, 255, 255, 0.25) !important;
                                border: 1px solid rgba(255, 255, 255, 0.3) !important;
                                color: white !important;
                                width: 40px !important;
                                height: 40px !important;
                                border-radius: 10px !important;
                                cursor: pointer !important;
                                font-size: 16px !important;
                                display: flex !important;
                                align-items: center !important;
                                justify-content: center !important;
                                transition: all 0.3s ease !important;
                                backdrop-filter: blur(10px) !important;
                            " title="Maximize">
                                <i class="fas fa-expand"></i>
                            </button>
                            <button onclick="closeExhibitionPageModal()" style="
                                background: rgba(239, 68, 68, 0.9) !important;
                                border: 1px solid rgba(239, 68, 68, 1) !important;
                                color: white !important;
                                width: 40px !important;
                                height: 40px !important;
                                border-radius: 10px !important;
                                cursor: pointer !important;
                                font-size: 16px !important;
                                display: flex !important;
                                align-items: center !important;
                                justify-content: center !important;
                                transition: all 0.3s ease !important;
                                backdrop-filter: blur(10px) !important;
                            " title="Close">
                                <i class="fas fa-times"></i>
                            </button>
                        </div>
                    </div>
                    <div id="exhibition-modal-content" style="
                        padding: 0 !important;
                        overflow-y: auto !important;
                        overflow-x: hidden !important;
                        flex: 1 !important;
                        background: linear-gradient(135deg, #fafbfc 0%, #f8fafc 100%) !important;
                    ">
                        <div style="
                            background: white !important;
                            margin: 32px !important;
                            padding: 48px !important;
                            border-radius: 20px !important;
                            box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12) !important;
                            line-height: 1.8 !important;
                            color: #374151 !important;
                            font-size: 16px !important;
                            word-wrap: break-word !important;
                            overflow-wrap: break-word !important;
                            border: 1px solid rgba(0, 0, 0, 0.05) !important;
                        ">
                            ${imageHTML}
                            <div style="font-size: 16px !important; line-height: 1.7 !important;">
                                ${content || '<p>Loading exhibition content...</p>'}
                            </div>
                            ${featuresHTML}
                        </div>
                    </div>
                </div>
            </div>
        `;

        document.body.insertAdjacentHTML('beforeend', modalHTML);
        document.body.style.overflow = 'hidden';

        // Set up event listeners
        setupExhibitionModalEventListeners();

        console.log('✅ EXHIBITION MODAL: Redesigned modal created and displayed');
    }

    // Setup exhibition modal event listeners
    function setupExhibitionModalEventListeners() {
        const modal = document.getElementById('exhibition-page-modal');
        const header = document.getElementById('exhibition-modal-header');

        // Close on overlay click
        modal.addEventListener('click', function(e) {
            if (e.target === modal) {
                closeExhibitionPageModal();
            }
        });

        // Close on Escape key
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape') {
                closeExhibitionPageModal();
            } else if (e.key === 'F11') {
                e.preventDefault();
                toggleExhibitionMaximize();
            }
        });

        // Double-click header to maximize
        header.addEventListener('dblclick', function() {
            toggleExhibitionMaximize();
        });

        // Enhanced button hover effects
        const maximizeBtn = document.getElementById('exhibition-maximize-btn');
        const closeBtn = modal.querySelector('button[onclick="closeExhibitionPageModal()"]');
        
        // Maximize button effects
        maximizeBtn.addEventListener('mouseenter', function() {
            this.style.background = 'rgba(255, 255, 255, 0.4)';
            this.style.transform = 'translateY(-2px) scale(1.05)';
            this.style.boxShadow = '0 4px 12px rgba(0, 0, 0, 0.15)';
        });
        maximizeBtn.addEventListener('mouseleave', function() {
            this.style.background = 'rgba(255, 255, 255, 0.25)';
            this.style.transform = 'translateY(0) scale(1)';
            this.style.boxShadow = 'none';
        });

        // Close button effects
        closeBtn.addEventListener('mouseenter', function() {
            this.style.background = 'rgba(220, 38, 38, 1)';
            this.style.transform = 'translateY(-2px) scale(1.05)';
            this.style.boxShadow = '0 4px 12px rgba(220, 38, 38, 0.3)';
        });
        closeBtn.addEventListener('mouseleave', function() {
            this.style.background = 'rgba(239, 68, 68, 0.9)';
            this.style.transform = 'translateY(0) scale(1)';
            this.style.boxShadow = 'none';
        });
    }

    // Toggle maximize/restore function for exhibition
    window.toggleExhibitionMaximize = function() {
        const dialog = document.getElementById('exhibition-modal-dialog');
        const maximizeBtn = document.getElementById('exhibition-maximize-btn');
        const icon = maximizeBtn.querySelector('i');

        if (exhibitionModalState.isMaximized) {
            // Restore to original size
            dialog.style.width = exhibitionModalState.originalSize.width;
            dialog.style.maxWidth = exhibitionModalState.originalSize.maxWidth;
            dialog.style.maxHeight = exhibitionModalState.originalSize.maxHeight;
            dialog.style.height = 'auto';
            
            icon.className = 'fas fa-expand';
            maximizeBtn.title = 'Maximize';
            exhibitionModalState.isMaximized = false;
            
            console.log('🏢 EXHIBITION PAGE: Modal restored to original size');
        } else {
            // Maximize to full screen
            dialog.style.width = '98vw';
            dialog.style.maxWidth = 'none';
            dialog.style.height = '95vh';
            dialog.style.maxHeight = 'none';
            
            icon.className = 'fas fa-compress';
            maximizeBtn.title = 'Restore';
            exhibitionModalState.isMaximized = true;
            
            console.log('🏢 EXHIBITION PAGE: Modal maximized');
        }
    };

    // Close exhibition modal function
    window.closeExhibitionPageModal = function() {
        const modal = document.getElementById('exhibition-page-modal');
        if (modal) {
            const dialog = document.getElementById('exhibition-modal-dialog');
            
            // Animate out
            dialog.style.transform = 'scale(0.95) translateY(-20px)';
            dialog.style.opacity = '0';
            modal.style.opacity = '0';
            
            setTimeout(() => {
                modal.remove();
                document.body.style.overflow = '';
                exhibitionModalState.isMaximized = false;
            }, 250);
        }
        console.log('✅ EXHIBITION PAGE: Modal closed');
    };

    // Main show exhibition modal function - redesigned
    window.showExhibitionPageModal = function(exhibitionId) {
        console.log('🏢 EXHIBITION MODAL: showExhibitionPageModal called with ID:', exhibitionId);

        if (!exhibitionId) {
            console.error('❌ EXHIBITION MODAL: No exhibition ID provided');
            createExhibitionPageModal('error', 'Error', '<p>Unable to load exhibition content. Missing exhibition ID.</p>', null, []);
            return;
        }

        // Get exhibition content with all details
        const exhibitionData = getExhibitionPageContent(exhibitionId);
        createExhibitionPageModal(
            exhibitionId, 
            exhibitionData.title, 
            exhibitionData.content, 
            exhibitionData.imageUrl, 
            exhibitionData.features
        );
    };

    // Get exhibition content - redesigned following news modal pattern
    function getExhibitionPageContent(exhibitionId) {
        let title = 'Exhibition Details';
        let content = '<p>Loading exhibition content...</p>';
        let imageUrl = null;
        let features = [];

        // Try to find existing modal first (like news modal does)
        const existingModal = document.getElementById('modal-' + exhibitionId);
        if (existingModal) {
            const titleElement = existingModal.querySelector('.modal-title, h2, h3, h4');
            const imageElement = existingModal.querySelector('.modal-image img, img');
            const contentElement = existingModal.querySelector('.modal-content-text, .modal-body');
            const featuresElement = existingModal.querySelector('.modal-features, .feature-tags');

            if (titleElement) title = titleElement.textContent.trim();
            if (imageElement && imageElement.src) imageUrl = imageElement.src;
            
            // Extract features
            if (featuresElement) {
                const featureTags = featuresElement.querySelectorAll('.feature-tag, .tag');
                features = Array.from(featureTags).map(tag => tag.textContent.trim()).filter(f => f);
            }

            // Extract content with HTML formatting preserved
            if (contentElement) {
                let htmlContent = contentElement.innerHTML || '';
                
                // Clean out unwanted elements but keep HTML formatting
                htmlContent = htmlContent.replace(/<div[^>]*modal-image[^>]*>.*?<\/div>/gi, '');
                htmlContent = htmlContent.replace(/<div[^>]*modal-footer[^>]*>.*?<\/div>/gi, '');
                htmlContent = htmlContent.replace(/<button[^>]*>.*?<\/button>/gi, '');
                htmlContent = htmlContent.replace(/<div[^>]*modal-actions[^>]*>.*?<\/div>/gi, '');
                htmlContent = htmlContent.replace(/<div[^>]*modal-features[^>]*>.*?<\/div>/gi, '');
                
                htmlContent = htmlContent.trim();
                
                if (htmlContent) {
                    content = htmlContent;
                } else {
                    content = '<p>Exhibition details are being prepared.</p>';
                }
            }
        } else {
            // Try to find exhibition card (fallback)
            const exhibitionButton = document.querySelector(`[data-exhibition-id="${exhibitionId}"]`);
            if (exhibitionButton) {
                const exhibitionCard = exhibitionButton.closest('.exhibition-card, .exhibition-preview-card, .card');
                if (exhibitionCard) {
                    const titleElement = exhibitionCard.querySelector('h3, h4, .exhibition-title, .card-title');
                    const descElement = exhibitionCard.querySelector('.exhibition-description, .card-text, p');
                    const imageElement = exhibitionCard.querySelector('.exhibition-image, img');
                    const featuresContainer = exhibitionCard.querySelector('.exhibition-features, .feature-tags');

                    if (titleElement) title = titleElement.textContent.trim();
                    if (imageElement && imageElement.src) imageUrl = imageElement.src;
                    
                    // Extract features from card
                    if (featuresContainer) {
                        const featureTags = featuresContainer.querySelectorAll('.feature-tag, .tag');
                        features = Array.from(featureTags).map(tag => tag.textContent.trim()).filter(f => f);
                    }

                    // Extract description with HTML formatting
                    if (descElement) {
                        content = descElement.innerHTML || descElement.textContent || '<p>Exhibition details are being prepared.</p>';
                    }
                }
            }
        }

        return { title, content, imageUrl, features };
    }

    // Set up exhibition page handlers
    function setupExhibitionPageHandlers() {
        console.log('🏢 EXHIBITION PAGE: Setting up independent modal handlers...');

        // Wait for DOM to be ready
        setTimeout(() => {
            // Find exhibition buttons on the exhibition page
            const exhibitionButtons = document.querySelectorAll('.exhibition-cta, [data-exhibition-id], .exhibition-detail-btn');
            
            console.log(`🏢 EXHIBITION PAGE: Found ${exhibitionButtons.length} exhibition buttons`);

            exhibitionButtons.forEach((button, index) => {
                // Get ID from various attributes
                const exhibitionId = button.getAttribute('data-exhibition-id') ||
                                   button.getAttribute('data-target')?.replace('#modal-', '') ||
                                   button.closest('[data-exhibition-id]')?.getAttribute('data-exhibition-id');

                if (exhibitionId) {
                    // Remove existing onclick
                    button.removeAttribute('onclick');

                    // Add new click handler for exhibition page
                    button.addEventListener('click', function(e) {
                        e.preventDefault();
                        e.stopPropagation();
                        e.stopImmediatePropagation();

                        console.log(`🏢 EXHIBITION PAGE: Exhibition button ${index + 1} clicked, ID: ${exhibitionId}`);
                        window.showExhibitionPageModal(exhibitionId);

                        return false;
                    }, true);

                    console.log(`✅ EXHIBITION PAGE: Handler set for exhibition button ${index + 1}, ID: ${exhibitionId}`);
                } else {
                    console.log(`⚠️ EXHIBITION PAGE: No ID found for button ${index + 1}`);
                }
            });

            console.log('✅ EXHIBITION PAGE: All exhibition handlers set up successfully');
        }, 1000);
    }

    // Test function for exhibition page
    window.testExhibitionPageModal = function() {
        console.log('🧪 EXHIBITION PAGE: Testing modal...');
        window.showExhibitionPageModal('test');
    };

    // Initialize on exhibition pages and home page (for exhibition sections)
    const isExhibitionPage = document.body.classList.contains('hero-page') || 
                            window.location.pathname.includes('exhibition') ||
                            window.location.pathname.includes('index');
    
    if (isExhibitionPage) {
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', setupExhibitionPageHandlers);
        } else {
            setupExhibitionPageHandlers();
        }
        console.log('✅ EXHIBITION MODAL: Redesigned system loaded and ready');
    } else {
        console.log('🏢 EXHIBITION MODAL: Not on exhibition-related page, skipping initialization');
    }

})();

// Enhanced CSS animations for exhibition modal
const exhibitionModalCSS = `
<style>
@keyframes exhibitionModalFadeIn {
    from { 
        opacity: 0; 
        backdrop-filter: blur(0px);
    }
    to { 
        opacity: 1; 
        backdrop-filter: blur(4px);
    }
}

@keyframes exhibitionModalSlideIn {
    from {
        transform: scale(0.85) translateY(-40px);
        opacity: 0;
    }
    50% {
        transform: scale(0.95) translateY(-20px);
        opacity: 0.7;
    }
    to {
        transform: scale(1) translateY(0);
        opacity: 1;
    }
}

#exhibition-page-modal * {
    box-sizing: border-box !important;
}

#exhibition-modal-content::-webkit-scrollbar {
    width: 10px !important;
}

#exhibition-modal-content::-webkit-scrollbar-track {
    background: #f8fafc !important;
    border-radius: 6px !important;
    margin: 4px !important;
}

#exhibition-modal-content::-webkit-scrollbar-thumb {
    background: linear-gradient(135deg, #cbd5e1, #94a3b8) !important;
    border-radius: 6px !important;
    border: 2px solid #f8fafc !important;
}

#exhibition-modal-content::-webkit-scrollbar-thumb:hover {
    background: linear-gradient(135deg, #94a3b8, #64748b) !important;
}

/* Enhanced modal styles */
.exhibition-modal .modal-content {
    border: none !important;
    border-radius: 20px !important;
    box-shadow: 0 25px 80px rgba(0, 0, 0, 0.3) !important;
    overflow: hidden !important;
}

.exhibition-modal .modal-header {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
    color: white !important;
    border: none !important;
    padding: 24px 28px !important;
    position: relative !important;
}

.exhibition-modal .modal-header::before {
    content: '' !important;
    position: absolute !important;
    top: 0 !important;
    left: 0 !important;
    right: 0 !important;
    bottom: 0 !important;
    background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grain" width="100" height="100" patternUnits="userSpaceOnUse"><circle cx="50" cy="50" r="1" fill="white" opacity="0.1"/></pattern></defs><rect width="100" height="100" fill="url(%23grain)"/></svg>') !important;
    opacity: 0.3 !important;
}

.exhibition-modal .modal-title {
    font-size: 22px !important;
    font-weight: 700 !important;
    margin: 0 !important;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1) !important;
}

.exhibition-modal .modal-category {
    font-size: 13px !important;
    opacity: 0.9 !important;
    margin-bottom: 8px !important;
    display: flex !important;
    align-items: center !important;
    gap: 8px !important;
    font-weight: 500 !important;
}

.exhibition-modal .modal-body {
    padding: 32px !important;
    background: linear-gradient(135deg, #fafbfc 0%, #f8fafc 100%) !important;
}

.exhibition-modal .modal-image img {
    width: 100% !important;
    max-height: 400px !important;
    object-fit: cover !important;
    border-radius: 12px !important;
    margin-bottom: 24px !important;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12) !important;
}

.exhibition-modal .modal-features {
    margin-top: 24px !important;
    padding-top: 24px !important;
    border-top: 1px solid #e5e7eb !important;
}

.exhibition-modal .feature-tags {
    display: flex !important;
    flex-wrap: wrap !important;
    gap: 8px !important;
    margin-top: 12px !important;
}

.exhibition-modal .feature-tag {
    background: linear-gradient(135deg, #3b82f6, #8b5cf6) !important;
    color: white !important;
    padding: 6px 12px !important;
    border-radius: 20px !important;
    font-size: 14px !important;
    font-weight: 500 !important;
    display: inline-block !important;
}

.exhibition-modal .close {
    position: relative !important;
    z-index: 2 !important;
    color: white !important;
    opacity: 0.9 !important;
    font-size: 24px !important;
    font-weight: 300 !important;
    text-shadow: none !important;
}

.exhibition-modal .close:hover {
    opacity: 1 !important;
    color: white !important;
}
</style>
`;

document.head.insertAdjacentHTML('beforeend', exhibitionModalCSS);