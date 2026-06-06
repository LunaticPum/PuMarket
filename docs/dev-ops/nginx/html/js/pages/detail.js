const PageDetail = {
  async render(params) {
    const skuId = params.skuId;
    if (!skuId) return '<div class="empty-state">商品不存在</div>';

    const userId = Utils.getUserId();
    const data = await API.getProductDetail(skuId, userId);
    if (!data) return '<div class="empty-state">商品不存在</div>';

    // 活动信息 — 只展示该商品当前绑定的活动 (后端返回的 activities 已经是单活动)
    const activity = (data.activities || [])[0];
    const actBanner = activity ? `
      <div class="act-banner">
        🎉 当前商品正在参与「${activity.activityName}」
        <div class="act-banner-sub">${activity.discountDesc} · ${activity.requiredNum || 1}人成团 · 名额剩余 ${activity.quotaRemaining ?? '不限'}</div>
      </div>` : '<div class="act-banner" style="background:#f5f5f5;color:#999">该商品暂无进行中的活动</div>';

    // 队伍列表
    let teamHtml = '';
    (data.activeTeams || []).forEach(t => {
      teamHtml += `<div class="team-item" data-team-id="${t.groupTeamId}">
        <div class="team-info"><div class="team-avatar">👤</div>
          <div class="team-detail">
            <div class="team-progress"><span>${t.currentNum}/${t.requiredNum}人</span><span class="team-slots">差${t.remainingSlots}人</span></div>
            <div class="team-time">⏰ ${Utils.formatTime(t.expireTime)} 截止</div>
          </div></div>
        <button class="btn-join-team">去参团</button></div>`;
    });

    return `
      <div class="detail-page">
        <div class="dp-img-wrap"><img class="dp-img" src="${data.imageUrl || ''}" alt="${data.productName}" onerror="this.src='data:image/svg+xml,<svg xmlns=%27http://www.w3.org/2000/svg%27 viewBox=%270 0 400 300%27><rect fill=%27%23f0f0f0%27 width=%27400%27 height=%27300%27/><text x=%2750%25%27 y=%2750%25%27 fill=%27%23ccc%27 font-size=%2716%27>暂无图片</text></svg>'"></div>
        <div class="dp-body">
          <div class="dp-name">${data.productName}</div>
          <div class="dp-desc">${data.description || ''}</div>
          ${actBanner}
          <div class="dp-price-row"><span class="dp-price">¥${Utils.formatPrice(data.originPrice)}</span><span class="dp-stock">库存: ${data.stock} | 已售: ${data.soldCount}</span></div>
          ${teamHtml ? `<div class="dp-section"><div class="dp-section-title">👥 进行中的拼团</div>${teamHtml}</div>` : '<div class="dp-section"><div class="dp-section-title">👥 暂无进行中的拼团队伍</div></div>'}
          <div class="dp-actions">
            <button class="btn-buy-single" data-sku="${skuId}">单独购买<br><small>¥${Utils.formatPrice(data.originPrice)}</small></button>
            <button class="btn-buy-group" data-sku="${skuId}">发起拼团<br><small>${activity ? activity.discountDesc : '无活动'}</small></button>
          </div>
        </div>
      </div>`;
  },

  afterRender(params) {
    const skuId = params.skuId, userId = Utils.getUserId();

    // 单独购买
    document.querySelector('.btn-buy-single')?.addEventListener('click', () => doBuy(skuId, null, null));

    // 发起拼团 — 使用该商品当前绑定的活动
    document.querySelector('.btn-buy-group')?.addEventListener('click', async () => {
      // 先获取商品详情拿到活动
      const d = await API.getProductDetail(skuId, userId);
      const act = (d?.activities || [])[0];
      if (!act) { Toast.show('该商品暂无活动可参与', 'error'); return; }
      doBuy(skuId, act.activityId, null);
    });

    // 参团
    document.querySelectorAll('.btn-join-team').forEach(btn => {
      btn.addEventListener('click', async () => {
        const teamId = +btn.closest('.team-item').dataset.teamId;
        const d = await API.getProductDetail(skuId, userId);
        const act = (d?.activities || [])[0];
        if (!act) { Toast.show('该商品暂无活动', 'error'); return; }
        doBuy(skuId, act.activityId, teamId);
      });
    });

    async function doBuy(skuId, activityId, groupTeamId) {
      const orderNo = Utils.generateOrderNo();
      try {
        showLoading('下单中...');
        await API.createOrder({ userId, orderNo, skuId, quantity: 1, activityId: activityId || 0, groupTeamId: groupTeamId || null, entrySource: 1 });
        hideLoading();

        Toast.confirm('确认支付', `支付金额 ¥${(0).toFixed(2)}？`, async () => {
          showLoading('支付中...');
          await API.settleOrder(orderNo, userId);
          hideLoading();
          Toast.show('支付成功！');
          App.navigate('orders');
        }, async () => {
          await API.cancelOrder(orderNo, userId);
          Toast.show('已取消支付', 'info');
        });
      } catch (e) {
        hideLoading();
        Toast.show('下单失败: ' + e.message, 'error');
      }
    }
  }
};
