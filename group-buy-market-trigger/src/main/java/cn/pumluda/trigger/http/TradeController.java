package cn.pumluda.trigger.http;

import cn.pumluda.api.ITradeController;
import cn.pumluda.api.dto.CreateOrderReqDTO;
import cn.pumluda.api.dto.CreateOrderResDTO;
import cn.pumluda.api.response.Response;
import cn.pumluda.rateLimiter.annotations.AccessRateLimit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("create_order")
    @Override
    public Response<CreateOrderResDTO> createOrder(@RequestBody CreateOrderReqDTO requestDTO) {

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
