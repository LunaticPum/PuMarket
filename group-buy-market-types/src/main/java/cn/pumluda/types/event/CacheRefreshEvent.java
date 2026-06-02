package cn.pumluda.types.event;

import cn.pumluda.types.enums.CacheType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Project: group-buy-market-better <p>
 * File: CacheRefreshEvent <p>
 * Created by: 16374 <p>
 * Date: 2026/6/2 <p>
 * Time: 08:23 <p>
 * Description: 写缓存事件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CacheRefreshEvent {

    /* 业务类型 */
    private CacheType cacheType;

    private String shardKey;

    /* 用户 ID */
    private Long userId;

    /* 活动 ID */
    private Long activityId;

    /* 拼团队伍 ID */
    private Long groupTeamId;

    /* 商品 ID */
    private Long skuId;

}
