package com.gag.RuiwuYuexin.listener;

import com.gag.RuiwuYuexin.entity.Order;
import com.gag.RuiwuYuexin.mapper.GoodsMapper;
import com.gag.RuiwuYuexin.mapper.OrderMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

/**
 * 监听 Redis key 过期事件，订单超时未支付处理
 */
@Component
public class KeyExpirationListener implements MessageListener {

    private static final Logger log = LoggerFactory.getLogger(KeyExpirationListener.class);
    private static final String PREFIX = "order:waiting:pay:";

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private GoodsMapper goodsMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        log.debug("Received expired event for key: {}", expiredKey);

        // 只处理订单超时键
        if (!expiredKey.startsWith(PREFIX)) {
            return;
        }

        // 从 key 中解析出 orderId
        String orderIdStr = expiredKey.substring(PREFIX.length());
        Long orderId;
        try {
            orderId = Long.valueOf(orderIdStr);
        } catch (NumberFormatException e) {
            log.warn("Failed to parse orderId from key: {}", expiredKey, e);
            return;
        }

        // 从数据库查询订单
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            log.warn("Order not found for id: {}", orderId);
            return;
        }

        // 只有“待支付”(status == 1) 的订单才处理
        if (!Integer.valueOf(1).equals(order.getStatus())) {
            log.debug("Order {} status is {}, not pending payment; skip", orderId, order.getStatus());
            return;
        }

        // 回滚库存
        Long goodId = order.getGoodId();
        Integer qty   = order.getNum();
        int updated = goodsMapper.addStock(goodId, qty);
        log.info("Rolled back stock for goodId={}, qty={}, result rows={}", goodId, qty, updated);

        // 更新订单状态为“已取消”(status == 3)
        int rows = orderMapper.updateStatus(orderId, 6);
        log.info("Updated order {} status to CANCELLED, result rows={}", orderId, rows);
    }
}
