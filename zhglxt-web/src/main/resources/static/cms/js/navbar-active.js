// Enhanced Navbar Active Menu Highlighting
document.addEventListener('DOMContentLoaded', function() {
    
    // Debug logging
    const DEBUG = true;
    function log(message, data) {
        if (DEBUG) {
            console.log('[Navbar Active]', message, data || '');
        }
    }
    
    // Function to set active menu item
    function setActiveMenuItem() {
        const currentPath = window.location.pathname;
        const currentPage = currentPath.split('/').pop().replace('.html', '');
        
        log('Current path:', currentPath);
        log('Current page:', currentPage);
        
        // Remove any existing active classes
        document.querySelectorAll('.navbar-nav .nav-item, .navbar-nav .dropdown-item').forEach(item => {
            item.classList.remove('active');
        });
        
        // Page mapping for different URLs
        const pageMapping = {
            'index': 'index',
            'exhibition': 'exhibition',
            'exhibition-detail': 'exhibition',
            'service': 'service',
            'service-detail': 'service',
            'news': 'news',
            'contact': 'contact',
            '': 'index' // Default to index for root path
        };
        
        // Get the mapped page name
        const mappedPage = pageMapping[currentPage] || currentPage;
        log('Mapped page:', mappedPage);
        
        // Strategy 1: Find by data-page attribute
        let activeItem = document.querySelector(`.navbar-nav .nav-item[data-page="${mappedPage}"]`);
        log('Found by data-page:', activeItem);
        
        // Strategy 2: Find by href matching
        if (!activeItem) {
            const links = document.querySelectorAll('.navbar-nav a[href]');
            for (let link of links) {
                const href = link.getAttribute('href');
                if (href && (href.includes(`${mappedPage}.html`) || href.endsWith(`/${mappedPage}.html`))) {
                    activeItem = link.closest('.nav-item');
                    log('Found by href matching:', activeItem);
                    break;
                }
            }
        }
        
        // Strategy 3: Find dropdown items
        if (!activeItem) {
            const dropdownItem = document.querySelector(`.navbar-nav .dropdown-item[data-page="${mappedPage}"]`);
            if (dropdownItem) {
                dropdownItem.classList.add('active');
                log('Found dropdown item:', dropdownItem);
                
                // Also mark parent dropdown as active
                const parentDropdown = dropdownItem.closest('.dropdown');
                if (parentDropdown) {
                    parentDropdown.classList.add('active');
                    log('Activated parent dropdown:', parentDropdown);
                }
                return;
            }
        }
        
        // Strategy 4: Partial URL matching
        if (!activeItem) {
            const links = document.querySelectorAll('.navbar-nav a[href]');
            for (let link of links) {
                const href = link.getAttribute('href');
                if (href && currentPath.includes(href.replace('/cms/', '').replace('.html', ''))) {
                    activeItem = link.closest('.nav-item');
                    log('Found by partial matching:', activeItem);
                    break;
                }
            }
        }
        
        // Strategy 5: Default to home for root paths
        if (!activeItem && (currentPage === '' || currentPath === '/' || currentPath.endsWith('/cms/') || currentPath.endsWith('/cms/index.html'))) {
            activeItem = document.querySelector('.navbar-nav .nav-item[data-page="index"]');
            log('Defaulted to home:', activeItem);
        }
        
        // Apply active class
        if (activeItem) {
            activeItem.classList.add('active');
            log('Activated menu item:', activeItem);
            
            // If it's a dropdown item, also mark parent as active
            const parentDropdown = activeItem.closest('.dropdown');
            if (parentDropdown && !parentDropdown.classList.contains('active')) {
                parentDropdown.classList.add('active');
                log('Activated parent dropdown:', parentDropdown);
            }
        } else {
            log('No matching menu item found');
        }
    }
    
    // Function to handle menu clicks and update active state
    function handleMenuClick() {
        document.querySelectorAll('.navbar-nav .nav-link, .navbar-nav .dropdown-link').forEach(link => {
            link.addEventListener('click', function(e) {
                // Don't prevent default, let navigation happen
                
                // Remove active from all items
                document.querySelectorAll('.navbar-nav .nav-item, .navbar-nav .dropdown-item').forEach(item => {
                    item.classList.remove('active');
                });
                
                // Add active to clicked item
                const navItem = this.closest('.nav-item') || this.closest('.dropdown-item');
                if (navItem) {
                    navItem.classList.add('active');
                    
                    // If it's a dropdown item, also mark parent as active
                    const parentDropdown = navItem.closest('.dropdown');
                    if (parentDropdown) {
                        parentDropdown.classList.add('active');
                    }
                }
            });
        });
    }
    
    // Function to handle responsive menu behavior
    function handleResponsiveMenu() {
        const navbarToggle = document.querySelector('.navbar-toggle');
        const navbarCollapse = document.querySelector('.navbar-collapse');
        
        if (navbarToggle && navbarCollapse) {
            // Close mobile menu when clicking on a link
            document.querySelectorAll('.navbar-nav a').forEach(link => {
                link.addEventListener('click', function() {
                    if (window.innerWidth < 768) {
                        navbarCollapse.classList.remove('in');
                        navbarToggle.classList.add('collapsed');
                        navbarToggle.setAttribute('aria-expanded', 'false');
                    }
                });
            });
        }
    }
    
    // Function to add visual feedback for active items
    function addVisualFeedback() {
        // Add subtle animation when hovering over menu items
        document.querySelectorAll('.navbar-nav .nav-link, .navbar-nav .dropdown-link').forEach(link => {
            link.addEventListener('mouseenter', function() {
                if (!this.closest('.nav-item, .dropdown-item').classList.contains('active')) {
                    this.style.transform = 'translateY(-1px)';
                    this.style.transition = 'all 0.2s ease';
                }
            });
            
            link.addEventListener('mouseleave', function() {
                this.style.transform = '';
            });
        });
    }
    
    // Initialize all functionality
    setActiveMenuItem();
    handleMenuClick();
    handleResponsiveMenu();
    addVisualFeedback();
    
    // Update active menu on browser back/forward
    window.addEventListener('popstate', function() {
        setTimeout(setActiveMenuItem, 100);
    });
    
    // Update active menu if URL changes (for SPA-like behavior)
    let currentUrl = window.location.href;
    setInterval(function() {
        if (window.location.href !== currentUrl) {
            currentUrl = window.location.href;
            setActiveMenuItem();
        }
    }, 1000);
    
    // Debug function (remove in production)
    window.debugNavbar = function() {
        console.log('Current path:', window.location.pathname);
        console.log('Current page:', window.location.pathname.split('/').pop().replace('.html', ''));
        console.log('Active items:', document.querySelectorAll('.navbar-nav .active'));
    };
});

// Additional function to manually set active menu (can be called from pages)
window.setActiveMenu = function(pageName) {
    // Remove existing active classes
    document.querySelectorAll('.navbar-nav .nav-item, .navbar-nav .dropdown-item').forEach(item => {
        item.classList.remove('active');
    });
    
    // Find and activate the specified page
    const targetItem = document.querySelector(`.navbar-nav .nav-item[data-page="${pageName}"], .navbar-nav .dropdown-item[data-page="${pageName}"]`);
    if (targetItem) {
        targetItem.classList.add('active');
        
        // If it's a dropdown item, also mark parent as active
        const parentDropdown = targetItem.closest('.dropdown');
        if (parentDropdown) {
            parentDropdown.classList.add('active');
        }
    }
};

// Enhanced functionality for better user experience
document.addEventListener('DOMContentLoaded', function() {
    
    // Function to add loading states
    function addLoadingState(element) {
        if (element) {
            element.classList.add('loading');
            setTimeout(() => {
                element.classList.remove('loading');
            }, 300);
        }
    }
    
    // Function to handle page transitions
    function handlePageTransition(targetUrl) {
        // Add loading state to clicked menu item
        const targetItem = document.querySelector(`a[href="${targetUrl}"]`);
        if (targetItem) {
            addLoadingState(targetItem.closest('.nav-item') || targetItem.closest('.dropdown-item'));
        }
        
        // Smooth transition effect
        document.body.style.opacity = '0.95';
        setTimeout(() => {
            document.body.style.opacity = '1';
        }, 200);
    }
    
    // Enhanced click handling with transitions
    document.querySelectorAll('.navbar-nav a[href]').forEach(link => {
        link.addEventListener('click', function(e) {
            const href = this.getAttribute('href');
            if (href && !href.startsWith('http') && !href.startsWith('#')) {
                handlePageTransition(href);
            }
        });
    });
    
    // Function to validate menu structure
    function validateMenuStructure() {
        const issues = [];
        
        // Check for missing data-page attributes
        document.querySelectorAll('.navbar-nav .nav-item').forEach((item, index) => {
            if (!item.hasAttribute('data-page')) {
                issues.push(`Nav item ${index + 1} missing data-page attribute`);
            }
        });
        
        // Check for duplicate data-page values
        const pageValues = [];
        document.querySelectorAll('[data-page]').forEach(item => {
            const page = item.getAttribute('data-page');
            if (pageValues.includes(page)) {
                issues.push(`Duplicate data-page value: ${page}`);
            }
            pageValues.push(page);
        });
        
        if (issues.length > 0) {
            console.warn('Navbar structure issues found:', issues);
        }
        
        return issues.length === 0;
    }
    
    // Function to create breadcrumb navigation
    function createBreadcrumb() {
        const activeItem = document.querySelector('.navbar-nav .active');
        if (activeItem) {
            const breadcrumbContainer = document.querySelector('.breadcrumb-container');
            if (breadcrumbContainer) {
                const pageName = activeItem.getAttribute('data-page') || 'Current Page';
                breadcrumbContainer.innerHTML = `
                    <nav aria-label="breadcrumb">
                        <ol class="breadcrumb">
                            <li class="breadcrumb-item"><a href="/cms/index.html">Home</a></li>
                            <li class="breadcrumb-item active" aria-current="page">${pageName}</li>
                        </ol>
                    </nav>
                `;
            }
        }
    }
    
    // Initialize enhanced features
    setTimeout(() => {
        validateMenuStructure();
        createBreadcrumb();
    }, 100);
    
    // Performance monitoring
    const performanceStart = performance.now();
    window.addEventListener('load', function() {
        const performanceEnd = performance.now();
        console.log(`Navbar initialization completed in ${(performanceEnd - performanceStart).toFixed(2)}ms`);
    });
});

// Global utility functions
window.navbarUtils = {
    // Force refresh of active menu
    refreshActiveMenu: function() {
        const event = new Event('DOMContentLoaded');
        document.dispatchEvent(event);
    },
    
    // Get current active menu item
    getActiveMenuItem: function() {
        return document.querySelector('.navbar-nav .active');
    },
    
    // Check if menu item exists
    hasMenuItem: function(pageName) {
        return !!document.querySelector(`[data-page="${pageName}"]`);
    },
    
    // Get all menu items
    getAllMenuItems: function() {
        return Array.from(document.querySelectorAll('.navbar-nav [data-page]')).map(item => ({
            page: item.getAttribute('data-page'),
            text: item.textContent.trim(),
            href: item.querySelector('a')?.getAttribute('href'),
            isActive: item.classList.contains('active')
        }));
    }
};