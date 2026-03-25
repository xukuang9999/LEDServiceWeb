/**
 * Modal System Status Checker
 * Comprehensive status checking and debugging for the modal system
 */

(function() {
    'use strict';

    console.log('📊 Loading Modal System Status Checker...');

    /**
     * Check the status of all modal system components
     */
    function checkModalSystemStatus() {
        const status = {
            timestamp: new Date().toISOString(),
            page: {
                url: window.location.pathname,
                title: document.title,
                isNewsPage: isNewsPage(),
                isHomePage: isHomePage()
            },
            modalSystems: {
                optimizedModal: !!window.optimizedNewsModal,
                draggableModal: !!window.initializeDraggableModals,
                simpleModal: !!window.showSimpleModal,
                bootstrapModal: checkBootstrapModal()
            },
            integration: {
                optimizedIntegration: !!window.checkOptimizedModalStatus,
                interferenceFixAvailable: !!window.checkNewsMenuInterference,
                interferenceFixed: checkInterferenceStatus()
            },
            buttons: {
                newsButtons: document.querySelectorAll('.news-detail-btn, .news-read-btn, .btn-read-more').length,
                fixedButtons: document.querySelectorAll('.interference-fixed, .optimized-modal-trigger').length,
                bootstrapButtons: document.querySelectorAll('[data-toggle="modal"]').length
            },
            modals: {
                bootstrapModals: document.querySelectorAll('.modal[id^="modal-"]').length,
                hiddenModals: document.querySelectorAll('.modal[id^="modal-"][style*="display: none"]').length,
                optimizedModal: document.querySelectorAll('#optimized-news-modal').length
            },
            errors: [],
            warnings: [],
            recommendations: []
        };

        // Check for potential issues
        analyzeStatus(status);

        return status;
    }

    /**
     * Check if current page is a news page
     */
    function isNewsPage() {
        return window.location.pathname.includes('news') ||
               document.querySelector('.news-grid') ||
               document.querySelector('.news-section') ||
               document.querySelector('.news-card');
    }

    /**
     * Check if current page is home page
     */
    function isHomePage() {
        return window.location.pathname === '/' ||
               window.location.pathname.includes('index') ||
               document.querySelector('.index-page');
    }

    /**
     * Check Bootstrap modal availability
     */
    function checkBootstrapModal() {
        if (typeof $ !== 'undefined' && $.fn.modal) {
            return 'jQuery + Bootstrap';
        } else if (typeof bootstrap !== 'undefined' && bootstrap.Modal) {
            return 'Bootstrap 5';
        } else {
            return false;
        }
    }

    /**
     * Check interference fix status
     */
    function checkInterferenceStatus() {
        if (window.checkNewsMenuInterference) {
            try {
                const status = window.checkNewsMenuInterference();
                return status.interferenceFixed;
            } catch (e) {
                return false;
            }
        }
        return false;
    }

    /**
     * Analyze status and provide recommendations
     */
    function analyzeStatus(status) {
        // Check for errors
        if (!status.modalSystems.optimizedModal) {
            status.errors.push('Optimized modal system not loaded');
        }

        if (status.page.isNewsPage && !status.integration.interferenceFixed) {
            status.errors.push('News page interference not fixed');
        }

        if (status.buttons.newsButtons === 0) {
            status.warnings.push('No news buttons found on page');
        }

        if (status.buttons.bootstrapButtons > 0 && status.page.isNewsPage) {
            status.warnings.push('Bootstrap modal buttons still present on news page');
        }

        // Provide recommendations
        if (status.errors.length > 0) {
            status.recommendations.push('Check console for JavaScript errors');
            status.recommendations.push('Verify all modal scripts are loaded');
        }

        if (status.warnings.length > 0) {
            status.recommendations.push('Review modal button configuration');
        }

        if (status.modalSystems.optimizedModal && status.integration.optimizedIntegration) {
            status.recommendations.push('System appears to be working correctly');
        }
    }

    /**
     * Display status in console with formatting
     */
    function displayStatus(status) {
        console.group('📊 Modal System Status Report');
        
        console.log('🌐 Page Info:', status.page);
        console.log('🔧 Modal Systems:', status.modalSystems);
        console.log('🔗 Integration:', status.integration);
        console.log('🔘 Buttons:', status.buttons);
        console.log('📱 Modals:', status.modals);

        if (status.errors.length > 0) {
            console.group('❌ Errors');
            status.errors.forEach(error => console.error(error));
            console.groupEnd();
        }

        if (status.warnings.length > 0) {
            console.group('⚠️ Warnings');
            status.warnings.forEach(warning => console.warn(warning));
            console.groupEnd();
        }

        if (status.recommendations.length > 0) {
            console.group('💡 Recommendations');
            status.recommendations.forEach(rec => console.info(rec));
            console.groupEnd();
        }

        console.groupEnd();
    }

    /**
     * Create visual status indicator
     */
    function createStatusIndicator() {
        // Remove existing indicator
        const existing = document.getElementById('modal-status-indicator');
        if (existing) existing.remove();

        const status = checkModalSystemStatus();
        const isHealthy = status.errors.length === 0 && status.modalSystems.optimizedModal;

        const indicator = document.createElement('div');
        indicator.id = 'modal-status-indicator';
        indicator.style.cssText = `
            position: fixed;
            bottom: 20px;
            left: 20px;
            background: ${isHealthy ? '#28a745' : '#dc3545'};
            color: white;
            padding: 8px 12px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: 600;
            z-index: 9999;
            cursor: pointer;
            transition: all 0.3s ease;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
        `;

        indicator.innerHTML = `
            <i class="fas fa-${isHealthy ? 'check-circle' : 'exclamation-triangle'}"></i>
            Modal System: ${isHealthy ? 'OK' : 'Issues'}
        `;

        indicator.addEventListener('click', () => {
            displayStatus(checkModalSystemStatus());
        });

        indicator.addEventListener('mouseenter', () => {
            indicator.style.transform = 'scale(1.05)';
        });

        indicator.addEventListener('mouseleave', () => {
            indicator.style.transform = 'scale(1)';
        });

        document.body.appendChild(indicator);
    }

    /**
     * Test modal functionality
     */
    function testModalFunctionality() {
        console.log('🧪 Testing modal functionality...');

        const tests = [];

        // Test optimized modal
        if (window.optimizedNewsModal) {
            tests.push({
                name: 'Optimized Modal',
                test: () => {
                    window.optimizedNewsModal.open({
                        id: 'test',
                        title: 'Test Modal',
                        content: '<p>This is a test of the optimized modal system.</p>',
                        createTime: new Date().toLocaleDateString()
                    });
                    setTimeout(() => window.optimizedNewsModal.close(), 3000);
                },
                result: 'success'
            });
        }

        // Test interference fix
        if (window.checkNewsMenuInterference) {
            const interferenceStatus = window.checkNewsMenuInterference();
            tests.push({
                name: 'Interference Fix',
                test: () => interferenceStatus,
                result: interferenceStatus.interferenceFixed ? 'success' : 'failed'
            });
        }

        // Run tests
        tests.forEach(test => {
            try {
                const result = test.test();
                console.log(`✅ ${test.name}: ${test.result}`, result);
            } catch (error) {
                console.error(`❌ ${test.name}: failed`, error);
            }
        });

        return tests;
    }

    /**
     * Auto-monitor system health
     */
    function startHealthMonitoring() {
        // Update status indicator every 5 seconds
        setInterval(() => {
            createStatusIndicator();
        }, 5000);

        // Log status every 30 seconds in development
        if (window.location.hostname === 'localhost' || window.location.hostname.includes('dev')) {
            setInterval(() => {
                const status = checkModalSystemStatus();
                if (status.errors.length > 0 || status.warnings.length > 0) {
                    displayStatus(status);
                }
            }, 30000);
        }
    }

    // Export functions
    window.checkModalSystemStatus = checkModalSystemStatus;
    window.displayModalStatus = () => displayStatus(checkModalSystemStatus());
    window.testModalFunctionality = testModalFunctionality;
    window.createModalStatusIndicator = createStatusIndicator;

    // Initialize when DOM is ready
    function init() {
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', () => {
                setTimeout(() => {
                    createStatusIndicator();
                    startHealthMonitoring();
                    
                    // Auto-display status if there are issues
                    const status = checkModalSystemStatus();
                    if (status.errors.length > 0) {
                        setTimeout(() => displayStatus(status), 2000);
                    }
                }, 1000);
            });
        } else {
            setTimeout(() => {
                createStatusIndicator();
                startHealthMonitoring();
                
                // Auto-display status if there are issues
                const status = checkModalSystemStatus();
                if (status.errors.length > 0) {
                    setTimeout(() => displayStatus(status), 2000);
                }
            }, 1000);
        }
    }

    // Start initialization
    init();

    console.log('✅ Modal System Status Checker loaded');

})();