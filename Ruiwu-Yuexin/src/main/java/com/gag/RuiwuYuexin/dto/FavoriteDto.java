package com.gag.RuiwuYuexin.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FavoriteDto {
    private Long id;
    private String goodName;
    private byte[] goodImage;
    private BigDecimal price;
}
