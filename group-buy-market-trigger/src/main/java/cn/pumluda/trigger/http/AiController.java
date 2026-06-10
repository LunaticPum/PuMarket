package cn.pumluda.trigger.http;

import cn.pumluda.api.IAiController;
import cn.pumluda.api.dto.AiQueryReqDTO;
import cn.pumluda.api.dto.AiQueryResDTO;
import cn.pumluda.api.response.Response;
import cn.pumluda.domain.trade.model.entity.ActivityConfigEntity;
import cn.pumluda.domain.trade.model.entity.SkuEntity;
import cn.pumluda.domain.trade.service.activityManage.ActivityManageService;
import cn.pumluda.domain.trade.service.productQuery.ProductQueryService;
import cn.pumluda.domain.trade.service.userQuery.UserOrderItem;
import cn.pumluda.domain.trade.service.userQuery.UserOrderListResult;
import cn.pumluda.domain.trade.service.userQuery.UserOrderQueryService;
import cn.pumluda.types.enums.ResponseEnum;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

/**
 * AI 数据查询接口 —— 为 Agent 提供实时商城数据
 */
@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/ai")
public class AiController implements IAiController {

    @Resource
    private ProductQueryService productQueryService;
    @Resource
    private UserOrderQueryService userOrderQueryService;
    @Resource
    private ActivityManageService activityManageService;

    @PostMapping("query")
    @Override
    public Response<AiQueryResDTO> queryData(@RequestBody AiQueryReqDTO req) {
        try {
            Long userId = req.getUserId();

            // 1. 商品列表 (最多20条，简化数据量)
            List<SkuEntity> skus = productQueryService.getProductList(1, 20);
            List<Map<String, Object>> products = new ArrayList<>();
            for (SkuEntity sku : skus) {
                Map<String, Object> p = new LinkedHashMap<>();
                p.put("skuId", sku.getSkuId());
                p.put("name", sku.getProductName());
                p.put("price", sku.getPrice());
                p.put("stock", sku.getStock());
                p.put("status", sku.getStatus());
                // 查该商品绑定的活动
                ActivityConfigEntity act = activityManageService.getActiveActivityForSku(sku.getSkuId());
                if (act != null) {
                    Map<String, Object> ai = new LinkedHashMap<>();
                    ai.put("activityId", act.getActivityId());
                    ai.put("name", act.getActivityName());
                    ai.put("type", act.getActivityType());
                    ai.put("discountType", act.getDiscountType() != null ? act.getDiscountType().getCode() : 0);
                    ai.put("discountConfig", act.getDiscountConfig());
                    ai.put("requiredNum", act.getTotalDiscountQuota());
                    p.put("activity", ai);
                }
                products.add(p);
            }

            // 2. 用户订单
            List<Map<String, Object>> orders = new ArrayList<>();
            if (userId != null) {
                UserOrderListResult result = userOrderQueryService.getUserOrderList(userId, 1, 10);
                for (UserOrderItem item : result.getItems()) {
                    Map<String, Object> o = new LinkedHashMap<>();
                    o.put("orderNo", item.getOrderNo());
                    o.put("productName", item.getProductName());
                    o.put("price", item.getActualPrice());
                    o.put("quantity", item.getQuantity());
                    o.put("displayStatus", item.getDisplayStatus());
                    o.put("teamProgress", item.getTeamProgress());
                    o.put("time", item.getOrderCreateTime());
                    orders.add(o);
                }
            }

            // 3. 活动列表
            List<ActivityConfigEntity> activities = activityManageService.getPublishedActivities();
            List<Map<String, Object>> acts = new ArrayList<>();
            for (ActivityConfigEntity a : activities) {
                Map<String, Object> ai = new LinkedHashMap<>();
                ai.put("activityId", a.getActivityId());
                ai.put("name", a.getActivityName());
                ai.put("type", a.getActivityType());
                ai.put("discountType", a.getDiscountType() != null ? a.getDiscountType().getCode() : 0);
                ai.put("discountConfig", a.getDiscountConfig());
                ai.put("requiredNum", a.getTotalDiscountQuota());
                ai.put("startTime", a.getStartTime());
                ai.put("endTime", a.getEndTime());
                acts.add(ai);
            }

            return Response.<AiQueryResDTO>builder()
                    .code(ResponseEnum.SUCCESS.getCode()).info(ResponseEnum.SUCCESS.getInfo())
                    .data(AiQueryResDTO.builder()
                            .productsJson(JSON.toJSONString(products))
                            .ordersJson(JSON.toJSONString(orders))
                            .activitiesJson(JSON.toJSONString(acts))
                            .build())
                    .build();

        } catch (Exception e) {
            log.error("[AI查询] 异常", e);
            return Response.<AiQueryResDTO>builder()
                    .code(ResponseEnum.UN_ERROR.getCode()).info(ResponseEnum.UN_ERROR.getInfo()).build();
        }
    }
}
