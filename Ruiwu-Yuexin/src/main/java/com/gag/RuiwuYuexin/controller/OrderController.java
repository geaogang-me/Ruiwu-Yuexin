package com.gag.RuiwuYuexin.controller;

import com.gag.RuiwuYuexin.dto.OrderDetailDto;
import com.gag.RuiwuYuexin.dto.OrderRequest;
import com.gag.RuiwuYuexin.service.OrderService;
import com.gag.RuiwuYuexin.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    public Result<Long> createOrder(@RequestBody OrderRequest req) {
        if (req.getUserId() == null) {
            return Result.error("用户未登录");
        }
        Long orderId = orderService.createOrder(req);
        return Result.success(orderId);
    }
    @PostMapping("/updateStatus")
    public Result<String> updateOrderStatus(@RequestParam Long orderId) {
        if (orderId == null) {
            return Result.error("订单ID不能为空");
        }
        orderService.updateOrderStatusToShipped(orderId);
        return Result.success("订单状态更新成功");
    }

    @GetMapping("/list")
    public Result<List<OrderDetailDto>> listOrders(@RequestParam Long userId) {
        List<OrderDetailDto> list = orderService.getOrderDetailsByUserId(userId);
        return Result.success(list);
    }
    @PostMapping("/confirmReceipt/{orderId}")
    public Result<String> confirmReceipt(@PathVariable Long orderId) {
        boolean success = orderService.confirmReceipt(orderId);
        if (success) {
            return Result.success("确认收货成功");
        } else {
            return Result.error("确认收货失败");
        }
    }

}
