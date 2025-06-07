package com.gag.RuiwuYuexin.service;

import com.gag.RuiwuYuexin.dto.OrderDetailDto;
import com.gag.RuiwuYuexin.dto.OrderRequest;

import java.util.List;

public interface OrderService {
    void createOrder(OrderRequest req);
    List<OrderDetailDto> getOrderDetailsByUserId(Long userId);
    boolean confirmReceipt(Long orderId);
}
