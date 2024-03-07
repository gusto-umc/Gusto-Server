package com.umc.gusto.domain.review.repository;

import com.umc.gusto.domain.review.entity.Review;
import com.umc.gusto.domain.store.entity.Store;
import com.umc.gusto.domain.user.entity.User;
import com.umc.gusto.global.common.BaseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    @Query("SELECT r FROM Review r WHERE r.status = 'ACTIVE' AND r.user.publishReview = 'PUBLIC' AND r.store = :store ORDER BY r.liked DESC LIMIT 1")
    Optional<Review> findFirstByStoreOrderByLikedDesc(Store store);
    @Query("SELECT r FROM Review r WHERE r.status = 'ACTIVE' AND r.user.publishReview = 'PUBLIC' AND r.store = :store ORDER BY r.liked DESC, r.reviewId DESC LIMIT 3")
    List<Review> findFirst3ByStoreOrderByLikedDesc(Store store);
    @Query("SELECT r FROM Review r WHERE r.status = 'ACTIVE' AND r.user.publishReview = 'PUBLIC' AND r.store = :store ORDER BY r.liked DESC, r.reviewId DESC LIMIT 4")
    List<Review> findFirst4ByStoreOrderByLikedDesc(Store store);
    @Query("SELECT r FROM Review r WHERE r.status = 'ACTIVE' AND r.user.publishReview = 'PUBLIC' AND r.store = :store ORDER BY r.visitedAt DESC, r.reviewId DESC")
    List<Review> findFirstReviewsByStore(Store store, Pageable pageable);
    @Query("SELECT r FROM Review r WHERE r.status = 'ACTIVE' AND r.user.publishReview = 'PUBLIC' AND r.store = :store AND r.visitedAt <= :visitedAt AND r.reviewId < :reviewId ORDER BY r.visitedAt DESC, r.reviewId DESC")
    List<Review> findReviewsAfterIdByStore(Store store, LocalDate visitedAt, Long reviewId, Pageable pageable);
    boolean existsByReviewIdAndUser(Long reviewId, User user);
    @Query("SELECT count(r.reviewId) FROM Review r WHERE r.status = 'ACTIVE' AND r.store = :store AND r.user.nickname = :nickname")
    Integer countByStoreAndUserNickname(Store store, String nickname);      // 방문횟수는 리뷰 공개여부과 상관 X
    @Query("SELECT count(r.reviewId) FROM Review r WHERE r.status = 'ACTIVE' AND r.store = :store AND r.user = :user")
    Integer countByStoreAndUser(Store store, User user);
    @Query("SELECT r.img1 FROM Review r WHERE r.store.storeId = :storeId ORDER BY r.liked DESC")
    List<String> findTopReviewImageByStoreId(Long storeId);

    Optional<Review> findByReviewIdAndStatus(Long reviewId, BaseEntity.Status status);
    Optional<Page<Review>> findAllByUserAndStatus(User user, BaseEntity.Status status, PageRequest pageRequest);
    Optional<Page<Review>> findAllByUserAndStatusAndReviewIdLessThan(User user, BaseEntity.Status status, Long reviewId,PageRequest pageRequest);
    List<Review> findByUserAndStatusAndVisitedAtBetween(User user, BaseEntity.Status status, LocalDate startDate, LocalDate lastDate);

    boolean existsByUserAndReviewIdLessThan(User user, Long reviewId);
    @Query(value = "SELECT * FROM review r WHERE r.user_id <> :user AND r.status = 'ACTIVE' AND r.skip_check=false ORDER BY RAND() limit 33", nativeQuery = true)
    List<Review> findRandomFeedByUser(@Param("user") UUID user); //WHERE r.user_id <> :userZ

    boolean existsByStoreAndUserNickname(Store store, String nickname);
    boolean existsByStore(Store store);

    //검색 기능
    @Query("SELECT r FROM Review r WHERE r.status = 'ACTIVE' AND r.skipCheck = false AND r.store.storeName like concat('%', :keyword, '%') OR r.comment like concat('%', :keyword, '%')")
    List<Review> searchByStoreContains(String keyword, PageRequest pageRequest); //TODO: 후에 페이징 처리 하기
    @Query("SELECT t.review FROM Tagging t WHERE t.review.status = 'ACTIVE' AND t.review.skipCheck=false AND t.review.store.storeName like concat('%', :keyword, '%') AND t.hashTag.hasTagId = :hashTagId")
    List<Review> searchByStoreAndHashTagContains(String keyword, Long hashTagId, PageRequest pageRequest);
    @Query("SELECT t.review FROM Tagging t WHERE t.review.status = 'ACTIVE' AND t.review.skipCheck=false AND t.hashTag.hasTagId = :hashTagId")
    List<Review> searchByHashTagContains(Long hashTagId, PageRequest pageRequest);
}
