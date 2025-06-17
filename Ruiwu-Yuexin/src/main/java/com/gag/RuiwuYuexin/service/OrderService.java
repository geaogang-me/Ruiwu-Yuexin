package com.gag.RuiwuYuexin.service;

import com.gag.RuiwuYuexin.dto.OrderDetailDto;
import com.gag.RuiwuYuexin.dto.OrderRequest;

import java.util.List;

public interface OrderService {
    Long createOrder(OrderRequest req);
    void updateOrderStatusToShipped(Long orderId);
    void batchUpdateOrderStatus(List<Long> orderIds);
    List<OrderDetailDto> getOrderDetailsByUserId(Long userId);
    boolean confirmReceipt(Long orderId);
    boolean completeEvaluation(Long orderId);
}
