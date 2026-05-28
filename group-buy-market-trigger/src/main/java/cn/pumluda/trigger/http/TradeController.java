package cn.pumluda.trigger.http;

import cn.pumluda.api.ITradeController;
import cn.pumluda.api.dto.CreateOrderReqDTO;
import cn.pumluda.api.dto.CreateOrderResDTO;
import cn.pumluda.api.response.Response;
import cn.pumluda.domain.trade.service.createOrder.ICreateOrderService;
import cn.pumluda.domain.trade.service.createOrder.IPreCheckService;
import cn.pumluda.rateLimiter.annotations.AccessRateLimit;
import cn.pumluda.types.enums.ResponseEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * Project: group-buy-market-better <p>
 * File: TradeController <p>
 * Created by: 16374 <p>
 * Date: 2026/5/26 <p>
 * Time: 17:55 <p>
 * Description: 交易业务 Controller
 */
@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/trade")
public class TradeController implements ITradeController {

    @Resource
    private IPreCheckService preCheckService;


    @PostMapping("create_order")
    @Override
    public Response<CreateOrderResDTO> createOrder(@RequestBody CreateOrderReqDTO requestDTO) {
        String userId = requestDTO.getUserId();
        Long activityId = requestDTO.getActivityId();
        Long skuId = requestDTO.getSkuId();

        ResponseEnum failedCheckResponse = preCheckService.preCheck(userId, activityId, skuId);

        if (failedCheckResponse != null) {
            return Response.<CreateOrderResDTO>builder().code(failedCheckResponse.getCode()).info(
                    failedCheckResponse.getInfo()).build();
        }

        // todo 后续下单逻辑。。。

        Long groupTeamId = requestDTO.getGroupTeamId();
        int entrySource = requestDTO.getEntrySource();
        String payChannel = requestDTO.getChannel();

        // 主动删除业务幂等号，避免阻塞下一个订单创建
        // idempotencyChecker.release(RedisKeyConstants.CREATE_ORDER, userId);

        return null;
    }

    @AccessRateLimit(limitKey = "userId", qps = 1, fallback = "testFallback", blockThreshold = 1)
    @GetMapping("test")
    public String testLimit(@RequestParam(name = "userId") String userId) {
        return "test";
    }


    public String testFallback(String info) {
        return "limit";
    }
}
