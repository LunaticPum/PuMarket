package cn.pumluda.domain.trade.service.preCheck;

import cn.pumluda.domain.trade.model.aggregate.OrderAggregate;
import cn.pumluda.domain.trade.model.entity.ActivityConfigEntity;
import cn.pumluda.domain.trade.model.entity.SkuEntity;

/**
 * Project: group-buy-market-better <p>
 * File: IPreCheck <p>
 * Created by: 16374 <p>
 * Date: 2026/5/28 <p>
 * Time: 16:42 <p>
 * Description: 前置校验抽象
 */
public interface IPreCheckService {

    Object preCheck(Long userId, String orderNo, Long activityId, Long skuId, int quantity);

    /**
     * 查询商品数据
     *
     * @param skuId 商品库存编码
     * @return 商品数据
     */
    SkuEntity findValidSkuBySkuId(Long skuId);

    /**
     * 查找重复订单
     * @param orderNo 交易单号
     * @return 交易订单主表记录
     */
    OrderAggregate finalDuplicatedOrder(String orderNo);

    /**
     * 查询仍在有效期内的活动配置
     *
     * @param activityId 活动 ID
     * @return 如果活动不在有效期或没在启动，则为 null
     */
    ActivityConfigEntity findValidActivityByActivityId(Long activityId);

}
