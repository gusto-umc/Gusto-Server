package com.umc.gusto.domain.group.service;

import com.umc.gusto.domain.group.entity.Group;
import com.umc.gusto.domain.group.entity.GroupList;
import com.umc.gusto.domain.group.entity.GroupMember;
import com.umc.gusto.domain.group.model.request.GroupListRequest;
import com.umc.gusto.domain.group.entity.InvitationCode;
import com.umc.gusto.domain.group.model.request.JoinGroupRequest;
import com.umc.gusto.domain.group.model.request.PostGroupRequest;
import com.umc.gusto.domain.group.model.request.TransferOwnershipRequest;
import com.umc.gusto.domain.group.model.request.UpdateGroupRequest;
import com.umc.gusto.domain.group.model.response.*;
import com.umc.gusto.domain.group.repository.GroupListRepository;
import com.umc.gusto.domain.group.repository.GroupMemberRepository;
import com.umc.gusto.domain.group.repository.GroupRepository;
import com.umc.gusto.domain.review.entity.Review;
import com.umc.gusto.domain.review.repository.ReviewRepository;
import com.umc.gusto.domain.route.entity.Route;
import com.umc.gusto.domain.store.entity.Store;
import com.umc.gusto.domain.store.repository.StoreRepository;
import com.umc.gusto.domain.group.repository.InvitationCodeRepository;
import com.umc.gusto.domain.route.repository.RouteRepository;
import com.umc.gusto.domain.user.entity.User;
import com.umc.gusto.domain.user.repository.UserRepository;
import com.umc.gusto.global.common.BaseEntity;
import com.umc.gusto.global.exception.Code;
import com.umc.gusto.global.exception.GeneralException;
import com.umc.gusto.global.exception.customException.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupServiceImpl implements GroupService{
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final InvitationCodeRepository invitationCodeRepository;
    private final GroupListRepository groupListRepository;
    private final RouteRepository routeRepository;
    private static final int INVITE_CODE_LENGTH = 12;
    private final StoreRepository storeRepository;
    private final ReviewRepository reviewRepository;
    private static final int GROUP_LIST_FIRST_PAGE = 8;
    private static final int GROUP_LIST_PAGE = 6;


    public void createGroup(User owner, PostGroupRequest postGroupRequest){
        // 그룹
        Group group = Group.builder()
                .groupName(postGroupRequest.getGroupName())
                .groupScript(postGroupRequest.getGroupScript())
                .owner(owner)
                .notice("멤버들에게 새로운 공지사항을 공유해보세요!")
                .build();
        Group savedGroup = groupRepository.save(group);

        // 그룹 멤버
        GroupMember ownerMember = GroupMember.builder()
                .group(savedGroup)
                .user(owner)
                .build();
        groupMemberRepository.save(ownerMember);

        // 초대 코드
        String code = RandomStringUtils.randomAlphanumeric(INVITE_CODE_LENGTH);
        InvitationCode invitationCode = InvitationCode.builder()
                .group(group)
                .code(code)
                .build();
        invitationCodeRepository.save(invitationCode);
    }

    @Transactional(readOnly = true)
    public GetGroupResponse getGroup(Long groupId){
        Group group = groupRepository.findGroupByGroupIdAndStatus(groupId, BaseEntity.Status.ACTIVE)
                .orElseThrow(()->new GeneralException(Code.FIND_FAIL_GROUP));
        Long ownerMemberId = groupMemberRepository.findGroupMemberIdByGroupAndUser(group, group.getOwner());
        List<GetGroupMemberResponse> groupMembersDto = groupMemberRepository.findGroupMembersByGroup(group).stream()
                .map(groupMember -> GetGroupMemberResponse.builder()
                        .groupMemberId(groupMember.getGroupMemberId())
                        .nickname(groupMember.getUser().getNickname())
                        .profileImg(groupMember.getUser().getProfileImage())
                        .build())
                .collect(Collectors.toList());
        return GetGroupResponse.builder()
                .groupId(group.getGroupId())
                .groupName(group.getGroupName())
                .groupScript(group.getGroupScript())
                .owner(ownerMemberId)
                .notice(group.getNotice())
                .groupMembers(groupMembersDto)
                .build();
    }

    public UpdateGroupResponse updateGroup(User owner, Long groupId, UpdateGroupRequest updateGroupRequest){
        Group group = groupRepository.findGroupByGroupIdAndStatus(groupId, BaseEntity.Status.ACTIVE)
                .orElseThrow(()->new GeneralException(Code.FIND_FAIL_GROUP));
        Long ownerMemberId = groupMemberRepository.findGroupMemberIdByGroupAndUser(group, group.getOwner());

        // 그룹 이름 수정 (owner만 수정 가능)
        if(updateGroupRequest.getGroupName() != null){
            if(group.getOwner().getUserId().equals(owner.getUserId())){
                group.updateGroupName(updateGroupRequest.getGroupName());
            } else{
                throw new GeneralException(Code.UNAUTHORIZED_MODIFY_GROUP_NAME);
            }
        }

        //그룹 공지 수정
        if(updateGroupRequest.getNotice() != null){
            group.updateNotice(updateGroupRequest.getNotice());
        }

        groupRepository.save(group);
        return UpdateGroupResponse.builder()
                .groupId(group.getGroupId())
                .groupName(group.getGroupName())
                .groupScript(group.getGroupScript())
                .owner(ownerMemberId)
                .notice(group.getNotice())
                .build();
    }

    public void deleteGroup(User owner, Long groupId){
        Group group = groupRepository.findGroupByGroupIdAndStatus(groupId, BaseEntity.Status.ACTIVE)
                .orElseThrow(()->new GeneralException(Code.FIND_FAIL_GROUP));

        // 그룹 소유자가 아닌 경우 권한 예외 처리
        if(!group.getOwner().getUserId().equals(owner.getUserId())){
            throw new GeneralException(Code.UNAUTHORIZED_DELETE_GROUP);
        }

        // 그룹 삭제 시 루트 INACTIVE
        List<Route> groupRoute = routeRepository.findRoutesByGroupAndStatus(group, BaseEntity.Status.ACTIVE);
        for (Route route : groupRoute) {
            route.updateStatus(BaseEntity.Status.INACTIVE);
            routeRepository.save(route);
        }

        // 그룹 삭제
        group.updateStatus(BaseEntity.Status.INACTIVE);
    }

    public void joinGroup(User user, JoinGroupRequest joinGroupRequest){
        Group group = groupRepository.findGroupByCodeAndStatus(joinGroupRequest.getCode(), BaseEntity.Status.ACTIVE)
                .orElseThrow(()->new GeneralException(Code.FIND_FAIL_GROUP));

        // 이미 그룹에 참여한 유저인지 확인
        boolean isMember = groupMemberRepository.existsGroupMemberByGroupAndUser(group, user);
        if (isMember) {
            throw new GeneralException(Code.ALREADY_JOINED_GROUP);
        }

        // 그룹 참여
        User joinUser = userRepository.findById(user.getUserId())
                .orElseThrow(()->new GeneralException(Code.USER_NOT_FOUND));

        GroupMember groupMember = GroupMember.builder()
                .group(group)
                .user(joinUser)
                .build();

        groupMemberRepository.save(groupMember);
    }

    public void leaveGroup(User user, Long groupId){
        Group group = groupRepository.findGroupByGroupIdAndStatus(groupId, BaseEntity.Status.ACTIVE)
                .orElseThrow(()->new GeneralException(Code.FIND_FAIL_GROUP));
        GroupMember groupMember = groupMemberRepository.findGroupMemberByGroupAndUser(group, user)
                        .orElseThrow(()->new GeneralException(Code.USER_NOT_IN_GROUP));

        groupMemberRepository.delete(groupMember);
    }
  
    @Transactional(readOnly = true)
    public GetInvitationCodeResponse getInvitationCode(Long groupId) {
        Group group = groupRepository.findGroupByGroupIdAndStatus(groupId, BaseEntity.Status.ACTIVE)
                .orElseThrow(() -> new GeneralException(Code.FIND_FAIL_GROUP));
        InvitationCode invitationCode = invitationCodeRepository.findInvitationCodeByGroup(group)
                .orElseThrow(() -> new GeneralException(Code.INVITATION_CODE_NOT_FOUND));

        return GetInvitationCodeResponse.builder()
                .invitationCodeId(invitationCode.getInvitationCodeId())
                .groupId(invitationCode.getGroup().getGroupId())
                .code(invitationCode.getCode())
                .build();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getUserGroups(User user, Long lastGroupId, int size) {
        Page<Group> groups = pagingGroup(user, lastGroupId, size);
        boolean hasNext = groups.hasNext();

        List<GetGroupsResponse> responses = groups.map(group -> {
                    int numMembers = groupMemberRepository.countGroupMembersByGroup(group);
                    int numRestaurants = groupListRepository.countGroupListsByGroup(group);
                    int numRoutes = routeRepository.findRoutesByGroupAndStatus(group, BaseEntity.Status.ACTIVE).size();

                    return GetGroupsResponse.builder()
                            .groupId(group.getGroupId())
                            .groupName(group.getGroupName())
                            .isOwner(user.getUserId().equals(group.getOwner().getUserId()))
                            .numMembers(numMembers)
                            .numRestaurants(numRestaurants)
                            .numRoutes(numRoutes)
                            .build();
                }).toList();

        Map<String, Object> map = new HashMap<>();
        map.put("groups", responses);
        map.put("hasNext", hasNext);
        return map;
    }

    private Page<Group> pagingGroup(User user, Long lastGroupId, int size){
        // 그룹 목록 커서 페이징 처리
        Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "groupId"));
        List<Long> groupIds = groupMemberRepository.findGroupIdsByUser(user);

        if (lastGroupId == null) {
            return groupRepository.findGroupsByGroupIdInAndStatus(groupIds, BaseEntity.Status.ACTIVE, pageable);
        }else{
            return groupRepository.findGroupsByStatusAndGroupIdInLessThan(groupIds, BaseEntity.Status.ACTIVE, lastGroupId, pageable);
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getGroupMembers(Long groupId, Long lastMemberId, int size){
        Page<GroupMember> groupMembers = pagingGroupMember(groupId, lastMemberId, size);
        boolean hasNext = groupMembers.hasNext();

        List<GetGroupMemberResponse> responses = groupMembers.map(groupMember -> GetGroupMemberResponse.builder()
                        .groupMemberId(groupMember.getGroupMemberId())
                        .nickname(groupMember.getUser().getNickname())
                        .profileImg(groupMember.getUser().getProfileImage())
                        .build()).toList();

        Map<String, Object> map = new HashMap<>();
        map.put("groupMembers", responses);
        map.put("hasNext", hasNext);
        return map;
    }

    private Page<GroupMember> pagingGroupMember(Long groupId, Long lastMemberId, int size){
        // 그룹 멤버 목록 커서 페이징 처리
        Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "groupMemberId"));
        Group group = groupRepository.findGroupByGroupIdAndStatus(groupId, BaseEntity.Status.ACTIVE)
                .orElseThrow(()->new GeneralException(Code.FIND_FAIL_GROUP));

        if (lastMemberId == null) {
            return groupMemberRepository.findGroupMembersByGroup(group, pageable);
        }else{
            return groupMemberRepository.findGroupMembersByGroupLessThan(group, lastMemberId, pageable);
        }
    }

    public TransferOwnershipResponse transferOwnership(User owner, Long groupId, TransferOwnershipRequest transferOwnershipRequest){
        Group group = groupRepository.findGroupByGroupIdAndStatus(groupId, BaseEntity.Status.ACTIVE)
                .orElseThrow(()->new GeneralException(Code.FIND_FAIL_GROUP));

        // 그룹 소유자 권한 확인
        if(!group.getOwner().getUserId().equals(owner.getUserId())){
            throw new GeneralException(Code.NO_TRANSFER_PERMISSION);
        }

        // 새로운 그룹장 찾기
        GroupMember newOwnerMember = groupMemberRepository.findGroupMemberByGroupAndGroupMemberId(group, transferOwnershipRequest.getNewOwner())
                .orElseThrow(()->new GeneralException(Code.USER_NOT_IN_GROUP));

        User newOwner = newOwnerMember.getUser();

        // 새로운 그룹장으로 권한 이전
        group.updateOwner(newOwner);
        groupRepository.save(group);

        return TransferOwnershipResponse.builder()
                .newOwner(newOwnerMember.getGroupMemberId())
                .build();
    }

    @Transactional(readOnly = true)
    public GetPreJoinGroupInfoResponse getPreJoinGroupInfo(JoinGroupRequest joinGroupRequest){
        Group group = groupRepository.findGroupByCodeAndStatus(joinGroupRequest.getCode(), BaseEntity.Status.ACTIVE)
                .orElseThrow(()->new GeneralException(Code.FIND_FAIL_GROUP));

        int numMembers = groupMemberRepository.countGroupMembersByGroup(group);

        List<String> groupMembersNickname = groupMemberRepository.findGroupMembersByGroup(group).stream()
                .map(groupMember -> groupMember.getUser().getNickname())
                .collect(Collectors.toList());

        return GetPreJoinGroupInfoResponse.builder()
                .groupName(group.getGroupName())
                .groupMembers(groupMembersNickname)
                .numMembers(numMembers)
                .build();
    }

    @Override
    public void deleteRoute(Long routeId, User user, Long groupId) {
        // 그룹 구성원인지 확인
        Group group = groupRepository.findGroupByGroupIdAndStatus(groupId, BaseEntity.Status.ACTIVE)
                .orElseThrow(()->new GeneralException(Code.FIND_FAIL_GROUP));
        if(!groupMemberRepository.existsGroupMemberByGroupAndUser(group,user)){
            throw new GeneralException(Code.USER_NOT_IN_GROUP);
        }

        Route route = routeRepository.findRouteByRouteIdAndStatus(routeId, BaseEntity.Status.ACTIVE)
                .orElseThrow(() -> new GeneralException(Code.ROUTE_NOT_FOUND));
        //루트 삭제 : soft delete
        route.updateStatus(BaseEntity.Status.INACTIVE);

    }

    @Override
    public void createGroupList(Long groupId,GroupListRequest request,User user) {
        // 그룹 존재 여부 확인
        Group group = groupRepository.findGroupByGroupIdAndStatus(groupId, BaseEntity.Status.ACTIVE)
                .orElseThrow(() -> new GeneralException(Code.FIND_FAIL_GROUP));
        // 그룹 구성원인지 확인
        if(!groupMemberRepository.existsGroupMemberByGroupAndUser(group,user))
            throw new GeneralException(Code.USER_NOT_IN_GROUP);

        Store store = storeRepository.findById(request.getStoreId()).orElseThrow(() -> new NotFoundException(Code.STORE_NOT_FOUND));

        //이미 존재하는 상점인지 확인
        if(groupListRepository.existsGroupListByGroupAndStore(group,store)){
            throw new GeneralException(Code.ALREADY_ADD_GROUP_LIST);
        }

        GroupList groupList =GroupList.builder()
                .group(group)
                .store(store)
                .user(user)
                .build();

        // 그룹리스트 저장
        groupListRepository.save(groupList);

    }

    @Override
    public void deleteGroupList(List<Long> groupListId,User user) {
        for (Long gl : groupListId) {
            GroupList groupList = groupListRepository.findGroupListByGroupListId(gl).orElseThrow(() -> new GeneralException(Code.GROUPLIST_NOT_FROUND));
            // 그룹 구성원인지 확인
            if(!groupMemberRepository.existsGroupMemberByGroupAndUser(groupList.getGroup(),user))
                throw new GeneralException(Code.USER_NOT_IN_GROUP);
            // 그룹리스트 삭제
            groupListRepository.delete(groupList);
        }

    }

    @Override
    public PagingResponse getAllGroupList(Long groupId, Long groupListId,User user) {

        // 그룹 존재여부 확인
        Group group = groupRepository.findGroupByGroupIdAndStatus(groupId, BaseEntity.Status.ACTIVE)
                .orElseThrow(() -> new GeneralException(Code.FIND_FAIL_GROUP));

        Page<GroupList> groupLists;
        if(groupListId == null){
            // 그룹 내 모든 찜한 상점 조회 = 그룹리스트 조회
            groupLists = groupListRepository.findFirstGroupListOrderByDesc(group, Pageable.ofSize(GROUP_LIST_FIRST_PAGE));
        }else{
            // 그룹 내 모든 찜한 상점 조회 = 그룹리스트 조회
            groupLists = groupListRepository.findGroupListByGroupOrderByCreatedAtDesc(group,groupListId, Pageable.ofSize(GROUP_LIST_PAGE));

        }


        // 그룹 리스트에 해당하는 각 상점 정보 조회
        List<GroupListResponse> list = groupLists.stream().map(gl -> {
            Optional<Review> topReviewOptional = reviewRepository.findFirstByStoreOrderByLikedDesc(gl.getStore()); // 가장 좋아요가 많은 review
            String reviewImg = topReviewOptional.map(Review::getImg1).orElse("");
            return GroupListResponse.builder()
                    .groupListId(gl.getGroupListId())
                    .storeId(gl.getStore().getStoreId())
                    .storeName(gl.getStore().getStoreName())
                    .storeProfileImg(reviewImg)
                    .userProfileImg(gl.getUser().getProfileImage())
                    .address(gl.getStore().getAddress())
                    .build();
        }).collect(Collectors.toList());

        return PagingResponse.builder()
                .result(list)
                .hasNext(groupLists.hasNext())
                .build();


    }

    @Transactional
    public void hardDeleteAllSoftDeleted() {
        groupRepository.deleteAllInActive();

    }

}
