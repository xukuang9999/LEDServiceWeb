/**
 * Modal Test Button
 * Simple test button to verify independent modal functionality
 */

(function() {
    'use strict';

    console.log('🧪 Loading Modal Test Button...');

    /**
     * Create test button
     */
    function createTestButton() {
        const button = document.createElement('button');
        button.innerHTML = '🧪 Test Independent Modal';
        button.style.cssText = `
            position: fixed !important;
            top: 20px !important;
            right: 20px !important;
            background: #28a745 !important;
            color: white !important;
            border: none !important;
            padding: 12px 16px !important;
            border-radius: 6px !important;
            cursor: pointer !important;
            z-index: 999998 !important;
            font-size: 14px !important;
            font-weight: 600 !important;
            box-shadow: 0 4px 12px rgba(40, 167, 69, 0.3) !important;
            transition: all 0.2s ease !important;
        `;
        
        button.addEventListener('mouseenter', function() {
            this.style.background = '#218838';
            this.style.transform = 'translateY(-2px)';
            this.style.boxShadow = '0 6px 16px rgba(40, 167, 69, 0.4)';
        });
        
        button.addEventListener('mouseleave', function() {
            this.style.background = '#28a745';
            this.style.transform = 'translateY(0)';
            this.style.boxShadow = '0 4px 12px rgba(40, 167, 69, 0.3)';
        });
        
        button.addEventListener('click', function() {
            console.log('🧪 Testing independent modal...');
            
            if (window.showIndependentModal) {
                // Create test data
                const testData = {
                    id: 'test',
                    title: 'Test Independent Modal',
                    content: `
                        <h3>Independent Modal Test</h3>
                        <p>This is a test of the independent news modal system.</p>
                        <h4>Features:</h4>
                        <ul>
                            <li>✅ Completely independent from parent components</li>
                            <li>✅ Maximize/Restore functionality</li>
                            <li>✅ No interference from parent containers</li>
                            <li>✅ Fixed positioning at z-index 999999</li>
                            <li>✅ Responsive design</li>
                        </ul>
                        <h4>Controls:</h4>
                        <ul>
                            <li>🔲 Click maximize button to toggle full screen</li>
                            <li>❌ Click close button or press Escape to close</li>
                            <li>🖱️ Click outside modal to close</li>
                        </ul>
                        <p><strong>This modal should display properly without any interference from parent components!</strong></p>
                    `,
                    createTime: new Date().toISOString(),
                    imageUrl: null
                };
                
                // Create temporary modal element for data extraction
                const tempModal = document.createElement('div');
                tempModal.id = 'modal-test';
                tempModal.innerHTML = `
                    <div class="modal-title">${testData.title}</div>
                    <div class="modal-content-text">${testData.content}</div>
                    <div class="modal-date">${testData.createTime}</div>
                `;
                tempModal.style.display = 'none';
                document.body.appendChild(tempModal);
                
                // Show modal
                window.showIndependentModal('test');
                
                // Clean up temp element
                setTimeout(() => {
                    if (tempModal && tempModal.parentNode) {
                        tempModal.parentNode.removeChild(tempModal);
                    }
                }, 1000);
                
            } else {
                alert('Independent modal system not loaded yet. Please wait a moment and try again.');
            }
        });
        
        document.body.appendChild(button);
        console.log('🧪 Test button created (top-right corner)');
    }

    // Create test button when page loads
    document.addEventListener('DOMContentLoaded', function() {
        setTimeout(createTestButton, 2000);
    });

    console.log('✅ Modal Test Button loaded');

})();