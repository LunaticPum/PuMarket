package cn.pumluda.domain.trade.model.entity;

import cn.pumluda.domain.trade.model.valobj.GroupTeamStatusEnumVo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Project: group-buy-market-better <p>
 * File: GroupTeamEntity <p>
 * Created by: 16374 <p>
 * Date: 2026/5/28 <p>
 * Time: 18:17 <p>
 * Description: 拼团队伍 Entity
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupTeamEntity {

    /* 活动 ID */
    private Long activityId;
    /* 拼团队伍 ID */
    private Long groupTeamId;
    /* 团长用户 ID */
    private Long leaderUserId;

    /* 成团所需人数：成团条件人数 - 团内已结算订单量 */
    private Integer requiredNum;
    /* 当前参团人数：优惠（参团）名额占用量 */
    private Integer currentNum;
    /* 团内已结算订单量 */
    private Integer settledTradeNum;

    /* 当前拼团队伍状态：0-进行中，1-已成团，2-已过期，3-已取消 */
    private GroupTeamStatusEnumVo teamStatus;
    /* 开团时间 */
    private Date teamCreateTime;
    /* 队伍有效截止时间 */
    private Date teamExpireTime;

}
