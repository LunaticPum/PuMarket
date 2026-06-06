const MktDashboard = {
  async render() {
    let data;
    try { data = await API.getDashboardOverview(); } catch (e) { return `<div class="loading">加载失败: ${e.message}</div>`; }
    if (!data) return '<div class="loading">暂无数据</div>';

    let blacklistHtml = '';
    (data.blacklist || []).forEach(u => {
      blacklistHtml += `<div style="padding:6px 0;font-size:13px;color:#666">⚠️ 用户${u.userId} · ${u.tagName}</div>`;
    });

    let rankHtml = '';
    (data.topProducts || []).forEach(r => {
      rankHtml += `<div style="display:flex;justify-content:space-between;padding:6px 0;font-size:13px;border-bottom:1px solid #f5f5f5">
        <span>${r.rank}. ${r.productName}</span><span style="color:#e4393c;font-weight:600">售${r.soldCount}件 ¥${Number(r.totalAmount).toFixed(0)}</span></div>`;
    });

    let actHtml = '';
    (data.activeActivities || []).forEach(a => {
      actHtml += `<div class="pub-act-item" style="cursor:pointer" data-actid="${a.activityId}">
        <div class="pai-header"><span class="pai-name">📋 ${a.activityName}</span><span class="pai-status active">进行中</span></div>
        <div class="pai-meta">${a.teamCount || 0}个团 · ${(a.skuIds||[]).length}个商品</div>
      </div>`;
    });

    return `
      <div class="dash-stats">
        <div class="ds-card"><div class="ds-val">${data.todayOrders}</div><div class="ds-label">今日订单</div></div>
        <div class="ds-card"><div class="ds-val">¥${Number(data.todayRevenue||0).toFixed(0)}</div><div class="ds-label">今日成交额</div></div>
        <div class="ds-card"><div class="ds-val">${data.activeActivityCount}</div><div class="ds-label">进行中活动</div></div>
        <div class="ds-card"><div class="ds-val">${data.pendingShipTeams}</div><div class="ds-label">待发货团队</div></div>
      </div>
      ${blacklistHtml ? `<div class="dash-section"><div class="dash-sect-title">⚠️ 黑名单用户</div>${blacklistHtml}</div>` : ''}
      <div class="dash-section"><div class="dash-sect-title">📈 商品销量排行</div>${rankHtml || '<div style="color:#999;font-size:13px">暂无数据</div>'}</div>
      <div class="dash-section"><div class="dash-sect-title">🏷 进行中活动</div>${actHtml || '<div style="color:#999;font-size:13px">暂无活动</div>'}</div>
    `;
  },

  afterRender() {
    document.querySelectorAll('.pub-act-item').forEach(el => {
      el.onclick = () => {
        const aid = el.dataset.actid;
        if (aid) MktApp.navigate('activityDetail', { activityId: Number(aid) });
      };
    });
  }
};
