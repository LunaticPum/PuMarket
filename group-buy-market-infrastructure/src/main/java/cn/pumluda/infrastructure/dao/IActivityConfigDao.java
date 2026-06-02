package cn.pumluda.infrastructure.dao;

import cn.pumluda.infrastructure.dao.po.ActivityConfigPo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Project: group-buy-market-better <p>
 * File: IActivityConfigDao <p>
 * Created by: 16374 <p>
 * Date: 2026/5/28 <p>
 * Time: 09:30 <p>
 * Description: 活动配置数据 DAO
 */
@Mapper
public interface IActivityConfigDao {

    ActivityConfigPo getActivityByActivityId(Long activityId);

    List<ActivityConfigPo> getAllActivity();

    List<Long> queryTimeoutActivities();
}
