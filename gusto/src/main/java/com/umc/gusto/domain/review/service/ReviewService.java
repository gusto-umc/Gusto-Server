package com.umc.gusto.domain.review.service;

import com.umc.gusto.domain.review.model.request.CreateReviewRequest;
import com.umc.gusto.domain.review.model.request.UpdateReviewRequest;
import com.umc.gusto.domain.review.model.response.ReviewDetailResponse;
import com.umc.gusto.domain.user.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ReviewService {
    void validateReviewByUser(final User user, final Long reviewId);
    void createReview(User user, List<MultipartFile> images, CreateReviewRequest createReviewRequest);
    void updateReview(Long reviewId, List<MultipartFile> images, UpdateReviewRequest updateReviewRequest);
    void deleteReview(User user, Long reviewId);
    ReviewDetailResponse getReview(Long reviewId);
    void likeReview(User user, Long reviewId);
    void unlikeReview(User user, Long reviewId);
    void hardDeleteAllSoftDeleted();
}
