package cn.pumluda.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class DashboardOverviewResDTO {
    private int todayOrders;
    private BigDecimal todayRevenue;
    private int activeActivityCount;
    private int pendingShipTeams;
    private List<BlacklistUser> blacklist;
    private List<ProductRankingResDTO.RankItem> topProducts;
    private List<PublishedActivityResDTO.PubActivity> activeActivities;

    @Data @Builder @AllArgsConstructor @NoArgsConstructor
    public static class BlacklistUser {
        private Long userId;
        private String tagName;
    }
}
