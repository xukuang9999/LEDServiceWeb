/**
 * Modal UX Enhancements
 * Additional user experience improvements for the independent modal system
 */

(function() {
    'use strict';

    // Wait for modal system to be available
    function waitForModalSystem() {
        if (typeof window.showIndependentNewsModal === 'function') {
            initializeUXEnhancements();
        } else {
            setTimeout(waitForModalSystem, 100);
        }
    }

    function initializeUXEnhancements() {
        console.log('🎨 Initializing modal UX enhancements');

        // Enhanced button hover effects
        document.addEventListener('mouseover', function(e) {
            if (e.target.matches('.modal-control-btn, .modal-action-btn')) {
                e.target.style.transform = 'translateY(-2px) scale(1.05)';
            }
        });

        document.addEventListener('mouseout', function(e) {
            if (e.target.matches('.modal-control-btn, .modal-action-btn')) {
                e.target.style.transform = '';
            }
        });

        // Smooth scrolling for modal content
        document.addEventListener('wheel', function(e) {
            const modalBody = e.target.closest('.independent-modal-body');
            if (modalBody) {
                e.preventDefault();
                modalBody.scrollBy({
                    top: e.deltaY * 0.5,
                    behavior: 'smooth'
                });
            }
        }, { passive: false });

        // Enhanced keyboard shortcuts
        document.addEventListener('keydown', function(e) {
            const modal = document.querySelector('#independent-modal-overlay');
            if (!modal || modal.style.display === 'none') return;

            switch(e.key) {
                case 'Escape':
                    e.preventDefault();
                    if (window.smoothCloseModal) {
                        window.smoothCloseModal();
                    }
                    break;
                    
                case 'F11':
                    e.preventDefault();
                    const maximizeBtn = modal.querySelector('#maximize-btn');
                    if (maximizeBtn) {
                        maximizeBtn.click();
                        if (window.playModalSound) {
                            window.playModalSound('maximize');
                        }
                    }
                    break;
                    
                case 'p':
                case 'P':
                    if (e.ctrlKey || e.metaKey) {
                        e.preventDefault();
                        const printBtn = modal.querySelector('#print-btn');
                        if (printBtn) {
                            printBtn.click();
                        }
                    }
                    break;
                    
                case 's':
                case 'S':
                    if (e.ctrlKey || e.metaKey) {
                        e.preventDefault();
                        const shareBtn = modal.querySelector('#share-btn');
                        if (shareBtn) {
                            shareBtn.click();
                        }
                    }
                    break;
            }
        });

        // Auto-hide cursor during reading
        let readingTimer;
        document.addEventListener('mousemove', function(e) {
            const modalBody = e.target.closest('.independent-modal-body');
            if (modalBody) {
                modalBody.style.cursor = 'auto';
                clearTimeout(readingTimer);
                
                readingTimer = setTimeout(() => {
                    if (!modalBody.matches(':hover')) {
                        modalBody.style.cursor = 'none';
                    }
                }, 3000);
            }
        });

        // Smooth image loading
        document.addEventListener('load', function(e) {
            if (e.target.matches('.featured-img, .modal-article-content img')) {
                e.target.style.opacity = '0';
                e.target.style.transform = 'scale(0.95)';
                
                setTimeout(() => {
                    e.target.style.transition = 'all 0.3s ease';
                    e.target.style.opacity = '1';
                    e.target.style.transform = 'scale(1)';
                }, 50);
            }
        }, true);

        // Reading progress indicator
        function addReadingProgress() {
            const modal = document.querySelector('#independent-modal-overlay');
            if (!modal) return;

            const progressBar = document.createElement('div');
            progressBar.id = 'reading-progress';
            progressBar.style.cssText = `
                position: absolute;
                top: 0;
                left: 0;
                height: 3px;
                background: linear-gradient(90deg, #667eea, #764ba2);
                width: 0%;
                transition: width 0.1s ease;
                z-index: 100001;
                border-radius: 0 0 2px 0;
            `;

            const dialog = modal.querySelector('.independent-modal-dialog');
            if (dialog) {
                dialog.appendChild(progressBar);

                const modalBody = dialog.querySelector('.independent-modal-body');
                if (modalBody) {
                    modalBody.addEventListener('scroll', function() {
                        const scrollTop = modalBody.scrollTop;
                        const scrollHeight = modalBody.scrollHeight - modalBody.clientHeight;
                        const progress = (scrollTop / scrollHeight) * 100;
                        
                        progressBar.style.width = Math.min(100, Math.max(0, progress)) + '%';
                    });
                }
            }
        }

        // Add reading progress to new modals
        const originalShowModal = window.showIndependentNewsModal;
        if (originalShowModal) {
            window.showIndependentNewsModal = function(newsId) {
                const result = originalShowModal.call(this, newsId);
                
                setTimeout(() => {
                    addReadingProgress();
                }, 500);
                
                return result;
            };
        }

        // Tooltip system for buttons
        function addTooltips() {
            const buttonsWithTooltips = document.querySelectorAll('[title]');
            
            buttonsWithTooltips.forEach(button => {
                let tooltip;
                
                button.addEventListener('mouseenter', function() {
                    tooltip = document.createElement('div');
                    tooltip.className = 'modal-tooltip';
                    tooltip.textContent = this.title;
                    tooltip.style.cssText = `
                        position: absolute;
                        background: rgba(0, 0, 0, 0.8);
                        color: white;
                        padding: 6px 10px;
                        border-radius: 4px;
                        font-size: 12px;
                        white-space: nowrap;
                        z-index: 100002;
                        pointer-events: none;
                        opacity: 0;
                        transition: opacity 0.2s ease;
                    `;
                    
                    document.body.appendChild(tooltip);
                    
                    const rect = this.getBoundingClientRect();
                    tooltip.style.left = (rect.left + rect.width / 2 - tooltip.offsetWidth / 2) + 'px';
                    tooltip.style.top = (rect.top - tooltip.offsetHeight - 8) + 'px';
                    
                    setTimeout(() => {
                        tooltip.style.opacity = '1';
                    }, 100);
                    
                    // Remove title to prevent default tooltip
                    this.setAttribute('data-title', this.title);
                    this.removeAttribute('title');
                });
                
                button.addEventListener('mouseleave', function() {
                    if (tooltip) {
                        tooltip.remove();
                    }
                    
                    // Restore title
                    if (this.getAttribute('data-title')) {
                        this.title = this.getAttribute('data-title');
                        this.removeAttribute('data-title');
                    }
                });
            });
        }

        // Initialize tooltips when modal opens
        document.addEventListener('DOMNodeInserted', function(e) {
            if (e.target.id === 'independent-modal-overlay') {
                setTimeout(addTooltips, 100);
            }
        });

        // Preload next/previous articles (if available)
        function preloadAdjacentArticles() {
            const newsButtons = document.querySelectorAll('.news-detail-btn');
            newsButtons.forEach((button, index) => {
                button.addEventListener('click', function() {
                    // Preload next and previous articles
                    const nextButton = newsButtons[index + 1];
                    const prevButton = newsButtons[index - 1];
                    
                    [nextButton, prevButton].forEach(btn => {
                        if (btn) {
                            const newsId = btn.getAttribute('data-news-id');
                            if (newsId) {
                                // Preload data in background
                                setTimeout(() => {
                                    fetch(`/cms/api/news/${newsId}`).catch(() => {});
                                }, 1000);
                            }
                        }
                    });
                });
            });
        }

        // Initialize preloading
        preloadAdjacentArticles();

        console.log('✨ Modal UX enhancements initialized');
    }

    // Start initialization
    waitForModalSystem();

})();