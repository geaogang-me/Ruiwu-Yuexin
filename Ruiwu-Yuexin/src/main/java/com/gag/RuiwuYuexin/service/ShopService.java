package com.gag.RuiwuYuexin.service;

import com.gag.RuiwuYuexin.entity.Shop;

public interface ShopService {
    Shop findByUserId(Long userId);
}
