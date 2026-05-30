package cn.pumluda.domain.trade.model.entity;

import cn.pumluda.types.enums.ActivityParticipationTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Project: group-buy-market-better <p>
 * File: GroupRecordEntity <p>
 * Created by: 16374 <p>
 * Date: 2026/5/29 <p>
 * Time: 14:21 <p>
 * Description: 参与活动的交易订单记录 Entity：
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityOrderRecordEntity {

    /* 交易单号 */
    private String orderNo;
    /* 用户 ID */
    private Long userId;
    /* 活动 ID */
    private Long activityId;
    /* 活动名称 */
    private String activityName;

    /**
     * 活动业务实例 ID。不同活动类型对应不同业务实例：
     * 拼团活动：
     * 拼团队伍 ID（groupTeamId）
     * 秒杀活动：
     * 秒杀批次 ID / 排队 ID
     * 抽签活动：
     * 抽签轮次 ID
     * 砍价活动：
     * 砍价任务 ID
     */
    private Long activityBusinessId;

    /* 活动参与方式：1-开团，2-参团，3-排队，4-抽签.. */
    private ActivityParticipationTypeEnum participationType;
    /* 活动优惠名额占有状态：0-未占用，1-已占用 */
    private Integer quotaOccupied;

    /**
     * 活动参与记录状态。表示当前订单在活动参与链路中的处理状态。
     * 0 - 处理中
     * 1 - 处理成功
     * 2 - 处理失败
     * 3 - 已取消
     */
    private Integer recordStatus;

    /* 参与活动时间 */
    private Date joinTime;

    /**
     * 活动参与记录的过期时间（TTL）。
     * 用途：
     * - 对于长时间存在的活动，如果用户未在规定时间内完成订单处理（如结算），该记录将被视为过期并记录过期时间。
     * - 过期记录表示用户需要重新参与活动或重新占用资格。
     * - 如果重新参与活动或占用资格，则设过期时间为 null， recordStatus = 0, quotaOccupied = 1
     */
    private Date expireTime;
}
