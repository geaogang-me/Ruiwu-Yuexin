package com.gag.RuiwuYuexin.service;
import com.gag.RuiwuYuexin.dto.GoodsDetailDTO;

import java.util.Map;


public interface GoodsService {
    Map<String, Object> findGoodsPage(String keyword, String type, int page, int size);
    GoodsDetailDTO findGoodsDetail(int id);

}
