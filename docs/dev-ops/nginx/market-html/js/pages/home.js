/**
 * 首页 — 商品列表
 */
const PageHome = {
  async render() {
    const data = await API.getProductList(1, 10);
    const list = data.list || [];

    let html = `
      <div class="home-banner">
        <div class="banner-title">🔥 拼团狂欢 限时特惠</div>
        <div class="banner-sub">好物低价，和好友一起拼更划算</div>
      </div>
      <div class="product-grid">`;

    list.forEach(item => {
      const minPrice = item.minGroupPrice
        ? `<span class="pg-min-price">拼团 ¥${Utils.formatPrice(item.minGroupPrice)}</span>`
        : '';
      const tags = (item.activityTags || []).slice(0, 2).map(t =>
        `<span class="pg-tag">${t.activityName}</span>`
      ).join('');
      const groupHint = item.activeGroupCount > 0
        ? `<span class="pg-group-hint">${item.activeGroupCount}个团进行中</span>`
        : '';

      html += `
        <div class="product-card" data-sku="${item.skuId}">
          <div class="pc-img-wrap">
            <img class="pc-img" src="${item.imageUrl || ''}" alt="${item.productName}" loading="lazy"
                 onerror="this.src='data:image/svg+xml,<svg xmlns=%27http://www.w3.org/2000/svg%27 viewBox=%270 0 200 200%27><rect fill=%27%23f0f0f0%27 width=%27200%27 height=%27200%27/><text x=%2750%25%27 y=%2750%25%27 text-anchor=%27middle%27 dy=%27.3em%27 fill=%27%23ccc%27 font-size=%2714%27>暂无图片</text></svg>'">
          </div>
          <div class="pc-info">
            <div class="pc-name">${item.productName}</div>
            <div class="pc-tags">${tags}${groupHint}</div>
            <div class="pc-price-row">
              <span class="pc-price">¥${Utils.formatPrice(item.originPrice)}</span>
              ${minPrice}
            </div>
          </div>
        </div>`;
    });

    html += '</div>';
    return html;
  },

  afterRender() {
    // 绑定商品卡片点击 → 跳转详情页
    document.querySelectorAll('.product-card').forEach(card => {
      card.addEventListener('click', () => {
        const skuId = card.dataset.sku;
        App.navigate('detail', { skuId: Number(skuId) });
      });
    });
  }
};
