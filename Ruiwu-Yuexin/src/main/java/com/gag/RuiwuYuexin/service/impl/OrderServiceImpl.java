package com.gag.RuiwuYuexin.service.impl;

import com.gag.RuiwuYuexin.dto.OrderDetailDto;
import com.gag.RuiwuYuexin.dto.OrderRequest;
import com.gag.RuiwuYuexin.entity.Order;
import com.gag.RuiwuYuexin.exception.ServiceException;
import com.gag.RuiwuYuexin.mapper.GoodsMapper;
import com.gag.RuiwuYuexin.mapper.OrderMapper;
import com.gag.RuiwuYuexin.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private GoodsMapper goodMapper;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public Long createOrder(OrderRequest req) {
        // 查询商品库存
        Integer stock = goodMapper.getStock(req.getGoodId());
        if (stock == null) {
            throw new ServiceException("ORDER_ERROR_001", "商品不存在");
        }

        // 校验库存是否充足
        if (stock < req.getQuantity()) {
            throw new ServiceException("ORDER_ERROR_002", "库存不足，当前库存: " + stock);
        }

        // 尝试扣减库存（使用乐观锁）
        int updateCount = goodMapper.reduceStock(req.getGoodId(), req.getQuantity());
        if (updateCount == 0) {
            throw new ServiceException("ORDER_ERROR_003", "库存不足或已被修改，请重试");
        }

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
        o.setStatus(1); // 初始状态：待支付
        o.setCreated(LocalDateTime.now());
        orderMapper.insertOrder(o);

        // 写 Redis，用 JSON 或简单拼接存储 goodId 和数量，30分钟后自动过期
        String redisKey = "order:waiting:pay:" + o.getId();
        String redisValue = o.getGoodId() + ":" + o.getNum();
        redisTemplate.opsForValue().set(redisKey, redisValue, 1, TimeUnit.MINUTES);

        return o.getId();
    }

    @Override
    public void updateOrderStatusToShipped(Long orderId) {
        // 更新订单状态为已支付
        orderMapper.updateOrderStatus(orderId, 2);
        // 支付完成后，删除 Redis 中的待支付缓存
        String redisKey = "order:waiting:pay:" + orderId;
        redisTemplate.delete(redisKey);
    }

    @Override
    public void batchUpdateOrderStatus(List<Long> orderIds) {
        // 批量更新订单状态为已支付
        orderMapper.batchUpdateOrderStatus(orderIds, 2);
        // 删除对应 Redis 缓存
        orderIds.forEach(id -> {
            String redisKey = "order:waiting:pay:" + id;
            redisTemplate.delete(redisKey);
        });
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
    public List<OrderDetailDto> getOrderDetailsByShopId(Long shopId) {
        return orderMapper.findOrderDetailsByShopId(shopId);
    }

    @Override
    public boolean confirmReceipt(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order != null && order.getStatus() == 3) { // 已发货状态才能收货
            order.setStatus(4); // 4 表示已完成
            orderMapper.updateById(order);
            return true;
        }
        return false;
    }

    @Override
    public boolean orderCancel(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order != null && order.getStatus() == 1) { // 仅待支付状态可取消
            order.setStatus(6); // 6 表示已取消
            orderMapper.updateById(order);
            // 恢复库存
            goodMapper.addStock(order.getGoodId(), order.getNum());
            // 删除 Redis 中的待支付缓存
            String redisKey = "order:waiting:pay:" + orderId;
            redisTemplate.delete(redisKey);
            return true;
        }
        return false;
    }

    @Override
    public boolean confirmDeliver(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order != null && order.getStatus() == 2) { // 待发货状态才能发货
            order.setStatus(3); // 3 表示已发货
            orderMapper.updateById(order);
            return true;
        }
        return false;
    }

    @Override
    public boolean completeEvaluation(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order != null && order.getStatus() == 4) { // 已完成状态才能评价
            order.setStatus(5); // 5 表示已评价
            orderMapper.updateById(order);
            return true;
        }
        return false;
    }
}
