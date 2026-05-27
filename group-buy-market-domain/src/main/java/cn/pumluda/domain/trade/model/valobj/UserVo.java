package cn.pumluda.domain.trade.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

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
public class UserVo {
    private BigInteger userId;
    private int userTag;
}
