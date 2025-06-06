package com.gag.RuiwuYuexin.dto;

import lombok.Data;

@Data
public class CartDeleteRequest {
    private Long userId;
    private Long goodId; // 或使用 goodId 更稳定
}