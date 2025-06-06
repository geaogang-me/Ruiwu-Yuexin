package com.gag.RuiwuYuexin.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderDetailDto {
    private Long id;
    private Long userId;
    private Long addressId;
    private Integer num;              // 购买数量
    private BigDecimal Price;    // 订单总价
    private Integer status;           // 订单状态
    private LocalDateTime created;    // 下单时间

    // 以下是商品信息
    private String goodName;
    private byte[] goodImage;      // 如果是 Base64，可直接放
    private BigDecimal unitPrice;     // 单价
}
