package cn.pumluda.trigger.http;

import cn.pumluda.api.IDCCController;
import cn.pumluda.api.response.Response;
import cn.pumluda.types.common.SplitConstants;
import cn.pumluda.types.enums.ResponseEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RTopic;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * Project: group-buy-market-pumluda <p>
 * File: DCCController <p>
 * Created by: 16374 <p>
 * Date: 2026/5/15 <p>
 * Time: 19:24 <p>
 * Description: 前端配置切换请求接口
 */
@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/dcc/")
public class DCCController implements IDCCController {

    @Resource
    private RTopic dccTopic;

    @GetMapping("update_config")
    @Override
    public Response<Boolean> updateConfig(@RequestParam(name = "key") String key,
                                          @RequestParam(name = "value") String value) {
        try {
            log.info("[执行配置切换] 执行配置: {}, 切换值: {}", key, value);
            dccTopic.publish(StringUtils.join(key, SplitConstants.SPLIT, value));

            return Response.<Boolean>builder().code(ResponseEnum.SUCCESS.getCode()).info(
                    ResponseEnum.SUCCESS.getInfo()).build();
        } catch (Exception e) {
            log.error("[执行配置切换] 执行配置: {}, 切换值: {} 配置变更失败: ", key, value, e);
            return Response.<Boolean>builder().code(ResponseEnum.UN_ERROR.getCode()).info(
                    ResponseEnum.UN_ERROR.getInfo()).build();
        }
    }
}
