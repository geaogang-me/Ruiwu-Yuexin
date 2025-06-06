package com.gag.RuiwuYuexin.dto;

import lombok.Data;

import java.math.BigDecimal;


@Data
public class CartItemDTO {
    private Long goodId;
    private Integer num;
    private String goodName;
    private byte[] goodImage; // 原始图片数据
    private String goodImageBase64; // Base64 字符串
    private BigDecimal price;
}
