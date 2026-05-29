package cn.pumluda.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Project: group-buy-market-better <p>
 * File: ActivityConfigPo <p>
 * Created by: 16374 <p>
 * Date: 2026/5/28 <p>
 * Time: 08:49 <p>
 * Description: 活动配置 PO
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityConfigPo {

    /* 自增主键 */
    private Long id;

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
    private Integer limitTag;
    /* 活动状态：0-禁用，1-启用 */
    private Integer status;

    /* 活动开始时间 */
    private Date startTime;
    /* 活动截止时间 */
    private Date endTime;

    /* 记录创建时间 */
    private Date createTime;
    /* 记录更新时间 */
    private Date updateTime;

}
