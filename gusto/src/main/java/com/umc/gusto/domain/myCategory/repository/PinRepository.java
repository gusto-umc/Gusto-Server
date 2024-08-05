package com.umc.gusto.domain.myCategory.repository;

import com.umc.gusto.domain.myCategory.entity.MyCategory;
import com.umc.gusto.domain.myCategory.entity.Pin;
import com.umc.gusto.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PinRepository extends JpaRepository<Pin, Long> {
    @Query("SELECT p FROM Pin p " +
            "WHERE p.myCategory = :myCategory " +
            "AND p.store.town.townName = :townName " +
            "ORDER BY p.pinId DESC")
    Page<Pin> findPinsByMyCategoryAndTownNameAndPinIdDESCFirstPaging(MyCategory myCategory, String townName, Pageable pageable);

    @Query("SELECT p FROM Pin p " +
            "WHERE p.myCategory = :myCategory " +
            "AND p.store.town.townName = :townName " +
            "ORDER BY p.pinId ASC")
    Page<Pin> findPinsByMyCategoryAndTownNameAndPinIdASCFirstPaging(MyCategory myCategory, String townName, Pageable pageable);

    @Query("SELECT p FROM Pin p " +
            "WHERE p.myCategory = :myCategory " +
            "AND p.store.town.townName = :townName " +
            "ORDER BY p.store.storeName DESC, p.pinId DESC")
    Page<Pin> findPinsByMyCategoryAndTownNameAndStoreNameDESCFirstPaging(MyCategory myCategory, String townName, Pageable pageable);

    @Query("SELECT p FROM Pin p " +
            "WHERE p.myCategory = :myCategory " +
            "AND p.store.town.townName = :townName " +
            "ORDER BY p.store.storeName ASC, p.pinId DESC")
    Page<Pin> findPinsByMyCategoryAndTownNameAndStoreNameASCFirstPaging(MyCategory myCategory, String townName, Pageable pageable);
    @Query("SELECT p FROM Pin p " +
            "WHERE p.myCategory = :myCategory " +
            "AND p.store.town.townName = :townName " +
            "ORDER BY p.pinId DESC")
    List<Pin> findPinsByMyCategoryAndTownNameAndPinIdDESC(MyCategory myCategory, String townName);
    @Query("SELECT p FROM Pin p " +
            "WHERE p.myCategory = :myCategory " +
            "AND p.store.town.townName = :townName " +
            "AND p.pinId < :pinId " +
            "ORDER BY p.pinId DESC")
    Page<Pin> findPinsByMyCategoryAndTownNameAndPinIdDESCPaging(MyCategory myCategory, String townName, Long pinId, Pageable pageable);

    @Query("SELECT p FROM Pin p " +
            "WHERE p.myCategory = :myCategory " +
            "AND p.store.town.townName = :townName " +
            "AND p.pinId > :pinId " +
            "ORDER BY p.pinId ASC")
    Page<Pin> findPinsByMyCategoryAndTownNameAndPinIdASCPaging(MyCategory myCategory, String townName, Long pinId, Pageable pageable);
    @Query("SELECT p FROM Pin p " +
            "WHERE p.myCategory = :myCategory " +
            "AND p.store.town.townName = :townName " +
            "AND (p.store.storeName < :storeName " +
            "OR (p.store.storeName = :storeName AND p.pinId < :pinId)) " +
            "ORDER BY p.store.storeName DESC, p.pinId DESC")
    Page<Pin> findPinsByMyCategoryAndTownNameAndStoreNameDESCPaging(MyCategory myCategory, String townName, Long pinId, String storeName, Pageable pageable);
    @Query("SELECT p FROM Pin p " +
            "WHERE p.myCategory = :myCategory " +
            "AND p.store.town.townName = :townName " +
            "AND (p.store.storeName > :storeName " +
            "OR (p.store.storeName = :storeName AND p.pinId < :pinId)) " +
            "ORDER BY p.store.storeName ASC, p.pinId DESC")
    Page<Pin> findPinsByMyCategoryAndTownNameAndStoreNameASCPaging(MyCategory myCategory, String townName, Long pinId, String storeName, Pageable pageable);
    @Query("SELECT p FROM Pin p " +
            "WHERE p.myCategory = :myCategory " +
            "ORDER BY p.pinId DESC")
    Page<Pin> findPinsByMyCategoryAndPinIdDESCFirstPaging(MyCategory myCategory, Pageable pageable);
    @Query("SELECT p FROM Pin p " +
            "WHERE p.myCategory = :myCategory " +
            "ORDER BY p.pinId ASC")
    Page<Pin> findPinsByMyCategoryAndPinIdASCFirstPaging(MyCategory myCategory, Pageable pageable);
    @Query("SELECT p FROM Pin p " +
            "WHERE p.myCategory = :myCategory " +
            "ORDER BY p.store.storeName DESC, p.pinId DESC")
    Page<Pin> findPinsByMyCategoryAndStoreNameDESCFirstPaging(MyCategory myCategory, Pageable pageable);
    @Query("SELECT p FROM Pin p " +
            "WHERE p.myCategory = :myCategory " +
            "ORDER BY p.store.storeName ASC, p.pinId DESC")
    Page<Pin> findPinsByMyCategoryAndStoreNameASCFirstPaging(MyCategory myCategory, Pageable pageable);
    @Query("SELECT p FROM Pin p " +
            "WHERE p.myCategory = :myCategory " +
            "ORDER BY p.pinId DESC")
    List<Pin> findPinsByMyCategoryAndPinIdDESC(MyCategory myCategory);
    @Query("SELECT p FROM Pin p " +
            "WHERE p.myCategory = :myCategory " +
            "AND p.pinId < :pinId " +
            "ORDER BY p.pinId DESC")
    Page<Pin> findPinsByMyCategoryAndPinIdDESCPaging(MyCategory myCategory, Long pinId, Pageable pageable);
    @Query("SELECT p FROM Pin p " +
            "WHERE p.myCategory = :myCategory " +
            "AND p.pinId > :pinId " +
            "ORDER BY p.pinId ASC")
    Page<Pin> findPinsByMyCategoryAndPinIdASCPaging(MyCategory myCategory, Long pinId, Pageable pageable);
    @Query("SELECT p FROM Pin p " +
            "WHERE p.myCategory = :myCategory " +
            "AND (p.store.storeName < :storeName " +
            "OR (p.store.storeName = :storeName AND p.pinId < :pinId)) " +
            "ORDER BY p.store.storeName DESC, p.pinId DESC")
    Page<Pin> findPinsByMyCategoryAndStoreNameDESCPaging(MyCategory myCategory, Long pinId, String storeName, Pageable pageable);
    @Query("SELECT p FROM Pin p " +
            "WHERE p.myCategory = :myCategory " +
            "AND (p.store.storeName > :storeName " +
            "OR (p.store.storeName = :storeName AND p.pinId < :pinId)) " +
            "ORDER BY p.store.storeName ASC, p.pinId DESC"
    )
    Page<Pin> findPinsByMyCategoryAndStoreNameASCPaging(MyCategory myCategory, Long pinId, String storeName, Pageable pageable);
    @Query("SELECT p FROM Pin p " +
            "WHERE p.user = :user " +
            "AND p.myCategory.myCategoryId = :myCategoryId " +
            "AND p.store.town.townName = :townName " +
            "ORDER BY p.pinId DESC")
    List<Pin> findPinsByUserAndMyCategoryIdAndTownNameAndPinIdDESC(User user, Long myCategoryId, String townName);
    Optional<Pin> findByUserAndPinId(User user, Long pinId);
    boolean existsByUserAndStoreStoreId(User user, Long storeId);       // 존재 여부
    @Query("SELECT p.store.storeId FROM Pin p WHERE p.user = :user AND p.myCategory.myCategoryId = :myCategoryId AND p.user.publishCategory = 'PUBLIC'")
    List<Long> findStoreIdsByUserAndMyCategoryId(User user, Long myCategoryId);
    @Query("SELECT p.store.storeId FROM Pin p WHERE p.user = :user")
    List<Long> findStoreIdsByUser(User user);
    @Query("SELECT p.pinId FROM Pin p WHERE p.user = :user AND p.store.storeId = :storeId")
    Long findByUserAndStoreStoreId(User user, Long storeId);
    @Query("SELECT p FROM Pin p " +
            "JOIN p.store s " +
            "JOIN s.town t " +
            "JOIN p.user u " +
            "WHERE p.user = :user " +
            "AND t.townName = :townName " +
            "ORDER BY p.pinId DESC")
    List<Pin> findPinsByUserAndTownNameAndPinIdDESC(User user, String townName);

}
