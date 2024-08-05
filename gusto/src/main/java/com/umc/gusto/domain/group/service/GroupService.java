package com.umc.gusto.domain.group.service;

import com.umc.gusto.domain.group.model.request.*;
import com.umc.gusto.domain.group.model.response.*;
import com.umc.gusto.domain.user.entity.User;

import java.util.List;
import java.util.Map;

public interface GroupService {
    // 그룹 생성
    void createGroup(User owner, PostGroupRequest postGroupRequest);

    // 그룹 1건 조회
    GetGroupResponse getGroup(Long groupId);

    // 그룹 수정
    UpdateGroupResponse updateGroup(User owner, Long groupId, UpdateGroupRequest updateGroupRequest);

    // 그룹 삭제
    void deleteGroup(User owner, Long groupId);

    // 그룹리스트 추가
    void createGroupList(Long groupId,GroupListRequest request,User user);

    // 그룹리스트 삭제
    void deleteGroupList(List<Long> groupListId, User user);

    // 그룹리스트 조회
    PagingResponse getAllGroupList(Long groupId, Long groupListId,User user);

    // 그룹 초대 코드 조회
    GetInvitationCodeResponse getInvitationCode(Long groupId);

    // 그룹 소유권 이전
    TransferOwnershipResponse transferOwnership(User owner, Long groupId, TransferOwnershipRequest transferOwnershipRequest);

    // 그룹 참여
    void joinGroup(User user, JoinGroupRequest joinGroupRequest);

    // 그룹 탈퇴
    void leaveGroup(User user, Long groupId);

    // 그룹 목록 조회
    Map<String, Object> getUserGroups(User user, Long lastGroupId, int size);

    //그룹 구성원 조회
    Map<String, Object> getGroupMembers(Long groupId, Long lastMemberId, int size);

    // 초대 코드로 그룹 정보 조회
    GetPreJoinGroupInfoResponse getPreJoinGroupInfo(JoinGroupRequest joinGroupRequest);

    //그룹 루트 삭제
    void deleteRoute(Long routeId, User user, Long groupId);

    //
    void hardDeleteAllSoftDeleted();

}
