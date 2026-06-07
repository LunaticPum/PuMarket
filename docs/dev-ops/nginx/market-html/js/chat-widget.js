/**
 * AI 聊天挂件 —— 拼团商城智能助手 "小拼"
 */

// Agent 服务地址 (部署时改为实际地址，建议 Agent 部署在 8092 避免和商城 8091 冲突)
const AGENT_BASE = '/api/agent';
const AGENT_ID = '200001';

(function () {
  // 创建 DOM
  const btn = document.createElement('button');
  btn.className = 'ai-chat-btn';
  btn.innerHTML = '🤖';
  btn.title = 'AI购物助手·小拼';

  const panel = document.createElement('div');
  panel.className = 'ai-chat-panel';
  panel.innerHTML = `
    <div class="ai-chat-header">
      <span>🤖 小拼 · AI购物助手</span>
      <button class="ai-chat-close">✕</button>
    </div>
    <div class="ai-chat-messages" id="aiMessages">
      <div class="ai-msg bot">👋 你好！我是小拼，你的AI购物助手～<br>可以问我商品推荐、拼团规则、订单状态等问题哦 <span class="msg-time">刚刚</span></div>
    </div>
    <div class="ai-chat-input">
      <input type="text" id="aiInput" placeholder="输入问题，例如：推荐一款耳机">
      <button id="aiSend">➤</button>
    </div>`;

  document.body.appendChild(btn);
  document.body.appendChild(panel);

  // 事件绑定
  btn.onclick = () => panel.classList.toggle('open');
  panel.querySelector('.ai-chat-close').onclick = () => panel.classList.remove('open');

  const input = document.getElementById('aiInput');
  const sendBtn = document.getElementById('aiSend');
  const messagesEl = document.getElementById('aiMessages');

  async function sendMessage() {
    const text = input.value.trim();
    if (!text) return;

    // 显示用户消息
    appendMsg('user', text);
    input.value = '';

    // 显示 typing
    const typingEl = document.createElement('div');
    typingEl.className = 'ai-msg bot';
    typingEl.innerHTML = '<div class="ai-typing"><span></span><span></span><span></span></div>';
    messagesEl.appendChild(typingEl);
    messagesEl.scrollTop = messagesEl.scrollHeight;

    try {
      // 1. 获取商城数据上下文
      const userId = (() => { try { return Utils ? Utils.getUserId() : null; } catch (e) { return null; } })();
      let dataContext = '';
      try {
        const aiData = await callApi('/api/v1/ai/query', { userId });
        if (aiData) {
          dataContext = `\n\n【当前商城数据】\n商品列表: ${aiData.productsJson || '[]'}\n用户订单: ${aiData.ordersJson || '[]'}\n可参与活动: ${aiData.activitiesJson || '[]'}`;
        }
      } catch (e) { /* 数据获取失败也继续 */ }

      // 2. 调用 Agent
      const fullMessage = dataContext ? `${text}${dataContext}` : text;
      const agentRes = await callAgent(AGENT_ID, userId || 'guest', fullMessage);

      // 3. 移除 typing，显示回复
      typingEl.remove();
      appendMsg('bot', agentRes || '抱歉，我暂时无法回答，请稍后再试。');

    } catch (e) {
      typingEl.remove();
      appendMsg('bot', '😅 抱歉，网络出了点问题，请稍后再试～');
    }
  }

  function appendMsg(role, content) {
    const el = document.createElement('div');
    el.className = `ai-msg ${role}`;
    const time = new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
    el.innerHTML = `${content}<div class="msg-time">${time}</div>`;
    messagesEl.appendChild(el);
    messagesEl.scrollTop = messagesEl.scrollHeight;
  }

  async function callApi(url, body) {
    const res = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    });
    const json = await res.json();
    return json.code === '0000' ? json.data : null;
  }

  async function callAgent(agentId, userId, message) {
    const res = await fetch(`${AGENT_BASE}/chat`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ agentId: String(agentId), userId: String(userId), message })
    });
    const json = await res.json();
    if (json.code === '0000' && json.data) {
      return json.data.content || json.data;
    }
    return null;
  }

  sendBtn.onclick = sendMessage;
  input.onkeydown = (e) => { if (e.key === 'Enter') sendMessage(); };
})();
