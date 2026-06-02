package cn.pumluda.infrastructure.cache;

/**
 * Project: group-buy-market-better <p>
 * File: ICacheReloadService <p>
 * Created by: 16374 <p>
 * Date: 2026/5/30 <p>
 * Time: 16:44 <p>
 * Description: 缓存重加载
 */
public interface ICacheManager {

    /**
     * 全量重建缓存。
     * <p>
     * 主要用于：
     * - 应用重启后的缓存恢复
     * - 运维场景下的手动重建
     * <p>
     * 不建议在业务链路中频繁调用。
     */
    void reloadConfigCache();

    /**
     * 全量重建活动配置缓存。
     * <p>
     * 适用于读多写少的配置型数据，可用于缓存预热
     */
    void reloadActivityCache();

    /**
     * 全量重建商品 SKU 缓存。
     * <p>
     * 适用于读多写少的配置型数据，可用于缓存预热
     */
    void reloadSkuCache();

    /**
     * 全量重建用户标签缓存。
     * <p>
     * 适用于读多写少的配置型数据，可用于缓存预热
     */
    void reloadUserTagCache();

    /**
     * 刷新指定拼团队伍缓存。
     * <p>
     * 当队伍状态发生变化时调用。
     */
    void refreshGroupTeamCache(Long activityId, Long groupTeamId);

    /**
     * 刷新指定活动参与记录缓存。
     * <p>
     * 当订单参与状态发生变化时调用。
     */
    void refreshActivityOrderRecordCache(Long userId, Long activityId);

    void refreshActivityCache(Long activityId);

    void refreshSkuCache(Long skuId);

    /**
     * 删除指定拼团队伍缓存。
     * <p>
     * 采用 Cache Aside 策略时，
     * 可在数据更新后删除缓存，
     * 由后续查询自动回填。
     */
    void evictGroupTeam(String cacheKey);

    /**
     * 删除指定活动参与记录缓存。
     * <p>
     * 采用 Cache Aside 策略时，
     * 可在数据更新后删除缓存，
     * 由后续查询自动回填。
     */
    void evictActivityOrderRecord(String cacheKey);

}
