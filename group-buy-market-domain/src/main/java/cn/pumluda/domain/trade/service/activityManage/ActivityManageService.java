package cn.pumluda.domain.trade.service.activityManage;

import cn.pumluda.domain.trade.adapter.repository.ITradeRepository;
import cn.pumluda.domain.trade.model.entity.ActivityConfigEntity;
import cn.pumluda.domain.trade.model.entity.GroupTeamEntity;
import cn.pumluda.domain.trade.model.valobj.GroupTeamStatusEnumVo;
import cn.pumluda.types.enums.ResponseEnum;
import cn.pumluda.types.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 活动管理领域服务 —— 创建、撤销、冲突检测
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityManageService {

    private final ITradeRepository repository;

    /**
     * 创建活动并绑定商品
     */
    @Transactional(rollbackFor = Exception.class)
    public void createActivity(ActivityConfigEntity activity, List<Long> skuIds) {
        // 1. 冲突检测（必须在 insert 之前，否则匹配到自己）
        List<String> conflictDetails = new ArrayList<>();
        for (Long skuId : skuIds) {
            List<Long> conflictIds = repository.findConflictingActivityIds(
                    skuId, activity.getStartTime(), activity.getEndTime());
            if (!conflictIds.isEmpty()) {
                ActivityConfigEntity conflicting = repository.getActivityByActivityId(conflictIds.get(0));
                conflictDetails.add(String.format("商品 #%d「%s」与活动「%s」冲突（有效期至 %tF %tR）",
                        skuId, "已存在活动",
                        conflicting != null ? conflicting.getActivityName() : "未知",
                        conflicting != null ? conflicting.getEndTime() : ""));
            }
        }

        if (!conflictDetails.isEmpty()) {
            String msg = String.join("；", conflictDetails);
            throw new AppException(ResponseEnum.ILLEGAL_PARAMETER.getCode(), msg);
        }

        // 2. 写入 activity_config 主表
        repository.insertActivityConfig(activity);

        // 3. 写入活动-商品关联
        for (Long skuId : skuIds) {
            repository.insertActivityProduct(activity.getActivityId(), skuId);
        }

        log.info("[活动管理] 活动创建成功 activityId={} skuIds={}", activity.getActivityId(), skuIds);
    }

    /**
     * 更新活动配置 —— 更新后强制所有进行中队伍成团
     */
    @Transactional(rollbackFor = Exception.class)
    public int updateActivity(ActivityConfigEntity activity) {
        repository.updateActivityConfig(activity);

        // 强制所有 PROGRESS 队伍成团
        List<GroupTeamEntity> teams = repository.getTeamsByActivityId(activity.getActivityId(), 0, Integer.MAX_VALUE);
        int completed = 0;
        for (GroupTeamEntity team : teams) {
            if (GroupTeamStatusEnumVo.PROGRESS.equals(team.getTeamStatus())) {
                repository.completeGroupTeam(team.getActivityId(), team.getGroupTeamId());
                completed++;
            }
        }
        log.info("[活动管理] 活动已更新 activityId={} 强制成团:{}", activity.getActivityId(), completed);
        return completed;
    }

    /**
     * 撤销活动 —— 删除商品关联 + 强制未成团→成团
     */
    @Transactional(rollbackFor = Exception.class)
    public int revokeActivity(Long activityId) {
        // 1. 删除所有商品-活动关联
        repository.deleteActivityProductByActivityId(activityId);

        // 2. 该活动下所有 PROGRESS 队伍强制成团
        List<GroupTeamEntity> teams = repository.getTeamsByActivityId(activityId, 0, Integer.MAX_VALUE);
        int completed = 0;
        for (GroupTeamEntity team : teams) {
            if (GroupTeamStatusEnumVo.PROGRESS.equals(team.getTeamStatus())) {
                repository.completeGroupTeam(team.getActivityId(), team.getGroupTeamId());
                completed++;
            }
        }

        log.info("[活动管理] 活动已撤销 activityId={} 强制成团:{}", activityId, completed);
        return completed;
    }

    /**
     * 获取某商品当前生效的活动
     */
    public ActivityConfigEntity getActiveActivityForSku(Long skuId) {
        Long activityId = repository.findActiveActivityIdBySkuId(skuId);
        if (activityId == null) return null;
        return repository.getActivityByActivityId(activityId);
    }

    /**
     * 获取已发布的活动列表
     */
    public List<ActivityConfigEntity> getPublishedActivities() {
        return repository.getAllActiveActivities();
    }

}
