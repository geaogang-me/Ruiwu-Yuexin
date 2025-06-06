package com.gag.RuiwuYuexin.mapper;


import com.gag.RuiwuYuexin.dto.CartItemDTO;
import com.gag.RuiwuYuexin.entity.Cart;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.web.bind.annotation.Mapping;

import java.util.List;

@Mapper
public interface CartMapper  {
    void addToCart(Cart cart);
    List<CartItemDTO> selectByUserId(Long userId);

    int deleteByUserIdAndGoodId(@Param("userId") Long userId, @Param("goodId") Long goodId);

    @Select("SELECT IFNULL(SUM(num), 0) FROM cart WHERE user_id = #{userId}")
    int getCartCountByUserId(@Param("userId") Long userId);
    ;
}
