package com.gag.RuiwuYuexin.service.impl;

import com.gag.RuiwuYuexin.entity.Review;
import com.gag.RuiwuYuexin.mapper.ReviewMapper;
import com.gag.RuiwuYuexin.service.ReviewService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewServiceImpl implements ReviewService {
    private final ReviewMapper mapper;

    public ReviewServiceImpl(ReviewMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Review addReview(Review review) {
        mapper.insert(review);
        return review;
    }

    @Override
    public List<Review> getReviewsByGoodId(Integer goodId) {
        return mapper.selectByGoodId(goodId);
    }
}