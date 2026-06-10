package cn.pumluda.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
import java.util.List;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class PublishedActivityResDTO {
    private List<PubActivity> list;

    @Data @Builder @AllArgsConstructor @NoArgsConstructor
    public static class PubActivity {
        private Long activityId;
        private String activityName;
        private Integer activityType;
        private Integer discountType;
        private Date startTime;
        private Date endTime;
        private int teamCount;
        private List<Long> skuIds;
        private Integer status;
    }
}
