package com.gag.RuiwuYuexin.service.impl;

import com.gag.RuiwuYuexin.dto.FavoriteDto;
import com.gag.RuiwuYuexin.entity.Favorite;
import com.gag.RuiwuYuexin.mapper.FavoriteMapper;
import com.gag.RuiwuYuexin.service.FavoriteService;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Override
    public boolean addFavorite(Long userId, Long goodId) {
        if (isFavorite(userId, goodId)) {
            return false; // 已收藏，不重复添加
        }
        Favorite uf = new Favorite();
        uf.setUserId(userId);
        uf.setGoodId(goodId);
        return favoriteMapper.insert(uf) > 0;
    }

    @Override
    public boolean removeFavorite(Long userId, Long goodId) {
        return favoriteMapper.deleteByUserIdAndGoodId(userId, goodId) > 0;
    }

    @Override
    public boolean isFavorite(Long userId, Long goodId) {
        return favoriteMapper.selectByUserIdAndGoodId(userId, goodId) != null;
    }

    @Override
    public List<FavoriteDto> listFavoriteGoodIds(Long userId) {
        return favoriteMapper.selectFavoriteGoodsByUserId(userId);
    }


}
