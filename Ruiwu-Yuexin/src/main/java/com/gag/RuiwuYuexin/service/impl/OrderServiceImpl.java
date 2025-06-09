package com.gag.RuiwuYuexin.service.impl;
import com.gag.RuiwuYuexin.dto.OrderDetailDto;
import com.gag.RuiwuYuexin.dto.OrderRequest;
import com.gag.RuiwuYuexin.entity.Order;
import com.gag.RuiwuYuexin.mapper.OrderMapper;
import com.gag.RuiwuYuexin.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public void createOrder(OrderRequest req) {
        // 1. 拿单价
        BigDecimal unitPrice = orderMapper.selectUnitPriceByGoodId(req.getGoodId());
        // 2. 计算总价
        BigDecimal total = unitPrice.multiply(BigDecimal.valueOf(req.getQuantity()));
        // 3. 构造实体并插入
        Order o = new Order();
        o.setUserId(req.getUserId());
        o.setGoodId(req.getGoodId());
        o.setAddressId(req.getAddressId());
        o.setNum(req.getQuantity());
        o.setPrice(total);
        o.setStatus(1); // 初始状态：待发货
        o.setCreated(LocalDateTime.now());
        orderMapper.insertOrder(o);
    }

    @Override
    public List<OrderDetailDto> getOrderDetailsByUserId(Long userId) {
        List<OrderDetailDto> items = orderMapper.getOrderDetailsByUserId(userId);
        for (OrderDetailDto item : items) {
            byte[] img = item.getGoodImage();
            if (img != null) {
                item.setGoodImage(img); // set 方法里已经做了 Base64 转换
            }
        }
        return items;
    }

    @Override
    public boolean confirmReceipt(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order != null && order.getStatus() == 2) { // 已发货状态才能收货
            order.setStatus(3); // 4 表示已完成
            orderMapper.updateById(order);
            return true;
        }
        return false;
    }


}
