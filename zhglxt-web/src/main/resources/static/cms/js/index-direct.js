// DIRECT INDEX PAGE FUNCTIONALITY - Simple and guaranteed to work
console.log('🚀 DIRECT INDEX SCRIPT LOADING...');

// Wait for everything to be ready
function initializeWhenReady() {
    // Check if DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initializeDirectly);
    } else {
        initializeDirectly();
    }
}

function initializeDirectly() {
    console.log('🎯 DIRECT: Starting initialization...');
    
    try {
        // 1. Fix Logo Click - DIRECT APPROACH
        setTimeout(function() {
            try {
                fixLogoClick();
            } catch (e) {
                console.error('🎯 DIRECT: Error in fixLogoClick:', e);
            }
        }, 100);
        
        // 2. Fix Service Card Hover - DIRECT APPROACH  
        setTimeout(function() {
            try {
                fixServiceCardHover();
            } catch (e) {
                console.error('🎯 DIRECT: Error in fixServiceCardHover:', e);
            }
        }, 500);
        
        // 3. Initialize other features
        setTimeout(function() {
            try {
                initializeOtherFeatures();
            } catch (e) {
                console.error('🎯 DIRECT: Error in initializeOtherFeatures:', e);
            }
        }, 1000);
    } catch (e) {
        console.error('🎯 DIRECT: Error in initializeDirectly:', e);
    }
}

// LOGO CLICK FIX - Multiple approaches
function fixLogoClick() {
    console.log('🏠 DIRECT: Fixing logo click...');
    
    // Try multiple selectors
    const selectors = ['.navbar-brand', 'a.navbar-brand', '.navbar-header a', '.navbar .navbar-brand'];
    let logoElement = null;
    
    for (let selector of selectors) {
        logoElement = document.querySelector(selector);
        if (logoElement) {
            console.log('🏠 DIRECT: Found logo with selector:', selector);
            break;
        }
    }
    
    if (logoElement) {
        // Remove any existing event listeners by cloning
        const newLogo = logoElement.cloneNode(true);
        logoElement.parentNode.replaceChild(newLogo, logoElement);
        
        // Add multiple event handlers for reliability
        newLogo.addEventListener('click', function(e) {
            console.log('🏠 DIRECT: Logo clicked! Navigating to home...');
            e.preventDefault();
            e.stopPropagation();
            window.location.href = '/zhglxt/cms/index.html';
            return false;
        });
        
        // Also add mousedown for extra reliability
        newLogo.addEventListener('mousedown', function(e) {
            console.log('🏠 DIRECT: Logo mousedown detected');
        });
        
        // Add visual feedback
        newLogo.style.cursor = 'pointer';
        newLogo.style.transition = 'transform 0.2s ease';
        
        newLogo.addEventListener('mouseenter', function() {
            this.style.transform = 'scale(1.05)';
        });
        
        newLogo.addEventListener('mouseleave', function() {
            this.style.transform = 'scale(1)';
        });
        
        console.log('✅ DIRECT: Logo click fixed!');
    } else {
        console.log('❌ DIRECT: Logo element not found!');
    }
}

// SERVICE CARD HOVER FIX - Use same approach as service page
function fixServiceCardHover() {
    console.log('🎯 DIRECT: Fixing service card hover (using service page approach)...');
    
    const cards = document.querySelectorAll('.premium-service-card');
    console.log('🎯 DIRECT: Found', cards.length, 'service cards');
    
    if (cards.length === 0) {
        console.log('❌ DIRECT: No service cards found, retrying in 1 second...');
        setTimeout(fixServiceCardHover, 1000);
        return;
    }
    
    // Use the same approach as the service page - just add animate-in class
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    };

    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('animate-in');
                console.log('🎯 DIRECT: Card animated in via CSS');
            }
        });
    }, observerOptions);

    cards.forEach((card, index) => {
        console.log('🎯 DIRECT: Setting up card', index + 1, 'for CSS animation');
        observer.observe(card);
        
        // Add hover logging for debugging
        card.addEventListener('mouseenter', function() {
            console.log('🎯 DIRECT: Card', index + 1, 'HOVER IN (CSS handled)');
        });
        
        card.addEventListener('mouseleave', function() {
            console.log('🎯 DIRECT: Card', index + 1, 'HOVER OUT (CSS handled)');
        });
    });
    
    console.log('✅ DIRECT: Service cards set up with CSS animations (same as service page)!');
}

// Other features
function initializeOtherFeatures() {
    console.log('🎯 DIRECT: Initializing other features...');
    
    // Back to top button
    const backBtn = document.getElementById('backToTop');
    if (backBtn) {
        backBtn.addEventListener('click', function() {
            window.scrollTo({ top: 0, behavior: 'smooth' });
        });
        
        window.addEventListener('scroll', function() {
            if (window.scrollY > 300) {
                backBtn.style.opacity = '1';
                backBtn.style.visibility = 'visible';
            } else {
                backBtn.style.opacity = '0';
                backBtn.style.visibility = 'hidden';
            }
        });
    }
    
    // Smooth scrolling
    document.querySelectorAll('a[href^="#"]').forEach(function(link) {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const target = document.querySelector(this.getAttribute('href'));
            if (target) {
                target.scrollIntoView({ behavior: 'smooth' });
            }
        });
    });
    
    // Ensure home menu stays active (don't interfere with navbar-active.js)
    setTimeout(function() {
        console.log('🎯 DIRECT: Ensuring home menu is highlighted...');
        if (window.setActiveMenu) {
            window.setActiveMenu('index');
            console.log('🎯 DIRECT: Home menu set as active');
        }
    }, 200);
    
    console.log('✅ DIRECT: Other features initialized');
}

// Start everything - simplified approach
initializeWhenReady();

// Backup initialization on window load
window.addEventListener('load', function() {
    console.log('🎯 DIRECT: Window loaded, ensuring functionality...');
    setTimeout(function() {
        fixLogoClick();
        fixServiceCardHover();
    }, 500);
});

console.log('✅ DIRECT INDEX SCRIPT LOADED');