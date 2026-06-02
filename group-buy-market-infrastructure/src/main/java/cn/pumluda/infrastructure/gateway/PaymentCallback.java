package cn.pumluda.infrastructure.gateway;

import cn.pumluda.infrastructure.dao.*;
import cn.pumluda.infrastructure.dao.po.GroupTeamPo;
import cn.pumluda.infrastructure.dao.po.TradeOrderItemPo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Project: group-buy-market-better <p>
 * File: PaymentCallback <p>
 * Created by: 16374 <p>
 * Date: 2026/6/2 <p>
 * Time: 17:26 <p>
 * Description: 支付回调
 */
@Service
public class PaymentCallback {

    @Resource
    private ITradeOrderDao orderDao;
    @Resource
    private ITradeOrderItemDao orderItemDao;
    @Resource
    private IGroupTeamDao groupTeamDao;
    @Resource
    private IActivityOrderRecordDao activityOrderRecordDao;
    @Resource
    private ISkuDao skuDao;

    public void settleTrade(String orderNo, Long userId) {
        orderDao.settleTradeOrder(orderNo, userId);
        TradeOrderItemPo orderItem = orderItemDao.getOrderItem(orderNo);

        Long skuId = orderItem.getSkuId();
        int quantity = orderItem.getQuantity();
        Long groupTeamId = orderItem.getActivityBusinessId();
        Long activityId = orderItem.getActivityId();

        skuDao.consumeLockedStockBySkuId(skuId, quantity);
        groupTeamDao.addSettledNum(activityId, groupTeamId);
        activityOrderRecordDao.settleActivityOrder(userId, activityId);
    }

    public void cancelTrade(String orderNo, Long userId) {
        orderDao.cancelTradeOrder(orderNo, userId);
        TradeOrderItemPo orderItem = orderItemDao.getOrderItem(orderNo);

        Long skuId = orderItem.getSkuId();
        int quantity = orderItem.getQuantity();
        Long groupTeamId = orderItem.getActivityBusinessId();
        Long activityId = orderItem.getActivityId();

        skuDao.repairStockBySkuId(skuId, quantity);
        groupTeamDao.repairQuota(activityId, groupTeamId);

        GroupTeamPo groupTeam = groupTeamDao.getGroupTeam(activityId, groupTeamId);
        if (groupTeam.getLeaderUserId().equals(userId)) {
            groupTeamDao.closeGroupTeam(activityId, groupTeamId);
        }
        activityOrderRecordDao.closeActivityOrder(userId, activityId);
    }

}
