package cn.pumluda.infrastructure.dao;

import cn.pumluda.infrastructure.dao.po.GroupTeamPo;
import org.apache.ibatis.annotations.Mapper;

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

}
