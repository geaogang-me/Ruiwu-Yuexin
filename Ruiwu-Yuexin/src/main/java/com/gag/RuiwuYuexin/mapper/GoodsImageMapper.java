package com.gag.RuiwuYuexin.mapper;
import com.gag.RuiwuYuexin.entity.GoodsImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GoodsImageMapper {
    List<GoodsImage> selectBygoodId(@Param("goodId") int goodId);

    int insertSelective(GoodsImage record);
}
