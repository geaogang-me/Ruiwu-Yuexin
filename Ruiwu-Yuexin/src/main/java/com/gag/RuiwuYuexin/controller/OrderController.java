package com.gag.RuiwuYuexin.controller;

import com.gag.RuiwuYuexin.dto.OrderDetailDto;
import com.gag.RuiwuYuexin.dto.OrderRequest;
import com.gag.RuiwuYuexin.service.OrderService;
import com.gag.RuiwuYuexin.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
    @PostMapping("/batchUpdateStatus")
    public Result<String> batchUpdateStatus(@RequestParam String orderIds) {
        try {
            // 解析逗号分隔的订单ID
            List<Long> idList = Arrays.stream(orderIds.split(","))
                    .map(Long::parseLong)
                    .collect(Collectors.toList());

            if (idList.isEmpty()) {
                return Result.error("订单ID不能为空");
            }

            orderService.batchUpdateOrderStatus(idList);
            return Result.success("成功更新 " + idList.size() + " 个订单状态");
        } catch (Exception e) {
            return Result.error("更新订单状态失败: " + e.getMessage());
        }
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
    @PostMapping("/evaluate/{orderId}")
    public Result<String> completeEvaluation(@PathVariable Long orderId) {
        if (orderId == null) {
            return Result.error("订单ID不能为空");
        }
        boolean success = orderService.completeEvaluation(orderId);
        if (success) {
            return Result.success("评价完成，订单状态已更新");
        } else {
            return Result.error("更新订单状态失败");
        }
    }

}
