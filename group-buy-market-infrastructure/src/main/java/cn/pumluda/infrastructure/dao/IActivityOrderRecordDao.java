package cn.pumluda.infrastructure.dao;

import cn.pumluda.infrastructure.dao.po.ActivityConfigPo;
import cn.pumluda.infrastructure.dao.po.ActivityOrderRecordPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Project: group-buy-market-better <p>
 * File: IActivityOrderRecordDao <p>
 * Created by: 16374 <p>
 * Date: 2026/5/30 <p>
 * Time: 16:29 <p>
 * Description: 参与活动订单记录 DAO
 */
@Mapper
public interface IActivityOrderRecordDao {

    ActivityOrderRecordPo getActivityOrderRecord(@Param("userId") Long userId, @Param("activityId") Long activityId);
    void addActivityOrderRecord(ActivityOrderRecordPo activityOrderRecord);

    List<ActivityOrderRecordPo> queryTimeoutRecord();

    void closeActivityOrder(@Param("userId") Long userId, @Param("activityId") Long activityId);
}
