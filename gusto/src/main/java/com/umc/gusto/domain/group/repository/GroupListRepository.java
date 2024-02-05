package com.umc.gusto.domain.group.repository;

import com.umc.gusto.domain.group.entity.Group;
import com.umc.gusto.domain.group.entity.GroupList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupListRepository extends JpaRepository<GroupList, Long>  {
    List<GroupList> findGroupListsByGroup(Group group);
}
