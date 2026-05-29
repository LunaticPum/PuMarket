package cn.pumluda.domain.trade.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Project: group-buy-market-better <p>
 * File: ActivityConfigEntity <p>
 * Created by: 16374 <p>
 * Date: 2026/5/27 <p>
 * Time: 10:11 <p>
 * Description: 活动配置实体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityConfigEntity {

    /* 活动 ID */
    private Long activityId;
    /* 活动名称 */
    private String activityName;
    /* 活动类型 */
    private Integer activityType;

    /* 优惠表达式 */
    private String discountExpr;
    /* 总优惠名额数量 */
    private Integer totalDiscountQuota;
    /* 已占用的优惠名额数量 */
    private Integer usedDiscountQuota;

    /* 限流人群标签 */
    private Integer limitTag = 0;
    /* 活动状态：0-禁用，1-启用 */
    private Integer status;

    /* 活动开始时间 */
    private Date startTime;
    /* 活动截止时间 */
    private Date endTime;

}
