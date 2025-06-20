package com.gag.RuiwuYuexin.mapper;

import com.gag.RuiwuYuexin.entity.Shop;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ShopMapper {
    Shop findByUserId(Long userId);
}
