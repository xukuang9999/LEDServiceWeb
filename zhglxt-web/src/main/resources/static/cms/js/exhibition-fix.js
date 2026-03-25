// Exhibition Elements Display Fix
document.addEventListener('DOMContentLoaded', function () {
    // Function to fix exhibition title display
    function fixExhibitionTitles() {
        const titles = document.querySelectorAll('.exhibition-title');

        titles.forEach(function (title) {
            // Remove any inline styles that might be causing issues
            title.removeAttribute('style');

            // Ensure proper CSS classes and properties
            title.style.cssText = `
                font-size: 2rem !important;
                font-weight: 700 !important;
                color: #1f2937 !important;
                margin: 0 0 1rem 0 !important;
                padding: 0 !important;
                line-height: 1.3 !important;
                word-wrap: break-word !important;
                overflow-wrap: break-word !important;
                hyphens: auto !important;
                white-space: normal !important;
                text-overflow: unset !important;
                overflow: visible !important;
                display: block !important;
                width: 100% !important;
                max-width: 100% !important;
                min-height: auto !important;
                height: auto !important;
                max-height: none !important;
                position: static !important;
                visibility: visible !important;
                opacity: 1 !important;
            `;

            // Ensure parent containers don't clip content
            let parent = title.parentElement;
            while (parent && !parent.classList.contains('exhibition-card')) {
                parent.style.overflow = 'visible';
                parent.style.height = 'auto';
                parent.style.minHeight = 'auto';
                parent.style.maxHeight = 'none';
                parent = parent.parentElement;
            }
        });
    }

    // Function to fix appliance items display
    function fixApplianceItems() {
        const applianceItems = document.querySelectorAll('.appliance-item');
        
        applianceItems.forEach(function (item) {
            item.style.cssText = `
                display: flex !important;
                align-items: center !important;
                justify-content: center !important;
                width: 60px !important;
                height: 60px !important;
                border-radius: 50% !important;
                background: rgba(59, 130, 246, 0.1) !important;
                border: 2px solid rgba(59, 130, 246, 0.3) !important;
                transition: all 0.3s ease !important;
                visibility: visible !important;
                opacity: 1 !important;
            `;

            // Fix specific appliance types
            if (item.classList.contains('oven')) {
                item.style.background = 'rgba(239, 68, 68, 0.1) !important';
                item.style.borderColor = 'rgba(239, 68, 68, 0.3) !important';
                const icon = item.querySelector('i');
                if (icon) icon.style.color = '#ef4444 !important';
            } else if (item.classList.contains('washer')) {
                item.style.background = 'rgba(16, 185, 129, 0.1) !important';
                item.style.borderColor = 'rgba(16, 185, 129, 0.3) !important';
                const icon = item.querySelector('i');
                if (icon) icon.style.color = '#10b981 !important';
            }

            // Fix icons inside appliance items
            const icon = item.querySelector('i');
            if (icon) {
                icon.style.cssText = `
                    font-size: 1.5rem !important;
                    color: #3b82f6 !important;
                    visibility: visible !important;
                    opacity: 1 !important;
                `;
            }
        });

        // Fix appliance showcase container
        const applianceShowcases = document.querySelectorAll('.appliance-showcase');
        applianceShowcases.forEach(function (showcase) {
            showcase.style.cssText = `
                display: flex !important;
                justify-content: space-around !important;
                align-items: center !important;
                padding: 2rem !important;
                gap: 1.5rem !important;
                width: 100% !important;
                visibility: visible !important;
                opacity: 1 !important;
            `;
        });
    }

    // Function to fix feature tags display
    function fixFeatureTags() {
        const featureTags = document.querySelectorAll('.feature-tag');
        
        featureTags.forEach(function (tag) {
            tag.style.cssText = `
                display: inline-block !important;
                background: rgba(59, 130, 246, 0.1) !important;
                color: #3b82f6 !important;
                padding: 8px 16px !important;
                border-radius: 20px !important;
                font-size: 0.9rem !important;
                font-weight: 600 !important;
                border: 1px solid rgba(59, 130, 246, 0.2) !important;
                transition: all 0.3s ease !important;
                margin-bottom: 0.5rem !important;
                margin-right: 0.5rem !important;
                visibility: visible !important;
                opacity: 1 !important;
            `;
        });

        // Fix feature tags container
        const featureContainers = document.querySelectorAll('.exhibition-features');
        featureContainers.forEach(function (container) {
            container.style.cssText = `
                display: flex !important;
                flex-wrap: wrap !important;
                gap: 0.8rem !important;
                margin: 1.5rem 0 !important;
                visibility: visible !important;
                opacity: 1 !important;
            `;
        });
    }

    // Function to fix all exhibition visual elements
    function fixExhibitionVisuals() {
        // Fix tech devices
        const techDevices = document.querySelectorAll('.tech-device');
        techDevices.forEach(function (device) {
            device.style.cssText = `
                display: flex !important;
                align-items: center !important;
                justify-content: center !important;
                width: 50px !important;
                height: 50px !important;
                border-radius: 12px !important;
                background: rgba(139, 92, 246, 0.1) !important;
                border: 2px solid rgba(139, 92, 246, 0.3) !important;
                transition: all 0.3s ease !important;
                visibility: visible !important;
                opacity: 1 !important;
            `;

            const icon = device.querySelector('i');
            if (icon) {
                icon.style.cssText = `
                    font-size: 1.3rem !important;
                    color: #8b5cf6 !important;
                    visibility: visible !important;
                    opacity: 1 !important;
                `;
            }
        });

        // Fix automotive parts
        const parts = document.querySelectorAll('.part');
        parts.forEach(function (part) {
            part.style.cssText = `
                display: flex !important;
                align-items: center !important;
                justify-content: center !important;
                width: 40px !important;
                height: 40px !important;
                border-radius: 8px !important;
                background: rgba(6, 182, 212, 0.1) !important;
                border: 1px solid rgba(6, 182, 212, 0.3) !important;
                transition: all 0.3s ease !important;
                visibility: visible !important;
                opacity: 1 !important;
            `;

            const icon = part.querySelector('i');
            if (icon) {
                icon.style.cssText = `
                    font-size: 1rem !important;
                    color: #06b6d4 !important;
                    visibility: visible !important;
                    opacity: 1 !important;
                `;
            }
        });

        // Fix vehicle silhouette
        const vehicles = document.querySelectorAll('.vehicle-silhouette');
        vehicles.forEach(function (vehicle) {
            vehicle.style.cssText = `
                display: flex !important;
                align-items: center !important;
                justify-content: center !important;
                width: 80px !important;
                height: 50px !important;
                border-radius: 12px !important;
                background: rgba(6, 182, 212, 0.1) !important;
                border: 2px solid rgba(6, 182, 212, 0.3) !important;
                transition: all 0.3s ease !important;
                visibility: visible !important;
                opacity: 1 !important;
            `;

            const icon = vehicle.querySelector('i');
            if (icon) {
                icon.style.cssText = `
                    font-size: 1.8rem !important;
                    color: #06b6d4 !important;
                    visibility: visible !important;
                    opacity: 1 !important;
                `;
            }
        });
    }

    // Combined fix function
    function fixAllExhibitionElements() {
        fixExhibitionTitles();
        fixApplianceItems();
        fixFeatureTags();
        fixExhibitionVisuals();
    }

    // Fix all elements immediately
    fixAllExhibitionElements();

    // Fix elements after delays to handle any dynamic content
    setTimeout(fixAllExhibitionElements, 100);
    setTimeout(fixAllExhibitionElements, 500);
    setTimeout(fixAllExhibitionElements, 1000);

    // Fix elements on window resize
    window.addEventListener('resize', function () {
        setTimeout(fixAllExhibitionElements, 100);
    });

    // Observer to fix elements when new content is added
    const observer = new MutationObserver(function (mutations) {
        let shouldFix = false;
        mutations.forEach(function (mutation) {
            if (mutation.type === 'childList') {
                mutation.addedNodes.forEach(function (node) {
                    if (node.nodeType === 1) { // Element node
                        if (node.classList && (
                            node.classList.contains('exhibition-title') ||
                            node.classList.contains('appliance-item') ||
                            node.classList.contains('feature-tag') ||
                            node.classList.contains('exhibition-card') ||
                            node.classList.contains('exhibition-preview-card')
                        ) || node.querySelector && (
                            node.querySelector('.exhibition-title') ||
                            node.querySelector('.appliance-item') ||
                            node.querySelector('.feature-tag')
                        )) {
                            shouldFix = true;
                        }
                    }
                });
            }
        });

        if (shouldFix) {
            setTimeout(fixAllExhibitionElements, 50);
        }
    });

    // Start observing
    observer.observe(document.body, {
        childList: true,
        subtree: true
    });
});

// Additional fix for any CSS animations or transitions that might interfere
document.addEventListener('DOMContentLoaded', function () {
    const style = document.createElement('style');
    style.textContent = `
        .exhibition-title {
            animation: none !important;
            transition: none !important;
        }
        
        .exhibition-title * {
            animation: none !important;
            transition: none !important;
        }

        .appliance-item,
        .feature-tag,
        .tech-device,
        .part,
        .vehicle-silhouette {
            animation: none !important;
        }

        .appliance-showcase,
        .tech-displays,
        .automotive-display,
        .exhibition-features {
            animation: none !important;
        }

        /* Force visibility for all exhibition elements */
        .exhibition-title,
        .appliance-item,
        .feature-tag,
        .tech-device,
        .part,
        .vehicle-silhouette,
        .appliance-showcase,
        .tech-displays,
        .automotive-display,
        .exhibition-features,
        .exhibition-visual,
        .exhibition-content {
            visibility: visible !important;
            opacity: 1 !important;
            display: block !important;
        }

        .appliance-item,
        .tech-device,
        .part,
        .vehicle-silhouette {
            display: flex !important;
        }

        .feature-tag {
            display: inline-block !important;
        }

        .appliance-showcase,
        .tech-displays,
        .automotive-display,
        .exhibition-features {
            display: flex !important;
        }

        .exhibition-content {
            display: flex !important;
            flex-direction: column !important;
        }
    `;
    document.head.appendChild(style);
});