package com.gag.RuiwuYuexin.controller;

import com.gag.RuiwuYuexin.common.Result;
import com.gag.RuiwuYuexin.dto.EvaluationDetailDTO;
import com.gag.RuiwuYuexin.service.impl.EvaluateServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class EvaluateController {
    private final EvaluateServiceImpl evaluateService;

    @PostMapping("/evaluate/submit")
    public Result<?> submitEvaluate(
            @RequestParam Long userId,
            @RequestParam Long goodId,
            @RequestParam Integer starLevel,
            @RequestParam(required = false) String comment,
            @RequestPart(name = "images", required = false) List<MultipartFile> images
    ) {
        return evaluateService.submitEvaluate(userId, goodId, starLevel, comment, images);
    }
    @GetMapping("/evaluations/{goodId}")
    public Result<List<EvaluationDetailDTO>> getEvaluationByGoodId(@PathVariable Long goodId) {
        return evaluateService.getEvaluationsByGoodId(goodId);
    }
}
