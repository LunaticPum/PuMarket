package cn.pumluda.domain.trade.service.createOrder;

import cn.pumluda.domain.trade.model.entity.ActivityConfigEntity;
import cn.pumluda.domain.trade.model.entity.OrderItemEntity;
import cn.pumluda.domain.trade.model.entity.SkuEntity;
import cn.pumluda.domain.trade.service.createOrder.dto.CreateOrderPreCheckResult;
import cn.pumluda.types.enums.ResponseEnum;

import java.math.BigInteger;

/**
 * Project: group-buy-market-better <p>
 * File: ICreateOrderService <p>
 * Created by: 16374 <p>
 * Date: 2026/5/27 <p>
 * Time: 09:39 <p>
 * Description: 创建订单服务
 */
public interface ICreateOrderService {

    ResponseEnum preCheck(String userId, BigInteger activityId, BigInteger skuId);

    /**
     * 查询商品数据
     * @param skuId 商品库存编码
     * @return 商品数据
     */
    SkuEntity queryProductBySkuId(BigInteger skuId);


    /**
     * 查询仍在有效期内的活动配置
     * @param ActivityId 活动 ID
     * @return 如果活动不在有效期或没在启动，则为 null
     */
    ActivityConfigEntity queryValidActivity(BigInteger ActivityId);

}
