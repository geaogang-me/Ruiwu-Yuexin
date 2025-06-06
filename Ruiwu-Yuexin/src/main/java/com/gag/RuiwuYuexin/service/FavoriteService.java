package com.gag.RuiwuYuexin.service;

import com.gag.RuiwuYuexin.dto.FavoriteDto;

import java.util.List;

public interface FavoriteService {
    boolean addFavorite(Long userId, Long goodId);
    boolean removeFavorite(Long userId, Long goodId);
    boolean isFavorite(Long userId, Long goodId);
    List<FavoriteDto> listFavoriteGoodIds(Long userId);
}