package cn.pumluda.infrastructure.dao;

import cn.pumluda.infrastructure.dao.po.ActivityProductPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IActivityProductDao {

    void insert(ActivityProductPo po);

    void deleteByActivityId(@Param("activityId") Long activityId);

    List<ActivityProductPo> findByActivityId(@Param("activityId") Long activityId);

    List<ActivityProductPo> findBySkuId(@Param("skuId") Long skuId);

    /** 查某商品当前生效的活动（在有效期内且启用） */
    ActivityProductPo findActiveBySkuId(@Param("skuId") Long skuId);

    /** 冲突检测：查询与指定时间范围重叠且启用的活动关联 */
    List<ActivityProductPo> findConflicting(@Param("skuId") Long skuId,
                                             @Param("startTime") java.util.Date startTime,
                                             @Param("endTime") java.util.Date endTime);
}
