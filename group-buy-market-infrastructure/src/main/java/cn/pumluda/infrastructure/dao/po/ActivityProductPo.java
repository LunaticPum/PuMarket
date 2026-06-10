package cn.pumluda.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 活动-商品关联 PO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityProductPo {

    private Long id;
    private Long activityId;
    private Long skuId;
    private Date createTime;

}
