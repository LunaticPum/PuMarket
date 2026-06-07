/**
 * 我的订单列表页
 */
const PageOrders = {
  async render() {
    const userId = Utils.getUserId();
    let data;
    try {
      data = await API.getUserOrders(userId, 1, 20);
    } catch (e) {
      return '<div class="empty-state">暂无订单<br><small>去首页逛一逛吧</small></div>';
    }

    const list = data.list || [];
    if (list.length === 0) {
      return `
        <div class="empty-state">
          <div class="empty-icon">📦</div>
          <div>暂无订单</div>
          <div class="empty-sub">去首页逛逛，和朋友一起拼团吧</div>
        </div>`;
    }

    const statusColors = {
      '待支付': '#fa8c16',
      '待成团': '#1890ff',
      '已发货': '#52c41a',
      '已取消': '#999',
      '拼团失败': '#ff4d4f'
    };

    let html = '';
    list.forEach(item => {
      const color = statusColors[item.displayStatus] || '#666';
      html += `
        <div class="order-card" data-orderno="${item.orderNo}">
          <div class="oc-header">
            <span class="oc-shop">拼团商城</span>
            <span class="oc-status" style="color:${color}">${item.displayStatus}</span>
          </div>
          <div class="oc-body">
            <img class="oc-img" src="${item.imageUrl || ''}" alt="${item.productName}"
                 onerror="this.style.display='none'">
            <div class="oc-info">
              <div class="oc-name">${item.productName}</div>
              <div class="oc-meta">
                <span>数量: ${item.quantity}</span>
                <span class="oc-price">¥${Utils.formatPrice(item.actualPrice || item.originPrice)}</span>
              </div>
              ${item.teamProgress ? `<div class="oc-team-progress">拼团进度: ${item.teamProgress}</div>` : ''}
              ${item.activityName ? `<div class="oc-activity">${item.activityName}</div>` : ''}
            </div>
          </div>
          <div class="oc-footer">
            <span class="oc-time">${Utils.formatTime(item.orderCreateTime)}</span>
            <span class="oc-order-no">订单号: ${item.orderNo}</span>
          </div>
        </div>`;
    });

    return `<div class="orders-page">${html}</div>`;
  },

  afterRender() {
    document.querySelectorAll('.order-card').forEach(card => {
      card.addEventListener('click', () => {
        const orderNo = card.dataset.orderno;
        App.navigate('orderDetail', { orderNo });
      });
    });
  }
};
