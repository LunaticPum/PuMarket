package cn.pumluda.api.dto;

import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class CreateActivityReqDTO {
    private String activityName;
    private Integer activityType;
    private Integer discountType;
    private String discountParam;  // JSON: {"percent":80} or {"amount":30}
    private Integer requiredNum;
    private Integer totalQuota;
    private Date startTime;
    private Date endTime;
    private List<Long> skuIds;
}
