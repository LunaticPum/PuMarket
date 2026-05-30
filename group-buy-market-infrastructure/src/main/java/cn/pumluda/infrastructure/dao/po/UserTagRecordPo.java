package cn.pumluda.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Project: group-buy-market-better <p>
 * File: UserTagRecordPo <p>
 * Created by: 16374 <p>
 * Date: 2026/5/30 <p>
 * Time: 15:28 <p>
 * Description: 用户标签记录 Po
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserTagRecordPo {

    /* 自增主键 */
    private Long id;

    /* 用户 ID */
    private Long userId;
    /* 用户标签 */
    private Integer userTag;
    /* 记录创建时间 */
    private Date createTime;
    /* 记录更新时间 */
    private Date updateTime;
}
