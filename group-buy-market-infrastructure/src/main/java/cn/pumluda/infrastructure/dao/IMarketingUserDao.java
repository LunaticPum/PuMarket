package cn.pumluda.infrastructure.dao;

import cn.pumluda.infrastructure.dao.po.MarketingUserPo;
import org.apache.ibatis.annotations.Mapper;

/**
 * Project: group-buy-market-better <p>
 * File: IMarketingUserDao <p>
 * Created by: 16374 <p>
 * Date: 2026/6/6 <p>
 * Description: 营销后台用户 DAO
 */
@Mapper
public interface IMarketingUserDao {

    MarketingUserPo findByUsername(String username);

}
