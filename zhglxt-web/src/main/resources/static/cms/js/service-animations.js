// Service Section Smooth Mouse Animations
document.addEventListener('DOMContentLoaded', function() {
    
    // Configuration
    const config = {
        // Mouse tracking sensitivity
        mouseSensitivity: 0.15,
        // Animation smoothness (lower = smoother)
        smoothness: 0.08,
        // Maximum tilt angle in degrees
        maxTilt: 15,
        // Maximum scale factor
        maxScale: 1.05,
        // Glow effect intensity
        glowIntensity: 0.3,
        // Animation duration for transitions
        transitionDuration: 300,
        // Parallax effect strength
        parallaxStrength: 0.02
    };
    
    // Mouse position tracking
    let mouseX = 0;
    let mouseY = 0;
    let isMouseMoving = false;
    let mouseTimeout;
    
    // Initialize service animations
    function initServiceAnimations() {
        const serviceCards = document.querySelectorAll('.premium-service-card');
        const servicesContainer = document.querySelector('.services-container');
        
        if (!serviceCards.length || !servicesContainer) {
            console.log('Service cards not found, skipping animations');
            return;
        }
        
        console.log(`Initializing animations for ${serviceCards.length} service cards`);
        
        // Add mouse tracking to the services container
        setupMouseTracking(servicesContainer);
        
        // Initialize each service card
        serviceCards.forEach((card, index) => {
            setupServiceCard(card, index);
        });
        
        // Add global mouse move listener
        setupGlobalMouseTracking();
        
        // Add scroll-based animations
        setupScrollAnimations();
    }
    
    // Setup mouse tracking for the services container
    function setupMouseTracking(container) {
        container.addEventListener('mousemove', handleMouseMove);
        container.addEventListener('mouseenter', handleMouseEnter);
        container.addEventListener('mouseleave', handleMouseLeave);
    }
    
    // Setup individual service card animations
    function setupServiceCard(card, index) {
        // Add data attributes for animation
        card.setAttribute('data-card-index', index);
        card.style.transition = `transform ${config.transitionDuration}ms cubic-bezier(0.4, 0, 0.2, 1)`;
        
        // Setup card-specific mouse events
        card.addEventListener('mouseenter', (e) => handleCardMouseEnter(e, card, index));
        card.addEventListener('mouseleave', (e) => handleCardMouseLeave(e, card, index));
        card.addEventListener('mousemove', (e) => handleCardMouseMove(e, card, index));
        
        // Setup visual elements
        setupCardVisualEffects(card, index);
        
        // Add click animation
        card.addEventListener('click', (e) => handleCardClick(e, card, index));
    }
    
    // Setup visual effects for each card
    function setupCardVisualEffects(card, index) {
        // Add glow effect element
        const glowElement = document.createElement('div');
        glowElement.className = 'card-glow-effect';
        glowElement.style.cssText = `
            position: absolute;
            top: -2px;
            left: -2px;
            right: -2px;
            bottom: -2px;
            background: linear-gradient(45deg, #3b82f6, #8b5cf6, #06b6d4, #10b981);
            border-radius: inherit;
            opacity: 0;
            z-index: -1;
            filter: blur(8px);
            transition: opacity ${config.transitionDuration}ms ease;
        `;
        
        // Make card position relative if not already
        if (getComputedStyle(card).position === 'static') {
            card.style.position = 'relative';
        }
        
        card.appendChild(glowElement);
        
        // Add floating particles
        createFloatingParticles(card, index);
        
        // Add shimmer effect
        createShimmerEffect(card, index);
    }
    
    // Create floating particles for visual appeal
    function createFloatingParticles(card, index) {
        const particlesContainer = document.createElement('div');
        particlesContainer.className = 'floating-particles';
        particlesContainer.style.cssText = `
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            pointer-events: none;
            overflow: hidden;
            border-radius: inherit;
            z-index: 1;
        `;
        
        // Create individual particles
        for (let i = 0; i < 5; i++) {
            const particle = document.createElement('div');
            particle.className = 'floating-particle';
            particle.style.cssText = `
                position: absolute;
                width: 4px;
                height: 4px;
                background: rgba(59, 130, 246, 0.6);
                border-radius: 50%;
                opacity: 0;
                animation: floatParticle${i} ${4 + i}s ease-in-out infinite;
                animation-delay: ${i * 0.5}s;
            `;
            
            particlesContainer.appendChild(particle);
        }
        
        card.appendChild(particlesContainer);
        
        // Add particle animation keyframes
        addParticleAnimations();
    }
    
    // Create shimmer effect
    function createShimmerEffect(card, index) {
        const shimmer = document.createElement('div');
        shimmer.className = 'card-shimmer';
        shimmer.style.cssText = `
            position: absolute;
            top: 0;
            left: -100%;
            width: 100%;
            height: 100%;
            background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
            pointer-events: none;
            z-index: 2;
            transition: left 0.6s ease;
        `;
        
        card.appendChild(shimmer);
    }
    
    // Handle mouse move on services container
    function handleMouseMove(e) {
        const rect = e.currentTarget.getBoundingClientRect();
        mouseX = (e.clientX - rect.left) / rect.width;
        mouseY = (e.clientY - rect.top) / rect.height;
        
        isMouseMoving = true;
        clearTimeout(mouseTimeout);
        mouseTimeout = setTimeout(() => {
            isMouseMoving = false;
        }, 100);
        
        // Update all cards based on global mouse position
        updateCardsGlobalMouse();
    }
    
    // Handle mouse enter on services container
    function handleMouseEnter(e) {
        document.body.classList.add('services-hover');
        
        // Activate all cards
        const cards = document.querySelectorAll('.premium-service-card');
        cards.forEach(card => {
            card.classList.add('container-hover');
        });
    }
    
    // Handle mouse leave on services container
    function handleMouseLeave(e) {
        document.body.classList.remove('services-hover');
        
        // Reset all cards
        const cards = document.querySelectorAll('.premium-service-card');
        cards.forEach(card => {
            card.classList.remove('container-hover');
            resetCardTransform(card);
        });
    }
    
    // Handle mouse enter on individual card
    function handleCardMouseEnter(e, card, index) {
        card.classList.add('card-hover');
        
        // Activate glow effect
        const glowEffect = card.querySelector('.card-glow-effect');
        if (glowEffect) {
            glowEffect.style.opacity = config.glowIntensity;
        }
        
        // Trigger shimmer effect
        const shimmer = card.querySelector('.card-shimmer');
        if (shimmer) {
            shimmer.style.left = '100%';
            setTimeout(() => {
                shimmer.style.left = '-100%';
            }, 600);
        }
        
        // Activate floating particles
        const particles = card.querySelectorAll('.floating-particle');
        particles.forEach(particle => {
            particle.style.opacity = '1';
        });
        
        // Scale up slightly
        card.style.transform = `scale(${config.maxScale})`;
        
        // Add ripple effect
        createRippleEffect(e, card);
    }
    
    // Handle mouse leave on individual card
    function handleCardMouseLeave(e, card, index) {
        card.classList.remove('card-hover');
        
        // Deactivate glow effect
        const glowEffect = card.querySelector('.card-glow-effect');
        if (glowEffect) {
            glowEffect.style.opacity = '0';
        }
        
        // Hide floating particles
        const particles = card.querySelectorAll('.floating-particle');
        particles.forEach(particle => {
            particle.style.opacity = '0';
        });
        
        // Reset transform
        resetCardTransform(card);
    }
    
    // Handle mouse move on individual card
    function handleCardMouseMove(e, card, index) {
        const rect = card.getBoundingClientRect();
        const cardCenterX = rect.left + rect.width / 2;
        const cardCenterY = rect.top + rect.height / 2;
        
        const deltaX = (e.clientX - cardCenterX) / (rect.width / 2);
        const deltaY = (e.clientY - cardCenterY) / (rect.height / 2);
        
        // Apply 3D tilt effect
        const tiltX = deltaY * config.maxTilt * config.mouseSensitivity;
        const tiltY = -deltaX * config.maxTilt * config.mouseSensitivity;
        
        // Apply transform with smooth transition
        requestAnimationFrame(() => {
            card.style.transform = `
                perspective(1000px)
                rotateX(${tiltX}deg)
                rotateY(${tiltY}deg)
                scale(${config.maxScale})
                translateZ(20px)
            `;
        });
        
        // Update glow position
        updateGlowPosition(card, deltaX, deltaY);
    }
    
    // Handle card click animation
    function handleCardClick(e, card, index) {
        // Create click wave effect
        createClickWave(e, card);
        
        // Temporary scale animation
        card.style.transform = `scale(0.98)`;
        setTimeout(() => {
            card.style.transform = `scale(${config.maxScale})`;
        }, 150);
    }
    
    // Update all cards based on global mouse position
    function updateCardsGlobalMouse() {
        const cards = document.querySelectorAll('.premium-service-card');
        
        cards.forEach((card, index) => {
            if (!card.classList.contains('card-hover')) {
                // Apply subtle parallax effect based on global mouse position
                const parallaxX = (mouseX - 0.5) * 20 * config.parallaxStrength;
                const parallaxY = (mouseY - 0.5) * 20 * config.parallaxStrength;
                
                requestAnimationFrame(() => {
                    card.style.transform = `
                        translateX(${parallaxX}px)
                        translateY(${parallaxY}px)
                        scale(1)
                    `;
                });
            }
        });
    }
    
    // Reset card transform
    function resetCardTransform(card) {
        requestAnimationFrame(() => {
            card.style.transform = 'perspective(1000px) rotateX(0deg) rotateY(0deg) scale(1) translateZ(0px)';
        });
    }
    
    // Update glow position based on mouse
    function updateGlowPosition(card, deltaX, deltaY) {
        const glowEffect = card.querySelector('.card-glow-effect');
        if (glowEffect) {
            const glowX = (deltaX + 1) * 50; // Convert to percentage
            const glowY = (deltaY + 1) * 50;
            
            glowEffect.style.background = `
                radial-gradient(circle at ${glowX}% ${glowY}%, 
                rgba(59, 130, 246, 0.3) 0%, 
                rgba(139, 92, 246, 0.2) 50%, 
                transparent 70%)
            `;
        }
    }
    
    // Create ripple effect on mouse enter
    function createRippleEffect(e, card) {
        const ripple = document.createElement('div');
        const rect = card.getBoundingClientRect();
        const size = Math.max(rect.width, rect.height);
        const x = e.clientX - rect.left - size / 2;
        const y = e.clientY - rect.top - size / 2;
        
        ripple.style.cssText = `
            position: absolute;
            width: ${size}px;
            height: ${size}px;
            left: ${x}px;
            top: ${y}px;
            background: radial-gradient(circle, rgba(59, 130, 246, 0.3) 0%, transparent 70%);
            border-radius: 50%;
            pointer-events: none;
            z-index: 0;
            animation: rippleExpand 0.6s ease-out forwards;
        `;
        
        card.appendChild(ripple);
        
        setTimeout(() => {
            ripple.remove();
        }, 600);
    }
    
    // Create click wave effect
    function createClickWave(e, card) {
        const wave = document.createElement('div');
        const rect = card.getBoundingClientRect();
        const x = e.clientX - rect.left;
        const y = e.clientY - rect.top;
        
        wave.style.cssText = `
            position: absolute;
            left: ${x}px;
            top: ${y}px;
            width: 0;
            height: 0;
            border-radius: 50%;
            background: rgba(59, 130, 246, 0.4);
            pointer-events: none;
            z-index: 3;
            animation: clickWaveExpand 0.5s ease-out forwards;
        `;
        
        card.appendChild(wave);
        
        setTimeout(() => {
            wave.remove();
        }, 500);
    }
    
    // Setup global mouse tracking for enhanced effects
    function setupGlobalMouseTracking() {
        document.addEventListener('mousemove', (e) => {
            // Update CSS custom properties for global mouse position
            document.documentElement.style.setProperty('--mouse-x', `${e.clientX}px`);
            document.documentElement.style.setProperty('--mouse-y', `${e.clientY}px`);
        });
    }
    
    // Setup scroll-based animations
    function setupScrollAnimations() {
        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.classList.add('animate-in');
                    
                    // Stagger animation for cards
                    const cards = entry.target.querySelectorAll('.premium-service-card');
                    cards.forEach((card, index) => {
                        setTimeout(() => {
                            card.classList.add('scroll-animate');
                        }, index * 100);
                    });
                }
            });
        }, {
            threshold: 0.1,
            rootMargin: '0px 0px -50px 0px'
        });
        
        const servicesSection = document.querySelector('#services');
        if (servicesSection) {
            observer.observe(servicesSection);
        }
    }
    
    // Add particle animation keyframes
    function addParticleAnimations() {
        if (document.querySelector('#particle-animations')) return;
        
        const style = document.createElement('style');
        style.id = 'particle-animations';
        style.textContent = `
            @keyframes floatParticle0 {
                0%, 100% { transform: translate(10px, 10px); opacity: 0; }
                50% { transform: translate(30px, -20px); opacity: 1; }
            }
            @keyframes floatParticle1 {
                0%, 100% { transform: translate(80%, 90%); opacity: 0; }
                50% { transform: translate(70%, 10%); opacity: 1; }
            }
            @keyframes floatParticle2 {
                0%, 100% { transform: translate(20%, 80%); opacity: 0; }
                50% { transform: translate(90%, 20%); opacity: 1; }
            }
            @keyframes floatParticle3 {
                0%, 100% { transform: translate(90%, 20%); opacity: 0; }
                50% { transform: translate(10%, 90%); opacity: 1; }
            }
            @keyframes floatParticle4 {
                0%, 100% { transform: translate(50%, 10%); opacity: 0; }
                50% { transform: translate(60%, 80%); opacity: 1; }
            }
            @keyframes rippleExpand {
                0% { transform: scale(0); opacity: 0.6; }
                100% { transform: scale(1); opacity: 0; }
            }
            @keyframes clickWaveExpand {
                0% { width: 0; height: 0; opacity: 0.8; }
                100% { width: 100px; height: 100px; margin: -50px 0 0 -50px; opacity: 0; }
            }
        `;
        document.head.appendChild(style);
    }
    
    // Performance optimization: throttle mouse events
    function throttle(func, limit) {
        let inThrottle;
        return function() {
            const args = arguments;
            const context = this;
            if (!inThrottle) {
                func.apply(context, args);
                inThrottle = true;
                setTimeout(() => inThrottle = false, limit);
            }
        }
    }
    
    // Initialize everything
    initServiceAnimations();
    
    // Export for debugging
    window.serviceAnimations = {
        config,
        reinitialize: initServiceAnimations,
        updateConfig: (newConfig) => Object.assign(config, newConfig)
    };
    
    console.log('Service animations initialized successfully');
});