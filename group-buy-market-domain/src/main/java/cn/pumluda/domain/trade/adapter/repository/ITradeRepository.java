package cn.pumluda.domain.trade.adapter.repository;

import cn.pumluda.domain.trade.model.entity.ActivityConfigEntity;
import cn.pumluda.domain.trade.model.entity.SkuEntity;

/**
 * Project: group-buy-market-better <p>
 * File: ITradeRepository <p>
 * Created by: 16374 <p>
 * Date: 2026/5/28 <p>
 * Time: 09:17 <p>
 * Description: 交易业务仓储适配接口
 */
public interface ITradeRepository {

    SkuEntity getSkuById(Long SkuId);

    ActivityConfigEntity getActivityByActivityId(Long activityId);

}
