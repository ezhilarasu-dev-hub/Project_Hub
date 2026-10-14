// Institutional AI Assistant Widget Module

const AIChat = {
    init() {
        if (document.getElementById('ai-assistant-widget')) {
            return;
        }

        const widgetContainer = document.createElement('div');
        widgetContainer.id = 'ai-assistant-widget';

        widgetContainer.innerHTML = `
            <!-- Launcher Button -->
            <button id="ai-launcher-btn" class="ai-widget-launcher" title="Open AI Assistant">
                <span>[ AI ]</span> Assistant
            </button>

            <!-- Chat Window -->
            <div id="ai-chat-box" class="ai-chat-window d-none">
                <div class="ai-chat-header">
                    <h4>Project Assistant</h4>
                    <button id="ai-chat-close-btn" class="ai-chat-close" title="Close">&times;</button>
                </div>
                <div id="ai-messages" class="ai-messages-container">
                    <div class="ai-message ai-message-assistant">
Ask about senior projects, verify if an idea has already been completed, or ask for institutional project statistics.
                    </div>
                </div>
                <div class="ai-chat-input-area">
                    <input type="text" id="ai-user-input" placeholder="Type message..." autocomplete="off" />
                    <button id="ai-send-btn">Send</button>
                </div>
            </div>
        `;

        document.body.appendChild(widgetContainer);

        const launcher = document.getElementById('ai-launcher-btn');
        const chatBox = document.getElementById('ai-chat-box');
        const closeBtn = document.getElementById('ai-chat-close-btn');
        const sendBtn = document.getElementById('ai-send-btn');
        const inputField = document.getElementById('ai-user-input');

        launcher.addEventListener('click', () => {
            chatBox.classList.toggle('d-none');
            if (!chatBox.classList.contains('d-none')) {
                inputField.focus();
            }
        });

        closeBtn.addEventListener('click', () => {
            chatBox.classList.add('d-none');
        });

        sendBtn.addEventListener('click', () => this.sendMessage());

        inputField.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
                this.sendMessage();
            }
        });
    },

    async sendMessage() {
        const input = document.getElementById('ai-user-input');
        const messagesContainer = document.getElementById('ai-messages');
        const text = input.value.trim();

        if (!text) return;

        // Append user message
        this.appendMessage(text, 'user');
        input.value = '';

        // Typing indicator
        const loadingId = 'ai-loading-' + Date.now();
        const loadingElem = document.createElement('div');
        loadingElem.id = loadingId;
        loadingElem.className = 'ai-message ai-message-assistant';
        loadingElem.style.fontStyle = 'italic';
        loadingElem.style.color = '#64748b';
        loadingElem.textContent = 'Searching senior projects & database...';
        messagesContainer.appendChild(loadingElem);
        messagesContainer.scrollTop = messagesContainer.scrollHeight;

        try {
            const response = await Auth.authenticatedFetch('/api/ai/chat', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ message: text })
            });

            const loading = document.getElementById(loadingId);
            if (loading) loading.remove();

            if (!response.ok) {
                const errData = await response.json().catch(() => ({}));
                this.appendMessage(errData.message || 'Error processing request.', 'assistant');
                return;
            }

            const data = await response.json();
            this.appendMessage(data.reply, 'assistant');

        } catch (err) {
            const loading = document.getElementById(loadingId);
            if (loading) loading.remove();
            this.appendMessage('Could not connect to AI service: ' + err.message, 'assistant');
        }
    },

    appendMessage(text, sender) {
        const messagesContainer = document.getElementById('ai-messages');
        if (!messagesContainer) return;

        const msgDiv = document.createElement('div');
        msgDiv.className = `ai-message ${sender === 'user' ? 'ai-message-user' : 'ai-message-assistant'}`;
        msgDiv.textContent = text;

        messagesContainer.appendChild(msgDiv);
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }
};

window.AIChat = AIChat;

// Automatically initialize when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    AIChat.init();
});
