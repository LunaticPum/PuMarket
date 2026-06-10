package cn.pumluda.infrastructure.cache;

import java.util.concurrent.TimeUnit;

/**
 * Project: group-buy-market-better <p>
 * File: ICacheService <p>
 * Created by: 16374 <p>
 * Date: 2026/5/28 <p>
 * Time: 14:12 <p>
 * Description: 缓存读写（JSON）接口
 */
public interface ICacheService {

    /**
     * 从缓存中读取 JSON 并反序列化为指定类型对象
     *
     * @param key   业务缓存键
     * @param clazz 业务数据对象
     * @param <T>   业务对象类型
     * @return 缓存对象，未命中或为空标记则返回 null
     */
    <T> T get(String key, Class<T> clazz);

    /**
     * 将对象序列化为 JSON 并写入缓存
     *
     * @param key   业务缓存键
     * @param value 缓存数据（不能为空，如果为空必须显示存 'NULL'）
     * @param ttl   过期时间
     * @param unit  时间单位
     * @param <T>   缓存数据类型
     */
    <T> void set(String key, T value, long ttl, TimeUnit unit);

    /**
     * 删除缓存
     *
     * @param key 业务缓存键
     */
    void delete(String key);

}
