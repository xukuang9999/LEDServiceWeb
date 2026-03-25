/**
 * Modal Status Check
 * Simple status indicator to show if the modal system is working
 */

(function() {
    'use strict';

    console.log('📊 Loading Modal Status Check...');

    /**
     * Create status indicator
     */
    function createStatusIndicator() {
        const indicator = document.createElement('div');
        indicator.id = 'modal-status-indicator';
        
        // Check if super modal is available
        const isWorking = typeof window.showSuperModal === 'function';
        
        indicator.style.cssText = `
            position: fixed !important;
            top: 50% !important;
            left: 10px !important;
            transform: translateY(-50%) !important;
            background: ${isWorking ? '#28a745' : '#dc3545'} !important;
            color: white !important;
            padding: 10px 15px !important;
            border-radius: 6px !important;
            z-index: 999997 !important;
            font-family: monospace !important;
            font-size: 12px !important;
            font-weight: bold !important;
            box-shadow: 0 4px 12px rgba(0,0,0,0.3) !important;
            cursor: pointer !important;
            transition: all 0.3s ease !important;
        `;

        indicator.innerHTML = `
            <div style="margin-bottom: 5px;">📊 Modal Status</div>
            <div style="font-size: 10px;">
                ${isWorking ? '✅ Super Modal Ready' : '❌ Modal Not Ready'}
            </div>
        `;

        indicator.addEventListener('click', function() {
            if (isWorking) {
                console.log('📊 Testing super modal...');
                if (window.testSuperModal) {
                    window.testSuperModal();
                } else if (window.showSuperModal) {
                    window.showSuperModal('status-test');
                }
            } else {
                alert('Modal system not ready. Please check console for errors.');
            }
        });

        indicator.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-50%) scale(1.05)';
            this.style.boxShadow = '0 6px 16px rgba(0,0,0,0.4)';
        });

        indicator.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(-50%) scale(1)';
            this.style.boxShadow = '0 4px 12px rgba(0,0,0,0.3)';
        });

        document.body.appendChild(indicator);
        
        console.log('📊 Status indicator created:', isWorking ? 'Working' : 'Not Working');
        
        // Auto-hide after 10 seconds if working
        if (isWorking) {
            setTimeout(() => {
                indicator.style.opacity = '0.3';
                indicator.style.pointerEvents = 'none';
            }, 10000);
        }
    }

    /**
     * Check modal system status
     */
    function checkModalStatus() {
        console.log('📊 === MODAL SYSTEM STATUS ===');
        
        const status = {
            superModal: typeof window.showSuperModal === 'function',
            testSuperModal: typeof window.testSuperModal === 'function',
            diagnosticTools: typeof window.forceShowModal === 'function',
            newsButtons: document.querySelectorAll('.news-read-btn, .news-detail-btn, .btn-read-more').length
        };
        
        console.log('📊 Super Modal Available:', status.superModal ? '✅' : '❌');
        console.log('📊 Test Function Available:', status.testSuperModal ? '✅' : '❌');
        console.log('📊 Diagnostic Tools Available:', status.diagnosticTools ? '✅' : '❌');
        console.log('📊 News Buttons Found:', status.newsButtons);
        
        if (status.superModal) {
            console.log('📊 ✅ Modal system is ready!');
        } else {
            console.log('📊 ❌ Modal system not ready. Check for script loading errors.');
        }
        
        return status;
    }

    // Create status indicator when page loads
    document.addEventListener('DOMContentLoaded', function() {
        setTimeout(() => {
            createStatusIndicator();
            checkModalStatus();
        }, 2000);
    });

    // Export for console use
    window.checkModalStatus = checkModalStatus;

    console.log('✅ Modal Status Check loaded');

})();