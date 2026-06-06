/**
 * 工具函数
 */
const Utils = {
  // 生成唯一订单号 (格式: ORD + 时间戳 + 随机串)
  generateOrderNo() {
    const ts = Date.now().toString(36).toUpperCase();
    const rand = Math.random().toString(36).substring(2, 10).toUpperCase();
    return `ORD${ts}${rand}`;
  },

  // 格式化时间 yyyy-MM-dd HH:mm
  formatTime(dateStr) {
    if (!dateStr) return '';
    const d = new Date(dateStr);
    const pad = n => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
  },

  // 格式化价格
  formatPrice(val) {
    if (val == null) return '--';
    return Number(val).toFixed(2);
  },

  // 获取当前模拟用户 ID (简单用 localStorage 存储)
  getUserId() {
    let uid = localStorage.getItem('gb_uid');
    if (!uid) {
      uid = '10' + String(Math.floor(Math.random() * 9000 + 1000)); // 1000-9999
      localStorage.setItem('gb_uid', uid);
    }
    return Number(uid);
  },

  getUserName() {
    return localStorage.getItem('gb_uname') || ('用户' + this.getUserId());
  },

  setUserName(name) {
    localStorage.setItem('gb_uname', name);
  }
};
