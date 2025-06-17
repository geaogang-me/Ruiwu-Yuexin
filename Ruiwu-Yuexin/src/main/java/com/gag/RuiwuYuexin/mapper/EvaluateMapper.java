package com.gag.RuiwuYuexin.mapper;

import com.gag.RuiwuYuexin.entity.Evaluate;
import com.gag.RuiwuYuexin.dto.EvaluationImageDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
@Mapper
public interface EvaluateMapper {
    int insertEvaluate(Evaluate evaluate);
    List<Evaluate> findEvaluatesByGoodId(Long goodId);
    List<EvaluationImageDTO> findImagesByEvaluateId(Long evaluateId);
}