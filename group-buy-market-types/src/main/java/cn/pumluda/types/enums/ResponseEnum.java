package cn.pumluda.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum ResponseEnum {

    SUCCESS("0000", "成功"),
    UN_ERROR("0001", "未知失败"),
    ILLEGAL_PARAMETER("0002", "非法参数"),
    TIME_OUT("0003", "请求超时"),
    DUPLICATE_CREATE_ORDER_REQUEST("0004", "订单处理中，请稍后重试"),
    SKU_NULL("0005", "不存在该商品"),
    SKU_OUT_OF_STOCK("0006", "商品库存不足"),
    SKU_OFFLINE("0007", "商品已下架"),
    ACTIVITY_NULL("0008", "不存在该活动"),
    ACTIVITY_EXPIRED("0009", "活动已过期"),
    ACTIVITY_NOT_STARTED("0010", "活动未开始"),
    GROUP_TEAM_NULL("0011", "不存在该拼团队伍"),
    GROUP_TEAM_FAIL("0012", "该拼团队伍已不可参与"),
    GROUP_TEAM_FULL("0013", "该拼团队伍已满"),
    ORDER_HAS_GROUP("0014", "订单已有进行中的团"),
    WRITE_DB_ERROR("0015", "DB 数据写入失败"),
    DUPLICATE_TRADE_ORDER("0016", "该笔交易订单已存在");

    private String code;
    private String info;

}
