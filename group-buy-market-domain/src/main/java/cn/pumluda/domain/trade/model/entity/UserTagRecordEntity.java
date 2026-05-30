package cn.pumluda.domain.trade.model.entity;

import cn.pumluda.types.enums.UserTagEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Project: group-buy-market-better <p>
 * File: UserVo <p>
 * Created by: 16374 <p>
 * Date: 2026/5/27 <p>
 * Time: 10:24 <p>
 * Description: 用户 Entity
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserTagRecordEntity {
    private Long userId;
    private Integer userTag;
}
