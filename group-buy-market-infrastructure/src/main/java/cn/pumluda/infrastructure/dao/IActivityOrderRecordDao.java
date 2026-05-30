package cn.pumluda.infrastructure.dao;

import cn.pumluda.infrastructure.dao.po.ActivityOrderRecordPo;
import org.apache.ibatis.annotations.Mapper;

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

    ActivityOrderRecordPo getActivityOrderRecord(String orderNo, Long activityId);

    void addActivityOrderRecord(ActivityOrderRecordPo activityOrderRecord);

}
