// SIMPLE DIRECT ANIMATIONS - No conflicts
console.log('🚀 SIMPLE ANIMATIONS LOADING...');

// Wait for page to fully load
window.addEventListener('load', function() {
    console.log('📄 Page loaded, starting animations');
    
    // 1. Service Cards - Now handled by service-index.js
    console.log('🎯 Service cards handled by dedicated service-index.js module');
    
    // 2. Navbar Brand - IMMEDIATE hover and click handling
    const navbar = document.querySelector('.navbar-brand');
    console.log('🎯 Navbar brand element:', navbar);
    
    if (navbar) {
        // Hover effects
        navbar.addEventListener('mouseenter', function() {
            console.log('🎯 Navbar hover: IN');
            this.style.setProperty('transform', 'scale(1.05)', 'important');
            this.style.setProperty('transition', 'transform 0.2s ease', 'important');
        });
        
        navbar.addEventListener('mouseleave', function() {
            console.log('🎯 Navbar hover: OUT');
            this.style.setProperty('transform', 'scale(1)', 'important');
        });
        
        // Click handling - FORCE navigation
        navbar.addEventListener('click', function(e) {
            console.log('🏠 Navbar brand clicked!');
            console.log('🏠 Event:', e);
            console.log('🏠 Current href:', this.getAttribute('href'));
            
            // Force navigation immediately
            const targetUrl = '/zhglxt/cms/index.html';
            console.log('🏠 Forcing navigation to:', targetUrl);
            window.location.href = targetUrl;
            
            // Also prevent default just in case
            e.preventDefault();
            e.stopPropagation();
            return false;
        });
        
        console.log('🎯 Navbar hover and click ready');
    } else {
        console.log('❌ Navbar brand element not found!');
    }
    
    // 3. Back to Top Button - Simple implementation
    let backBtn = document.getElementById('backToTop');
    if (backBtn) {
        backBtn.style.cssText = `
            position: fixed !important;
            bottom: 30px !important;
            right: 30px !important;
            width: 50px !important;
            height: 50px !important;
            background: linear-gradient(135deg, #3b82f6, #8b5cf6) !important;
            color: white !important;
            border: none !important;
            border-radius: 50% !important;
            display: flex !important;
            align-items: center !important;
            justify-content: center !important;
            font-size: 1.2rem !important;
            cursor: pointer !important;
            z-index: 9999 !important;
            transition: all 0.3s ease !important;
            opacity: 0 !important;
            visibility: hidden !important;
        `;
        
        function toggleBackBtn() {
            if (window.scrollY > 300) {
                backBtn.style.setProperty('opacity', '1', 'important');
                backBtn.style.setProperty('visibility', 'visible', 'important');
            } else {
                backBtn.style.setProperty('opacity', '0', 'important');
                backBtn.style.setProperty('visibility', 'hidden', 'important');
            }
        }
        
        backBtn.onclick = () => window.scrollTo({ top: 0, behavior: 'smooth' });
        window.addEventListener('scroll', toggleBackBtn);
        
        console.log('🎯 Back to top ready');
    }
    
    console.log('🎉 SIMPLE ANIMATIONS COMPLETE');
});