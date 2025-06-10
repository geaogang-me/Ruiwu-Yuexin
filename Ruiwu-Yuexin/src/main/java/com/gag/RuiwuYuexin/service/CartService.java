package com.gag.RuiwuYuexin.service;


import com.gag.RuiwuYuexin.dto.CartItemDTO;
import com.gag.RuiwuYuexin.entity.Cart;

import java.util.List;


public interface CartService {
     void addToCart(Cart cart);
     List<CartItemDTO> getCartByUserId(Long userId);

     boolean deleteItemById(Long userId, Long Id);
     int getCartCount(Long userId);
}
