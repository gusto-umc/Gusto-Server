package com.umc.gusto.domain.review.repository;

import com.umc.gusto.domain.review.entity.Review;
import com.umc.gusto.domain.store.entity.Store;
import com.umc.gusto.domain.user.entity.User;
import com.umc.gusto.global.common.BaseEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    @Query("SELECT r FROM Review r WHERE r.user.memberStatus = 'ACTIVE' AND r.status = 'ACTIVE' AND r.user.publishReview = 'PUBLIC' AND r.publishReview = 'PUBLIC' AND r.store = :store ORDER BY r.liked DESC LIMIT 1")
    Optional<Review> findFirstByStoreOrderByLikedDesc(@Param("store") Store store);
    @Query("SELECT r FROM Review r WHERE r.user.memberStatus = 'ACTIVE' AND  r.status = 'ACTIVE' AND r.user.publishReview = 'PUBLIC' AND r.publishReview = 'PUBLIC' AND r.store = :store ORDER BY r.liked DESC, r.reviewId DESC LIMIT 3") //
    List<Review> findFirst3ByStoreOrderByLikedDesc(@Param("store") Store store);
    @Query("SELECT r FROM Review r WHERE r.user.memberStatus = 'ACTIVE' AND r.status = 'ACTIVE' AND r.user.publishReview = 'PUBLIC' AND r.publishReview = 'PUBLIC' AND r.store = :store ORDER BY r.liked DESC, r.reviewId DESC LIMIT 4") //
    List<Review> findFirst4ByStoreOrderByLikedDesc(@Param("store") Store store);
    @Query("SELECT r FROM Review r WHERE r.user.memberStatus = 'ACTIVE' AND  r.status = 'ACTIVE' AND r.user.publishReview = 'PUBLIC' AND r.publishReview = 'PUBLIC' AND r.store = :store ORDER BY r.visitedAt DESC, r.reviewId DESC")
    Page<Review> findFirstReviewsByStore(@Param("store") Store store, Pageable pageable);
    @Query("SELECT r FROM Review r WHERE r.user.memberStatus = 'ACTIVE' AND r.status = 'ACTIVE' AND r.user.publishReview = 'PUBLIC' AND r.publishReview = 'PUBLIC'AND r.store = :store AND r.visitedAt <= :visitedAt AND r.reviewId < :reviewId ORDER BY r.visitedAt DESC, r.reviewId DESC")
    Page<Review> findReviewsAfterIdByStore(@Param("store") Store store, @Param("visitedAt") LocalDate visitedAt, @Param("reviewId") Long reviewId, Pageable pageable);
    boolean existsByReviewIdAndUser(Long reviewId, User user);
    @Query("SELECT count(r.reviewId) FROM Review r WHERE r.user.memberStatus = 'ACTIVE' AND r.status = 'ACTIVE' AND r.store = :store AND r.user.nickname = :nickname")
    Integer countByStoreAndUserNickname(Store store, String nickname);      // 방문횟수는 리뷰 공개여부과 상관 X
    @Query("SELECT count(r.reviewId) FROM Review r WHERE r.status = 'ACTIVE' AND r.store = :store AND r.user = :user")
    Integer countByStoreAndUser(Store store, User user);

    Optional<Review> findByReviewIdAndStatus(Long reviewId, BaseEntity.Status status);
    Optional<Page<Review>> findAllByUserAndStatus(User user, BaseEntity.Status status, PageRequest pageRequest);
    List<Review> findByUserAndStatusAndVisitedAtBetween(User user, BaseEntity.Status status, LocalDate startDate, LocalDate lastDate);

    @Query(value = "SELECT * FROM review r join user u on r.user_id = u.user_id  WHERE r.user_id <> :user AND u.publish_review = 'PUBLIC' AND r.status = 'ACTIVE' AND r.publish_review = 'PUBLIC' AND r.skip_check=false ORDER BY RAND() limit 33", nativeQuery = true)
    List<Review> findRandomFeedByUser(@Param("user") UUID user); //WHERE r.user_id <> :userZ

    boolean existsByStoreAndUserNickname(Store store, String nickname);
  
    /*
        검색 관련
     */
    @Query("SELECT r FROM Review r WHERE r.status = 'ACTIVE' AND r.publishReview = 'PUBLIC' AND r.skipCheck = false " +
            "AND r.reviewId < :cursorId AND r.store.storeName like concat('%', :keyword, '%') OR r.comment like concat('%', :keyword, '%')" +
            "ORDER BY r.reviewId desc")
    Page<Review> searchByStoreContains(String keyword, Long cursorId, PageRequest pageRequest); //TODO: 후에 페이징 처리 하기
    @Query("SELECT t.review FROM Tagging t WHERE t.review.status = 'ACTIVE' AND t.review.publishReview = 'PUBLIC' AND t.review.skipCheck=false " +
            "AND t.review.reviewId < :cursorId AND t.review.store.storeName like concat('%', :keyword, '%') AND t.hashTag.hasTagId = :hashTagId" +
            " ORDER BY t.review.reviewId desc")
    Page<Review> searchByStoreAndHashTagContains(String keyword, Long hashTagId, Long cursorId, PageRequest pageRequest);
    @Query("SELECT t.review FROM Tagging t WHERE t.review.status = 'ACTIVE' AND t.review.publishReview = 'PUBLIC' AND t.review.skipCheck=false " +
            "AND t.review.reviewId < :cursorId AND t.hashTag.hasTagId = :hashTagId ORDER BY t.review.reviewId desc")
    Page<Review> searchByHashTagContains(Long hashTagId, Long cursorId, PageRequest pageRequest);

    /*
        리뷰 모아보기 페이징 처리
     */
    @Query("select r from Review r where r.user = :user and r.status = 'ACTIVE' and r.publishReview = 'PUBLIC'")
    Page<Review> pagingInstaViewNoCursor(User user, PageRequest pageRequest);
    @Query("select r from Review r where r.user = :user and r.status = 'ACTIVE' and r.publishReview = 'PUBLIC' " +
            "and r.visitedAt < :visitedAt or (r.visitedAt = :visitedAt and r.reviewId < :reviewId)")
    Page<Review> pagingInstaView(User user, Long reviewId, LocalDate visitedAt,PageRequest pageRequest);

    @Query("select r from Review r where r.user = :user and r.status = 'ACTIVE'" +
            "and r.visitedAt < :visitedAt or (r.visitedAt = :visitedAt and r.reviewId < :reviewId)")
    Page<Review> pagingMyReview(User user, Long reviewId, LocalDate visitedAt,PageRequest pageRequest);

    // Hard delete
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Review r WHERE r.status = 'INACTIVE'")
    int deleteAllInActive();
}
