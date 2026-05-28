package cn.pumluda.domain.trade.model.entity;

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
 * Description: 用户值对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {
    private Long userId;
    private int userTag;
}
