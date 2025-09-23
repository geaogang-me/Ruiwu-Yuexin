package com.gag.RuiwuYuexin.service.impl;

import com.gag.RuiwuYuexin.dto.FavoriteDto;
import com.gag.RuiwuYuexin.entity.Favorite;
import com.gag.RuiwuYuexin.mapper.FavoriteMapper;
import com.gag.RuiwuYuexin.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String FAVORITE_KEY_PREFIX = "favorite:user:";
    // 缓存 30 天（根据业务调整；也可不设置 TTL）
    private static final Duration FAVORITE_TTL = Duration.ofDays(30);

    private String keyForUser(Long userId) {
        return FAVORITE_KEY_PREFIX + userId;
    }

    @Override
    public boolean addFavorite(Long userId, Long goodId) {
        // 优先检查 Redis（若 Redis 不可用，会回退到 DB 检查）
        if (isFavorite(userId, goodId)) {
            return false; // 已收藏，不重复添加
        }
        Favorite uf = new Favorite();
        uf.setUserId(userId);
        uf.setGoodId(goodId);
        int rows = favoriteMapper.insert(uf);
        boolean ok = rows > 0;
        if (ok) {
            // 同步更新 Redis set
            try {
                String key = keyForUser(userId);
                redisTemplate.opsForSet().add(key, String.valueOf(goodId));
                redisTemplate.expire(key, FAVORITE_TTL);
            } catch (Exception ignored) {
                // Redis 不应该影响主流程，捕获但不抛出
            }
        }
        return ok;
    }

    @Override
    public boolean removeFavorite(Long userId, Long goodId) {
        int rows = favoriteMapper.deleteByUserIdAndGoodId(userId, goodId);
        boolean ok = rows > 0;
        if (ok) {
            try {
                String key = keyForUser(userId);
                redisTemplate.opsForSet().remove(key, String.valueOf(goodId));
            } catch (Exception ignored) {}
        }
        return ok;
    }

    @Override
    public boolean isFavorite(Long userId, Long goodId) {
        String key = keyForUser(userId);
        try {
            Boolean member = redisTemplate.opsForSet().isMember(key, String.valueOf(goodId));
            if (Boolean.TRUE.equals(member)) {
                return true;
            }
        } catch (Exception ignored) {
            // Redis 出错则回退 DB
        }
        // 回退到 DB 查询（并在查询到时把结果写回 Redis，便于下次命中）
        boolean existsInDb = favoriteMapper.selectByUserIdAndGoodId(userId, goodId) != null;
        if (existsInDb) {
            try {
                redisTemplate.opsForSet().add(key, String.valueOf(goodId));
                redisTemplate.expire(key, FAVORITE_TTL);
            } catch (Exception ignored) {}
        }
        return existsInDb;
    }

    @Override
    public List<FavoriteDto> listFavoriteGoodIds(Long userId) {
        String key = keyForUser(userId);
        try {
            Set<String> members = redisTemplate.opsForSet().members(key);
            // 如果 Redis 已有成员，为保持 DTO 的完整性我们仍从 DB 获取详情（DB 一般很快且保证最新）
            if (members != null && !members.isEmpty()) {
                List<FavoriteDto> list = favoriteMapper.selectFavoriteGoodsByUserId(userId);
                return list == null ? Collections.emptyList() : list;
            }
        } catch (Exception ignored) {}
        // Redis 未命中或异常 -> 从 DB 读取并刷新 Redis 缓存（仅缓存 goodId）
        List<FavoriteDto> list = favoriteMapper.selectFavoriteGoodsByUserId(userId);
        if (list == null || list.isEmpty()) {
            // 为防止缓存穿透，不写空集合（也可选择写空标记）
            return Collections.emptyList();
        }
        try {
            String[] ids = list.stream()
                    .map(dto -> String.valueOf(dto.getId()))
                    .toArray(String[]::new);
            if (ids.length > 0) {
                redisTemplate.opsForSet().add(key, ids);
                redisTemplate.expire(key, FAVORITE_TTL);
            }
        } catch (Exception ignored) {}
        return list;
    }
}
