package com.gag.RuiwuYuexin.controller;

import com.gag.RuiwuYuexin.common.Result;
import com.gag.RuiwuYuexin.entity.Review;
import com.gag.RuiwuYuexin.service.ReviewService;
import lombok.extern.slf4j.Slf4j;          // <-- 引入 Lombok 日志注解
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/review")
@Slf4j                                 // <-- 启用日志
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/add")
    public Result<Review> add(@RequestBody Review review) {
        try {
            Review saved = reviewService.addReview(review);
            return Result.success(saved);
        } catch (Exception e) {
            // 打印 MyBatisSystemException 自身
            log.error("新增评价失败（MyBatisSystemException）：", e);
            // 如果是 MyBatisSystemException，再把它的底层异常也打印出来
            if (e instanceof org.mybatis.spring.MyBatisSystemException) {
                Throwable root = ((org.mybatis.spring.MyBatisSystemException) e).getRootCause();
                log.error(">>> Root cause of MyBatisSystemException:", root);
            }
            String errMsg = e.getMessage() != null
                    ? e.getMessage()
                    : "服务器内部错误，请稍后重试";
            return Result.error("500", "新增评价失败：" + errMsg);
        }
    }


    @GetMapping("/list")
    public Result<List<Review>> list(@RequestParam Integer goodId) {
        try {
            List<Review> reviews = reviewService.getReviewsByGoodId(goodId);
            return Result.success(reviews);
        } catch (Exception e) {
            log.error("【查询评价失败】goodId={}", goodId, e);
            String errMsg = e.getMessage() != null
                    ? e.getMessage()
                    : "服务器内部错误，请稍后重试";
            return Result.error("500", "查询评价失败：" + errMsg);
        }
    }
}
