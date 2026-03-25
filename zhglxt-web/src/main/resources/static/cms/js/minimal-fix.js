// MINIMAL FIX - Absolutely minimal approach to avoid any loading issues
console.log('🔧 MINIMAL FIX: Loading...');

// Simple function to fix logo click
function fixLogo() {
    try {
        const logo = document.querySelector('.navbar-brand');
        if (logo) {
            logo.addEventListener('click', function(e) {
                console.log('🏠 Logo clicked - navigating home');
                window.location.href = '/zhglxt/cms/index.html';
            });
            console.log('✅ Logo click fixed');
        }
    } catch (e) {
        console.log('❌ Logo fix error:', e);
    }
}

// Simple function to add service card hover
function fixServiceHover() {
    try {
        const cards = document.querySelectorAll('.premium-service-card');
        console.log('🎯 Found', cards.length, 'service cards');
        
        cards.forEach(function(card, index) {
            card.addEventListener('mouseenter', function() {
                this.style.transform = 'translateY(-10px)';
                this.style.boxShadow = '0 20px 40px rgba(0,0,0,0.15)';
                this.style.transition = 'all 0.3s ease';
                console.log('🎯 Card', index + 1, 'hover in');
            });
            
            card.addEventListener('mouseleave', function() {
                this.style.transform = 'translateY(0)';
                this.style.boxShadow = '';
                console.log('🎯 Card', index + 1, 'hover out');
            });
        });
        
        console.log('✅ Service hover fixed');
    } catch (e) {
        console.log('❌ Service hover error:', e);
    }
}

// Simple function to fix navbar active
function fixNavbarActive() {
    try {
        // Remove active from all
        document.querySelectorAll('.navbar-nav li').forEach(function(item) {
            item.classList.remove('active');
        });
        
        // Add active to home
        const homeItem = document.querySelector('[data-page="index"]') || 
                        document.querySelector('.navbar-nav li:first-child');
        
        if (homeItem) {
            homeItem.classList.add('active');
            const link = homeItem.querySelector('a');
            if (link) {
                link.style.color = '#3b82f6';
                link.style.fontWeight = '600';
            }
            console.log('✅ Home menu activated');
        }
    } catch (e) {
        console.log('❌ Navbar active error:', e);
    }
}

// Initialize everything with delays to avoid conflicts
setTimeout(fixLogo, 100);
setTimeout(fixServiceHover, 500);
setTimeout(fixNavbarActive, 1000);

// Also try on window load
window.addEventListener('load', function() {
    setTimeout(fixLogo, 100);
    setTimeout(fixServiceHover, 200);
    setTimeout(fixNavbarActive, 300);
});

console.log('✅ MINIMAL FIX: Loaded');