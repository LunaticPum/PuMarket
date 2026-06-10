package cn.pumluda.domain.trade.service.marketing;

import cn.pumluda.domain.trade.model.entity.ActivityConfigEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Project: group-buy-market-better <p>
 * File: ActivityDetailResult <p>
 * Created by: 16374 <p>
 * Date: 2026/6/6 <p>
 * Description: 活动详情查询结果
 */
@Getter
@AllArgsConstructor
public class ActivityDetailResult {

    private final ActivityConfigEntity activity;
    private final int totalTeams;
    private final List<TeamSummary> teams;

}
