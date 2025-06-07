package com.gag.RuiwuYuexin.service;

import com.gag.RuiwuYuexin.entity.Review;

import java.util.List;

public interface ReviewService {
    Review addReview(Review review);

    List<Review> getReviewsByGoodId(Integer goodId);
}
