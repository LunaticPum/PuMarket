const MktApp = (() => {
  let currentPage = 'dashboard', pageParams = {};
  const pageContainer = document.getElementById('pageContainer');
  const tabs = document.querySelectorAll('.tab-item');
  const pages = { dashboard: MktDashboard, activity: MktActivity, profile: MktProfile };

  function switchTab(name) {
    currentPage = name; pageParams = {};
    tabs.forEach(t => t.classList.remove('active'));
    document.querySelector(`.tab-item[data-page="${name}"]`)?.classList.add('active');
    render();
  }

  async function render() {
    pageContainer.innerHTML = '<div class="loading">加载中...</div>';
    try {
      const page = pages[currentPage];
      if (page?.render) {
        const html = await page.render(pageParams);
        pageContainer.innerHTML = html;
        if (page.afterRender) page.afterRender(pageParams);
      }
    } catch (e) {
      pageContainer.innerHTML = `<div class="loading">加载失败: ${e.message}</div>`;
    }
  }

  tabs.forEach(t => t.addEventListener('click', () => { const p = t.dataset.page; if (p) switchTab(p); }));
  render();
  return { switchTab, render };
})();
