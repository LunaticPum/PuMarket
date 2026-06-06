package cn.pumluda.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class SalesDetailResDTO {
    private String period;
    private int totalOrders;
    private BigDecimal totalRevenue;
    private int cancelCount;
    private List<Item> items;

    @Data @Builder @AllArgsConstructor @NoArgsConstructor
    public static class Item {
        private String date;
        private int orderCount;
        private BigDecimal revenue;
        private int cancelCount;
    }
}
