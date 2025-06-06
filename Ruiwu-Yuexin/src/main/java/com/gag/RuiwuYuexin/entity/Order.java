package com.gag.RuiwuYuexin.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Order {
    private Long id;
    private Long userId;
    private Long goodId;
    private Long addressId;
    private Integer num;
    private BigDecimal price;
    private Integer status;
    private LocalDateTime created;
    private List<OrderItem> items;
}

