/**
 * Modal Diagnostic Tool
 * Helps identify what's interfering with the modal display
 */

(function() {
    'use strict';

    console.log('🔍 Loading Modal Diagnostic Tool...');

    /**
     * Check z-index hierarchy
     */
    function checkZIndexHierarchy() {
        console.log('🔍 === Z-INDEX HIERARCHY CHECK ===');
        
        const elements = document.querySelectorAll('*');
        const zIndexElements = [];
        
        elements.forEach(el => {
            const style = window.getComputedStyle(el);
            const zIndex = parseInt(style.zIndex);
            
            if (!isNaN(zIndex) && zIndex > 1000) {
                zIndexElements.push({
                    element: el,
                    zIndex: zIndex,
                    position: style.position,
                    tagName: el.tagName,
                    id: el.id,
                    className: el.className
                });
            }
        });
        
        // Sort by z-index
        zIndexElements.sort((a, b) => b.zIndex - a.zIndex);
        
        console.log('🔍 High z-index elements found:', zIndexElements.length);
        zIndexElements.forEach((item, index) => {
            console.log(`${index + 1}. z-index: ${item.zIndex}, ${item.tagName}${item.id ? '#' + item.id : ''}${item.className ? '.' + item.className.split(' ')[0] : ''}`);
        });
        
        return zIndexElements;
    }

    /**
     * Check for modal interference
     */
    function checkModalInterference() {
        console.log('🔍 === MODAL INTERFERENCE CHECK ===');
        
        // Check for existing modals
        const bootstrapModals = document.querySelectorAll('.modal');
        console.log('🔍 Bootstrap modals found:', bootstrapModals.length);
        
        bootstrapModals.forEach((modal, index) => {
            const style = window.getComputedStyle(modal);
            console.log(`Modal ${index + 1}: display=${style.display}, z-index=${style.zIndex}, position=${style.position}`);
        });
        
        // Check for modal backdrops
        const backdrops = document.querySelectorAll('.modal-backdrop');
        console.log('🔍 Modal backdrops found:', backdrops.length);
        
        backdrops.forEach((backdrop, index) => {
            const style = window.getComputedStyle(backdrop);
            console.log(`Backdrop ${index + 1}: display=${style.display}, z-index=${style.zIndex}`);
        });
        
        // Check body classes
        console.log('🔍 Body classes:', document.body.className);
        
        // Check for overflow hidden
        const bodyStyle = window.getComputedStyle(document.body);
        console.log('🔍 Body overflow:', bodyStyle.overflow);
    }

    /**
     * Force show independent modal
     */
    function forceShowModal() {
        console.log('🔍 Force showing independent modal...');
        
        // Remove any existing modal
        const existing = document.getElementById('independent-news-modal');
        if (existing) {
            existing.remove();
        }
        
        // Create a very simple, high-priority modal
        const modal = document.createElement('div');
        modal.id = 'independent-news-modal';
        modal.style.cssText = `
            position: fixed !important;
            top: 0 !important;
            left: 0 !important;
            width: 100vw !important;
            height: 100vh !important;
            background: rgba(255, 0, 0, 0.8) !important;
            z-index: 2147483647 !important;
            display: flex !important;
            align-items: center !important;
            justify-content: center !important;
            pointer-events: auto !important;
            visibility: visible !important;
            opacity: 1 !important;
        `;
        
        modal.innerHTML = `
            <div style="
                background: white !important;
                padding: 40px !important;
                border-radius: 10px !important;
                text-align: center !important;
                box-shadow: 0 20px 40px rgba(0,0,0,0.5) !important;
                max-width: 500px !important;
                position: relative !important;
                z-index: 2147483647 !important;
            ">
                <h2 style="color: #333 !important; margin-top: 0 !important;">🔍 Diagnostic Modal</h2>
                <p style="color: #666 !important;">If you can see this modal with a RED background, the independent modal system is working!</p>
                <p style="color: #666 !important;">Z-index: 2147483647 (maximum possible)</p>
                <button onclick="this.closest('#independent-news-modal').remove()" style="
                    background: #dc3545 !important;
                    color: white !important;
                    border: none !important;
                    padding: 10px 20px !important;
                    border-radius: 5px !important;
                    cursor: pointer !important;
                    font-size: 16px !important;
                ">Close Diagnostic Modal</button>
            </div>
        `;
        
        // Add directly to body
        document.body.appendChild(modal);
        
        console.log('🔍 Diagnostic modal created');
        console.log('🔍 Modal element:', modal);
        console.log('🔍 Computed z-index:', window.getComputedStyle(modal).zIndex);
        
        return modal;
    }

    /**
     * Create diagnostic panel
     */
    function createDiagnosticPanel() {
        const panel = document.createElement('div');
        panel.id = 'modal-diagnostic-panel';
        panel.style.cssText = `
            position: fixed !important;
            bottom: 20px !important;
            right: 20px !important;
            background: #007bff !important;
            color: white !important;
            padding: 15px !important;
            border-radius: 8px !important;
            z-index: 999998 !important;
            font-family: monospace !important;
            font-size: 12px !important;
            max-width: 250px !important;
            box-shadow: 0 4px 12px rgba(0,0,0,0.3) !important;
        `;

        panel.innerHTML = `
            <div style="font-weight: bold; margin-bottom: 10px; color: #fff;">🔍 Modal Diagnostic</div>
            <button onclick="checkZIndexHierarchy()" style="margin: 2px; padding: 5px 8px; font-size: 11px; background: white; color: #007bff; border: none; border-radius: 3px; cursor: pointer; width: 100%;">Check Z-Index</button>
            <button onclick="checkModalInterference()" style="margin: 2px; padding: 5px 8px; font-size: 11px; background: white; color: #007bff; border: none; border-radius: 3px; cursor: pointer; width: 100%;">Check Interference</button>
            <button onclick="forceShowModal()" style="margin: 2px; padding: 5px 8px; font-size: 11px; background: #28a745; color: white; border: none; border-radius: 3px; cursor: pointer; width: 100%;">Force Show Modal</button>
            <button onclick="document.getElementById('modal-diagnostic-panel').remove()" style="margin: 2px; padding: 5px 8px; font-size: 11px; background: #dc3545; color: white; border: none; border-radius: 3px; cursor: pointer; width: 100%;">Close Panel</button>
        `;

        document.body.appendChild(panel);
        console.log('🔍 Diagnostic panel created');
    }

    // Make functions global
    window.checkZIndexHierarchy = checkZIndexHierarchy;
    window.checkModalInterference = checkModalInterference;
    window.forceShowModal = forceShowModal;
    window.createModalDiagnosticPanel = createDiagnosticPanel;

    // Auto-create diagnostic panel
    document.addEventListener('DOMContentLoaded', function() {
        setTimeout(createDiagnosticPanel, 3000);
    });

    console.log('✅ Modal Diagnostic Tool loaded');

})();