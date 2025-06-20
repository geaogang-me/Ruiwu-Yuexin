package com.gag.RuiwuYuexin.service.impl;

import com.gag.RuiwuYuexin.dto.GoodsDetailDTO;
import com.gag.RuiwuYuexin.entity.Goods;
import com.gag.RuiwuYuexin.entity.GoodsImage;
import com.gag.RuiwuYuexin.mapper.GoodsImageMapper;
import com.gag.RuiwuYuexin.mapper.GoodsMapper;
import com.gag.RuiwuYuexin.service.GoodsService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GoodsServiceImpl implements GoodsService {
    @Autowired
    GoodsMapper goodsMapper;
    @Autowired
    GoodsImageMapper goodsImageMapper;

    @Override
    public Map<String, Object> findGoodsPage(String keyword, String type, int page, int size) {
        int offset = (page - 1) * size;
        List<Goods> list = goodsMapper.findGoodsPage("%" + (keyword == null ? "" : keyword) + "%", type, offset, size);
        int total = goodsMapper.countGoods("%" + (keyword == null ? "" : keyword) + "%", type);

        Map<String, Object> result = new HashMap<>();
        result.put("records", list);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
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
        return updated > 0;
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
            return newId;
        } catch (IOException e) {
            // 可以包装成自定义运行时异常，或直接抛 RuntimeException
            throw new RuntimeException("图片保存失败", e);
        }
    }
    @Override
    public boolean deleteByIdAndShop(Integer id, Long shopId) {
        return goodsMapper.deleteByIdAndShop(id, shopId) > 0;
    }

    @Override
    public int deleteBatchByShop(List<Integer> ids, Long shopId) {
        return goodsMapper.deleteBatchByShop(ids, shopId);
    }
}