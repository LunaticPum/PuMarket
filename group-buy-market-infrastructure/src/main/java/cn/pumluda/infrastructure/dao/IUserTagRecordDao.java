package cn.pumluda.infrastructure.dao;

import cn.pumluda.infrastructure.dao.po.UserTagRecordPo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Project: group-buy-market-better <p>
 * File: IUserTagRecordDao <p>
 * Created by: 16374 <p>
 * Date: 2026/5/30 <p>
 * Time: 16:31 <p>
 * Description: 用户标签记录 DAO
 */
@Mapper
public interface IUserTagRecordDao {

    UserTagRecordPo getUserTagRecordById(Long userId);

    List<UserTagRecordPo> getUserTagRecordsByTag(int userTag);

    List<UserTagRecordPo> getAllUserTagRecord();

}
