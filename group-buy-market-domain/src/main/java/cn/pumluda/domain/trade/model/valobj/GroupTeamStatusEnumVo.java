package cn.pumluda.domain.trade.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Project: group-buy-market-better <p>
 * File: GroupTeamStatusEnumVo <p>
 * Created by: 16374 <p>
 * Date: 2026/5/27 <p>
 * Time: 10:01 <p>
 * Description: 拼团队伍状态枚举值对象
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum GroupTeamStatusEnumVo {

    PROGRESS(0, "拼团中"),
    COMPLETE(1, "拼团完成"),
    FAILED(2, "拼团失败");

    private Integer code;
    private String info;

    public static GroupTeamStatusEnumVo getStatus(Integer code) {
        return switch (code) {
            case 1 -> COMPLETE;
            case 2 -> FAILED;
            default -> PROGRESS;
        };
    }
}
