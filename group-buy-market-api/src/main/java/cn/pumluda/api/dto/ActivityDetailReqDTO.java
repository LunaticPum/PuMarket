package cn.pumluda.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Project: group-buy-market-better <p>
 * File: ActivityDetailReqDTO <p>
 * Created by: 16374 <p>
 * Date: 2026/6/6 <p>
 * Description: 活动详情请求 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityDetailReqDTO {

    private Long activityId;
    private Integer page;
    private Integer size;

}
