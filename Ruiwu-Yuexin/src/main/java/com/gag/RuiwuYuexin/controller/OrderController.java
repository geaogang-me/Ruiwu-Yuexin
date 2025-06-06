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
    public Result<?> createOrder(@RequestBody OrderRequest req) {
        if (req.getUserId() == null) {
            return Result.error("用户未登录");
        }
        orderService.createOrder(req);
        return Result.success("订单创建成功");
    }
    @GetMapping("/list")
    public Result<List<OrderDetailDto>> listOrders(@RequestParam Long userId) {
        List<OrderDetailDto> list = orderService.getOrderDetailsByUserId(userId);
        return Result.success(list);
    }
}
