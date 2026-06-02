package cn.pumluda.trigger.job;

import cn.pumluda.infrastructure.dao.IActivityConfigDao;
import cn.pumluda.infrastructure.dao.IGroupTeamDao;
import cn.pumluda.infrastructure.dao.IMqTaskDao;
import cn.pumluda.infrastructure.dao.ITradeOrderDao;
import cn.pumluda.infrastructure.dao.po.MqTaskPo;
import cn.pumluda.infrastructure.dao.po.TradeOrderPo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * Project: group-buy-market-better <p>
 * File: TimeoutTaskDispatcher <p>
 * Created by: 16374 <p>
 * Date: 2026/6/2 <p>
 * Time: 11:42 <p>
 * Description: 超时记录处理
 */
@Component
@Slf4j
public class TimeoutTaskDispatcher {

    @Resource
    private ITradeOrderDao orderDao;

    @Resource
    private IGroupTeamDao groupTeamDao;

    @Resource
    private IActivityConfigDao activityDao;

    @Resource
    private IMqTaskDao mqTaskDao;

    @Scheduled(fixedDelay = 3000)
    public void scan() {

        // 1. 超时订单
        List<String> timeoutOrders = orderDao.queryTimeoutOrders();

        for (String orderNo : timeoutOrders) {
            createTask("ORDER_TIMEOUT_CLOSE", orderNo, null);
        }

        // 2. 超时拼团
        List<Long> timeoutGroups = groupTeamDao.queryTimeoutGroups();

        for (Long groupTeamId : timeoutGroups) {
            createTask("GROUP_TIMEOUT_CLOSE", groupTeamId, null);
        }

        // 3. 超时活动
        List<Long> timeoutActivities = activityDao.queryTimeoutActivities();

        for (Long activityId : timeoutActivities) {
            createTask("ACTIVITY_TIMEOUT_CLOSE", activityId, null);
        }
    }

    private void createTask(String eventType, Object bizId, String extra) {

        MqTaskPo task = MqTaskPo.builder()
                                .bizId(eventType + ":" + bizId)
                                .bizType("TIMEOUT")
                                .eventType(eventType)
                                .topic("biz-timeout-topic")
                                .shardKey(bizId instanceof String ? (String) bizId : String.valueOf(bizId))
                                .payload("{\"id\":" + bizId + "}")
                                .status(0)
                                .retryTimes(0)
                                .maxRetryTimes(3)
                                .build();

        mqTaskDao.insert(task);
    }

}
