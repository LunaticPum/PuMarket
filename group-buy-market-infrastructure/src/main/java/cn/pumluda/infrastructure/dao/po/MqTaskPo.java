package cn.pumluda.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Project: group-buy-market-better <p>
 * File: MqTaskPo <p>
 * Created by: 16374 <p>
 * Date: 2026/6/2 <p>
 * Time: 09:52 <p>
 * Description: MQ 任务
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MqTaskPo {

    private Long id;

    /* 业务幂等 ID：防重复投递 */
    private String bizId;

    /* 业务类型 */
    private String bizType;

    /* 事件类型 */
    private String eventType;

    /* 消息体 */
    private String payload;

    /* 任务状态 */
    private Integer status;

    /* 重试次数 */
    private Integer retryTimes;

    /* 最大重试次数 */
    private Integer maxRetryTimes;

    /* 下次重试时间 */
    private Date nextRetryTime;

    /* 分区 Key */
    private String shardKey;

    /* 目标 MQ */
    private String topic;

    private Date createTime;

    private Date updateTime;

}
