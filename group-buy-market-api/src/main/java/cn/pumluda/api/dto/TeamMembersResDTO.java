package cn.pumluda.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * Project: group-buy-market-better <p>
 * File: TeamMembersResDTO <p>
 * Created by: 16374 <p>
 * Date: 2026/6/6 <p>
 * Description: 队伍成员列表响应 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamMembersResDTO {

    private List<Member> members;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Member {

        private String orderNo;
        private Long userId;
        private Integer participationType;
        private String participationTypeName;
        private String orderStatus;
        private Date joinTime;
    }

}
