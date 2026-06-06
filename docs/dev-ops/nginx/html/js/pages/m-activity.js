const MktActivity = {
  async render() {
    // 获取已发布的活动列表 + 商品列表（用于新建活动选商品）
    let published, products;
    try {
      [published, products] = await Promise.all([
        API.getPublishedActivities(),
        API.getProductList(1, 100)
      ]);
    } catch (e) { return `<div class="loading">加载失败</div>`; }

    // 已发布活动列表
    let pubHtml = '';
    (published?.list || []).forEach(a => {
      const statusClass = a.status === 1 ? 'active' : '';
      const statusText = a.status === 1 ? '进行中' : '已禁用';
      pubHtml += `<div class="pub-act-item">
        <div class="pai-header"><span class="pai-name">📋 ${a.activityName}</span><span class="pai-status ${statusClass}">${statusText}</span></div>
        <div class="pai-meta">商品: ${(a.skuIds||[]).join(', ')}</div>
        <div class="pai-meta">时间: ${new Date(a.startTime).toLocaleDateString()} ~ ${new Date(a.endTime).toLocaleDateString()} · ${a.teamCount||0}个团</div>
        <button class="btn-danger" style="margin-top:8px;padding:6px" onclick="MktActivity.revoke(${a.activityId})">撤销此活动</button>
      </div>`;
    });

    // 商品 checkbox 列表
    let cbHtml = '';
    (products?.list || []).forEach(p => {
      cbHtml += `<label class="cb-item"><input type="checkbox" class="cb-sku" value="${p.skuId}" data-name="${p.productName}"> ${p.productName} (¥${p.originPrice})</label>`;
    });

    // 优惠算法选项
    const discountOptions = [
      {v:1, n:'立减 (n元立减)'}, {v:2, n:'直降 (n元秒杀)'}, {v:3, n:'n折优惠'}, {v:4, n:'满减 (满n减n)'}
    ].map(o => `<option value="${o.v}">${o.n}</option>`).join('');

    return `
      ${pubHtml ? `<div class="act-section"><div class="act-sect-title">📋 已发布活动</div>${pubHtml}</div>` : ''}
      <div class="act-section">
        <div class="act-sect-title">➕ 发布新活动</div>
        <div class="form-group"><label class="form-label">活动名称</label><input class="form-input" id="actName" placeholder="例如：三人拼团8折"></div>
        <div class="form-group"><label class="form-label">活动类型</label><select class="form-select" id="actType"><option value="1">拼团活动</option><option value="2">凑单活动</option></select></div>
        <div class="form-group"><label class="form-label">优惠算法</label><select class="form-select" id="actDiscount">${discountOptions}</select></div>
        <div class="form-group" id="discountParamGroup"><label class="form-label">优惠参数</label><div class="form-row"><input class="form-input" id="discountParamVal" placeholder="例如：80" style="flex:2"><span style="line-height:40px;color:#999;flex:1" id="discountUnit">%</span></div></div>
        <div class="form-group"><label class="form-label">参团人数</label><input class="form-input" type="number" id="actRequiredNum" value="3"></div>
        <div class="form-group"><label class="form-label">总优惠名额</label><input class="form-input" type="number" id="actTotalQuota" value="100"></div>
        <div class="form-row"><div class="form-group"><label class="form-label">开始时间</label><input class="form-input" type="datetime-local" id="actStart"></div><div class="form-group"><label class="form-label">结束时间</label><input class="form-input" type="datetime-local" id="actEnd"></div></div>
        <div class="form-group"><label class="form-label">适用商品（可多选）</label><div class="checkbox-list">${cbHtml}</div></div>
        <button class="btn-primary" id="btnCheckConflict">🔍 检查冲突</button>
        <button class="btn-primary" id="btnCreateActivity" style="background:linear-gradient(135deg,#e4393c,#ff6b3d);margin-top:4px">✅ 发布活动</button>
      </div>`;
  },

  afterRender() {
    // 优惠算法切换 → 修改参数单位
    document.getElementById('actDiscount').onchange = function() {
      const map = {1:'元', 2:'元', 3:'%', 4:'满/减'};
      document.getElementById('discountUnit').textContent = map[this.value] || '';
    };

    // 检查冲突
    document.getElementById('btnCheckConflict').onclick = async () => {
      const skuIds = getSelectedSkuIds();
      if (!skuIds.length) { Toast.show('请先选择商品', 'error'); return; }
      Toast.show('可通过发布时自动检测冲突', 'info');
    };

    // 发布活动
    document.getElementById('btnCreateActivity').onclick = async () => {
      const name = document.getElementById('actName').value.trim();
      const discountType = +document.getElementById('actDiscount').value;
      const paramVal = document.getElementById('discountParamVal').value.trim();
      const requiredNum = +document.getElementById('actRequiredNum').value;
      const totalQuota = +document.getElementById('actTotalQuota').value;
      const startTime = document.getElementById('actStart').value;
      const endTime = document.getElementById('actEnd').value;
      const skuIds = getSelectedSkuIds();

      if (!name || !paramVal || !startTime || !endTime || !skuIds.length) {
        Toast.show('请填写完整信息', 'error'); return;
      }

      // 构造 discountParam JSON
      let discountParam;
      switch (discountType) {
        case 1: discountParam = JSON.stringify({amount: +paramVal}); break;
        case 2: discountParam = JSON.stringify({price: +paramVal}); break;
        case 3: discountParam = JSON.stringify({percent: +paramVal}); break;
        case 4: {
          const parts = paramVal.split('/');
          discountParam = JSON.stringify({threshold: +parts[0], amount: +parts[1]});
          break;
        }
      }

      showLoading('发布中...');
      try {
        await API.createActivity({
          activityName: name, activityType: +document.getElementById('actType').value,
          discountType, discountParam, requiredNum, totalQuota,
          startTime: new Date(startTime), endTime: new Date(endTime), skuIds
        });
        hideLoading();
        Toast.alert('✅ 发布成功', '活动已创建', () => MktApp.render());
      } catch (e) {
        hideLoading();
        Toast.alert('⚠️ 发布失败', e.message, () => {});
      }
    };
  },

  async revoke(activityId) {
    Toast.confirm('⚠️ 确认撤销活动', '撤销后商品页面不再展示该活动，未成团队伍将直接成团。确定要撤销吗？', async () => {
      showLoading('撤销中...');
      try {
        await API.revokeActivity(activityId);
        hideLoading();
        Toast.show('已撤销');
        MktApp.render();
      } catch (e) {
        hideLoading();
        Toast.show('撤销失败: ' + e.message, 'error');
      }
    });
  }
};

function getSelectedSkuIds() {
  return [...document.querySelectorAll('.cb-sku:checked')].map(cb => +cb.value);
}
function showLoading(m) { const e = document.getElementById('globalLoading'); if (e) { e.style.display = 'flex'; e.querySelector('div:last-child').textContent = m; } }
function hideLoading() { const e = document.getElementById('globalLoading'); if (e) e.style.display = 'none'; }
