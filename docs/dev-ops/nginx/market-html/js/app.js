/**
 * 应用入口 — Tab 路由 + 全局状态 + 页面切换
 */
const App = (() => {
  // 当前页面
  let currentPage = 'home';
  // 页面参数缓存
  let pageParams = {};

  // 页面容器
  const pageContainer = document.getElementById('pageContainer');

  // 底部 Tab
  const tabs = document.querySelectorAll('.tab-item');

  // 页面模块映射 (动态 import 模式，但原生 JS 用全局变量代替)
  const pages = {
    home: PageHome,
    detail: PageDetail,
    orders: PageOrders,
    orderDetail: PageOrderDetail,
    profile: PageProfile
  };

  function switchTab(tabName) {
    currentPage = tabName;
    tabs.forEach(t => t.classList.remove('active'));
    const target = document.querySelector(`.tab-item[data-page="${tabName}"]`);
    if (target) target.classList.add('active');
    render();
  }

  function navigate(page, params) {
    pageParams = params || {};
    currentPage = page;
    // 非 tab 页面高亮对应 tab
    if (page === 'detail') {
      tabs.forEach(t => t.classList.remove('active'));
    }
    render();
  }

  function goBack() {
    if (currentPage === 'detail') { navigate('home'); }
    else if (currentPage === 'orderDetail') { navigate('orders'); }
    document.getElementById('btnBack').style.visibility =
      (currentPage === 'detail' || currentPage === 'orderDetail') ? 'visible' : 'hidden';
  }

  async function render() {
    pageContainer.innerHTML = '<div class="loading">加载中...</div>';

    try {
      const page = pages[currentPage];
      if (page && page.render) {
        const html = await page.render(pageParams);
        pageContainer.innerHTML = html;
        if (page.afterRender) {
          page.afterRender(pageParams);
        }
      } else {
        pageContainer.innerHTML = '<div class="empty-state">页面建设中...</div>';
      }
    } catch (e) {
      console.error('页面渲染失败:', e);
      pageContainer.innerHTML = `<div class="empty-state">加载失败: ${e.message}</div>`;
    }
  }

  // 绑定 Tab 点击
  tabs.forEach(tab => {
    tab.addEventListener('click', () => {
      const page = tab.dataset.page;
      if (page) switchTab(page);
    });
  });

  // 监听全局返回按钮
  document.addEventListener('click', e => {
    if (e.target.closest('#btnBack')) {
      goBack();
    }
  });

  // 初始渲染
  render();

  return {
    switchTab,
    navigate,
    goBack,
    refresh: render
  };
})();
