package com.gag.RuiwuYuexin.controller;

import com.gag.RuiwuYuexin.dto.FavoriteDto;
import com.gag.RuiwuYuexin.service.FavoriteService;
import com.gag.RuiwuYuexin.common.Result;  // 你之前提到有统一Result类
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {

    @Autowired
    private FavoriteService userFavoriteService;

    // 添加收藏
    @GetMapping("/add")
    public Result<?> addFavorite(@RequestParam Long userId, @RequestParam Long goodId) {
        boolean success = userFavoriteService.addFavorite(userId, goodId);
        if (success) {
            return Result.success("收藏成功");
        } else {
            return Result.error("该商品已收藏");
        }
    }

    // 取消收藏
    @DeleteMapping("/remove")
    public Result<?> removeFavorite(@RequestParam Long userId, @RequestParam Long goodId) {
        boolean success = userFavoriteService.removeFavorite(userId, goodId);
        if (success) {
            return Result.success("取消收藏成功");
        } else {
            return Result.error("取消收藏失败");
        }
    }

    // 判断是否收藏（可用于初始化收藏按钮状态）
    @GetMapping("/check")
    public Result<Boolean> isFavorite(@RequestParam Long userId, @RequestParam Long goodId) {
        boolean favorite = userFavoriteService.isFavorite(userId, goodId);
        return Result.success(favorite);
    }

    // 获取用户所有收藏商品ID（可用于批量标记收藏状态）
    @GetMapping("/list")
    public Result<List<FavoriteDto>> listFavorites(@RequestParam Long userId) {
        List<FavoriteDto> favoriteGoods = userFavoriteService.listFavoriteGoodIds(userId);
        for (FavoriteDto fav : favoriteGoods) {
            byte[] img = fav.getGoodImage();
            if (img != null) {
                fav.setGoodImage(img); // set 方法里已经做了 Base64 转换
            }
        }
        return Result.success(favoriteGoods);
    }
}
