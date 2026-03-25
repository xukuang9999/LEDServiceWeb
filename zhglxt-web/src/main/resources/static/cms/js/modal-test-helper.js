/**
 * Modal Test Helper
 * Simple helper to test modal functionality
 */

(function() {
    'use strict';

    // Create test button for modal system
    function createTestButton() {
        // Check if button already exists
        if (document.querySelector('#modal-test-btn')) {
            return;
        }

        const testBtn = document.createElement('button');
        testBtn.id = 'modal-test-btn';
        testBtn.innerHTML = '<i class="fas fa-vial"></i> Test Modal';
        testBtn.style.cssText = `
            position: fixed;
            bottom: 20px;
            right: 20px;
            z-index: 1000;
            background: linear-gradient(135deg, #28a745, #20c997);
            color: white;
            border: none;
            padding: 12px 20px;
            border-radius: 25px;
            cursor: pointer;
            font-weight: 600;
            box-shadow: 0 4px 12px rgba(40, 167, 69, 0.3);
            transition: all 0.3s ease;
        `;

        testBtn.addEventListener('click', function() {
            if (window.testIndependentModal) {
                window.testIndependentModal();
            } else {
                alert('Modal system not loaded yet. Please wait a moment and try again.');
            }
        });

        testBtn.addEventListener('mouseover', function() {
            this.style.transform = 'translateY(-2px)';
            this.style.boxShadow = '0 8px 25px rgba(40, 167, 69, 0.4)';
        });

        testBtn.addEventListener('mouseout', function() {
            this.style.transform = 'translateY(0)';
            this.style.boxShadow = '0 4px 12px rgba(40, 167, 69, 0.3)';
        });

        document.body.appendChild(testBtn);
    }

    // Initialize when DOM is ready
    document.addEventListener('DOMContentLoaded', function() {
        setTimeout(createTestButton, 2000);
    });

})();