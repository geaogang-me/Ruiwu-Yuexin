package com.gag.RuiwuYuexin.controller;

import com.gag.RuiwuYuexin.common.Result;
import com.gag.RuiwuYuexin.dto.EvaluationDetailDTO;
import com.gag.RuiwuYuexin.service.impl.EvaluateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class EvaluateController {
    private final EvaluateService evaluateService;

    @GetMapping("/evaluations/{goodId}")
    public Result<List<EvaluationDetailDTO>> getEvaluationByGoodId(@PathVariable Long goodId) {
        return evaluateService.getEvaluationsByGoodId(goodId);
    }
}
