package com.gag.RuiwuYuexin.mapper;

import com.gag.RuiwuYuexin.entity.Review;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
@Mapper
public interface ReviewMapper {
    int insert(Review review);

    // 根据商品ID查询评价列表（按时间倒序）
    List<Review> selectByGoodId(@Param("goodId") Integer goodId);
}
