/**
 * 商品详情页
 */
const PageDetail = {
  async render(params) {
    const skuId = params.skuId;
    if (!skuId) return '<div class="empty-state">商品不存在</div>';

    const userId = Utils.getUserId();
    const data = await API.getProductDetail(skuId, userId);
    if (!data) return '<div class="empty-state">商品不存在</div>';

    // 活动列表
    let actHtml = '';
    (data.activities || []).forEach(a => {
      actHtml += `
        <div class="act-item" data-activity-id="${a.activityId}">
          <div class="act-name">🎯 ${a.activityName}</div>
          <div class="act-desc">${a.discountDesc}</div>
          <div class="act-meta">${a.activityTypeName} · ${a.requiredNum || 1}人成团</div>
        </div>`;
    });

    // 队伍列表
    let teamHtml = '';
    (data.activeTeams || []).forEach(t => {
      const timeLeft = t.expireTime ? Utils.formatTime(t.expireTime) : '';
      teamHtml += `
        <div class="team-item" data-team-id="${t.groupTeamId}">
          <div class="team-info">
            <div class="team-avatar">👤</div>
            <div class="team-detail">
              <div class="team-progress">
                <span>${t.currentNum}/${t.requiredNum}人</span>
                <span class="team-slots">差${t.remainingSlots}人成团</span>
              </div>
              <div class="team-time">⏰ ${timeLeft} 截止</div>
            </div>
          </div>
          <button class="btn-join-team">去参团</button>
        </div>`;
    });

    return `
      <div class="detail-page">
        <div class="dp-img-wrap">
          <img class="dp-img" src="${data.imageUrl || ''}" alt="${data.productName}"
               onerror="this.src='data:image/svg+xml,<svg xmlns=%27http://www.w3.org/2000/svg%27 viewBox=%270 0 400 300%27><rect fill=%27%23f0f0f0%27 width=%27400%27 height=%27300%27/><text x=%2750%25%27 y=%2750%25%27 text-anchor=%27middle%27 fill=%27%23ccc%27 font-size=%2716%27>暂无图片</text></svg>'">
        </div>
        <div class="dp-body">
          <div class="dp-name">${data.productName}</div>
          <div class="dp-desc">${data.description || ''}</div>
          <div class="dp-price-row">
            <span class="dp-price">¥${Utils.formatPrice(data.originPrice)}</span>
            <span class="dp-stock">库存: ${data.stock} | 已售: ${data.soldCount}</span>
          </div>

          ${actHtml ? `<div class="dp-section"><div class="dp-section-title">🎪 可参与的活动</div>${actHtml}</div>` : ''}
          ${teamHtml ? `<div class="dp-section"><div class="dp-section-title">👥 进行中的拼团</div>${teamHtml}</div>`
            : '<div class="dp-section"><div class="dp-section-title">👥 暂无进行中的拼团队伍</div></div>'}

          <div class="dp-actions">
            <button class="btn-buy-single" data-sku="${skuId}">单独购买<br><small>¥${Utils.formatPrice(data.originPrice)}</small></button>
            <button class="btn-buy-group" data-sku="${skuId}">发起拼团<br><small>最低 ¥${data.activities && data.activities[0] ? Utils.formatPrice(data.originPrice) : '--'}</small></button>
          </div>
        </div>
      </div>`;
  },

  afterRender(params) {
    const skuId = params.skuId;
    const userId = Utils.getUserId();

    // 活动选中高亮
    let selectedActivityId = null;
    document.querySelectorAll('.act-item').forEach(item => {
      item.addEventListener('click', () => {
        document.querySelectorAll('.act-item').forEach(i => i.classList.remove('selected'));
        item.classList.add('selected');
        selectedActivityId = Number(item.dataset.activityId);
      });
    });

    // 单独购买
    document.querySelector('.btn-buy-single')?.addEventListener('click', async () => {
      await doBuy(skuId, null, null);
    });

    // 发起拼团
    document.querySelector('.btn-buy-group')?.addEventListener('click', async () => {
      if (!selectedActivityId) {
        alert('请先选择一个活动');
        return;
      }
      await doBuy(skuId, selectedActivityId, null);
    });

    // 参团
    document.querySelectorAll('.btn-join-team').forEach(btn => {
      btn.addEventListener('click', async () => {
        const teamItem = btn.closest('.team-item');
        const teamId = Number(teamItem.dataset.teamId);
        // 参团需要知道 activityId，从选中的活动获取，或默认用第一个
        const actId = selectedActivityId ||
          (document.querySelector('.act-item')?.dataset.activityId || null);
        if (!actId) {
          alert('请先选择一个活动');
          return;
        }
        await doBuy(skuId, Number(actId), teamId);
      });
    });

    async function doBuy(skuId, activityId, groupTeamId) {
      const orderNo = Utils.generateOrderNo();
      try {
        showLoading('下单中...');
        const result = await API.createOrder({
          userId, orderNo, skuId, quantity: 1,
          activityId: activityId || 0,
          groupTeamId: groupTeamId || null,
          entrySource: 1
        });
        hideLoading();

        if (result) {
          const confirmed = await showPayModal(result.payPrice || result.actualPrice);
          if (confirmed) {
            showLoading('支付中...');
            await API.settleOrder(orderNo, userId);
            hideLoading();
            alert('✅ 支付成功！订单已生成');
            App.navigate('orders');
          } else {
            // 用户取消支付 → 取消订单
            await API.cancelOrder(orderNo, userId);
            alert('已取消支付');
          }
        }
      } catch (e) {
        hideLoading();
        alert('下单失败: ' + e.message);
      }
    }
  }
};

// 简单的加载和支付弹窗 (内联实现)
function showLoading(msg) {
  let el = document.getElementById('globalLoading');
  if (!el) {
    el = document.createElement('div');
    el.id = 'globalLoading';
    el.className = 'global-loading';
    el.innerHTML = '<div class="loading-spinner"></div><div class="loading-text"></div>';
    document.body.appendChild(el);
  }
  el.querySelector('.loading-text').textContent = msg;
  el.style.display = 'flex';
}

function hideLoading() {
  const el = document.getElementById('globalLoading');
  if (el) el.style.display = 'none';
}

function showPayModal(amount) {
  return new Promise(resolve => {
    const displayAmount = Number(amount).toFixed(2);
    const ok = confirm(`确认支付 ¥${displayAmount}？\n\n点击"确定"模拟支付成功\n点击"取消"放弃支付`);
    resolve(ok);
  });
}
