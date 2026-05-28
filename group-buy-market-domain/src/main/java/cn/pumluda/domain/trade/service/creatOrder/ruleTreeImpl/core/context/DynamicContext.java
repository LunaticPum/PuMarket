package cn.pumluda.domain.trade.service.creatOrder.ruleTreeImpl.core.context;

import cn.pumluda.domain.trade.model.entity.GroupTeamEntity;
import cn.pumluda.domain.trade.model.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Project: group-buy-market-better <p>
 * File: DynamicContext <p>
 * Created by: 16374 <p>
 * Date: 2026/5/28 <p>
 * Time: 17:50 <p>
 * Description: 动态上下文
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DynamicContext {

    /* 用户 ID + 标签 */
    private UserEntity user;
    /* 限制目标人群标签 */
    private Integer limitTags;

    /* 优惠金额：优惠表达式(商品单价) * 下单数量 */
    private BigDecimal discountPrice;
    /* 原始金额：商品单价 * 下单数量 */
    private BigDecimal originalPrice;

    /* 拼团队伍 */
    private GroupTeamEntity groupTeam;

}
