package com.gag.RuiwuYuexin.service;

import com.gag.RuiwuYuexin.dto.OrderDetailDto;
import com.gag.RuiwuYuexin.dto.OrderRequest;
import jakarta.transaction.Transactional;

import java.util.List;

public interface OrderService {
    @Transactional
    Long createOrder(OrderRequest req);
    void updateOrderStatusToShipped(Long orderId);
    void batchUpdateOrderStatus(List<Long> orderIds);
    List<OrderDetailDto> getOrderDetailsByUserId(Long userId);
    boolean confirmReceipt(Long orderId);
    boolean confirmDeliver(Long orderId);
    boolean completeEvaluation(Long orderId);
    List<OrderDetailDto> getOrderDetailsByShopId(Long shopId);
}
