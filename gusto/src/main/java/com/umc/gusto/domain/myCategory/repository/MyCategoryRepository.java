package com.umc.gusto.domain.myCategory.repository;

import com.umc.gusto.domain.myCategory.entity.MyCategory;
import com.umc.gusto.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MyCategoryRepository extends JpaRepository<MyCategory, Long> {
    @Query("SELECT m FROM MyCategory m WHERE m.status = 'ACTIVE' AND m.user.memberStatus = 'ACTIVE' AND m.user.publishCategory = 'PUBLIC' AND m.publishCategory = 'PUBLIC' AND m.user.nickname = :nickname AND m.myCategoryId = :myCategoryId ORDER BY m.myCategoryId DESC")
    Optional<MyCategory> findByMyCategoryPublicIdAndUserNickname(String nickname, Long myCategoryId);
    @Query("SELECT m FROM MyCategory m WHERE m.status = 'ACTIVE' AND m.user.publishCategory = 'PUBLIC' AND m.publishCategory = 'PUBLIC' AND m.user.nickname = :nickname AND m.myCategoryId = :myCategoryId ORDER BY m.myCategoryId DESC")
    Optional<MyCategory> findByMyCategoryIdAndUserNickname(String nickname, Long myCategoryId);
    @Query("SELECT m FROM MyCategory m WHERE m.status = 'ACTIVE' AND m.user.memberStatus = 'ACTIVE' AND m.user.publishCategory = 'PUBLIC' AND m.publishCategory = 'PUBLIC' AND m.user = :user ORDER BY m.myCategoryId DESC")
    Page<MyCategory> findByUserNicknameAndPublishCategoryPublic(User user, Pageable pageable);
    @Query("SELECT m FROM MyCategory m WHERE m.status = 'ACTIVE' AND m.user.memberStatus = 'ACTIVE' AND m.user.publishCategory = 'PUBLIC' AND m.publishCategory = 'PUBLIC' AND m.user = :user AND m.myCategoryId < :myCategoryId ORDER BY m.myCategoryId DESC")
    Page<MyCategory> findByUserNicknameAndPublishCategoryPublicPaging(User user, Long myCategoryId, Pageable pageable);
    @Query("SELECT m FROM MyCategory m WHERE m.status = 'ACTIVE' AND m.user = :user ORDER BY m.myCategoryId DESC")
    Page<MyCategory> findByUserNicknameAndPublishCategory(User user, Pageable pageable);
    @Query("SELECT m FROM MyCategory m WHERE m.status = 'ACTIVE' AND m.user = :user AND m.myCategoryId < :myCategoryId ORDER BY m.myCategoryId DESC")
    Page<MyCategory> findByUserNicknameAndPublishCategoryPaging(User user, Long myCategoryId, Pageable pageable);
    @Query("SELECT m FROM MyCategory m WHERE m.status = 'ACTIVE' AND m.myCategoryName = :myCategoryName AND m.user = :user")
    Optional<MyCategory> findByMyCategoryNameAndUser(String myCategoryName, User user);
    @Query("SELECT m FROM MyCategory m WHERE m.status = 'ACTIVE' AND m.myCategoryId = :myCategoryId AND m.user = :user")
    Optional<MyCategory> findByUserAndMyCategoryId(User user, Long myCategoryId);
    List<MyCategory> findByUser(User user);
}
