// 依赖 config.js 中的 CONFIG 对象
const API_BASE = (typeof CONFIG !== 'undefined' && CONFIG.API_BASE_URL) ? CONFIG.API_BASE_URL : 'http://127.0.0.1:8091/api/agent';

let currentUserId = null;
let currentAgent = null;
let currentSessionId = null;
let isWaitingResponse = false;
let totalTokensConsumed = 0;
let sendDebounceTimer = null;

// DOM 元素
const messagesArea = document.getElementById('messagesArea');
const messageInput = document.getElementById('messageInput');
const sendBtn = document.getElementById('sendBtn');
const currentAgentNameSpan = document.getElementById('currentAgentName');
const currentAgentDescSpan = document.getElementById('currentAgentDesc');
const agentStateLabelSpan = document.getElementById('agentStateLabel');
const agentThinkingStatusSpan = document.getElementById('agentThinkingStatus');
const tokenUsageSpan = document.getElementById('tokenUsage');
const sessionIdDisplaySpan = document.getElementById('sessionIdDisplay');
const switchAgentBtn = document.getElementById('switchAgentBtn');
const charCounterSpan = document.getElementById('charCounter');

// ========== 辅助函数 ==========
function getCookie(name) {
    let value = `; ${document.cookie}`;
    let parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
    return null;
}

function checkAuthAndRedirect() {
    const userId = getCookie('userId');
    if (!userId) {
        alert('请先登录');
        window.location.href = 'login.html';
        return false;
    }
    currentUserId = userId;
    return true;
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/[&<>]/g, function (m) {
        if (m === '&') return '&amp;';
        if (m === '<') return '&lt;';
        if (m === '>') return '&gt;';
        return m;
    });
}

function scrollToBottom() {
    messagesArea.scrollTop = messagesArea.scrollHeight;
}

function appendSystemMessage(text, isError = false) {
    const msgDiv = document.createElement('div');
    msgDiv.className = 'message assistant-message';
    msgDiv.innerHTML = `<div class="message-bubble" style="background:#ffe6e5; color:#b91c1c;">⚠️ ${escapeHtml(text)}</div>`;
    messagesArea.appendChild(msgDiv);
    scrollToBottom();
}

function appendUserMessage(content) {
    const msgDiv = document.createElement('div');
    msgDiv.className = 'message user-message';
    msgDiv.innerHTML = `<div class="message-bubble">${escapeHtml(content)}</div><div class="message-meta">你 · ${new Date().toLocaleTimeString()}</div>`;
    messagesArea.appendChild(msgDiv);
    scrollToBottom();
}

function createAssistantMessagePlaceholder() {
    const msgDiv = document.createElement('div');
    msgDiv.className = 'message assistant-message';
    msgDiv.innerHTML = `<div class="message-bubble" id="streamingBubble"></div><div class="message-meta">AI 思考中...</div>`;
    messagesArea.appendChild(msgDiv);
    scrollToBottom();
    return msgDiv.querySelector('#streamingBubble');
}

function updateAgentUI(statusType) {
    if (statusType === 'thinking') {
        agentStateLabelSpan.innerText = '🧠 思考中';
        agentStateLabelSpan.className = 'status-badge thinking';
        agentThinkingStatusSpan.innerText = '推理中 ...';
    } else if (statusType === 'streaming') {
        agentStateLabelSpan.innerText = '⚡ 流式输出';
        agentStateLabelSpan.className = 'status-badge';
        agentThinkingStatusSpan.innerText = '逐字输出中';
    } else if (statusType === 'waiting') {
        agentStateLabelSpan.innerText = '💤 等候中';
        agentStateLabelSpan.className = 'status-badge';
        agentThinkingStatusSpan.innerText = '空闲';
    }
    sessionIdDisplaySpan.innerText = currentSessionId ? currentSessionId.substring(0, 12) + '...' : '流式会话中';
}

function animateNumber(element, start, end, duration = 300) {
    if (start === end) return;
    const range = end - start;
    const startTime = performance.now();
    const update = (now) => {
        const elapsed = now - startTime;
        const progress = Math.min(1, elapsed / duration);
        const current = start + range * progress;
        element.innerText = Math.floor(current);
        if (progress < 1) {
            requestAnimationFrame(update);
        } else {
            element.innerText = end;
        }
    };
    requestAnimationFrame(update);
}

function autoResizeTextarea() {
    if (!messageInput) return;
    messageInput.style.height = 'auto';
    const newHeight = Math.min(messageInput.scrollHeight, 160);
    messageInput.style.height = newHeight + 'px';
}

function updateCharCounter() {
    const len = messageInput.value.length;
    const maxLen = 200;
    charCounterSpan.innerText = `${len} / ${maxLen}`;
    if (len >= maxLen) {
        charCounterSpan.classList.add('danger');
        charCounterSpan.classList.remove('warning');
    } else if (len >= maxLen * 0.9) {
        charCounterSpan.classList.add('warning');
        charCounterSpan.classList.remove('danger');
    } else {
        charCounterSpan.classList.remove('warning', 'danger');
    }
}

function bindInputEvents() {
    messageInput.addEventListener('input', function (e) {
        if (this.value.length > 200) {
            this.value = this.value.slice(0, 200);
        }
        updateCharCounter();
        autoResizeTextarea();
    });
    updateCharCounter();
    autoResizeTextarea();
}

// 获取智能体列表
async function fetchAndSelectAgent(forceSelect = false) {
    try {
        const resp = await fetch(`${API_BASE}/query_valid_agents`);
        const result = await resp.json();
        if (result.code !== "0000" || !result.data || result.data.length === 0) {
            appendSystemMessage('没有可用的智能体，请联系管理员。');
            return false;
        }
        const agents = result.data;
        if (agents.length === 1 && !forceSelect) {
            currentAgent = agents[0];
            currentAgentNameSpan.innerText = currentAgent.agentName;
            currentAgentDescSpan.innerText = currentAgent.description;
            appendSystemMessage(`✨ 已自动选用默认智能体：「${currentAgent.agentName}」`);
            return true;
        } else {
            return showAgentSelectionModal(agents);
        }
    } catch (err) {
        console.error(err);
        appendSystemMessage('获取智能体列表失败，请刷新页面');
        return false;
    }
}

function showAgentSelectionModal(agents) {
    return new Promise((resolve) => {
        const modalDiv = document.createElement('div');
        modalDiv.className = 'modal-mask';
        modalDiv.innerHTML = `
            <div class="agent-selector">
                <h3>🤖 选择对话智能体</h3>
                <div id="agentListContainer"></div>
                <div style="margin-top: 24px; display: flex; justify-content: flex-end;">
                    <button id="confirmAgentBtn" style="background:#0f2f40; color:white; border:none; padding:10px 24px; border-radius:40px;">确认选择</button>
                </div>
            </div>
        `;
        document.body.appendChild(modalDiv);
        const container = modalDiv.querySelector('#agentListContainer');
        let selectedAgentId = null;
        agents.forEach(agent => {
            const card = document.createElement('div');
            card.className = 'agent-card-select';
            card.dataset.id = agent.agentId;
            card.innerHTML = `<strong>${escapeHtml(agent.agentName)}</strong><br><small>${escapeHtml(agent.description)}</small>`;
            card.addEventListener('click', () => {
                document.querySelectorAll('.agent-card-select').forEach(c => c.classList.remove('selected'));
                card.classList.add('selected');
                selectedAgentId = agent.agentId;
            });
            container.appendChild(card);
        });
        const confirmBtn = modalDiv.querySelector('#confirmAgentBtn');
        confirmBtn.onclick = async () => {
            if (!selectedAgentId) {
                alert('请选择一个智能体');
                return;
            }
            const selected = agents.find(a => a.agentId === selectedAgentId);
            if (selected) {
                currentAgent = selected;
                currentAgentNameSpan.innerText = currentAgent.agentName;
                currentAgentDescSpan.innerText = currentAgent.description;
                currentSessionId = null;
                updateAgentUI('waiting');
                modalDiv.remove();
                resolve(true);
            } else {
                resolve(false);
            }
        };
    });
}

// ========== SSE 流式解析器 ==========

/**
 * 从 ReadableStream 中逐条解析 SSE 事件
 *
 * SSE 协议的每条消息格式：
 *   event: <事件名>\n
 *   data: <JSON数据>\n
 *   \n
 *
 * 服务端 SseEmitter 每调用一次 send(SseEmitter.event().name("text").data("{...}"))，
 * 前端收到的就是上面这个格式的一个消息块。
 *
 *
 * @param {ReadableStream} body - fetch response.body
 * @param {Object} handlers - { eventName: (data) => void } 事件处理器映射
 */
async function parseSseStream(body, handlers) {
    const reader = body.getReader();
    const decoder = new TextDecoder('utf-8');
    let buffer = '';

    while (true) {
        const { done, value } = await reader.read();
        if (done) break;

        buffer += decoder.decode(value, { stream: true });

        // 按 \n\n 分割消息块（SSE 协议的消息分界符）
        const parts = buffer.split('\n\n');
        // 最后一部分可能不完整，留在 buffer 中等待下次拼接
        buffer = parts.pop();

        for (const part of parts) {
            if (!part.trim()) continue;

            // 解析 event: xxx 和 data: yyy
            let eventName = 'message'; // SSE 默认事件名
            let data = '';

            const lines = part.split('\n');
            for (const line of lines) {
                if (line.startsWith('event:')) {
                    eventName = line.substring(6).trim();
                } else if (line.startsWith('data:')) {
                    // SseEmitter 格式为 "data:xxx"（无空格），也兼容 "data: xxx"（有空格）
                    data = line.substring(5).trim();
                }
            }

            if (data && handlers[eventName]) {
                try {
                    const parsed = JSON.parse(data);
                    handlers[eventName](parsed);
                } catch (e) {
                    console.warn('[SSE] JSON 解析失败 event:', eventName, 'data:', data, e);
                }
            }
        }
    }

    // 处理流结束后 buffer 中可能残留的最后一条消息
    if (buffer.trim()) {
        const lines = buffer.split('\n');
        let eventName = 'message';
        let data = '';
        for (const line of lines) {
            if (line.startsWith('event:')) eventName = line.substring(6).trim();
            else if (line.startsWith('data:')) data = line.substring(5).trim();
        }
        if (data && handlers[eventName]) {
            try {
                handlers[eventName](JSON.parse(data));
            } catch (e) {
                console.warn('[SSE] 最终 JSON 解析失败', e);
            }
        }
    }
}

// ========== 核心流式对话函数（SSE 事件驱动 + Markdown 渲染） ==========
async function sendMessageStream(userMessage) {
    if (!currentAgent || !currentUserId) {
        appendSystemMessage('请先选择一个智能体');
        return;
    }
    if (isWaitingResponse) {
        appendSystemMessage('请等待上一个回复完成');
        return;
    }

    messageInput.value = '';
    updateCharCounter();
    autoResizeTextarea();

    isWaitingResponse = true;
    sendBtn.disabled = true;
    updateAgentUI('thinking');
    appendUserMessage(userMessage);

    const bubbleElement = createAssistantMessagePlaceholder();
    let fullRawText = "";
    let displayedText = "";

    // 逐字打印队列
    const CHAR_DELAY_MS = 15;
    let pendingChars = [];
    let isPrinting = false;

    function escapeHtmlForDisplay(text) {
        if (!text) return '';
        return text.replace(/[&<>]/g, function (m) {
            if (m === '&') return '&amp;';
            if (m === '<') return '&lt;';
            if (m === '>') return '&gt;';
            return m;
        });
    }

    const printNextChar = () => {
        if (pendingChars.length === 0) { isPrinting = false; return; }
        isPrinting = true;
        displayedText += pendingChars.shift();
        if (bubbleElement) {
            bubbleElement.innerHTML = escapeHtmlForDisplay(displayedText).replace(/\n/g, '<br>');
            scrollToBottom();
        }
        setTimeout(printNextChar, CHAR_DELAY_MS);
    };

    async function renderMarkdownToBubble(markdownText) {
        if (!bubbleElement) return;
        try {
            bubbleElement.innerHTML = await marked.parse(markdownText);
            scrollToBottom();
        } catch (err) {
            console.error('Markdown 渲染失败', err);
            bubbleElement.innerText = markdownText;
        }
    }

    const requestBody = {
        agentId: currentAgent.agentId,
        userId: currentUserId,
        sessionId: currentSessionId || "",
        message: userMessage
    };

    try {
        const response = await fetch(`${API_BASE}/chat_stream`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(requestBody)
        });
        if (!response.body) throw new Error('No response body');

        updateAgentUI('streaming');

        /*
         * SSE 事件驱动处理 —— 按 type 分发
         *
         * 和后端的对应关系：
         *   SseEmitter.event().name("meta")  →  handlers.meta
         *   SseEmitter.event().name("text")  →  handlers.text
         *   SseEmitter.event().name("finish")→  handlers.finish
         */
        await parseSseStream(response.body, {
            // 元信息：sessionId
            meta: (d) => {
                if (d.sessionId && !currentSessionId) {
                    currentSessionId = d.sessionId;
                    sessionIdDisplaySpan.innerText = currentSessionId.substring(0, 12) + '...';
                    console.log('[SSE] 收到 sessionId:', d.sessionId);
                }
            },

            // 文本增量：追加到累积文本并送入逐字打印队列
            text: (d) => {
                if (!d.delta) return;
                fullRawText += d.delta;
                const chars = d.delta.split('');
                pendingChars.push(...chars);
                if (!isPrinting) printNextChar();
            },

            // 工具调用：在气泡中显示思考状态
            tool_call: (d) => {
                console.log('[SSE] Agent 调用工具:', d.toolName || 'unknown', d.toolArgs || '');
            },

            // 工具返回
            tool_result: (d) => {
                console.log('[SSE] 工具返回:', d.toolResult ? d.toolResult.substring(0, 100) : '');
            },

            // 错误
            error: (d) => {
                console.error('[SSE] 服务端错误:', d.error);
                if (bubbleElement) {
                    bubbleElement.innerHTML = `<span style="color:#e74c3c;">⚠️ ${escapeHtml(d.error || '未知错误')}</span>`;
                }
            },

            // 流结束：携带累计 token 消耗
            finish: (d) => {
                if (d.totalTokens != null && d.totalTokens > 0) {
                    animateNumber(tokenUsageSpan, totalTokensConsumed, d.totalTokens, 200);
                    totalTokensConsumed = d.totalTokens;
                }
                console.log('[SSE] 流结束, totalTokens:', d.totalTokens);
            }
        });

        // 等待逐字打印完成
        while (pendingChars.length > 0 || isPrinting) {
            await new Promise(resolve => setTimeout(resolve, 20));
        }

        // 确保最终文本完整
        if (bubbleElement && fullRawText !== displayedText) {
            bubbleElement.innerHTML = escapeHtmlForDisplay(fullRawText).replace(/\n/g, '<br>');
            displayedText = fullRawText;
        }

        if (fullRawText === "") {
            if (bubbleElement) bubbleElement.innerText = "[无响应内容]";
        } else {
            await renderMarkdownToBubble(fullRawText);
        }

        const metaSpan = bubbleElement?.parentElement?.querySelector('.message-meta');
        if (metaSpan) metaSpan.innerText = `AI · ${new Date().toLocaleTimeString()}`;
        updateAgentUI('waiting');

    } catch (err) {
        console.error('流式错误', err);
        if (bubbleElement) bubbleElement.innerText = `流式错误: ${err.message}`;
        appendSystemMessage('对话失败，请重试');
        updateAgentUI('waiting');
    } finally {
        isWaitingResponse = false;
        sendBtn.disabled = false;
        messageInput.focus();
    }
}

function handleSend() {
    if (sendDebounceTimer) clearTimeout(sendDebounceTimer);
    sendDebounceTimer = setTimeout(async () => {
        const msg = messageInput.value.trim();
        if (!msg) return;
        if (!currentAgent) {
            const selected = await fetchAndSelectAgent();
            if (!selected || !currentAgent) {
                appendSystemMessage('请先选择一个智能体再开始对话');
                return;
            }
        }
        await sendMessageStream(msg);
    }, 100);
}

async function initPage() {
    if (!checkAuthAndRedirect()) return;
    currentAgentNameSpan.innerText = '加载智能体...';
    const success = await fetchAndSelectAgent(false);
    if (!success || !currentAgent) {
        appendSystemMessage('未获取到任何智能体，请刷新或联系管理员');
    }
    updateAgentUI('waiting');
    bindInputEvents();
    sendBtn.addEventListener('click', handleSend);
    messageInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            handleSend();
        }
    });
    switchAgentBtn.addEventListener('click', async () => {
        if (isWaitingResponse) {
            appendSystemMessage('请等待当前回复完成再切换');
            return;
        }
        await fetchAndSelectAgent(true);
        appendSystemMessage(`已切换至智能体：「${currentAgent?.agentName}」`);
    });
}

initPage();