/**
 * Unified Modal System
 * Handles both Bootstrap modals (news page) and Independent modals (home page)
 * Ensures proper functionality across all pages
 */

(function() {
    'use strict';

    // System configuration
    const config = {
        debug: true,
        homePagePaths: ['index.html', '/zhglxt/cms/index.html', '/cms/index.html'],
        newsPagePaths: ['news.html', '/zhglxt/cms/news.html', '/cms/news.html']
    };

    /**
     * Debug logging
     */
    function log(message, data = null) {
        if (config.debug) {
            console.log('🔗 Unified Modal:', message, data || '');
        }
    }

    /**
     * Detect current page type
     */
    function getCurrentPageType() {
        const currentPath = window.location.pathname;
        
        if (config.homePagePaths.some(path => currentPath.includes(path))) {
            return 'home';
        } else if (config.newsPagePaths.some(path => currentPath.includes(path))) {
            return 'news';
        } else {
            return 'other';
        }
    }

    /**
     * Initialize modal system based on page type
     */
    function initializeModalSystem() {
        const pageType = getCurrentPageType();
        log('Initializing modal system for page type:', pageType);

        switch (pageType) {
            case 'home':
                initializeHomePageModals();
                break;
            case 'news':
                initializeNewsPageModals();
                break;
            default:
                initializeGenericModals();
                break;
        }
    }

    /**
     * Initialize home page modals (independent modal system)
     */
    function initializeHomePageModals() {
        log('Setting up home page independent modal system');

        // Wait for independent modal system to load
        function waitForIndependentModal() {
            if (window.showIndependentNewsModal) {
                log('Independent modal system available');
                setupHomePageHandlers();
            } else {
                setTimeout(waitForIndependentModal, 100);
            }
        }

        waitForIndependentModal();
    }

    /**
     * Setup home page modal handlers
     */
    function setupHomePageHandlers() {
        // Use event delegation for home page news buttons
        document.addEventListener('click', function(e) {
            const button = e.target.closest('.news-detail-btn');
            
            if (button) {
                e.preventDefault();
                e.stopPropagation();

                const newsId = button.getAttribute('data-news-id');
                log('Home page news button clicked:', newsId);

                if (newsId && window.showIndependentNewsModal) {
                    window.showIndependentNewsModal(newsId);
                } else {
                    log('Independent modal not available, using fallback');
                    openNewsDetailPage(newsId);
                }
            }
        });

        log('Home page modal handlers ready');
    }

    /**
     * Initialize news page modals (Bootstrap modal system)
     */
    function initializeNewsPageModals() {
        log('Setting up news page Bootstrap modal system');

        // Ensure jQuery and Bootstrap are available
        if (typeof $ === 'undefined' || !$.fn.modal) {
            log('jQuery or Bootstrap modal not available, using fallback');
            setupFallbackHandlers();
            return;
        }

        // Initialize Bootstrap modals
        $('.news-modal').each(function() {
            const $modal = $(this);
            const modalId = $modal.attr('id');
            
            log('Initializing Bootstrap modal:', modalId);
            
            $modal.modal({
                show: false,
                backdrop: true,
                keyboard: true,
                focus: true
            });

            // Fix modal positioning
            $modal.on('show.bs.modal', function() {
                log('Bootstrap modal showing:', modalId);
                
                // Ensure proper z-index
                setTimeout(() => {
                    const $backdrop = $('.modal-backdrop');
                    if ($backdrop.length) {
                        $backdrop.css('z-index', 1040);
                    }
                    $modal.css('z-index', 1050);
                }, 10);
            });

            $modal.on('shown.bs.modal', function() {
                log('Bootstrap modal shown:', modalId);
            });
        });

        log('News page Bootstrap modals initialized');
    }

    /**
     * Initialize generic modals for other pages
     */
    function initializeGenericModals() {
        log('Setting up generic modal system');
        
        // Try to use whatever modal system is available
        if (window.showIndependentNewsModal) {
            setupHomePageHandlers();
        } else if (typeof $ !== 'undefined' && $.fn.modal) {
            initializeNewsPageModals();
        } else {
            setupFallbackHandlers();
        }
    }

    /**
     * Setup fallback handlers when no modal system is available
     */
    function setupFallbackHandlers() {
        log('Setting up fallback modal handlers');

        document.addEventListener('click', function(e) {
            const button = e.target.closest('.news-detail-btn, .news-read-btn');
            
            if (button) {
                e.preventDefault();
                const newsId = button.getAttribute('data-news-id');
                
                if (newsId) {
                    openNewsDetailPage(newsId);
                }
            }
        });
    }

    /**
     * Open news detail page as fallback
     */
    function openNewsDetailPage(newsId) {
        const detailUrl = `/cms/news-detail.html?id=${newsId}`;
        log('Opening news detail page:', detailUrl);
        
        try {
            window.open(detailUrl, '_blank');
        } catch (error) {
            log('Error opening detail page:', error);
            alert('Unable to load news article. Please try again.');
        }
    }

    /**
     * Global modal function that works on any page
     */
    window.showUnifiedModal = function(newsId) {
        const pageType = getCurrentPageType();
        log('Showing unified modal for page type:', pageType, 'newsId:', newsId);

        switch (pageType) {
            case 'home':
                if (window.showIndependentNewsModal) {
                    return window.showIndependentNewsModal(newsId);
                }
                break;
                
            case 'news':
                if (typeof $ !== 'undefined' && $.fn.modal) {
                    const $modal = $(`#modal-${newsId}`);
                    if ($modal.length) {
                        $modal.modal('show');
                        return;
                    }
                }
                break;
        }

        // Fallback
        openNewsDetailPage(newsId);
    };

    /**
     * Override existing modal functions for compatibility
     */
    function setupCompatibilityLayer() {
        // Override showModalById for compatibility
        const originalShowModalById = window.showModalById;
        window.showModalById = function(newsId) {
            log('showModalById called with newsId:', newsId);
            
            // Try original function first
            if (originalShowModalById && typeof originalShowModalById === 'function') {
                try {
                    return originalShowModalById(newsId);
                } catch (error) {
                    log('Original showModalById failed:', error);
                }
            }
            
            // Use unified system
            return window.showUnifiedModal(newsId);
        };

        log('Compatibility layer established');
    }

    /**
     * Initialize the unified modal system
     */
    function initialize() {
        log('Initializing unified modal system');
        log('Current page:', window.location.pathname);
        log('Page type:', getCurrentPageType());

        // Wait for DOM to be ready
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', function() {
                setTimeout(() => {
                    initializeModalSystem();
                    setupCompatibilityLayer();
                }, 500);
            });
        } else {
            setTimeout(() => {
                initializeModalSystem();
                setupCompatibilityLayer();
            }, 500);
        }
    }

    // Start initialization
    initialize();

    log('Unified modal system script loaded');

})();