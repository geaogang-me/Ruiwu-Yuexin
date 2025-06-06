package com.gag.RuiwuYuexin.entity;

import lombok.Data;

@Data
public class OrderItem {
    private int id;
    private int orderId;
    private int goodId;
    private String goodName;
    private String goodImage;
    private int quantity;
    private double unitPrice;
    private double totalPrice;

    // Getters and Setters
}

