package cn.pumluda.domain.trade.model.aggregate;

import cn.pumluda.domain.trade.model.entity.ActivityConfigEntity;
import cn.pumluda.domain.trade.model.entity.GroupTeamEntity;
import cn.pumluda.domain.trade.model.entity.SkuEntity;
import cn.pumluda.domain.trade.model.valobj.TradeSCVo;
import cn.pumluda.domain.trade.model.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Project: group-buy-market-better <p>
 * File: ServiceAggregate <p>
 * Created by: 16374 <p>
 * Date: 2026/5/28 <p>
 * Time: 17:59 <p>
 * Description: 业务聚合类 —— 用于领域服务实现 (createOrder)
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BusinessAggregate {

    /* 用户 ID */
    private Long userId;

    /* 商品 SKU */
    private SkuEntity sku;

    /* 活动配置 */
    private ActivityConfigEntity activityConfig;

    /* 流量来源和支付渠道 */
    private TradeSCVo tradeSC;
}
