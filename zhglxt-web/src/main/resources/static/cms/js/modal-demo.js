/**
 * Modal Demo Script
 * Provides demo functionality and testing utilities for the independent modal system
 */

(function() {
    'use strict';

    // Demo configuration
    const demoConfig = {
        enableConsoleCommands: true,
        showWelcomeMessage: true,
        autoCreateTestData: true
    };

    /**
     * Sample news data for testing
     */
    const sampleNewsData = [
        {
            id: 'demo-1',
            title: 'Revolutionary LED Technology Breakthrough',
            content: `
                <p>Scientists have developed a groundbreaking LED technology that promises to revolutionize the lighting industry with unprecedented efficiency and color accuracy.</p>
                
                <h3>Key Innovations</h3>
                <ul>
                    <li>50% improvement in energy efficiency</li>
                    <li>Enhanced color reproduction with 99% accuracy</li>
                    <li>Extended lifespan of up to 100,000 hours</li>
                    <li>Reduced manufacturing costs</li>
                </ul>
                
                <h3>Market Impact</h3>
                <p>This breakthrough is expected to accelerate the adoption of LED technology across residential, commercial, and industrial applications.</p>
                
                <blockquote>
                    "This represents a quantum leap in LED technology that will benefit consumers and the environment alike."
                    <footer>— Dr. Sarah Johnson, Lead Researcher</footer>
                </blockquote>
            `,
            createTime: new Date().toISOString(),
            author: 'Research Team',
            category: 'Technology',
            imageUrl: 'https://images.unsplash.com/photo-1558618666-fcd25c85cd64?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80',
            tags: ['LED', 'Technology', 'Innovation', 'Research']
        },
        {
            id: 'demo-2',
            title: 'Smart Lighting Solutions for Modern Homes',
            content: `
                <p>The integration of smart technology with LED lighting systems is transforming how we interact with our living spaces, offering unprecedented control and customization.</p>
                
                <h3>Smart Features</h3>
                <ul>
                    <li>Voice control integration with popular assistants</li>
                    <li>Automated scheduling and dimming</li>
                    <li>Color temperature adjustment throughout the day</li>
                    <li>Energy usage monitoring and optimization</li>
                </ul>
                
                <h3>Benefits for Homeowners</h3>
                <p>Smart lighting systems offer numerous advantages:</p>
                <ul>
                    <li><strong>Energy Savings:</strong> Up to 60% reduction in electricity costs</li>
                    <li><strong>Convenience:</strong> Remote control from anywhere</li>
                    <li><strong>Security:</strong> Automated lighting patterns when away</li>
                    <li><strong>Health:</strong> Circadian rhythm support</li>
                </ul>
                
                <p>As smart home technology continues to evolve, LED lighting remains at the forefront of innovation, providing the foundation for truly intelligent living spaces.</p>
            `,
            createTime: new Date(Date.now() - 86400000).toISOString(), // Yesterday
            author: 'Smart Home Team',
            category: 'Smart Home',
            tags: ['Smart Lighting', 'Home Automation', 'LED', 'IoT']
        },
        {
            id: 'demo-3',
            title: 'Sustainable Lighting: Environmental Impact of LED Technology',
            content: `
                <p>As environmental consciousness grows, LED technology stands out as a crucial component in reducing our carbon footprint and creating a more sustainable future.</p>
                
                <h3>Environmental Benefits</h3>
                <p>LED technology contributes to environmental sustainability through:</p>
                
                <h4>Energy Efficiency</h4>
                <ul>
                    <li>80% less energy consumption than incandescent bulbs</li>
                    <li>50% less energy than fluorescent lights</li>
                    <li>Significant reduction in greenhouse gas emissions</li>
                </ul>
                
                <h4>Longevity and Waste Reduction</h4>
                <ul>
                    <li>25-50 times longer lifespan than traditional bulbs</li>
                    <li>Reduced packaging and transportation waste</li>
                    <li>No mercury or hazardous materials</li>
                    <li>Fully recyclable components</li>
                </ul>
                
                <h3>Global Impact</h3>
                <p>The widespread adoption of LED technology has resulted in:</p>
                <ul>
                    <li>Billions of tons of CO2 emissions prevented</li>
                    <li>Reduced strain on power grids worldwide</li>
                    <li>Lower demand for new power generation facilities</li>
                    <li>Significant cost savings for consumers and businesses</li>
                </ul>
                
                <h3>Future Outlook</h3>
                <p>As LED technology continues to improve and costs decrease, we can expect even greater environmental benefits. The next generation of LEDs promises even higher efficiency and new applications that will further reduce our environmental impact.</p>
                
                <p>By choosing LED lighting solutions, consumers and businesses are not just saving money – they're actively contributing to a more sustainable planet for future generations.</p>
            `,
            createTime: new Date(Date.now() - 172800000).toISOString(), // 2 days ago
            author: 'Sustainability Team',
            category: 'Environment',
            imageUrl: 'https://images.unsplash.com/photo-1497435334941-8c899ee9e8e9?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80',
            tags: ['Sustainability', 'Environment', 'LED', 'Green Technology']
        }
    ];

    /**
     * Create demo data elements in the DOM
     */
    function createDemoData() {
        sampleNewsData.forEach(newsData => {
            // Check if data element already exists
            if (!document.querySelector(`#news-data-${newsData.id}`)) {
                const dataElement = document.createElement('script');
                dataElement.type = 'application/json';
                dataElement.id = `news-data-${newsData.id}`;
                dataElement.className = 'news-data-json demo-data';
                dataElement.textContent = JSON.stringify(newsData);
                document.body.appendChild(dataElement);
            }
        });
    }

    /**
     * Create demo buttons for testing
     */
    function createDemoButtons() {
        // Check if demo container already exists
        if (document.querySelector('#modal-demo-container')) {
            return;
        }

        const demoContainer = document.createElement('div');
        demoContainer.id = 'modal-demo-container';
        demoContainer.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            z-index: 1000;
            background: white;
            border-radius: 8px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
            padding: 16px;
            max-width: 250px;
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
        `;

        demoContainer.innerHTML = `
            <div style="margin-bottom: 12px; font-weight: 600; color: #333; font-size: 14px;">
                <i class="fas fa-flask" style="color: #667eea; margin-right: 6px;"></i>
                Modal Demo
            </div>
            <div style="display: flex; flex-direction: column; gap: 8px;">
                ${sampleNewsData.map(news => `
                    <button onclick="showIndependentNewsModal('${news.id}')" 
                            style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); 
                                   color: white; border: none; padding: 8px 12px; 
                                   border-radius: 4px; cursor: pointer; font-size: 12px;
                                   transition: transform 0.2s ease;"
                            onmouseover="this.style.transform='translateY(-1px)'"
                            onmouseout="this.style.transform='translateY(0)'">
                        ${news.title.substring(0, 25)}...
                    </button>
                `).join('')}
                <button onclick="window.testIndependentModal()" 
                        style="background: #28a745; color: white; border: none; 
                               padding: 8px 12px; border-radius: 4px; cursor: pointer; 
                               font-size: 12px; margin-top: 4px;
                               transition: transform 0.2s ease;"
                        onmouseover="this.style.transform='translateY(-1px)'"
                        onmouseout="this.style.transform='translateY(0)'">
                    <i class="fas fa-vial"></i> Test Modal
                </button>
                <button onclick="document.getElementById('modal-demo-container').remove()" 
                        style="background: #dc3545; color: white; border: none; 
                               padding: 6px 10px; border-radius: 4px; cursor: pointer; 
                               font-size: 11px; margin-top: 4px;
                               transition: transform 0.2s ease;"
                        onmouseover="this.style.transform='translateY(-1px)'"
                        onmouseout="this.style.transform='translateY(0)'">
                    <i class="fas fa-times"></i> Close
                </button>
            </div>
        `;

        document.body.appendChild(demoContainer);
    }

    /**
     * Setup console commands for testing
     */
    function setupConsoleCommands() {
        if (!demoConfig.enableConsoleCommands) return;

        // Global demo functions
        window.modalDemo = {
            // Show demo buttons
            showButtons: createDemoButtons,
            
            // Hide demo buttons
            hideButtons: function() {
                const container = document.querySelector('#modal-demo-container');
                if (container) container.remove();
            },
            
            // Test specific demo article
            showDemo: function(demoId) {
                const validIds = sampleNewsData.map(d => d.id);
                if (validIds.includes(demoId)) {
                    return window.showIndependentNewsModal(demoId);
                } else {
                    console.error('Invalid demo ID. Valid IDs:', validIds);
                }
            },
            
            // Show all available demos
            listDemos: function() {
                console.table(sampleNewsData.map(d => ({
                    id: d.id,
                    title: d.title,
                    category: d.category,
                    author: d.author
                })));
            },
            
            // Get modal system status
            status: function() {
                if (window.modalIntegration) {
                    return window.modalIntegration.getStatus();
                } else {
                    return {
                        independentModalAvailable: typeof window.showIndependentNewsModal === 'function',
                        testModalAvailable: typeof window.testIndependentModal === 'function',
                        integrationAvailable: false
                    };
                }
            },
            
            // Enable debug mode
            debug: function(enabled = true) {
                if (window.modalIntegration) {
                    window.modalIntegration.setDebugMode(enabled);
                }
                console.log('Debug mode ' + (enabled ? 'enabled' : 'disabled'));
            },
            
            // Performance test
            performanceTest: async function(iterations = 5) {
                console.log(`Running performance test with ${iterations} iterations...`);
                const times = [];
                
                for (let i = 0; i < iterations; i++) {
                    const start = performance.now();
                    await window.showIndependentNewsModal('demo-1');
                    
                    // Close modal
                    const closeBtn = document.querySelector('#close-btn');
                    if (closeBtn) closeBtn.click();
                    
                    // Wait for close animation
                    await new Promise(resolve => setTimeout(resolve, 500));
                    
                    const end = performance.now();
                    times.push(end - start);
                    
                    console.log(`Iteration ${i + 1}: ${(end - start).toFixed(2)}ms`);
                }
                
                const average = times.reduce((a, b) => a + b, 0) / times.length;
                console.log(`Average time: ${average.toFixed(2)}ms`);
                console.log(`Min time: ${Math.min(...times).toFixed(2)}ms`);
                console.log(`Max time: ${Math.max(...times).toFixed(2)}ms`);
                
                return { times, average, min: Math.min(...times), max: Math.max(...times) };
            }
        };

        // Show welcome message
        if (demoConfig.showWelcomeMessage) {
            console.log(`
🎉 Modal Demo System Loaded!

Available commands:
• modalDemo.showButtons() - Show demo buttons
• modalDemo.listDemos() - List available demos  
• modalDemo.showDemo('demo-1') - Show specific demo
• modalDemo.status() - Check system status
• modalDemo.debug(true) - Enable debug mode
• modalDemo.performanceTest() - Run performance test

Quick start: modalDemo.showButtons()
            `);
        }
    }

    /**
     * Initialize demo system
     */
    function initializeDemo() {
        // Wait for DOM and modal system to be ready
        const checkReady = () => {
            if (document.readyState === 'complete' && window.showIndependentNewsModal) {
                if (demoConfig.autoCreateTestData) {
                    createDemoData();
                }
                setupConsoleCommands();
                
                // Auto-show demo buttons in test environment
                if (window.location.pathname.includes('test-modal.html')) {
                    setTimeout(createDemoButtons, 1000);
                }
            } else {
                setTimeout(checkReady, 100);
            }
        };
        
        checkReady();
    }

    // Initialize when script loads
    initializeDemo();

})();