// NAVBAR FIX - Direct approach to fix active menu highlighting
console.log('🎯 NAVBAR FIX: Loading...');

// Function to force home menu active
function forceHomeMenuActive() {
    console.log('🎯 NAVBAR FIX: Forcing home menu active...');
    
    // Remove active from all items
    document.querySelectorAll('.navbar-nav li, .nav-item, .dropdown-item, .dropdown').forEach(function(item) {
        item.classList.remove('active');
    });
    
    // Find home menu item
    let homeItem = null;
    
    // Try different selectors
    const selectors = [
        'li[data-page="index"]',
        '.nav-item[data-page="index"]', 
        '.navbar-nav li:first-child',
        'li[data-href="/cms/index.html"]',
        '.navbar-nav .nav-item:first-child'
    ];
    
    for (let selector of selectors) {
        homeItem = document.querySelector(selector);
        if (homeItem) {
            console.log('🎯 NAVBAR FIX: Found home item with:', selector);
            break;
        }
    }
    
    if (homeItem) {
        // Add active class
        homeItem.classList.add('active');
        console.log('🎯 NAVBAR FIX: Added active class to home item');
        
        // Force visual styling
        const link = homeItem.querySelector('a');
        if (link) {
            link.style.setProperty('color', '#3b82f6', 'important');
            link.style.setProperty('background-color', 'rgba(59, 130, 246, 0.15)', 'important');
            link.style.setProperty('border-radius', '6px', 'important');
            link.style.setProperty('font-weight', '600', 'important');
            link.style.setProperty('padding', '8px 16px', 'important');
            console.log('🎯 NAVBAR FIX: Applied visual styling to home link');
        }
        
        return true;
    } else {
        console.log('❌ NAVBAR FIX: Could not find home menu item');
        // Debug: show all nav items
        const allItems = document.querySelectorAll('.navbar-nav li, .navbar-nav .nav-item');
        console.log('🎯 NAVBAR FIX: All nav items:', allItems);
        return false;
    }
}

// Function to set any menu active
function setMenuActive(pageName) {
    console.log('🎯 NAVBAR FIX: Setting menu active for:', pageName);
    
    // Remove active from all items
    document.querySelectorAll('.navbar-nav li, .nav-item, .dropdown-item, .dropdown').forEach(function(item) {
        item.classList.remove('active');
        // Also remove inline styles
        const link = item.querySelector('a');
        if (link) {
            link.style.removeProperty('color');
            link.style.removeProperty('background-color');
            link.style.removeProperty('border-radius');
            link.style.removeProperty('font-weight');
            link.style.removeProperty('padding');
        }
    });
    
    // Find target menu item with multiple approaches
    let targetItem = null;
    
    // Try different selectors for the page
    const selectors = [
        `[data-page="${pageName}"]`,
        `[data-href="${pageName}.html"]`,
        `[data-href="/cms/${pageName}.html"]`,
        `a[href*="${pageName}.html"]`,
        `a[href*="${pageName}"]`
    ];
    
    // Special case for about page - try additional selectors
    if (pageName === 'about') {
        selectors.push(
            `[data-page="abouts"]`,
            `[data-href="abouts.html"]`,
            `a[href*="about"]`,
            `a[text*="About"]`,
            `a[text*="关于"]`
        );
    }
    
    for (let selector of selectors) {
        targetItem = document.querySelector(selector);
        if (targetItem) {
            // If we found a link, get its parent nav item
            if (targetItem.tagName === 'A') {
                targetItem = targetItem.closest('li, .nav-item');
            }
            console.log('🎯 NAVBAR FIX: Found menu item with selector:', selector);
            break;
        }
    }
    
    if (targetItem) {
        targetItem.classList.add('active');
        
        // Force visual styling
        const link = targetItem.querySelector('a');
        if (link) {
            link.style.setProperty('color', '#3b82f6', 'important');
            link.style.setProperty('background-color', 'rgba(59, 130, 246, 0.15)', 'important');
            link.style.setProperty('border-radius', '6px', 'important');
            link.style.setProperty('font-weight', '600', 'important');
            link.style.setProperty('padding', '8px 16px', 'important');
        }
        
        console.log('🎯 NAVBAR FIX: Menu activated for:', pageName, targetItem);
        return true;
    } else {
        console.log('❌ NAVBAR FIX: Could not find menu for:', pageName);
        // Debug: show all nav items
        const allItems = document.querySelectorAll('.navbar-nav li, .navbar-nav .nav-item');
        console.log('🎯 NAVBAR FIX: All available nav items:');
        allItems.forEach((item, index) => {
            const dataPage = item.getAttribute('data-page');
            const dataHref = item.getAttribute('data-href');
            const linkHref = item.querySelector('a')?.getAttribute('href');
            const linkText = item.querySelector('a')?.textContent?.trim();
            console.log(`  ${index + 1}. data-page="${dataPage}" data-href="${dataHref}" href="${linkHref}" text="${linkText}"`);
        });
        return false;
    }
}

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    console.log('🎯 NAVBAR FIX: DOM ready, initializing...');
    
    // Determine current page
    const currentPath = window.location.pathname;
    console.log('🎯 NAVBAR FIX: Current path:', currentPath);
    
    if (currentPath.includes('index.html') || currentPath.endsWith('/cms/') || currentPath.endsWith('/')) {
        // Home page
        console.log('🎯 NAVBAR FIX: Detected home page');
        setTimeout(forceHomeMenuActive, 100);
        setTimeout(forceHomeMenuActive, 500);
        setTimeout(forceHomeMenuActive, 1000);
    } else {
        // Other pages - extract page name
        const pageName = currentPath.split('/').pop().replace('.html', '');
        console.log('🎯 NAVBAR FIX: Detected page:', pageName);
        
        // Special handling for about page
        if (pageName === 'about') {
            console.log('🎯 NAVBAR FIX: Setting up about page menu');
            setTimeout(() => setMenuActive('about'), 100);
            setTimeout(() => setMenuActive('about'), 500);
            setTimeout(() => setMenuActive('about'), 1000);
        } else {
            setTimeout(() => setMenuActive(pageName), 100);
            setTimeout(() => setMenuActive(pageName), 500);
        }
    }
});

// Also try on window load
window.addEventListener('load', function() {
    console.log('🎯 NAVBAR FIX: Window loaded, ensuring menu is active...');
    
    const currentPath = window.location.pathname;
    if (currentPath.includes('index.html') || currentPath.endsWith('/cms/') || currentPath.endsWith('/')) {
        setTimeout(forceHomeMenuActive, 200);
    }
});

// Export function for manual use
window.forceHomeMenuActive = forceHomeMenuActive;
window.setMenuActive = setMenuActive;

console.log('✅ NAVBAR FIX: Loaded and ready');