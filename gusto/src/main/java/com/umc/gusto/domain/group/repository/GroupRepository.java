package com.umc.gusto.domain.group.repository;

import com.umc.gusto.domain.group.entity.Group;
import com.umc.gusto.global.common.BaseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {
    Optional<Group> findGroupByGroupIdAndStatus(Long groupId, BaseEntity.Status status);
    @Query("SELECT ic.group FROM InvitationCode ic WHERE ic.code = :code AND ic.group.status = :status")
    Optional<Group> findGroupByCodeAndStatus(@Param("code") String code, @Param("status") BaseEntity.Status status);
    @Query("SELECT g FROM Group g WHERE g.groupId IN :groupIds AND g.status = :status ORDER BY g.groupId DESC")
    Page<Group> findGroupsByGroupIdInAndStatus(List<Long> groupIds, BaseEntity.Status status, Pageable pageable);

    @Query("SELECT g FROM Group g WHERE g.groupId IN :groupIds AND g.status = :status AND g.groupId < :lastGroupId ORDER BY g.groupId DESC")
    Page<Group> findGroupsByStatusAndGroupIdInLessThan(List<Long> groupIds, BaseEntity.Status status, Long lastGroupId, Pageable pageable);


    // Hard delete
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Group g WHERE g.status = 'INACTIVE'")
    int deleteAllInActive();

}