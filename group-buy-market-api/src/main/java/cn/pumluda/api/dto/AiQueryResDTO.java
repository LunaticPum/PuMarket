package cn.pumluda.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class AiQueryResDTO {
    /** JSON 字符串：商品列表 */
    private String productsJson;
    /** JSON 字符串：用户订单列表 */
    private String ordersJson;
    /** JSON 字符串：可参与的活动 */
    private String activitiesJson;
}
