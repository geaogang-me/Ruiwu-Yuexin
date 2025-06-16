package com.gag.RuiwuYuexin.mapper;
import com.gag.RuiwuYuexin.dto.EvaluationImageDTO;
import com.gag.RuiwuYuexin.entity.Evaluate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface EvaluateMapper {

    // 获取商品的所有评价（按商品ID查询）
    @Select("SELECT id, user_id, good_id, star_level, evaluate AS comment, create_time " +
            "FROM t_evaluates " +
            "WHERE good_id = #{goodId}")
    List<Evaluate> findEvaluatesByGoodId(Long goodId);

    // 获取评价关联的图片信息（保持不变）
    @Select("SELECT id, TO_BASE64(image_data) AS imageData, sort_order " +
            "FROM t_evaluate_images " +
            "WHERE evaluate_id = #{evaluateId} " +
            "ORDER BY sort_order")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "sortOrder", column = "sort_order"),
            @Result(property = "imageData", column = "imageData")
    })
    List<EvaluationImageDTO> findImagesByEvaluateId(Long evaluateId);
}
