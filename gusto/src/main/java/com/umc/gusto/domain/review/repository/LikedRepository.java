package com.umc.gusto.domain.review.repository;

import com.umc.gusto.domain.review.entity.Liked;
import com.umc.gusto.domain.review.entity.Review;
import com.umc.gusto.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikedRepository extends JpaRepository<Liked, Long> {
    Optional<Liked> findByUserAndReview(User user, Review review);
    boolean existsByUserAndReview(User user, Review review);

}
