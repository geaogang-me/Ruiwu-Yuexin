package com.gag.RuiwuYuexin.service.impl;

import com.gag.RuiwuYuexin.dto.GoodsDetailDTO;
import com.gag.RuiwuYuexin.entity.Goods;
import com.gag.RuiwuYuexin.mapper.GoodsMapper;
import com.gag.RuiwuYuexin.service.GoodsService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GoodsServiceImpl implements GoodsService {
    @Autowired
    GoodsMapper goodsMapper;
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
}
