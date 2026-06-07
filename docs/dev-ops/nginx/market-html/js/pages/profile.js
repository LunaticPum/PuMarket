const PageProfile = {
  async render() {
    const name = localStorage.getItem('gb_uname') || Utils.getUserName();
    const uid = Utils.getUserId();
    return `
      <div class="profile-card">
        <div class="profile-avatar">👤</div>
        <div class="profile-name">${name}</div>
        <div class="profile-role">用户ID: ${uid}</div>
      </div>
      <div class="menu-item" id="menuOrders"><span>📦 我的订单</span><span class="menu-arrow">→</span></div>
      <div class="menu-item" id="menuLogout"><span>🚪 退出登录</span><span class="menu-arrow">→</span></div>
    `;
  },
  afterRender() {
    document.getElementById('menuOrders').onclick = () => App.switchTab('orders');
    document.getElementById('menuLogout').onclick = () => {
      Toast.confirm('退出登录', '确定要退出吗？', () => {
        localStorage.removeItem('gb_uname');
        location.href = 'login.html';
      });
    };
  }
};
