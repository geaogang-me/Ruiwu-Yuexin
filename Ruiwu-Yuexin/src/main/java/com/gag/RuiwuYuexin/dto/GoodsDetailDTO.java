package com.gag.RuiwuYuexin.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@Data
public class GoodsDetailDTO {
    private int id;
    private String goodName;
    private List<String> images; // Base64 字符串列表
    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal price;
    private LocalDateTime createTime;
}
