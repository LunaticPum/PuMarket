/**
 * 订单详情页
 */
const PageOrderDetail = {
  async render(params) {
    const orderNo = params.orderNo;
    if (!orderNo) return '<div class="empty-state">订单不存在</div>';

    const userId = Utils.getUserId();
    const data = await API.getUserOrderDetail(orderNo, userId);
    if (!data) return '<div class="empty-state">订单不存在或无权查看</div>';

    const statusColors = {
      '待支付': '#fa8c16',
      '待成团': '#1890ff',
      '已发货': '#52c41a',
      '已取消': '#999',
      '拼团失败': '#ff4d4f'
    };
    const color = statusColors[data.displayStatus] || '#666';

    // 拼团信息
    let teamHtml = '';
    if (data.teamInfo) {
      const t = data.teamInfo;
      teamHtml = `
        <div class="od-section">
          <div class="od-section-title">👥 拼团信息</div>
          <div class="od-team-card">
            <div class="od-team-row">
              <span>队伍编号</span><span>${t.groupTeamId}</span>
            </div>
            <div class="od-team-row">
              <span>团长 ID</span><span>${t.leaderUserId}</span>
            </div>
            <div class="od-team-row">
              <span>成团进度</span>
              <span class="od-team-progress">
                <span class="progress-bar">
                  <span class="progress-fill" style="width:${(t.settledTradeNum / t.requiredNum * 100).toFixed(0)}%"></span>
                </span>
                ${t.settledTradeNum}/${t.requiredNum}人已结算
              </span>
            </div>
            <div class="od-team-row">
              <span>当前参团</span><span>${t.currentNum}人 (差${t.remainingSlots}人)</span>
            </div>
            <div class="od-team-row">
              <span>队伍状态</span><span class="od-team-status">${t.teamStatusName}</span>
            </div>
            <div class="od-team-row">
              <span>截止时间</span><span>${Utils.formatTime(t.teamExpireTime)}</span>
            </div>
          </div>
        </div>`;
    }

    // 操作按钮
    let actionHtml = '';
    if (data.orderStatus === 0) { // 待支付
      actionHtml = `
        <div class="od-actions">
          <button class="btn-cancel" id="btnCancelOrder">取消订单</button>
          <button class="btn-pay" id="btnPayOrder">立即支付 ¥${Utils.formatPrice(data.actualPrice || data.originPrice)}</button>
        </div>`;
    }

    return `
      <div class="od-page">
        <div class="od-status-bar" style="background:${color}">
          <div class="od-status-icon">
            ${data.displayStatus === '已发货' ? '🚚' : data.displayStatus === '待成团' ? '⏳' : data.displayStatus === '待支付' ? '💳' : '📋'}
          </div>
          <div class="od-status-text">${data.displayStatus}</div>
        </div>

        <div class="od-section">
          <div class="od-section-title">📦 商品信息</div>
          <div class="od-product">
            <img class="od-pimg" src="${data.imageUrl || ''}" alt="${data.productName}"
                 onerror="this.style.display='none'">
            <div class="od-pinfo">
              <div class="od-pname">${data.productName}</div>
              <div class="od-pprice">¥${Utils.formatPrice(data.actualPrice || data.originPrice)} × ${data.quantity}</div>
              ${data.discountPrice > 0 ? `<div class="od-pdiscount">优惠: ¥${Utils.formatPrice(data.discountPrice)}</div>` : ''}
            </div>
          </div>
        </div>

        ${data.activityName ? `
        <div class="od-section">
          <div class="od-section-title">🎯 参与活动</div>
          <div class="od-activity">${data.activityName}</div>
        </div>` : ''}

        ${teamHtml}

        <div class="od-section">
          <div class="od-section-title">📋 订单信息</div>
          <div class="od-info-list">
            <div class="od-info-row"><span>订单编号</span><span class="od-info-val">${data.orderNo}</span></div>
            <div class="od-info-row"><span>创建时间</span><span>${Utils.formatTime(data.orderCreateTime)}</span></div>
            <div class="od-info-row"><span>过期时间</span><span>${Utils.formatTime(data.orderExpireTime)}</span></div>
            <div class="od-info-row"><span>用户 ID</span><span>${data.userId}</span></div>
          </div>
        </div>

        ${actionHtml}
      </div>`;
  },

  afterRender(params) {
    const orderNo = params.orderNo;
    const userId = Utils.getUserId();

    // 去支付
    document.getElementById('btnPayOrder')?.addEventListener('click', async () => {
      const ok = confirm('确认支付？');
      if (!ok) return;
      try {
        showLoading('支付中...');
        await API.settleOrder(orderNo, userId);
        hideLoading();
        Toast.show('支付成功！');
        App.navigate('orders');
      } catch (e) {
        hideLoading();
        Toast.show('支付失败: ' + e.message, 'error');
      }
    });

    // 取消订单
    document.getElementById('btnCancelOrder')?.addEventListener('click', async () => {
      const ok = confirm('确定取消该订单？');
      if (!ok) return;
      try {
        showLoading('取消中...');
        await API.cancelOrder(orderNo, userId);
        hideLoading();
        Toast.show('已取消订单');
        App.navigate('orders');
      } catch (e) {
        hideLoading();
        Toast.show('取消失败: ' + e.message, 'error');
      }
    });
  }
};
