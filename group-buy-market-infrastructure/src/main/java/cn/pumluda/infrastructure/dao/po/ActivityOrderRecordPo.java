package cn.pumluda.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Project: group-buy-market-better <p>
 * File: ActivityOrderRecordPo <p>
 * Created by: 16374 <p>
 * Date: 2026/5/30 <p>
 * Time: 15:26 <p>
 * Description: 参与活动订单记录 PO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityOrderRecordPo {

    /* 自增主键 */
    private Long id;

    /* 交易单号 */
    private String orderNo;
    /* 用户 ID */
    private Long userId;

    /* 活动 ID */
    private Long activityId;
    /* 活动名称 */
    private String activityName;
    /* 活动业务 ID */
    private Long activityBusinessId;

    /* 活动参与方式 */
    private Integer participationType;
    /* 优惠名额占有状态 */
    private Integer quotaOccupied;
    /* 活动参与记录状态。 */
    private Integer recordStatus;

    /* 参与活动时间 */
    private Date joinTime;
    /* 活动参与记录的过期时间（TTL） */
    private Date expireTime;

    /* 记录创建时间 */
    private Date createTime;
    /* 记录更新时间 */
    private Date updateTime;

}
