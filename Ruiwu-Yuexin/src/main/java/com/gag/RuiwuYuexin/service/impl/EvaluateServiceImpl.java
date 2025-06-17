package com.gag.RuiwuYuexin.service.impl;

import com.gag.RuiwuYuexin.common.Result;
import com.gag.RuiwuYuexin.dto.EvaluationDetailDTO;
import com.gag.RuiwuYuexin.dto.EvaluationImageDTO;
import com.gag.RuiwuYuexin.entity.Evaluate;
import com.gag.RuiwuYuexin.entity.EvaluateImage;
import com.gag.RuiwuYuexin.entity.User;
import com.gag.RuiwuYuexin.mapper.EvaluateImageMapper;
import com.gag.RuiwuYuexin.mapper.EvaluateMapper;
import com.gag.RuiwuYuexin.mapper.UserMapper;
import com.gag.RuiwuYuexin.service.EvaluateService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EvaluateServiceImpl implements EvaluateService {
    private final EvaluateMapper evaluateMapper;
    private final UserMapper userMapper;
    private final EvaluateImageMapper evaluateImageMapper;
    @Override
    public Result<?> submitEvaluate(
            Long userId,
            Long goodId,
            Integer starLevel,
            String comment,
            List<MultipartFile> images
    ) {
        // 1. 校验
        if (starLevel == null || starLevel < 1 || starLevel > 5) {
            return Result.error("评分星级必须是 1 到 5 之间的整数");
        }

        // 2. 保存评价主表
        Evaluate eval = new Evaluate();
        eval.setUserId(userId);
        eval.setGoodId(goodId);
        eval.setStarLevel(starLevel);
        eval.setComment(comment);
        eval.setCreateTime(LocalDateTime.now());
        evaluateMapper.insertEvaluate(eval);  // 会回写 eval.id

        // 3. 保存图片表（如果有）
        if (images != null && !images.isEmpty()) {
            int order = 1;
            for (MultipartFile file : images) {
                try {
                    EvaluateImage img = new EvaluateImage();
                    img.setEvaluateId(eval.getId());
                    img.setImageData(file.getBytes());
                    img.setSortOrder(order++);
                    img.setCreateTime(LocalDateTime.now());
                    evaluateImageMapper.insertImage(img);
                } catch (Exception e) {
                    // 记录日志，继续下一个
                    e.printStackTrace();
                }
            }
        }

        return Result.success("评价提交成功");
    }

    @Override
    public Result<List<EvaluationDetailDTO>> getEvaluationsByGoodId(Long goodId) {
        List<Evaluate> evaluates = evaluateMapper.findEvaluatesByGoodId(goodId);

        if (evaluates == null || evaluates.isEmpty()) {
            return Result.success(Collections.emptyList());
        }
        List<EvaluationDetailDTO> dtos = new ArrayList<>();
        for (Evaluate evaluate : evaluates) {
            User userInfo = userMapper.findById(evaluate.getUserId());
            List<EvaluationImageDTO> images = evaluateMapper.findImagesByEvaluateId(evaluate.getId());
            EvaluationDetailDTO detailDTO = EvaluationDetailDTO.builder()
                    .id(evaluate.getId())
                    .userId(evaluate.getUserId())
                    .goodId(evaluate.getGoodId())
                    .starLevel(evaluate.getStarLevel())
                    .comment(evaluate.getComment())
                    .createTime(evaluate.getCreateTime())
                    .images(images)
                    .build();
            detailDTO.setUserName(userInfo.getUsername());
            detailDTO.setUserAvatar(userInfo.getAvatar());
            dtos.add(detailDTO);
        }
        return Result.success(dtos);
    }
}