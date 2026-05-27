package cn.pumluda.domain.trade.service.createOrder;

import cn.pumluda.domain.trade.model.entity.ActivityConfigEntity;
import cn.pumluda.domain.trade.model.entity.OrderItemEntity;
import cn.pumluda.domain.trade.model.entity.SkuEntity;

import java.math.BigInteger;

/**
 * Project: group-buy-market-better <p>
 * File: CreateOrderService <p>
 * Created by: 16374 <p>
 * Date: 2026/5/27 <p>
 * Time: 16:29 <p>
 * Description: 创建订单服务实现类
 */
public class CreateOrderService implements ICreateOrderService{
    @Override
    public SkuEntity queryProductBySkuId(BigInteger skuId) {
        return null;
    }

    @Override
    public ActivityConfigEntity queryValidActivity(BigInteger ActivityId) {
        return null;
    }
}
