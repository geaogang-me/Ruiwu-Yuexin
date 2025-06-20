package com.gag.RuiwuYuexin.service;
import com.gag.RuiwuYuexin.dto.GoodsDetailDTO;
import com.gag.RuiwuYuexin.entity.Goods;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;


public interface GoodsService {
    Map<String, Object> findGoodsPage(String keyword, String type, int page, int size);
    boolean updateGoods(Goods goods);
    Map<String,Object> findGoodsPageByShop(Long shopId,Integer status, int page, int size);
    GoodsDetailDTO findGoodsDetail(int id);

    public Long createGoodWithImages(Goods goods,
                                     MultipartFile mainImage,
                                     List<MultipartFile> images);

    boolean deleteByIdAndShop(Integer id, Long shopId);
    int deleteBatchByShop(List<Integer> ids, Long shopId);

}
