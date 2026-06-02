package cn.pumluda.trigger.job;

import cn.pumluda.infrastructure.dao.IMqTaskDao;
import cn.pumluda.infrastructure.dao.po.MqTaskPo;
import cn.pumluda.infrastructure.mq.kafka.producer.CacheRefreshProducer;
import cn.pumluda.types.event.CacheRefreshEvent;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * Project: group-buy-market-better <p>
 * File: MqTaskDispatcher <p>
 * Created by: 16374 <p>
 * Date: 2026/6/2 <p>
 * Time: 10:14 <p>
 * Description: Mq 任务分配
 */
@Component
@Slf4j
public class MqTaskDispatcher {

    @Resource
    private IMqTaskDao mqTaskDao;
    @Resource
    private CacheRefreshProducer producer;

    @Scheduled(fixedDelay = 1000)
    public void dispatch() {
        List<MqTaskPo> tasks = mqTaskDao.queryPendingTasks(100);

        for (MqTaskPo task : tasks) {
            try {
                CacheRefreshEvent event = JSON.parseObject(task.getPayload(), CacheRefreshEvent.class);
                producer.send(event);
                mqTaskDao.markSuccess(task.getId());
            } catch (Exception e) {
                int newRetry = task.getRetryTimes() + 1;

                if (newRetry >= task.getMaxRetryTimes()) {
                    mqTaskDao.markFail(task.getId());
                } else {
                    mqTaskDao.updateRetry(task.getId(), newRetry, new Date(System.currentTimeMillis() + 3000));
                }

                log.error("MQ 任务发送失败 id={}", task.getId(), e);
            }
        }
    }
}
