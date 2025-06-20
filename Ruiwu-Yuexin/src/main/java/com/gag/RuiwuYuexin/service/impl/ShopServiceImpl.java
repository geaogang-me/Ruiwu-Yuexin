package com.gag.RuiwuYuexin.service.impl;

import com.gag.RuiwuYuexin.entity.Shop;
import com.gag.RuiwuYuexin.mapper.ShopMapper;
import com.gag.RuiwuYuexin.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ShopServiceImpl implements ShopService {
    @Autowired
    ShopMapper shopMapper;
    @Override
    public Shop findByUserId(Long userId) {
        return shopMapper.findByUserId(userId);
    }
}
