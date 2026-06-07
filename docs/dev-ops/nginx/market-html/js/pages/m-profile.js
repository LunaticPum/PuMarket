const MktProfile = {
  async render() {
    const username = localStorage.getItem('mkt_username') || 'admin';
    return `
      <div class="profile-card">
        <div class="profile-avatar">🔧</div>
        <div class="profile-name">${username}</div>
        <div class="profile-role">营销管理员</div>
      </div>
      <div class="menu-item" id="btnLogout"><span>🚪 退出登录</span><span class="menu-arrow">→</span></div>
    `;
  },
  afterRender() {
    document.getElementById('btnLogout').onclick = () => {
      Toast.confirm('退出登录', '确定要退出吗？', () => {
        localStorage.clear();
        location.href = 'login.html';
      });
    };
  }
};
