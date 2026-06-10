const MktActivityDetail = {
  async render(params) {
    const activityId = params.activityId;
    if (!activityId) return '<div class="loading">活动不存在</div>';

    let activity, teams;
    try {
      [activity, teams] = await Promise.all([
        API.getActivityDetail(activityId, 1, 100),
        API.getPublishedActivities()
      ]);
    } catch (e) { return `<div class="loading">加载失败: ${e.message}</div>`; }

    const act = [...(activity?.teams || [])];
    const actInfo = teams?.list?.find(a => String(a.activityId) === String(activityId));
    if (!actInfo) return '<div class="loading">活动不存在</div>';

    return `
      <div style="padding:16px">
        <div class="act-section" style="margin:0 0 12px">
          <div class="act-sect-title">📋 ${actInfo.activityName}</div>
          <div style="font-size:13px;color:#666;margin:4px 0">类型: ${actInfo.activityType === 1 ? '拼团活动' : '凑单活动'} · 优惠: ${actInfo.discountType === 1 ? '立减' : actInfo.discountType === 2 ? '直降' : actInfo.discountType === 3 ? '折扣' : '满减'}</div>
          <div style="font-size:13px;color:#666">时间: ${new Date(actInfo.startTime).toLocaleDateString()} ~ ${new Date(actInfo.endTime).toLocaleDateString()} · ${actInfo.teamCount || 0}个团</div>
          <div style="font-size:13px;color:#666">适用商品: ${(actInfo.skuIds || []).join(', ')}</div>
          <button class="btn-secondary" style="margin-top:10px" id="btnEdit">✏️ 编辑活动</button>
        </div>

        <div class="act-section" style="margin:0">
          <div class="act-sect-title">👥 队伍列表 (${act.length})</div>
          ${act.map(t => `
            <div style="padding:8px;background:#fafafa;border-radius:8px;margin-bottom:6px;font-size:13px">
              <div>队${t.groupTeamId} · 团长:${t.leaderUserId} · ${t.currentNum}/${t.requiredNum}人 · ${t.teamStatusName}</div>
              <div style="color:#999;font-size:11px">创建: ${new Date(t.teamCreateTime).toLocaleString()} · 截止: ${new Date(t.teamExpireTime).toLocaleString()}</div>
            </div>`).join('') || '<div style="color:#999;font-size:13px">暂无队伍</div>'}
        </div>
      </div>
      <div id="editForm" style="display:none;padding:16px">
        <div class="act-section" style="margin:0">
          <div class="act-sect-title">✏️ 编辑活动</div>
          <div class="form-group"><label class="form-label">活动名称</label><input class="form-input" id="editName" value="${actInfo.activityName}"></div>
          <div class="form-group"><label class="form-label">优惠算法</label><select class="form-select" id="editDiscount">
            <option value="1" ${actInfo.discountType===1?'selected':''}>立减</option>
            <option value="2" ${actInfo.discountType===2?'selected':''}>直降</option>
            <option value="3" ${actInfo.discountType===3?'selected':''}>n折优惠</option>
            <option value="4" ${actInfo.discountType===4?'selected':''}>满减(满/减)</option>
          </select></div>
          <div class="form-group"><label class="form-label">优惠参数</label><input class="form-input" id="editParam" placeholder="80"></div>
          <div class="form-group"><label class="form-label">总名额</label><input class="form-input" type="number" id="editQuota" value="${actInfo.totalDiscountQuota || 100}"></div>
          <div class="form-row"><div class="form-group"><label class="form-label">开始</label><input class="form-input" type="datetime-local" id="editStart"></div>
          <div class="form-group"><label class="form-label">结束</label><input class="form-input" type="datetime-local" id="editEnd"></div></div>
          <button class="btn-primary" id="btnSaveEdit" style="background:linear-gradient(135deg,#ff6b3d,#e4393c)">💾 保存修改（编辑后当前拼团队伍将立即成团）</button>
          <button class="btn-secondary" id="btnCancelEdit">取消</button>
        </div>
      </div>`;
  },

  afterRender(params) {
    const activityId = params.activityId;

    const btnEdit = document.getElementById('btnEdit');
    const btnCancelEdit = document.getElementById('btnCancelEdit');
    const btnSaveEdit = document.getElementById('btnSaveEdit');

    if (btnEdit) btnEdit.onclick = () => {
      document.getElementById('editForm').style.display = 'block';
    };
    if (btnCancelEdit) btnCancelEdit.onclick = () => {
      document.getElementById('editForm').style.display = 'none';
    };

    if (btnSaveEdit) btnSaveEdit.onclick = () => {
      const name = document.getElementById('editName').value.trim();
      const discountType = +document.getElementById('editDiscount').value;
      const paramVal = document.getElementById('editParam').value.trim();
      const quota = +document.getElementById('editQuota').value;
      const start = document.getElementById('editStart').value;
      const end = document.getElementById('editEnd').value;

      if (!name || !paramVal || !start || !end) {
        Toast.show('请填写完整信息', 'error'); return;
      }

      let discountParam;
      switch (discountType) {
        case 1: discountParam = JSON.stringify({amount: +paramVal}); break;
        case 2: discountParam = JSON.stringify({price: +paramVal}); break;
        case 3: discountParam = JSON.stringify({percent: +paramVal}); break;
        case 4: { const p = paramVal.split('/'); discountParam = JSON.stringify({threshold: +p[0], amount: +p[1]}); break; }
      }

      Toast.confirm('⚠️ 确认修改活动',
        '修改后该活动下所有进行中的拼团队伍将立即成团。确定要修改吗？',
        async () => {
          showLoading('保存中...');
          try {
            await API.updateActivity({
              activityId, activityName: name,
              discountType, discountParam, totalQuota: quota,
              startTime: new Date(start), endTime: new Date(end)
            });
            hideLoading();
            Toast.show('活动已更新，队伍已强制成团');
            MktApp.switchTab('dashboard');
          } catch (e) {
            hideLoading();
            Toast.show('修改失败: ' + e.message, 'error');
          }
        });
    };
  }
};
