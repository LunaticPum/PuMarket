/**
 * 自定义 Toast / Modal 组件 —— 替代浏览器 alert/confirm
 */
const Toast = {
  // 轻提示 (顶部滑入, 2s 自动消失)
  show(msg, type = 'success') {
    const el = document.createElement('div');
    el.className = `toast toast-${type}`;
    el.innerHTML = `<span>${type === 'success' ? '✅' : type === 'error' ? '❌' : 'ℹ️'}</span> ${msg}`;
    document.body.appendChild(el);
    setTimeout(() => { el.classList.add('show'); }, 10);
    setTimeout(() => { el.classList.remove('show'); setTimeout(() => el.remove(), 300); }, 2200);
  },

  // 确认对话框 (替代 confirm)
  confirm(title, msg, onOk, onCancel) {
    const mask = document.createElement('div');
    mask.className = 't-modal-mask';
    mask.innerHTML = `<div class="t-modal">
      <div class="t-modal-hd">${title}</div>
      <div class="t-modal-bd">${msg}</div>
      <div class="t-modal-ft">
        <button class="t-btn-cancel">取消</button>
        <button class="t-btn-ok">确定</button>
      </div></div>`;
    document.body.appendChild(mask);
    setTimeout(() => mask.classList.add('show'), 10);

    mask.querySelector('.t-btn-ok').onclick = () => { close(); if (onOk) onOk(); };
    mask.querySelector('.t-btn-cancel').onclick = () => { close(); if (onCancel) onCancel(); };
    mask.addEventListener('click', e => { if (e.target === mask) { close(); if (onCancel) onCancel(); } });

    function close() { mask.classList.remove('show'); setTimeout(() => mask.remove(), 200); }
  },

  // 提示对话框 (只有一个确定按钮)
  alert(title, msg, onOk) {
    const mask = document.createElement('div');
    mask.className = 't-modal-mask';
    mask.innerHTML = `<div class="t-modal">
      <div class="t-modal-hd">${title}</div>
      <div class="t-modal-bd">${msg}</div>
      <div class="t-modal-ft"><button class="t-btn-ok" style="flex:1">知道了</button></div></div>`;
    document.body.appendChild(mask);
    setTimeout(() => mask.classList.add('show'), 10);
    mask.querySelector('.t-btn-ok').onclick = () => { close(); if (onOk) onOk(); };
    mask.addEventListener('click', e => { if (e.target === mask) { close(); if (onOk) onOk(); } });
    function close() { mask.classList.remove('show'); setTimeout(() => mask.remove(), 200); }
  }
};
