package com.gag.RuiwuYuexin.service;

import com.gag.RuiwuYuexin.common.Result;
import com.gag.RuiwuYuexin.dto.EvaluationDetailDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface EvaluateService {
    Result<?> submitEvaluate(
            Long userId,
            Long goodId,
            Integer starLevel,
            String comment,
            List<MultipartFile> images
    );
    Result<List<EvaluationDetailDTO>> getEvaluationsByGoodId(Long goodId);
}
