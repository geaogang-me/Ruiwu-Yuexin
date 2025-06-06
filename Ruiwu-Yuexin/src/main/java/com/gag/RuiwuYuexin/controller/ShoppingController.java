package com.gag.RuiwuYuexin.controller;

import com.gag.RuiwuYuexin.dto.CartDeleteRequest;
import com.gag.RuiwuYuexin.dto.CartItemDTO;
import com.gag.RuiwuYuexin.entity.Cart;
import com.gag.RuiwuYuexin.service.CartService;
import com.gag.RuiwuYuexin.common.Result;
import com.gag.RuiwuYuexin.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ShoppingController {
    @Autowired
    private CartService cartService;
    @Autowired
    private JwtUtils jwtUtils;
    @PostMapping("/addToCart")
    public Result addGoodToCart(@RequestBody Map<String, Object> map, HttpServletRequest request) {
        // 从请求头提取 Token
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return Result.error("未登录或Token格式错误");
        }

        String jwtToken = token.substring(7);
        Long userId;
        try {
            userId = jwtUtils.getUserIdFromToken(jwtToken);
        } catch (Exception e) {
            return Result.error("Token无效");
        }

        // 构造购物车对象
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setGoodId(((Integer) map.get("goodId")).longValue());
        cart.setNum((Integer) map.get("num"));

        cartService.addToCart(cart);
        return Result.success(null);
    }

    @GetMapping("/list/{userId}")
    public Result<List<CartItemDTO>> getCartByUserId(@PathVariable Long userId) {
        return Result.success(cartService.getCartByUserId(userId));
    }
    @DeleteMapping("/cart/delete")
    public Result<?> deleteFromCart(@RequestBody CartDeleteRequest request) {
        boolean success = cartService.deleteItemByGoodId(request.getUserId(), request.getGoodId());
        return success ? Result.success() : Result.error("删除失败");
    }
    @GetMapping("cart/count")
    public Result<Integer> getCartCount(@RequestParam Long userId) {
        int count = cartService.getCartCount(userId);
        return Result.success(count);
    }
}
