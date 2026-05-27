package cn.pumluda.infrastructure.dcc;

import cn.pumluda.types.annotations.DCCValue;
import cn.pumluda.types.common.SplitConstants;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * Project: group-buy-market-pumluda <p>
 * File: DCCService <p>
 * Created by: 16374 <p>
 * Date: 2026/5/15 <p>
 * Time: 19:13 <p>
 * Description: 动态配置切换
 */
@Service
public class DCCService {
    @DCCValue("downgrade:0")
    private String downgrade;
    @DCCValue("cutRange:0")
    private String cutRange;
    @DCCValue("tradeSCBlockList:s02c02")
    private String tradeSCBlockList;


    public boolean downgrade() {
        return "1".equals(downgrade);
    }

    public boolean cutRange(String userId) {
        // 计算哈希码的绝对值
        int hashCode = Math.abs(userId.hashCode());

        // 获取最后两位
        int lastTwoDigits = hashCode % 100;

        // 判断是否在切量范围内
        if (lastTwoDigits <= Integer.parseInt(cutRange)) {
            return true;
        }

        return false;
    }

    public boolean TradeSCBlock(String source, String channel) {
        List<String> list = Arrays.asList(tradeSCBlockList.split(SplitConstants.SPLIT));
        return list.contains(source + channel);
    }
}
