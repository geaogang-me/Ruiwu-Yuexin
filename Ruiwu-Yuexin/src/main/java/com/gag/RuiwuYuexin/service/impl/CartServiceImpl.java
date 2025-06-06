package com.gag.RuiwuYuexin.service.impl;

import com.gag.RuiwuYuexin.dto.CartItemDTO;
import com.gag.RuiwuYuexin.entity.Cart;
import com.gag.RuiwuYuexin.mapper.CartMapper;
import com.gag.RuiwuYuexin.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    CartMapper cartMapper;
    @Override
    public void addToCart(Cart cart) {
        cartMapper.addToCart(cart);
    }

    @Override
    public List<CartItemDTO> getCartByUserId(Long userId) {
        List<CartItemDTO> items = cartMapper.selectByUserId(userId);
        for (CartItemDTO item : items) {
            byte[] img = item.getGoodImage();
            if (img != null) {
                item.setGoodImage(img); // set 方法里已经做了 Base64 转换
            }
        }
        return items;
    }
    public boolean deleteItemByGoodId(Long userId, Long goodId) {
        return cartMapper.deleteByUserIdAndGoodId(userId, goodId) > 0;
    }

    @Override
    public int getCartCount(Long userId) {
        return cartMapper.getCartCountByUserId(userId);
    }


}
