package com.umc.gusto.domain.group.controller;

import com.umc.gusto.domain.group.model.request.*;
import com.umc.gusto.domain.group.model.response.*;
import com.umc.gusto.domain.group.service.GroupService;
import com.umc.gusto.domain.user.entity.User;
import com.umc.gusto.global.auth.model.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/groups")
public class GroupController {
    private final GroupService groupService;

    /**
     * 그룹 생성
     * [POST] /groups
     */
    @PostMapping
    public ResponseEntity<?> createGroup(@AuthenticationPrincipal AuthUser authUser, @RequestBody PostGroupRequest postGroupRequest){
        User owner = authUser.getUser();
        groupService.createGroup(owner, postGroupRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 그룹 1건 조회
     * [GET] /groups/{groupId}
     */
    @GetMapping("/{groupId}")
    public ResponseEntity<GetGroupResponse> getGroup(@PathVariable Long groupId){
        GetGroupResponse getGroup = groupService.getGroup(groupId);
        return ResponseEntity.status(HttpStatus.OK).body(getGroup);
    }

    /**
     * 그룹 수정
     * [PATCH] /groups/{groupId}
     */
    @PatchMapping("/{groupId}")
    public ResponseEntity<UpdateGroupResponse> updateGroup(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long groupId, @RequestBody UpdateGroupRequest updateGroupRequest){
        User owner = authUser.getUser();
        UpdateGroupResponse updatedGroup = groupService.updateGroup(owner, groupId, updateGroupRequest);
        return ResponseEntity.status(HttpStatus.OK).body(updatedGroup);
    }

    /**
     * 그룹 삭제
     * [DELETE] /groups/{groupId}
     */
    @DeleteMapping("/{groupId}")
    public ResponseEntity<?> deleteGroup(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long groupId){
        User owner = authUser.getUser();
        groupService.deleteGroup(owner, groupId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * 그룹리스트 추가 = 그룹에서 찜
     * [POST] /groups/{groupId}/groupList
     */
    @PostMapping("/{groupId}/groupList")
    public ResponseEntity<?> createGroupList(
            @AuthenticationPrincipal AuthUser authUser,@PathVariable Long groupId, @RequestBody GroupListRequest request){
        User user = authUser.getUser();
        groupService.createGroupList(groupId,request,user);
        return ResponseEntity.status(HttpStatus.CREATED).build();
     }

    /**
     * 초대 코드 조회
     * [GET] /groups/{groupId}/invitationCode
     */
    @GetMapping("/{groupId}/invitationCode")
    public ResponseEntity<GetInvitationCodeResponse> getInvitationCode(@PathVariable Long groupId) {
        GetInvitationCodeResponse getInvitationCode = groupService.getInvitationCode(groupId);
        return ResponseEntity.status(HttpStatus.OK).body(getInvitationCode);
    }

    /**
     * 그룹 소유권 이전
     * [PATCH] /groups/{groupId}/transfer-ownership
     */
    @PatchMapping("/{groupId}/transfer-ownership")
    public ResponseEntity<TransferOwnershipResponse> transferOwnership(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long groupId, @RequestBody TransferOwnershipRequest transferOwnershipRequest) {
        User owner = authUser.getUser();
        TransferOwnershipResponse transferOwnership = groupService.transferOwnership(owner, groupId, transferOwnershipRequest);
        return ResponseEntity.status(HttpStatus.OK).body(transferOwnership);
    }

    /**
     * 그룹 참여
     * [POST] /groups/join
     */
    @PostMapping("/join")
    public ResponseEntity<?> joinGroup(@AuthenticationPrincipal AuthUser authUser, @RequestBody JoinGroupRequest joinGroupRequest){
        User user = authUser.getUser();
        groupService.joinGroup(user, joinGroupRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 그룹 리스트 삭제
     * [DELETE] /groups/groupLists?groupListId=1
     */
    @DeleteMapping("/groupLists")
    public ResponseEntity<?> deleteGroupList(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(name = "groupListId") List<Long> groupListId){
        User user = authUser.getUser();
        groupService.deleteGroupList(groupListId,user);
        return ResponseEntity.ok().build();
    }

    /**
     * 그룹 리스트 조회 == 그룹 내 찜한 식당 정보
     * [GET] /groups/{groupId}/groupLists
     */
    @GetMapping("/{groupId}/groupLists")
    public ResponseEntity<PagingResponse> getGroupList
    (@PathVariable Long groupId, @RequestParam(required = false, name = "groupListId") Long groupListId,@AuthenticationPrincipal AuthUser authUser ){
        User user = authUser.getUser();
        return ResponseEntity.ok().body(groupService.getAllGroupList(groupId, groupListId,user));
    }

      
     /** 그룹 탈퇴
     * [DELETE] /groups/{groupId}/leave
     */
    @DeleteMapping("/{groupId}/leave")
    public ResponseEntity<?> leaveGroup(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long groupId){
        User user = authUser.getUser();
        groupService.leaveGroup(user, groupId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
  
    /**
     * 그룹 목록 조회
     * [GET] /groups?lastGroupId={lastGroupId}
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getGroups(@AuthenticationPrincipal AuthUser authUser,
                                                         @RequestParam(name = "lastGroupId", required = false) Long lastGroupId,
                                                         @RequestParam(name = "size", defaultValue = "5") int size)
    {
        User user = authUser.getUser();
        Map<String, Object> getGroups = groupService.getUserGroups(user, lastGroupId, size);
        return ResponseEntity.status(HttpStatus.OK).body(getGroups);
    }

    /**
     * 그룹 구성원 조회
     * [GET] /groups/{groupId}/members?lastMemberId={lastMemberId}
     */
    @GetMapping("/{groupId}/members")
    public ResponseEntity<Map<String, Object>> getGroupMembers (@PathVariable Long groupId,
                                                                         @RequestParam(name = "lastMemberId", required = false) Long lastMemberId,
                                                                         @RequestParam(name = "size", defaultValue = "10") int size){
        Map<String, Object> getGroupMembers = groupService.getGroupMembers(groupId, lastMemberId, size);
        return ResponseEntity.status(HttpStatus.OK).body(getGroupMembers);
    }

    /**
     * 초대 코드로 그룹 정보 조회
     * [POST] /groups/check-invitation
     */
    @PostMapping("/check-invitation")
    public ResponseEntity<GetPreJoinGroupInfoResponse> getPreJoinGroupInfo(@RequestBody JoinGroupRequest joinGroupRequest){
        GetPreJoinGroupInfoResponse getPreJoinGroupInfo = groupService.getPreJoinGroupInfo(joinGroupRequest);
        return ResponseEntity.status(HttpStatus.OK).body(getPreJoinGroupInfo);
    }

    /**
     * 그룹 루트 단일값 삭제
     * [DELETE] /groups/routes/{routeId}??groupId={groupId}
     */
    @DeleteMapping("/routes/{routeId}")
    public ResponseEntity<?> getDeleteGroupRoute
    (@PathVariable Long routeId,
     @AuthenticationPrincipal AuthUser authUser,
     @RequestParam Long groupId
    ){
        groupService.deleteRoute(routeId,authUser.getUser(),groupId);
        return ResponseEntity.ok(HttpStatus.OK);
    }
}
