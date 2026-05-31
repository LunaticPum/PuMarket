package cn.pumluda.infrastructure.dao.po;

import cn.pumluda.domain.trade.model.valobj.GroupTeamStatusEnumVo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Project: group-buy-market-better <p>
 * File: GroupTeamPo <p>
 * Created by: 16374 <p>
 * Date: 2026/5/30 <p>
 * Time: 15:26 <p>
 * Description: 拼团队伍 PO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupTeamPo {

    /* 自增主键 */
    private Long id;

    /* 活动 ID */
    private Long activityId;
    /* 拼团队伍 ID */
    private Long groupTeamId;
    /* 团长用户 ID */
    private Long leaderUserId;
    /* 商品 ID */
    private Long skuId;

    /* 成团所需人数 */
    private int requiredNum;
    /* 当前参团人数 */
    private int currentNum;
    /* 团内已结算订单量 */
    private int settledTradeNum;
    /* 当前拼团队伍状态 */
    private int teamStatus;

    /* 开团时间 */
    private Date teamCreateTime;
    /* 队伍有效截止时间 */
    private Date teamExpireTime;
    /* 队伍结束时间*/
    private Date teamEndTime;

    /* 记录创建时间 */
    private Date createTime;
    /* 记录更新时间 */
    private Date updateTime;

}
