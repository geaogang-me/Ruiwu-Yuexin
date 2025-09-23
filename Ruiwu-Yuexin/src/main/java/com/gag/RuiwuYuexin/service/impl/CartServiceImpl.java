package com.gag.RuiwuYuexin.service.impl;

import com.gag.RuiwuYuexin.dto.CartItemDTO;
import com.gag.RuiwuYuexin.entity.Cart;
import com.gag.RuiwuYuexin.mapper.CartMapper;
import com.gag.RuiwuYuexin.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    CartMapper cartMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String CART_KEY_PREFIX = "cart:user:";
    // 缓存 7 天（根据需要调整）
    private static final Duration CART_TTL = Duration.ofDays(7);

    private String keyForUser(Long userId) {
        return CART_KEY_PREFIX + userId;
    }

    @Override
    public void addToCart(Cart cart) {
        // 1) 写 DB（主存）
        cartMapper.addToCart(cart);

        // 2) 更新 Redis：以 goodId 为 field，存放数量（使用 incr 保证并发安全）
        if (cart.getUserId() == null || cart.getGoodId() == null || cart.getNum() == null) {
            return;
        }
        String key = keyForUser(cart.getUserId());
        String field = String.valueOf(cart.getGoodId());
        // 使用 increment 保证原子性（value 存为整数字符串）
        try {
            redisTemplate.opsForHash().increment(key, field, cart.getNum());
            redisTemplate.expire(key, CART_TTL);
        } catch (Exception ignored) {
            // Redis 不应该影响主流程，捕获异常但不抛出
        }
    }

    @Override
    public List<CartItemDTO> getCartByUserId(Long userId) {
        String key = keyForUser(userId);
        try {
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
            if (entries == null || entries.isEmpty()) {
                // Redis 未命中 -> 从 DB 读取并缓存到 Redis（只缓存数量）
                List<CartItemDTO> items = cartMapper.selectByUserId(userId);
                if (items == null) return new ArrayList<>();
                // 缓存到 Redis（field = goodId, value = num）
                for (CartItemDTO dto : items) {
                    if (dto.getGoodId() != null && dto.getNum() != null) {
                        try {
                            redisTemplate.opsForHash().put(key, String.valueOf(dto.getGoodId()), String.valueOf(dto.getNum()));
                        } catch (Exception ignored) {}
                    }
                }
                try { redisTemplate.expire(key, CART_TTL); } catch (Exception ignored) {}
                // 确保图片已经经过处理（你原有逻辑）
                for (CartItemDTO item : items) {
                    byte[] img = item.getGoodImage();
                    if (img != null) {
                        item.setGoodImage(img); // set 方法里已经做了 Base64 转换
                    }
                }
                return items;
            } else {
                // Redis 命中 -> 仍从 DB 读取商品详情，再用 Redis 的数量覆盖 DTO 的数量（保证详情最新）
                List<CartItemDTO> items = cartMapper.selectByUserId(userId);
                if (items == null) return new ArrayList<>();
                for (CartItemDTO item : items) {
                    if (item.getGoodId() == null) continue;
                    Object v = entries.get(String.valueOf(item.getGoodId()));
                    if (v != null) {
                        try {
                            int num = Integer.parseInt(String.valueOf(v));
                            item.setNum(num);
                        } catch (NumberFormatException ignored) {}
                    }
                    byte[] img = item.getGoodImage();
                    if (img != null) {
                        item.setGoodImage(img); // 保持你原来的图片处理逻辑
                    }
                }
                return items;
            }
        } catch (Exception e) {
            // Redis 操作异常则退回到 DB（保证可用性）
            List<CartItemDTO> items = cartMapper.selectByUserId(userId);
            if (items == null) return new ArrayList<>();
            for (CartItemDTO item : items) {
                byte[] img = item.getGoodImage();
                if (img != null) {
                    item.setGoodImage(img);
                }
            }
            return items;
        }
    }

    public boolean deleteItemById(Long userId, Long id) {
        int rows = cartMapper.deleteByUserIdAndId(userId, id);
        boolean success = rows > 0;
        if (success) {
            // 删除成功后，为保证一致性，清空该用户的购物车缓存（简单可靠）
            try {
                redisTemplate.delete(keyForUser(userId));
            } catch (Exception ignored) {}
        }
        return success;
    }

    @Override
    public int getCartCount(Long userId) {
        String key = keyForUser(userId);
        try {
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
            if (entries != null && !entries.isEmpty()) {
                int sum = 0;
                for (Object val : entries.values()) {
                    try {
                        sum += Integer.parseInt(String.valueOf(val));
                    } catch (NumberFormatException ignored) {}
                }
                return sum;
            }
        } catch (Exception ignored) {
            // 如果 Redis 出错则回退 DB
        }
        return cartMapper.getCartCountByUserId(userId);
    }
}
