/**
 * API 客户端 — 统一封装所有后端接口请求
 */
const API = (() => {
  const BASE = '/api/v1';

  async function request(url, body) {
    const res = await fetch(`${BASE}${url}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    });
    const json = await res.json();
    if (json.code !== '0000') throw new Error(json.info || '请求失败');
    return json.data;
  }

  // 带 JWT Token 的营销后台请求
  async function mktRequest(url, body) {
    const token = localStorage.getItem('mkt_token') || '';
    const res = await fetch(`${BASE}${url}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
      body: body ? JSON.stringify(body) : '{}'
    });
    if (res.status === 401) { localStorage.clear(); location.href = 'login.html'; return null; }
    const json = await res.json();
    if (json.code !== '0000') throw new Error(json.info || '请求失败');
    return json.data;
  }

  return {
    // 登录
    mktLogin(username, password) { return mktRequest('/marketing/login', { username, password }); },

    // 商品
    getProductList(page = 1, size = 10) { return request('/product/list', { page, size }); },
    getProductDetail(skuId, userId) { return request('/product/detail', { skuId, userId }); },

    // 交易
    createOrder(params) { return request('/trade/create_order', params); },
    settleOrder(orderNo, userId) { return request('/trade/settle_order', { orderNo, userId }); },
    cancelOrder(orderNo, userId) { return request('/trade/cancel_order', { orderNo, userId }); },

    // 用户
    getUserOrders(userId, page = 1, size = 10) { return request('/user/orders', { userId, page, size }); },
    getUserOrderDetail(orderNo, userId) { return request('/user/order/detail', { orderNo, userId }); },

    // 营销后台
    getActivityOverview() { return mktRequest('/marketing/activity/overview'); },
    getActivityDetail(activityId, page, size) { return mktRequest('/marketing/activity/detail', { activityId, page, size }); },
    getTeamMembers(groupTeamId) { return mktRequest('/marketing/team/members', { groupTeamId }); },
    getSalesOverview() { return mktRequest('/marketing/sales/overview'); },
    getProductRanking(limit) { return mktRequest('/marketing/sales/ranking', { limit }); },

    // 活动管理 (新)
    createActivity(params) { return mktRequest('/marketing/activity/create', params); },
    revokeActivity(activityId) { return mktRequest('/marketing/activity/revoke', { activityId }); },
    getPublishedActivities() { return mktRequest('/marketing/activity/published'); },
    updateActivity(params) { return mktRequest('/marketing/activity/update', params); },
    getDashboardOverview() { return mktRequest('/marketing/dashboard/overview'); },
    getSalesDetail(period) { return mktRequest('/marketing/dashboard/sales', { period }); }
  };
})();
