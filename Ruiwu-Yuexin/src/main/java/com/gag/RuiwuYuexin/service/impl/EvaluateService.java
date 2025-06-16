package com.gag.RuiwuYuexin.service.impl;

import com.gag.RuiwuYuexin.common.Result;
import com.gag.RuiwuYuexin.dto.EvaluationDetailDTO;
import com.gag.RuiwuYuexin.dto.EvaluationImageDTO;
import com.gag.RuiwuYuexin.entity.Evaluate;
import com.gag.RuiwuYuexin.entity.User;
import com.gag.RuiwuYuexin.mapper.EvaluateMapper;
import com.gag.RuiwuYuexin.mapper.UserMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EvaluateService {
    private final EvaluateMapper evaluateMapper;
    private final UserMapper userMapper;

    public Result<List<EvaluationDetailDTO>> getEvaluationsByGoodId(Long goodId) {
        // 1. 获取商品的所有评价基本信息
        List<Evaluate> evaluates = evaluateMapper.findEvaluatesByGoodId(goodId);

        if (evaluates == null || evaluates.isEmpty()) {
            return Result.success(Collections.emptyList());
        }

        // 2. 构建DTO对象列表
        List<EvaluationDetailDTO> dtos = new ArrayList<>();
        for (Evaluate evaluate : evaluates) {
            // 获取用户信息（您需要实现这个方法）
            User userInfo = userMapper.findById(evaluate.getUserId());
            // 获取每条评价的图片
            List<EvaluationImageDTO> images = evaluateMapper.findImagesByEvaluateId(evaluate.getId());

            // 创建DTO对象
            EvaluationDetailDTO detailDTO = EvaluationDetailDTO.builder()
                    .id(evaluate.getId())
                    .userId(evaluate.getUserId())
                    .goodId(evaluate.getGoodId())
                    .starLevel(evaluate.getStarLevel())
                    .comment(evaluate.getComment())
                    .createTime(evaluate.getCreateTime())
                    .images(images)
                    .build();

            dtos.add(detailDTO);
            detailDTO.setUserName(userInfo.getUsername());
            detailDTO.setUserAvatar(userInfo.getAvatar());
        }

        return Result.success(dtos);
    }
}