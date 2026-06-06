package cn.pumluda.domain.trade.service.productQuery;

import cn.pumluda.domain.trade.adapter.repository.ITradeRepository;
import cn.pumluda.domain.trade.model.entity.ActivityConfigEntity;
import cn.pumluda.domain.trade.model.entity.GroupTeamEntity;
import cn.pumluda.domain.trade.model.entity.SkuEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Project: group-buy-market-better <p>
 * File: ProductQueryService <p>
 * Created by: 16374 <p>
 * Date: 2026/6/6 <p>
 * Time: 14:00 <p>
 * Description: 商品查询领域服务 —— 聚合商品、活动、拼团队伍数据
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductQueryService {

    private final ITradeRepository repository;

    /**
     * 分页查询商品列表
     *
     * @param page 页码（从 1 开始）
     * @param size 每页条数
     * @return 商品实体列表
     */
    public List<SkuEntity> getProductList(int page, int size) {
        int offset = (page - 1) * size;
        log.info("[商品查询] 分页查询商品列表 page={}, size={}, offset={}", page, size, offset);
        return repository.listSkus(offset, size);
    }

    /**
     * 统计上架商品总数
     */
    public int countProducts() {
        return repository.countSkus();
    }

    /**
     * 按商品 ID 查询商品详情
     */
    public SkuEntity getProductDetail(Long skuId) {
        log.info("[商品查询] 查询商品详情 skuId={}", skuId);
        return repository.getSkuById(skuId);
    }

    /**
     * 获取所有启用且未过期的活动列表
     */
    public List<ActivityConfigEntity> getAvailableActivities() {
        return repository.getAllActiveActivities();
    }

    /**
     * 获取某商品下所有进行中的拼团队伍
     */
    public List<GroupTeamEntity> getActiveTeams(Long skuId) {
        log.info("[商品查询] 查询商品进行中的拼团队伍 skuId={}", skuId);
        return repository.getActiveTeamsBySkuId(skuId);
    }

    /**
     * 统计某商品进行中的拼团队伍数
     */
    public int countActiveTeams(Long skuId) {
        return repository.countActiveTeamsBySkuId(skuId);
    }

    /**
     * 统计某商品历史已售数量
     */
    public int getSoldCount(Long skuId) {
        return repository.getSoldCountBySkuId(skuId);
    }

}
