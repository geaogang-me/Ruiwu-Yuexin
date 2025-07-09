package com.gag.RuiwuYuexin.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.gag.RuiwuYuexin.dto.GoodsDetailDTO;
import com.gag.RuiwuYuexin.entity.Goods;
import com.gag.RuiwuYuexin.entity.GoodsImage;
import com.gag.RuiwuYuexin.mapper.GoodsImageMapper;
import com.gag.RuiwuYuexin.mapper.GoodsMapper;
import com.gag.RuiwuYuexin.service.GoodsService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    @Override
    public Map<String, Object> findGoodsPage(String keyword, String type, int page, int size) {
        // 1. 生成缓存 key
        String safeKeyword = (keyword == null ? "" : keyword);
        String safeType    = (type    == null ? "all" : type);
        String cacheKey = String.format("goods:page:%s:%s:%d:%d",
                safeKeyword, safeType, page, size);

        // 2. 尝试从 Redis 读取
        String cachedJson = redisTemplate.opsForValue().get(cacheKey);
        if (cachedJson != null) {
            // 反序列化回 Map<String, Object>
            return JSON.parseObject(cachedJson, new TypeReference<Map<String, Object>>() {});
        }

        // 3. 缓存不命中，查询数据库
        int offset = (page - 1) * size;
        List<Goods> list = goodsMapper.findGoodsPage(
                "%" + safeKeyword + "%", type, offset, size);
        int total = goodsMapper.countGoods("%" + safeKeyword + "%", type);

        Map<String, Object> result = new HashMap<>();
        result.put("records", list);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);

        // 4. 序列化并写入 Redis，设置过期时间（如 30 分钟）
        String jsonToCache = JSON.toJSONString(result);
        redisTemplate.opsForValue()
                .set(cacheKey, jsonToCache, 30, TimeUnit.MINUTES);

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
        // 选做：可以先查询一下，确认这条商品的 belong_shop 与入参一致
        Goods existing = goodsMapper.selectById(goods.getId());
        if (existing == null || !existing.getBelongShop().equals(goods.getBelongShop())) {
            return false;
        }
        goods.setCreateTime(null);
        // 手动设置更新时间
        goods.setUpdateTime(LocalDateTime.now());
        // 调用 Mapper 的选择性更新
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
        // images: byte[] → Base64
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
            // 保存主图到 Goods.goodImage
            if (mainImage != null && !mainImage.isEmpty()) {
                goods.setGoodImage(mainImage.getBytes());
            }
            // 设置创建时间
            goods.setCreateTime(LocalDateTime.now());
            // 插入 goods，主图一起插入
            goodsMapper.insertSelective(goods);
            // 生成的主键ID
            Long newId = goods.getId().longValue();

            // 保存附加图片到 goods_image
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
            // 可以包装成自定义运行时异常，或直接抛 RuntimeException
            throw new RuntimeException("图片保存失败", e);
        }
    }
    @Override
    public boolean deleteByIdAndShop(Integer id, Long shopId) {
        int deleted = goodsMapper.deleteByIdAndShop(id, shopId);
        if (deleted > 0) {
            // 清除分页缓存
            clearGoodsPageCache();
            return true;
        }
        return false;
    }

    @Override
    public int deleteBatchByShop(List<Integer> ids, Long shopId) {
        int deleted = goodsMapper.deleteBatchByShop(ids, shopId);
        if (deleted > 0) {
            // 清除分页缓存
            clearGoodsPageCache();
        }
        return deleted;
    }

    private void clearGoodsPageCache() {
        Set<String> keys = redisTemplate.keys("goods:page*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}