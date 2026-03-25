/**
 * Home Page Independent Modal System
 * Dedicated modal component for home page news section
 * Completely isolated from other modal systems
 */

(function () {
    'use strict';

    // Create home page modal container
    function createHomeModalContainer() {
        // Remove any existing container
        $('#home-modal-container').remove();

        var container = $('<div id="home-modal-container"></div>');

        // Apply maximum isolation styles
        container.css({
            'position': 'fixed',
            'top': '0',
            'left': '0',
            'width': '100vw',
            'height': '100vh',
            'z-index': '2147483647', // Maximum z-index
            'pointer-events': 'none',
            'isolation': 'isolate',
            'contain': 'layout style paint',
            'transform': 'translateZ(0)',
            'margin': '0',
            'padding': '0',
            'border': 'none',
            'outline': 'none',
            'background': 'transparent',
            'opacity': '1',
            'visibility': 'visible',
            'display': 'block'
        });

        // Append directly to body
        $('body').append(container);

        console.log('Home modal container created');
        return container;
    }

    // Create home page modal
    window.createHomeModal = function (newsId, newsData) {
        console.log('Creating home page modal for news ID:', newsId);

        // Close any existing modals
        window.closeHomeModal();

        var container = createHomeModalContainer();

        // Create backdrop
        var backdrop = $('<div class="home-modal-backdrop"></div>');
        backdrop.css({
            'position': 'fixed',
            'top': '0',
            'left': '0',
            'width': '100vw',
            'height': '100vh',
            'background-color': 'rgba(0, 0, 0, 0.6)',
            'z-index': '2147483640',
            'pointer-events': 'auto',
            'isolation': 'isolate',
            'contain': 'layout style paint'
        });

        // Create modal
        var modal = $(`
            <div class="home-modal" id="home-modal-${newsId}">
                <div class="home-modal-dialog">
                    <div class="home-modal-content">
                        <div class="home-modal-header">
                            <div class="home-drag-handle">⋮⋮</div>
                            <div class="home-modal-header-content">
                                <div class="home-modal-category">
                                    <i class="fas fa-home"></i>
                                    <span>Home News</span>
                                </div>
                                <h2 class="home-modal-title">${newsData.title || 'News Article'}</h2>
                                <div class="home-modal-meta">
                                    <span class="home-modal-date">${newsData.date || 'Recent'}</span>
                                    <span class="home-modal-reading-time">
                                        <i class="fas fa-clock"></i>
                                        3 min read
                                    </span>
                                </div>
                            </div>
                            <div class="home-modal-controls">
                                <button type="button" class="home-modal-maximize-btn" title="Maximize">
                                    <i class="fas fa-expand"></i>
                                </button>
                                <button type="button" class="home-modal-close" title="Close">
                                    <i class="fas fa-times"></i>
                                </button>
                            </div>
                        </div>
                        <div class="home-modal-body">
                            <div class="home-modal-image" style="display: ${newsData.imageUrl ? 'block' : 'none'}">
                                <img src="${newsData.imageUrl || ''}" alt="News Image">
                            </div>
                            <div class="home-modal-content-text">
                                ${newsData.content || '<p>Loading news content...</p>'}
                            </div>
                        </div>
                        <div class="home-modal-footer">
                            <div class="home-modal-footer-content">
                                <div class="home-modal-tags">
                                    <span class="home-tag">Home</span>
                                    <span class="home-tag">News</span>
                                    <span class="home-tag">Latest</span>
                                </div>
                                <div class="home-modal-actions">
                                    <button type="button" class="home-modal-close-btn">
                                        <i class="fas fa-times"></i>
                                        Close
                                    </button>
                                </div>
                            </div>
                        </div>
                        <div class="home-resize-handle"></div>
                    </div>
                </div>
            </div>
        `);

        // Style the modal with maximum isolation
        modal.css({
            'position': 'fixed',
            'top': '0',
            'left': '0',
            'right': '0',
            'bottom': '0',
            'z-index': '2147483645',
            'pointer-events': 'auto',
            'isolation': 'isolate',
            'contain': 'layout style paint',
            'transform': 'translateZ(0)'
        });

        // Calculate perfect center position
        var windowWidth = $(window).width();
        var windowHeight = $(window).height();
        var modalWidth = Math.min(800, windowWidth * 0.9);
        var modalHeight = Math.min(600, windowHeight * 0.9);

        var centerX = (windowWidth - modalWidth) / 2;
        var centerY = (windowHeight - modalHeight) / 2;

        var dialog = modal.find('.home-modal-dialog');
        dialog.css({
            'position': 'absolute',
            'left': centerX + 'px',
            'top': centerY + 'px',
            'width': modalWidth + 'px',
            'max-height': modalHeight + 'px',
            'background': 'white',
            'border-radius': '20px',
            'box-shadow': '0 25px 50px rgba(0, 0, 0, 0.3)',
            'overflow': 'hidden',
            'z-index': '2147483646',
            'isolation': 'isolate',
            'contain': 'layout style paint'
        });

        // Style modal parts
        styleHomeModalParts(modal);

        // Add to container
        container.append(backdrop);
        container.append(modal);

        // Make draggable and resizable
        makeHomeDraggable(modal);
        makeHomeResizable(modal);

        // Add event handlers
        addHomeModalEventHandlers(modal, backdrop);

        // Prevent body scroll
        $('body').css('overflow', 'hidden');

        console.log('Home modal created and displayed');
        return modal;
    };

    // Style modal parts
    function styleHomeModalParts(modal) {
        var header = modal.find('.home-modal-header');
        header.css({
            'background': 'linear-gradient(135deg, #10b981 0%, #059669 100%)',
            'color': 'white',
            'padding': '25px 30px',
            'position': 'relative',
            'cursor': 'move',
            'user-select': 'none'
        });

        var body = modal.find('.home-modal-body');
        body.css({
            'padding': '25px 30px',
            'max-height': 'calc(90vh - 200px)',
            'overflow-y': 'auto',
            'overflow-x': 'hidden'
        });

        var footer = modal.find('.home-modal-footer');
        footer.css({
            'background': '#f8fafc',
            'border-top': '1px solid #e5e7eb',
            'padding': '25px 30px'
        });

        // Style drag handle
        modal.find('.home-drag-handle').css({
            'position': 'absolute',
            'left': '10px',
            'top': '50%',
            'transform': 'translateY(-50%)',
            'color': 'rgba(255,255,255,0.7)',
            'font-size': '1.2rem',
            'cursor': 'move'
        });

        // Style controls
        modal.find('.home-modal-controls').css({
            'position': 'absolute',
            'top': '15px',
            'right': '15px',
            'display': 'flex',
            'gap': '10px'
        });

        modal.find('.home-modal-maximize-btn, .home-modal-close').css({
            'width': '35px',
            'height': '35px',
            'background': 'rgba(255, 255, 255, 0.2)',
            'border': 'none',
            'border-radius': '50%',
            'color': 'white',
            'cursor': 'pointer',
            'display': 'flex',
            'align-items': 'center',
            'justify-content': 'center',
            'font-size': '1.2rem'
        });

        // Style resize handle
        modal.find('.home-resize-handle').css({
            'position': 'absolute',
            'bottom': '0',
            'right': '0',
            'width': '20px',
            'height': '20px',
            'cursor': 'se-resize',
            'background': 'linear-gradient(-45deg, transparent 0%, transparent 40%, #ccc 40%, #ccc 60%, transparent 60%)',
            'opacity': '0.7'
        });
    }

    // Make modal draggable
    function makeHomeDraggable(modal) {
        var dialog = modal.find('.home-modal-dialog');
        var header = modal.find('.home-modal-header');

        var isDragging = false;
        var startX, startY, startLeft, startTop;

        header.on('mousedown', function (e) {
            if ($(e.target).closest('.home-modal-controls, button, a, input').length) return;

            isDragging = true;
            startX = e.clientX;
            startY = e.clientY;

            var position = dialog.position();
            startLeft = position.left;
            startTop = position.top;

            dialog.addClass('user-moved');
            $('body').addClass('dragging-modal');
            e.preventDefault();
        });

        $(document).on('mousemove', function (e) {
            if (!isDragging) return;

            var deltaX = e.clientX - startX;
            var deltaY = e.clientY - startY;

            var newLeft = startLeft + deltaX;
            var newTop = startTop + deltaY;

            // Constrain to viewport
            var dialogWidth = dialog.outerWidth();
            var dialogHeight = dialog.outerHeight();
            var windowWidth = $(window).width();
            var windowHeight = $(window).height();

            newLeft = Math.max(10, Math.min(newLeft, windowWidth - dialogWidth - 10));
            newTop = Math.max(10, Math.min(newTop, windowHeight - dialogHeight - 10));

            dialog.css({
                'left': newLeft + 'px',
                'top': newTop + 'px'
            });
        });

        $(document).on('mouseup', function () {
            if (isDragging) {
                isDragging = false;
                $('body').removeClass('dragging-modal');
            }
        });
    }

    // Make modal resizable
    function makeHomeResizable(modal) {
        var dialog = modal.find('.home-modal-dialog');
        var handle = modal.find('.home-resize-handle');

        var isResizing = false;
        var startX, startY, startWidth, startHeight;

        handle.on('mousedown', function (e) {
            isResizing = true;
            startX = e.clientX;
            startY = e.clientY;
            startWidth = dialog.outerWidth();
            startHeight = dialog.outerHeight();

            $('body').addClass('resizing-modal');
            e.preventDefault();
            e.stopPropagation();
        });

        $(document).on('mousemove', function (e) {
            if (!isResizing) return;

            var deltaX = e.clientX - startX;
            var deltaY = e.clientY - startY;

            var newWidth = Math.max(400, startWidth + deltaX);
            var newHeight = Math.max(300, startHeight + deltaY);

            var maxWidth = $(window).width() - 40;
            var maxHeight = $(window).height() - 40;

            newWidth = Math.min(newWidth, maxWidth);
            newHeight = Math.min(newHeight, maxHeight);

            dialog.css({
                'width': newWidth + 'px',
                'height': newHeight + 'px'
            });

            var headerHeight = modal.find('.home-modal-header').outerHeight();
            var footerHeight = modal.find('.home-modal-footer').outerHeight();
            var bodyHeight = newHeight - headerHeight - footerHeight;

            modal.find('.home-modal-body').css({
                'max-height': bodyHeight + 'px'
            });
        });

        $(document).on('mouseup', function () {
            if (isResizing) {
                isResizing = false;
                $('body').removeClass('resizing-modal');
            }
        });
    }

    // Add event handlers
    function addHomeModalEventHandlers(modal, backdrop) {
        backdrop.on('click', function () {
            window.closeHomeModal();
        });

        modal.find('.home-modal-close, .home-modal-close-btn').on('click', function () {
            window.closeHomeModal();
        });

        modal.find('.home-modal-maximize-btn').on('click', function () {
            window.toggleHomeModalMaximize(modal);
        });
    }

    // Close home modal
    window.closeHomeModal = function () {
        $('#home-modal-container').remove();
        $('body').css('overflow', '');
        $('body').removeClass('dragging-modal resizing-modal');
        console.log('Home modal closed');
    };

    // Center home modal
    window.centerHomeModal = function () {
        var modal = $('.home-modal');
        if (!modal.length) return;

        var dialog = modal.find('.home-modal-dialog');
        var windowWidth = $(window).width();
        var windowHeight = $(window).height();
        var dialogWidth = dialog.outerWidth();
        var dialogHeight = dialog.outerHeight();

        var centerX = Math.max(20, (windowWidth - dialogWidth) / 2);
        var centerY = Math.max(20, (windowHeight - dialogHeight) / 2);

        dialog.css({
            'left': centerX + 'px',
            'top': centerY + 'px'
        });

        console.log('Home modal centered');
    };

    // Toggle maximize
    window.toggleHomeModalMaximize = function (modal) {
        if (!modal || !modal.length) {
            modal = $('.home-modal');
        }

        if (!modal.length) return;

        var $modal = $(modal);
        var $dialog = $modal.find('.home-modal-dialog');
        var $maximizeBtn = $modal.find('.home-modal-maximize-btn');
        var $maximizeIcon = $maximizeBtn.find('i');

        if ($modal.hasClass('maximized')) {
            // Restore
            $modal.removeClass('maximized');
            $maximizeIcon.removeClass('fa-compress').addClass('fa-expand');

            var savedState = $modal.data('saved-state');
            if (savedState) {
                $dialog.css(savedState);
            } else {
                window.centerHomeModal();
            }
        } else {
            // Maximize
            var currentState = {
                'left': $dialog.css('left'),
                'top': $dialog.css('top'),
                'width': $dialog.css('width'),
                'height': $dialog.css('height')
            };
            $modal.data('saved-state', currentState);

            $modal.addClass('maximized');
            $maximizeIcon.removeClass('fa-expand').addClass('fa-compress');

            // Get actual viewport dimensions for full system window
            var viewportWidth = window.innerWidth || document.documentElement.clientWidth;
            var viewportHeight = window.innerHeight || document.documentElement.clientHeight;

            $dialog.css({
                'position': 'fixed',
                'left': '0px',
                'top': '0px',
                'width': viewportWidth + 'px',
                'height': viewportHeight + 'px',
                'max-width': 'none',
                'max-height': 'none',
                'border-radius': '0',
                'margin': '0',
                'padding': '0'
            });

            // Calculate body height for full window
            setTimeout(function () {
                var headerHeight = $modal.find('.home-modal-header').outerHeight() || 0;
                var footerHeight = $modal.find('.home-modal-footer').outerHeight() || 0;
                var bodyHeight = viewportHeight - headerHeight - footerHeight;

                $modal.find('.home-modal-body').css({
                    'max-height': bodyHeight + 'px',
                    'height': bodyHeight + 'px'
                });

                console.log('Home modal maximized to full system window:', viewportWidth + 'x' + viewportHeight);
            }, 50);
        }
    };

    // Extract news data from existing modal or news card
    function extractHomeNewsData(newsId) {
        console.log('Extracting news data for ID:', newsId);

        // First try to get data from the existing modal
        var existingModal = $('#modal-' + newsId);
        if (existingModal.length) {
            console.log('Found existing modal for news ID:', newsId);
            return {
                title: existingModal.find('.modal-title').text() || 'Home News Article',
                content: existingModal.find('.modal-content-text').html() || '<p>Loading content...</p>',
                date: existingModal.find('.modal-date').text() || 'Recent',
                imageUrl: existingModal.find('.modal-image img').attr('src') || null
            };
        }

        // Try to get data from the news card itself
        var newsCard = $('[data-news-id="' + newsId + '"]').closest('.news-card');
        if (newsCard.length) {
            console.log('Found news card for ID:', newsId);
            return {
                title: newsCard.find('.news-title').text() || 'Home News Article',
                content: newsCard.find('.news-excerpt').html() || '<p>Loading content...</p>',
                date: newsCard.find('.news-date span').text() || 'Recent',
                imageUrl: newsCard.find('.news-image').attr('src') || null
            };
        }

        console.log('No existing data found for news ID:', newsId, 'using defaults');
        return {
            title: 'Home News Article',
            content: '<p>Unable to load news content. Please try refreshing the page.</p>',
            date: 'Recent',
            imageUrl: null
        };
    }

    // Show home modal for news
    window.showHomeNewsModal = function (newsId) {
        console.log('Showing home news modal for ID:', newsId);

        var newsData = extractHomeNewsData(newsId);
        return window.createHomeModal(newsId, newsData);
    };

    // Keyboard support
    $(document).on('keydown', function (e) {
        if (e.keyCode === 27 && $('#home-modal-container').length) { // Escape
            window.closeHomeModal();
        }
    });

    // Initialize event handlers when DOM is ready
    $(document).ready(function () {
        console.log('🚀 Home modal system initializing...');

        // Add click handlers for news buttons
        function attachNewsButtonHandlers() {
            // Handle both news-detail-btn and news-read-btn classes
            const buttons = $('.news-detail-btn, .news-read-btn');
            console.log('🔍 Searching for news buttons...');
            console.log('Found news buttons:', buttons.length);

            // Debug: Show all elements with these classes
            $('.news-detail-btn').each(function (index) {
                console.log('news-detail-btn', index + 1, ':', this, 'ID:', $(this).attr('data-news-id'));
            });
            $('.news-read-btn').each(function (index) {
                console.log('news-read-btn', index + 1, ':', this, 'ID:', $(this).attr('data-news-id'));
            });

            // Also check for any buttons with data-news-id
            $('[data-news-id]').each(function (index) {
                console.log('Element with data-news-id', index + 1, ':', this, 'Classes:', this.className);
            });

            buttons.off('click.homeModal').on('click.homeModal', function (e) {
                e.preventDefault();
                e.stopPropagation();

                let newsId = $(this).attr('data-news-id');

                // If no data-news-id, try to extract from data-target (for news-read-btn)
                if (!newsId) {
                    const dataTarget = $(this).attr('data-target');
                    if (dataTarget) {
                        // Extract ID from "#modal-{id}" format
                        newsId = dataTarget.replace('#modal-', '');
                        console.log('🔍 Extracted news ID from data-target:', newsId);
                    }
                }

                console.log('🔥 News button clicked! ID:', newsId);
                console.log('Button element:', this);
                console.log('Button classes:', this.className);
                console.log('Event:', e);

                if (newsId && window.showHomeNewsModal) {
                    console.log('✅ Calling showHomeNewsModal with ID:', newsId);
                    window.showHomeNewsModal(newsId);
                } else {
                    console.error('❌ Missing news ID or showHomeNewsModal function');
                    console.log('newsId:', newsId);
                    console.log('showHomeNewsModal available:', !!window.showHomeNewsModal);
                }
            });

            // Use event delegation from document level - this works with dynamically loaded content
            $(document).off('click.homeModalDelegated').on('click.homeModalDelegated', '.news-detail-btn, .news-read-btn', function (e) {
                e.preventDefault();
                e.stopPropagation();

                console.log('🎯🎯🎯 DELEGATED CLICK DETECTED!');
                console.log('Button element:', this);
                console.log('Button classes:', this.className);

                let newsId = $(this).attr('data-news-id');

                // If no data-news-id, try to extract from data-target (for news-read-btn)
                if (!newsId) {
                    const dataTarget = $(this).attr('data-target');
                    if (dataTarget) {
                        newsId = dataTarget.replace('#modal-', '');
                        console.log('🎯 Delegated: Extracted news ID from data-target:', newsId);
                    }
                }

                console.log('🎯 Delegated click handler triggered! ID:', newsId);

                if (newsId && window.showHomeNewsModal) {
                    console.log('✅✅✅ Calling showHomeNewsModal via delegation with ID:', newsId);
                    window.showHomeNewsModal(newsId);
                } else {
                    console.error('❌❌❌ Delegation: Missing news ID or showHomeNewsModal function');
                    console.log('newsId:', newsId);
                    console.log('showHomeNewsModal available:', !!window.showHomeNewsModal);
                }
            });

            console.log('✅ News button handlers attached to', buttons.length, 'buttons');
            console.log('✅ Event delegation also set up for dynamic content');
        }

        // Multiple initialization attempts to handle timing issues
        function initializeWithRetry() {
            attachNewsButtonHandlers();

            // Retry after delays to catch dynamically loaded content
            setTimeout(attachNewsButtonHandlers, 1000);
            setTimeout(attachNewsButtonHandlers, 2000);
            setTimeout(attachNewsButtonHandlers, 3000);
        }

        // Initial attachment
        initializeWithRetry();

        // Re-attach handlers if content is dynamically loaded
        const observer = new MutationObserver(function (mutations) {
            let shouldReattach = false;
            mutations.forEach(function (mutation) {
                if (mutation.type === 'childList') {
                    mutation.addedNodes.forEach(function (node) {
                        if (node.nodeType === 1) { // Element node
                            if ($(node).find('.news-detail-btn, .news-read-btn').length > 0 ||
                                $(node).hasClass('news-detail-btn') || $(node).hasClass('news-read-btn')) {
                                shouldReattach = true;
                            }
                        }
                    });
                }
            });

            if (shouldReattach) {
                console.log('🔄 New news buttons detected, re-attaching handlers');
                attachNewsButtonHandlers();
            }
        });

        // Observe the document for changes
        observer.observe(document.body, {
            childList: true,
            subtree: true
        });

        console.log('✅ Home modal system initialized successfully');

        // Add the most basic click detection possible
        $(document).on('click', '*', function (e) {
            const target = e.target;
            if (target.classList.contains('news-detail-btn') || target.classList.contains('news-read-btn')) {
                console.log('🚨🚨🚨 BASIC CLICK DETECTED ON NEWS BUTTON!');
                console.log('Target:', target);
                console.log('Classes:', target.className);
                console.log('Event:', e);

                // Try to prevent any other handlers
                e.preventDefault();
                e.stopPropagation();
                e.stopImmediatePropagation();

                // Get the news ID
                let newsId = target.getAttribute('data-news-id');
                if (!newsId) {
                    const dataTarget = target.getAttribute('data-target');
                    if (dataTarget) {
                        newsId = dataTarget.replace('#modal-', '');
                    }
                }

                console.log('News ID:', newsId);

                if (newsId && window.showHomeNewsModal) {
                    console.log('🚀🚀🚀 CALLING HOME MODAL!');
                    window.showHomeNewsModal(newsId);
                } else {
                    console.log('❌ Cannot call modal - newsId:', newsId, 'showHomeNewsModal:', !!window.showHomeNewsModal);
                }

                return false;
            }
        });

        // Add a global function to manually test button clicks
        window.testActualButtonClick = function () {
            console.log('🧪 Testing actual button clicks...');

            // Find the first news button and try to click it programmatically
            const firstDetailBtn = document.querySelector('.news-detail-btn');
            const firstReadBtn = document.querySelector('.news-read-btn');

            console.log('First detail button:', firstDetailBtn);
            console.log('First read button:', firstReadBtn);

            if (firstDetailBtn) {
                console.log('Trying to click detail button...');
                console.log('Button onclick:', firstDetailBtn.getAttribute('onclick'));
                console.log('Button data-news-id:', firstDetailBtn.getAttribute('data-news-id'));

                // Try different ways to trigger the click
                firstDetailBtn.click();

                // Also try jQuery click
                $(firstDetailBtn).trigger('click');

                // Try manual event dispatch
                const event = new Event('click', { bubbles: true });
                firstDetailBtn.dispatchEvent(event);
            }

            if (firstReadBtn) {
                console.log('Trying to click read button...');
                console.log('Button data-target:', firstReadBtn.getAttribute('data-target'));

                firstReadBtn.click();
                $(firstReadBtn).trigger('click');

                const event = new Event('click', { bubbles: true });
                firstReadBtn.dispatchEvent(event);
            }
        };

        // Add a global function to manually check buttons
        window.debugNewsButtons = function () {
            console.log('🔧 Manual button debug:');
            console.log('news-detail-btn count:', $('.news-detail-btn').length);
            console.log('news-read-btn count:', $('.news-read-btn').length);
            console.log('Elements with data-news-id:', $('[data-news-id]').length);

            // Check all possible button selectors
            console.log('All buttons:', $('button').length);
            console.log('Buttons with news in class:', $('button[class*="news"]').length);

            $('[data-news-id]').each(function (i) {
                const $el = $(this);
                console.log('Element', i + 1, ':', this);
                console.log('  - Tag:', this.tagName);
                console.log('  - Classes:', this.className);
                console.log('  - ID:', $el.attr('data-news-id'));
                console.log('  - Visible:', $el.is(':visible'));
                console.log('  - Display:', $el.css('display'));
                console.log('  - Opacity:', $el.css('opacity'));
                console.log('  - Pointer events:', $el.css('pointer-events'));
            });

            // Try to find news sections
            console.log('News sections found:', $('.news-section').length);
            console.log('News cards found:', $('.news-card').length);
            console.log('News articles found:', $('.news-article').length);
        };

        // Add a function to force attach handlers to any button with data-news-id or data-target
        window.forceAttachNewsHandlers = function () {
            console.log('🔧 Force attaching handlers to all news buttons...');

            $('.news-detail-btn, .news-read-btn').off('click.forceModal').on('click.forceModal', function (e) {
                e.preventDefault();
                e.stopPropagation();

                let newsId = $(this).attr('data-news-id');

                // If no data-news-id, try to extract from data-target (for news-read-btn)
                if (!newsId) {
                    const dataTarget = $(this).attr('data-target');
                    if (dataTarget) {
                        newsId = dataTarget.replace('#modal-', '');
                        console.log('🚀 FORCE: Extracted news ID from data-target:', newsId);
                    }
                }

                console.log('🚀 FORCE HANDLER: Button clicked! ID:', newsId);
                console.log('Element:', this);

                if (newsId && window.showHomeNewsModal) {
                    console.log('✅ Calling showHomeNewsModal with ID:', newsId);
                    window.showHomeNewsModal(newsId);
                } else {
                    console.error('❌ Missing news ID or showHomeNewsModal function');
                }
            });

            console.log('✅ Force handlers attached to', $('.news-detail-btn, .news-read-btn').length, 'elements');
        };

        // Override the existing handleNewsClick function to use our home modal system
        window.handleNewsClick = function (button) {
            console.log('🔥🔥🔥 handleNewsClick called with button:', button);
            console.log('🔥🔥🔥 Button type:', typeof button);
            console.log('🔥🔥🔥 Button element:', button);

            const newsId = button.getAttribute('data-news-id');
            console.log('🔥🔥🔥 News ID from handleNewsClick:', newsId);

            if (newsId && window.showHomeNewsModal) {
                console.log('✅✅✅ Using home modal system for news ID:', newsId);
                window.showHomeNewsModal(newsId);
                return false; // Prevent any default behavior
            } else {
                console.error('❌❌❌ handleNewsClick: Missing news ID or showHomeNewsModal function');
                console.log('newsId:', newsId);
                console.log('showHomeNewsModal available:', !!window.showHomeNewsModal);
                return false;
            }
        };

        console.log('✅ handleNewsClick function overridden to use home modal system');

        // Add direct click listeners to all news buttons as a backup - multiple attempts
        function addDirectClickListeners() {
            console.log('🔧 Adding direct click listeners to news buttons...');
            console.log('Current news-detail-btn count:', $('.news-detail-btn').length);
            console.log('Current news-read-btn count:', $('.news-read-btn').length);

            // Add listeners to news-detail-btn buttons (the ones with onclick)
            $('.news-detail-btn').each(function (index) {
                const button = this;
                const newsId = $(button).attr('data-news-id');
                console.log('Adding direct listener to news-detail-btn', index + 1, 'ID:', newsId);

                // Remove existing onclick and add our own
                button.removeAttribute('onclick');

                $(button).off('click.direct').on('click.direct', function (e) {
                    e.preventDefault();
                    e.stopPropagation();
                    console.log('🚨🚨🚨 DIRECT CLICK on news-detail-btn! ID:', newsId);

                    if (newsId && window.showHomeNewsModal) {
                        console.log('✅✅✅ Calling showHomeNewsModal directly with ID:', newsId);
                        window.showHomeNewsModal(newsId);
                    } else {
                        console.error('❌❌❌ Direct click: Missing news ID or function');
                    }
                });
            });

            // Add listeners to news-read-btn buttons
            $('.news-read-btn').each(function (index) {
                const button = this;
                const dataTarget = $(button).attr('data-target');
                const newsId = dataTarget ? dataTarget.replace('#modal-', '') : null;
                console.log('Adding direct listener to news-read-btn', index + 1, 'ID:', newsId);

                $(button).off('click.direct').on('click.direct', function (e) {
                    e.preventDefault();
                    e.stopPropagation();
                    console.log('🚨🚨🚨 DIRECT CLICK on news-read-btn! ID:', newsId);

                    if (newsId && window.showHomeNewsModal) {
                        console.log('✅✅✅ Calling showHomeNewsModal directly with ID:', newsId);
                        window.showHomeNewsModal(newsId);
                    } else {
                        console.error('❌❌❌ Direct click: Missing news ID or function');
                    }
                });
            });

            console.log('✅ Direct click listeners added to all news buttons');
        }

        // Try multiple times to ensure we catch the buttons
        addDirectClickListeners(); // Immediate
        setTimeout(addDirectClickListeners, 500); // After 0.5s
        setTimeout(addDirectClickListeners, 1000); // After 1s
        setTimeout(addDirectClickListeners, 2000); // After 2s

        // Also make it available globally for manual testing
        window.addDirectClickListeners = addDirectClickListeners;

        // Add comprehensive button inspection
        window.inspectNewsButtons = function () {
            console.log('🔍 COMPREHENSIVE BUTTON INSPECTION:');

            // Check all news buttons
            const allNewsButtons = document.querySelectorAll('.news-detail-btn, .news-read-btn');
            console.log('Total news buttons found:', allNewsButtons.length);

            allNewsButtons.forEach((button, index) => {
                console.log(`\n--- Button ${index + 1} ---`);
                console.log('Element:', button);
                console.log('Classes:', button.className);
                console.log('onclick attribute:', button.getAttribute('onclick'));
                console.log('data-news-id:', button.getAttribute('data-news-id'));
                console.log('data-target:', button.getAttribute('data-target'));
                console.log('Visible:', button.offsetParent !== null);
                console.log('Disabled:', button.disabled);
                console.log('Style display:', window.getComputedStyle(button).display);
                console.log('Style pointer-events:', window.getComputedStyle(button).pointerEvents);

                // Check for event listeners
                const events = $._data(button, 'events');
                console.log('jQuery events:', events);

                // Try to add a test listener
                button.addEventListener('click', function (e) {
                    console.log(`🚨 NATIVE CLICK DETECTED on button ${index + 1}!`);
                    console.log('Event:', e);
                    console.log('Target:', e.target);
                    console.log('Current target:', e.currentTarget);
                }, true); // Use capture phase
            });
        };
    });

})();