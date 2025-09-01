package com.gag.RuiwuYuexin.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.gag.RuiwuYuexin.dto.GoodsDetailDTO;
import com.gag.RuiwuYuexin.entity.Goods;
import com.gag.RuiwuYuexin.entity.GoodsImage;
import com.gag.RuiwuYuexin.mapper.GoodsImageMapper;
import com.gag.RuiwuYuexin.mapper.GoodsMapper;
import com.gag.RuiwuYuexin.service.GoodsService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class GoodsServiceImpl implements GoodsService {
    @Autowired
    GoodsMapper goodsMapper;
    @Autowired
    GoodsImageMapper goodsImageMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;

    // ---------- Key builder to avoid empty segments ----------
    private static String buildGoodsPageKey(String keyword, String type, int page, int size) {
        List<String> parts = new ArrayList<>();
        parts.add("goods");
        parts.add("page");
        if (keyword != null) {
            String trimmed = keyword.trim();
            if (!trimmed.isEmpty()) {
                parts.add(trimmed);
            }
        }
        // ensure we always have a type segment (use "all" as default)
        String safeType = (type == null || type.trim().isEmpty()) ? "all" : type.trim();
        parts.add(safeType);

        parts.add(String.valueOf(page));
        parts.add(String.valueOf(size));
        return String.join(":", parts);
    }

    @Override
    public Map<String, Object> findGoodsPage(String keyword, String type, int page, int size) {
        // 1. 生成缓存 key（不会生成空段）
        String cacheKey = buildGoodsPageKey(keyword, type, page, size);

        // 2. 尝试从 Redis 读取
        String cachedJson = redisTemplate.opsForValue().get(cacheKey);
        if (cachedJson != null) {
            return JSON.parseObject(cachedJson, new TypeReference<Map<String, Object>>() {});
        }

        // 3. 缓存不命中，查询数据库
        int offset = (page - 1) * size;
        String safeKeywordForQuery = (keyword == null || keyword.trim().isEmpty()) ? "%" : ("%" + keyword.trim() + "%");
        String safeTypeForQuery = (type == null || type.trim().isEmpty()) ? null : type.trim();
        List<Goods> list = goodsMapper.findGoodsPage(safeKeywordForQuery, safeTypeForQuery, offset, size);
        int total = goodsMapper.countGoods(safeKeywordForQuery, safeTypeForQuery);

        Map<String, Object> result = new HashMap<>();
        result.put("records", list);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);

        // 4. 序列化并写入 Redis，设置过期时间（如 30 分钟）
        String jsonToCache = JSON.toJSONString(result);
        redisTemplate.opsForValue().set(cacheKey, jsonToCache, 30, TimeUnit.MINUTES);

        return result;
    }

    @Override
    public Map<String, Object> findGoodsPageByShop(Long shopId, Integer status, int page, int size) {
        int offset = (page - 1) * size;
        List<Goods> list = goodsMapper.selectPageByShop(shopId, status, offset, size);
        long total = goodsMapper.countByShop(shopId, status);
        Map<String, Object> result = new HashMap<>();
        result.put("records", list);
        result.put("total", total);
        return result;
    }

    @Override
    public boolean updateGoods(Goods goods) {
        Goods existing = goodsMapper.selectById(goods.getId());
        if (existing == null || !existing.getBelongShop().equals(goods.getBelongShop())) {
            return false;
        }
        goods.setCreateTime(null);
        goods.setUpdateTime(LocalDateTime.now());
        int updated = goodsMapper.updateByPrimaryKeySelective(goods);
        if (updated > 0) {
            clearGoodsPageCache();
            return true;
        }
        return false;
    }

    @Override
    public GoodsDetailDTO findGoodsDetail(int id) {
        Goods p = goodsMapper.selectByIdWithImages(id);
        if (p == null) return null;
        GoodsDetailDTO dto = new GoodsDetailDTO();
        BeanUtils.copyProperties(p, dto, "images");
        List<String> b64 = p.getImages().stream()
                .map(img -> Base64.getEncoder().encodeToString(img.getImageData()))
                .collect(Collectors.toList());
        dto.setImages(b64);
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createGoodWithImages(Goods goods,
                                     MultipartFile mainImage,
                                     List<MultipartFile> images) {
        try {
            if (mainImage != null && !mainImage.isEmpty()) {
                goods.setGoodImage(mainImage.getBytes());
            }
            goods.setCreateTime(LocalDateTime.now());
            goodsMapper.insertSelective(goods);
            Long newId = goods.getId().longValue();

            if (images != null) {
                int idx = 0;
                for (MultipartFile file : images) {
                    if (file != null && !file.isEmpty()) {
                        GoodsImage gi = new GoodsImage();
                        gi.setGoodId(newId.intValue());
                        gi.setImageData(file.getBytes());
                        gi.setSortOrder(idx++);
                        gi.setCreateTime(LocalDateTime.now());
                        goodsImageMapper.insertSelective(gi);
                    }
                }
            }

            clearGoodsPageCache();
            return newId;
        } catch (IOException e) {
            throw new RuntimeException("图片保存失败", e);
        }
    }

    @Override
    public boolean deleteByIdAndShop(Integer id, Long shopId) {
        int deleted = goodsMapper.deleteByIdAndShop(id, shopId);
        if (deleted > 0) {
            clearGoodsPageCache();
            return true;
        }
        return false;
    }

    @Override
    public int deleteBatchByShop(List<Integer> ids, Long shopId) {
        int deleted = goodsMapper.deleteBatchByShop(ids, shopId);
        if (deleted > 0) {
            clearGoodsPageCache();
        }
        return deleted;
    }

    /**
     * 安全的批量清理 goods:page* 缓存 —— 使用 SCAN 而不是 KEYS，防止阻塞 Redis。
     * 会分批收集并删除，适合线上大 Key 场景。
     */
    public void clearGoodsPageCache() {
        ScanOptions options = ScanOptions.scanOptions().match("goods:page*").count(500).build();
        RedisConnection connection = null;
        Cursor<byte[]> cursor = null;
        try {
            connection = redisTemplate.getConnectionFactory().getConnection();
            cursor = connection.scan(options);
            List<String> batch = new ArrayList<>(200);
            while (cursor.hasNext()) {
                byte[] raw = cursor.next();
                String key = new String(raw, StandardCharsets.UTF_8);
                batch.add(key);
                if (batch.size() >= 100) {
                    redisTemplate.delete(batch);
                    batch.clear();
                }
            }
            if (!batch.isEmpty()) {
                redisTemplate.delete(batch);
            }
        } catch (Exception e) {
            // LOG: 根据你项目的日志框架记录异常（这里用简单打印）
            System.err.println("clearGoodsPageCache error: " + e.getMessage());
        } finally {
            // 关闭 cursor & connection（cursor implements Closeable）
            try {
                if (cursor != null) cursor.close();
            } catch (Exception ignored) {}
            try {
                if (connection != null) connection.close();
            } catch (Exception ignored) {}
        }
    }
}
