package cn.pumluda.api.dto;

import lombok.Data;
import java.util.Date;

@Data
public class UpdateActivityReqDTO {
    private Long activityId;
    private String activityName;
    private Integer discountType;
    private String discountParam;
    private Integer totalQuota;
    private Date startTime;
    private Date endTime;
}
