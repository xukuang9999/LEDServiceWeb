// SERVICE INDEX - Dedicated service card animations and interactions
console.log('🎯 SERVICE INDEX LOADING...');

// Service card functionality
document.addEventListener('DOMContentLoaded', function() {
    console.log('🎯 Service Index: DOM loaded, initializing service cards');
    
    // Initialize service cards immediately
    initializeServiceCards();
});

// Also initialize on window load as backup
window.addEventListener('load', function() {
    console.log('🎯 Service Index: Window loaded, ensuring service cards are ready');
    initializeServiceCards();
});

function initializeServiceCards() {
    const cards = document.querySelectorAll('.premium-service-card');
    console.log('🎯 Service Index: Found', cards.length, 'service cards');
    
    if (cards.length === 0) {
        console.log('❌ Service Index: No service cards found, retrying in 500ms...');
        setTimeout(initializeServiceCards, 500);
        return;
    }
    
    cards.forEach(function(card, index) {
        console.log('🎯 Service Index: Setting up card', index + 1);
        
        // Remove any existing event listeners by cloning the element
        const newCard = card.cloneNode(true);
        card.parentNode.replaceChild(newCard, card);
        
        // Setup hover effects on the new element
        setupServiceCardHover(newCard, index);
        
        // Setup entrance animation
        setupServiceCardEntrance(newCard, index);
    });
}

function setupServiceCardHover(card, index) {
    console.log('🎯 Service Index: Adding hover effects to card', index + 1);
    
    // Mouse enter effect
    card.addEventListener('mouseenter', function() {
        console.log('🎯 Service Index: Card', index + 1, 'hover IN');
        
        // Exhibition-style hover effects
        this.style.setProperty('transform', 'translateY(-15px) scale(1.02)', 'important');
        this.style.setProperty('box-shadow', '0 25px 50px rgba(0, 0, 0, 0.2)', 'important');
        this.style.setProperty('transition', 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)', 'important');
        
        // Visual feedback
        this.style.setProperty('background', 'rgba(59, 130, 246, 0.05)', 'important');
        this.style.setProperty('border', '1px solid rgba(59, 130, 246, 0.3)', 'important');
        
        // Add glow effect
        this.style.setProperty('filter', 'brightness(1.05)', 'important');
    });
    
    // Mouse leave effect
    card.addEventListener('mouseleave', function() {
        console.log('🎯 Service Index: Card', index + 1, 'hover OUT');
        
        // Reset to normal state
        this.style.setProperty('transform', 'translateY(0) scale(1)', 'important');
        this.style.setProperty('box-shadow', 'none', 'important');
        this.style.setProperty('transition', 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)', 'important');
        
        // Reset visual feedback
        this.style.setProperty('background', '', 'important');
        this.style.setProperty('border', '', 'important');
        this.style.setProperty('filter', '', 'important');
    });
    
    console.log('✅ Service Index: Hover effects added to card', index + 1);
}

function setupServiceCardEntrance(card, index) {
    console.log('🎯 Service Index: Setting up entrance animation for card', index + 1);
    
    // Set initial hidden state
    card.style.setProperty('opacity', '0', 'important');
    card.style.setProperty('transform', 'translateY(60px) scale(0.9)', 'important');
    card.style.setProperty('transition', 'all 0.8s cubic-bezier(0.4, 0, 0.2, 1)', 'important');
    
    // Animate entrance with staggered delay
    const delay = (index * 300) + 800;
    setTimeout(function() {
        console.log('🎯 Service Index: Animating card', index + 1, 'entrance');
        
        // Add CSS class for additional effects
        card.classList.add('animate-in', 'service-card-visible');
        
        // Set visible state
        card.style.setProperty('opacity', '1', 'important');
        card.style.setProperty('transform', 'translateY(0) scale(1)', 'important');
        
        console.log('✅ Service Index: Card', index + 1, 'entrance animation complete');
    }, delay);
}

// Intersection Observer for scroll-triggered animations
function setupScrollAnimations() {
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const card = entry.target;
                const index = Array.from(document.querySelectorAll('.premium-service-card')).indexOf(card);
                
                console.log('🎯 Service Index: Card', index + 1, 'entered viewport');
                
                // Trigger entrance animation
                setTimeout(() => {
                    card.classList.add('animate-in', 'service-card-visible');
                    card.style.setProperty('opacity', '1', 'important');
                    card.style.setProperty('transform', 'translateY(0) scale(1)', 'important');
                }, index * 150);
                
                observer.unobserve(card);
            }
        });
    }, {
        threshold: 0.2,
        rootMargin: '50px'
    });
    
    // Observe all service cards
    document.querySelectorAll('.premium-service-card').forEach(card => {
        observer.observe(card);
    });
}

// Utility function to refresh service cards
window.refreshServiceCards = function() {
    console.log('🎯 Service Index: Refreshing service cards...');
    initializeServiceCards();
};

// Export functions for external use
window.serviceIndex = {
    initialize: initializeServiceCards,
    setupHover: setupServiceCardHover,
    setupEntrance: setupServiceCardEntrance,
    setupScrollAnimations: setupScrollAnimations
};

console.log('✅ Service Index: Module loaded and ready');