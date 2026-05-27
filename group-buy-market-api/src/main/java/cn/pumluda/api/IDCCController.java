package cn.pumluda.api;

import cn.pumluda.api.response.Response;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description DCC 动态配置中心
 * @create 2025-01-03 19:16
 */
public interface IDCCController {

    Response<Boolean> updateConfig(String key, String value);

}
