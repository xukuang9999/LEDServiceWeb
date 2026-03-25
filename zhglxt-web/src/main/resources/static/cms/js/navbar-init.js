// Navbar Initialization Helper
// This script can be used to manually set active menu items on specific pages

(function() {
    'use strict';
    
    // Page-specific active menu mapping
    const pageActiveMapping = {
        'index.html': 'index',
        'exhibition.html': 'exhibition',
        'exhibition-detail.html': 'exhibition',
        'service.html': 'service',
        'service-detail.html': 'service',
        'news.html': 'news',
        'contact.html': 'contact'
    };
    
    // Function to initialize active menu for current page
    function initActiveMenu() {
        const currentPage = window.location.pathname.split('/').pop();
        const activeMenu = pageActiveMapping[currentPage];
        
        if (activeMenu && window.setActiveMenu) {
            window.setActiveMenu(activeMenu);
        }
    }
    
    // Initialize when DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initActiveMenu);
    } else {
        initActiveMenu();
    }
    
    // Export for manual use
    window.initActiveMenu = initActiveMenu;
    
})();