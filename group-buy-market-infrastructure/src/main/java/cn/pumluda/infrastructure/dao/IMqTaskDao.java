package cn.pumluda.infrastructure.dao;

import cn.pumluda.infrastructure.dao.po.MqTaskPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * Project: group-buy-market-better <p>
 * File: IMQTask <p>
 * Created by: 16374 <p>
 * Date: 2026/6/2 <p>
 * Time: 09:51 <p>
 * Description: MQ 任务表 DAO
 */
@Mapper
public interface IMqTaskDao {

    /* 新建 MQ 任务 */
    void insert(MqTaskPo task);

    /* 查询待处理 MQ 任务 */
    List<MqTaskPo> queryPendingTasks(int limit);

    void markSuccess(Long id);

    void markFail(Long id);

    void updateRetry(@Param("id") Long id, @Param("retryTimes") int retryTimes, @Param("nextRetryTime") Date nextRetryTime);

    boolean exists(String bizId);
}
