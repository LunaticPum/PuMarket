package cn.pumluda.trigger.job;

import cn.pumluda.infrastructure.dao.IMqTaskDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Project: group-buy-market-better <p>
 * File: StockSupplyDispatcher <p>
 * Created by: 16374 <p>
 * Date: 2026/6/2 <p>
 * Time: 11:58 <p>
 * Description: 商品库存补充通知
 */
@Component
@Slf4j
public class StockSupplyDispatcher {

    @Resource
    private IMqTaskDao mqTaskDao;


}
