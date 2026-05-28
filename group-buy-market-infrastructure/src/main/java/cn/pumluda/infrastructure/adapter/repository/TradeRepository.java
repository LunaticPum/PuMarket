package cn.pumluda.infrastructure.adapter.repository;

import cn.pumluda.domain.trade.adapter.repository.ITradeRepository;
import cn.pumluda.domain.trade.model.entity.ActivityConfigEntity;
import cn.pumluda.domain.trade.model.entity.SkuEntity;
import cn.pumluda.infrastructure.dao.IActivityConfigDao;
import cn.pumluda.infrastructure.dao.ISkuDao;
import cn.pumluda.infrastructure.dao.po.ActivityConfigPo;
import cn.pumluda.infrastructure.dao.po.SkuPo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * Project: group-buy-market-better <p>
 * File: TradeRepository <p>
 * Created by: 16374 <p>
 * Date: 2026/5/28 <p>
 * Time: 09:23 <p>
 * Description: 交易业务仓储接口实现
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class TradeRepository implements ITradeRepository {

    private final IActivityConfigDao activityConfigDao;
    private final ISkuDao skuDao;

    @Override
    public SkuEntity getSkuById(Long SkuId) {
        SkuPo skuPo = skuDao.getSkuById(SkuId);
        return SkuEntity.builder()
                        .skuId(skuPo.getSkuId())
                        .productName(skuPo.getProductName())
                        .price(skuPo.getPrice())
                        .stock(skuPo.getStock())
                        .status(skuPo.getStatus())
                        .build();
    }

    @Override
    public ActivityConfigEntity getActivityByActivityId(Long activityId) {
        ActivityConfigPo activityConfigPo = activityConfigDao.getActivityByActivityId(activityId);
        return ActivityConfigEntity.builder()
                                   .activityId(activityConfigPo.getActivityId())
                                   .activityName(activityConfigPo.getActivityName())
                                   .activityType(activityConfigPo.getActivityType())
                                   .discountExpr(activityConfigPo.getDiscountExpr())
                                   .totalDiscountQuota(activityConfigPo.getTotalDiscountQuota())
                                   .usedDiscountQuota(activityConfigPo.getUsedDiscountQuota())
                                   .limitTags(activityConfigPo.getLimitTags())
                                   .status(activityConfigPo.getStatus())
                                   .startTime(activityConfigPo.getStartTime())
                                   .endTime(activityConfigPo.getEndTime())
                                   .build();
    }
}
