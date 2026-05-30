package cn.pumluda.infrastructure.dao.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;

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
    /* 活动类型：0-默认活动，1-拼团，2-凑单 */
    private Integer activityType;

    /* 优惠类型：1-直减，2-直降，3-折扣，4-满减 */
    private Integer discountType;
    /* 优惠配置(JSON) */
    @TableField(
            value = "discount_config",
            typeHandler = JacksonTypeHandler.class
    )
    private Map<String, Object> discountConfig;
    /* 总优惠名额数量（拼团时表示成团人数） */
    private Integer totalDiscountQuota;

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
