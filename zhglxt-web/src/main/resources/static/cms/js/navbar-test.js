// Navbar Active Menu Test Script
// This script can be used to test and debug the active menu functionality

(function() {
    'use strict';
    
    // Test function to verify navbar functionality
    function testNavbarActive() {
        console.log('=== Navbar Active Menu Test ===');
        
        // Check if navbar exists
        const navbar = document.querySelector('.navbar-nav');
        if (!navbar) {
            console.error('❌ Navbar not found');
            return false;
        }
        console.log('✅ Navbar found');
        
        // Check if menu items exist
        const menuItems = document.querySelectorAll('.navbar-nav .nav-item');
        console.log(`✅ Found ${menuItems.length} menu items`);
        
        // List all menu items with their data-page attributes
        menuItems.forEach((item, index) => {
            const page = item.getAttribute('data-page');
            const link = item.querySelector('a');
            const href = link ? link.getAttribute('href') : 'No link';
            const text = link ? link.textContent.trim() : 'No text';
            const isActive = item.classList.contains('active');
            
            console.log(`  ${index + 1}. ${text} (page: ${page}, href: ${href}, active: ${isActive})`);
        });
        
        // Check if CSS is loaded
        const activeStyles = window.getComputedStyle(document.querySelector('.navbar-nav .nav-item.active > a') || document.createElement('div'));
        const hasActiveStyles = activeStyles.color !== 'rgb(0, 0, 0)' || activeStyles.backgroundColor !== 'rgba(0, 0, 0, 0)';
        
        if (hasActiveStyles) {
            console.log('✅ Active menu CSS styles are loaded');
        } else {
            console.warn('⚠️ Active menu CSS styles may not be loaded properly');
        }
        
        // Check if JavaScript functions are available
        if (typeof window.setActiveMenu === 'function') {
            console.log('✅ setActiveMenu function is available');
        } else {
            console.error('❌ setActiveMenu function is not available');
        }
        
        if (typeof window.navbarUtils === 'object') {
            console.log('✅ navbarUtils object is available');
        } else {
            console.warn('⚠️ navbarUtils object is not available');
        }
        
        // Test current page detection
        const currentPath = window.location.pathname;
        const currentPage = currentPath.split('/').pop().replace('.html', '');
        console.log(`📍 Current page detected as: ${currentPage}`);
        
        // Check if current page has active menu
        const activeItems = document.querySelectorAll('.navbar-nav .active');
        if (activeItems.length > 0) {
            console.log(`✅ Found ${activeItems.length} active menu item(s)`);
            activeItems.forEach((item, index) => {
                const page = item.getAttribute('data-page');
                const text = item.querySelector('a')?.textContent.trim();
                console.log(`  Active ${index + 1}: ${text} (${page})`);
            });
        } else {
            console.warn('⚠️ No active menu items found');
        }
        
        console.log('=== Test Complete ===');
        return true;
    }
    
    // Test function to manually set active menu
    function testSetActiveMenu(pageName) {
        console.log(`Testing setActiveMenu('${pageName}')`);
        
        if (typeof window.setActiveMenu === 'function') {
            window.setActiveMenu(pageName);
            
            // Check if it worked
            setTimeout(() => {
                const activeItem = document.querySelector(`.navbar-nav [data-page="${pageName}"].active`);
                if (activeItem) {
                    console.log(`✅ Successfully set ${pageName} as active`);
                } else {
                    console.error(`❌ Failed to set ${pageName} as active`);
                }
            }, 100);
        } else {
            console.error('❌ setActiveMenu function not available');
        }
    }
    
    // Auto-run test when DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', function() {
            setTimeout(testNavbarActive, 500);
        });
    } else {
        setTimeout(testNavbarActive, 500);
    }
    
    // Export test functions to global scope for manual testing
    window.testNavbarActive = testNavbarActive;
    window.testSetActiveMenu = testSetActiveMenu;
    
    // Add visual debug mode
    window.enableNavbarDebug = function() {
        document.querySelector('.navbar-nav').classList.add('navbar-debug');
        console.log('🔍 Navbar debug mode enabled - active items will have colored borders');
    };
    
    window.disableNavbarDebug = function() {
        document.querySelector('.navbar-nav').classList.remove('navbar-debug');
        console.log('🔍 Navbar debug mode disabled');
    };
    
})();

// Instructions for manual testing:
console.log(`
🧪 Navbar Active Menu Testing Instructions:
1. Open browser console
2. Run: testNavbarActive() - to test current state
3. Run: testSetActiveMenu('exhibition') - to test setting specific menu
4. Run: enableNavbarDebug() - to enable visual debugging
5. Run: disableNavbarDebug() - to disable visual debugging
6. Run: window.navbarUtils.getAllMenuItems() - to see all menu items
`);