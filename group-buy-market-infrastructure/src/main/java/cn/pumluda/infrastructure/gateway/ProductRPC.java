package cn.pumluda.infrastructure.gateway;

import cn.pumluda.infrastructure.dao.ISkuDao;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Project: group-buy-market-better <p>
 * File: ProductRPC <p>
 * Created by: 16374 <p>
 * Date: 2026/6/2 <p>
 * Time: 17:18 <p>
 * Description: 商品补货
 */
@Service
public class ProductRPC {

    @Resource
    private ISkuDao skuDao;

    public void restockProductBySkuId(long SkuId) {
        skuDao.restockSkuBySkuId(SkuId, 100);
    }
}
