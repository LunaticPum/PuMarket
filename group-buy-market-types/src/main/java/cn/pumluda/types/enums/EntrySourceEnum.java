package cn.pumluda.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Project: group-buy-market-better <p>
 * File: EntrySourceEnum <p>
 * Created by: 16374 <p>
 * Date: 2026/5/26 <p>
 * Time: 17:36 <p>
 * Description: 流量来源枚举
 */
@Getter
@AllArgsConstructor
public enum EntrySourceEnum {
    WECHAT(1),
    DOUYIN(2),
    WEB(3),
    APP(4);

    private final int code;
}
