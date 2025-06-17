package com.gag.RuiwuYuexin.mapper;

import com.gag.RuiwuYuexin.entity.EvaluateImage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EvaluateImageMapper {
    int insertImage(EvaluateImage image);
}
