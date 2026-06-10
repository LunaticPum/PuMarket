package cn.pumluda.api.dto;

import lombok.Data;

@Data
public class SalesDetailReqDTO {
    private String period; // "1d" / "7d" / "30d"
}
