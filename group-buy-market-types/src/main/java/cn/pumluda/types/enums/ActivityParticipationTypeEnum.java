package cn.pumluda.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Project: group-buy-market-better <p>
 * File: ActivityParticipationTypeEnum <p>
 * Created by: 16374 <p>
 * Date: 2026/5/29 <p>
 * Time: 15:15 <p>
 * Description: 活动参与方式枚举
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum ActivityParticipationTypeEnum {

    CREATE_GROUP(1, "开团"),
    JOIN_GROUP(2, "参团"),
    QUEUE(3, "排队"),
    DRAW(4, "抽签"),
    ;

    private int code;
    private String info;
}
