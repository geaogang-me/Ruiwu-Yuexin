package com.gag.RuiwuYuexin.service;


import com.gag.RuiwuYuexin.dto.CartItemDTO;
import com.gag.RuiwuYuexin.entity.Cart;

import java.util.List;


public interface CartService {
     void addToCart(Cart cart);
     List<CartItemDTO> getCartByUserId(Long userId);

     boolean deleteItemByGoodId(Long userId, Long goodId);
     int getCartCount(Long userId);
}
