package cn.pumluda.infrastructure.dao;

import cn.pumluda.infrastructure.dao.po.SkuPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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

    SkuPo getSkuById(Long skuId);

    List<SkuPo> getAllSku();

    List<SkuPo> getSkusByPage(@Param("offset") int offset, @Param("limit") int limit);

    int countSkus();

    void reserveSkuStock(@Param("skuId") Long skuId, @Param("quantity") int quantity);

    void updateSkuStatus(@Param("skuId") Long skuId, @Param("status") int status);

    void consumeLockedStockBySkuId(@Param("skuId") Long skuId, @Param("quantity") int quantity);

    void restockSkuBySkuId(@Param("skuId") Long skuId, @Param("quantity") int quantity);

    void repairStockBySkuId(@Param("skuId") Long skuId, @Param("quantity") int quantity);
}
