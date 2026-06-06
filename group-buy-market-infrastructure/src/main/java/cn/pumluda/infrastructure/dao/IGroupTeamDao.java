package cn.pumluda.infrastructure.dao;

import cn.pumluda.infrastructure.dao.po.GroupTeamPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Project: group-buy-market-better <p>
 * File: IActivityConfigDao <p>
 * Created by: 16374 <p>
 * Date: 2026/5/28 <p>
 * Time: 09:30 <p>
 * Description: 拼团队伍 DAO
 */
@Mapper
public interface IGroupTeamDao {

    void addGroupTeam(GroupTeamPo groupTeam);

    void joinGroupTeam(@Param("activityId") Long activityId, @Param("groupTeamId") Long groupTeamId);

    GroupTeamPo getGroupTeam(@Param("activityId") Long activityId, @Param("groupTeamId") Long groupTeamId);

    List<GroupTeamPo> getAllGroupTeam();

    List<GroupTeamPo> queryTimeoutGroups();

    void closeGroupTeam(@Param("activityId") Long activityId, @Param("groupTeamId") Long groupTeamId);

    void repairQuota(@Param("activityId") Long activityId, @Param("groupTeamId") Long groupTeamId);

    void completeGroupTeam(@Param("activityId") Long activityId, @Param("groupTeamId") Long groupTeamId);

    void addSettledNum(@Param("activityId") Long activityId, @Param("groupTeamId") Long groupTeamId);

    List<GroupTeamPo> getActiveTeamsBySkuId(Long skuId);

    int countActiveTeamsBySkuId(Long skuId);

    List<GroupTeamPo> getTeamsByActivityId(@Param("activityId") Long activityId,
                                            @Param("offset") int offset,
                                            @Param("limit") int limit);

    int countTeamsByActivityId(@Param("activityId") Long activityId);
}
