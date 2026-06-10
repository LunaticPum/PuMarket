// API 根地址配置
// Docker 部署时 nginx 统一代理 /api/agent/ → ai-agent:8091，前端用相对路径即可
const CONFIG = {
    API_BASE_URL: '/api/agent'
    // 本地开发直连: 'http://localhost:8091/api/agent'
};