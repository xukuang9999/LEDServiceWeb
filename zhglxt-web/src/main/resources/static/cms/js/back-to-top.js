/**
 * Back to Top Button Functionality
 * Universal implementation for all CMS pages
 */
(function() {
    'use strict';
    
    // Initialize back to top functionality when DOM is ready
    function initBackToTop() {
        const backBtn = document.getElementById('backToTop');
        
        if (!backBtn) {
            console.warn('Back to top button not found');
            return;
        }
        
        // Click handler - smooth scroll to top
        backBtn.addEventListener('click', function(e) {
            e.preventDefault();
            window.scrollTo({ 
                top: 0, 
                behavior: 'smooth' 
            });
        });
        
        // Scroll handler - show/hide button based on scroll position
        let isVisible = false;
        const scrollThreshold = 300;
        
        function toggleVisibility() {
            const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
            const shouldShow = scrollTop > scrollThreshold;
            
            if (shouldShow && !isVisible) {
                backBtn.classList.add('visible');
                backBtn.style.opacity = '1';
                backBtn.style.visibility = 'visible';
                isVisible = true;
            } else if (!shouldShow && isVisible) {
                backBtn.classList.remove('visible');
                backBtn.style.opacity = '0';
                backBtn.style.visibility = 'hidden';
                isVisible = false;
            }
        }
        
        // Throttled scroll handler for better performance
        let ticking = false;
        function handleScroll() {
            if (!ticking) {
                requestAnimationFrame(function() {
                    toggleVisibility();
                    ticking = false;
                });
                ticking = true;
            }
        }
        
        // Add scroll event listener
        window.addEventListener('scroll', handleScroll, { passive: true });
        
        // Initial check
        toggleVisibility();
        
        console.log('✅ Back to top functionality initialized');
    }
    
    // Initialize when DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initBackToTop);
    } else {
        initBackToTop();
    }
    
    // Also make the footer "Back to top" link functional if it exists
    function initFooterBackToTop() {
        const footerLinks = document.querySelectorAll('footer a[href="javascript:;"]');
        footerLinks.forEach(link => {
            if (link.textContent.toLowerCase().includes('back to top') || 
                link.textContent.includes('返回顶部')) {
                link.addEventListener('click', function(e) {
                    e.preventDefault();
                    window.scrollTo({ 
                        top: 0, 
                        behavior: 'smooth' 
                    });
                });
            }
        });
    }
    
    // Initialize footer links when DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initFooterBackToTop);
    } else {
        initFooterBackToTop();
    }
    
})();