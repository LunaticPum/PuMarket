/**
 * API 客户端 — 统一封装所有后端接口请求
 */
const API = (() => {
  // 后端地址：通过 nginx 反向代理，同源访问无需跨域
  // 本地开发时可改为 'http://localhost:8080/api/v1'
  const BASE = '/api/v1';

  async function request(url, body) {
    const res = await fetch(`${BASE}${url}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    });
    const json = await res.json();
    if (json.code !== '0000') {
      throw new Error(json.info || '请求失败');
    }
    return json.data;
  }

  return {
    // 商品
    getProductList(page = 1, size = 10) {
      return request('/product/list', { page, size });
    },
    getProductDetail(skuId, userId) {
      return request('/product/detail', { skuId, userId });
    },

    // 交易
    createOrder(params) {
      return request('/trade/create_order', params);
    },
    settleOrder(orderNo, userId) {
      return request('/trade/settle_order', { orderNo, userId });
    },
    cancelOrder(orderNo, userId) {
      return request('/trade/cancel_order', { orderNo, userId });
    },

    // 用户
    getUserOrders(userId, page = 1, size = 10) {
      return request('/user/orders', { userId, page, size });
    },
    getUserOrderDetail(orderNo, userId) {
      return request('/user/order/detail', { orderNo, userId });
    }
  };
})();
