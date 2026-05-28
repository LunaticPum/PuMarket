package cn.pumluda.infrastructure.dao;

import cn.pumluda.infrastructure.dao.po.SkuPo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Project: group-buy-market-better <p>
 * File: ISkuDao <p>
 * Created by: 16374 <p>
 * Date: 2026/5/28 <p>
 * Time: 09:31 <p>
 * Description: 商品 SKU 数据 DAO
 */
@Mapper
public interface ISkuDao {

    SkuPo getSkuById(Long SkuId);

    List<SkuPo> getAllSku();

}
