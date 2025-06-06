package com.gag.RuiwuYuexin.dto;

import lombok.Data;

@Data
public class OrderRequest {
    private Long userId;
    private Long goodId;
    private Long addressId;
    private Integer quantity;
}