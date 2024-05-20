package com.umc.gusto.domain.route.service;

import com.umc.gusto.domain.group.entity.Group;
import com.umc.gusto.domain.group.repository.GroupRepository;
import com.umc.gusto.domain.route.entity.Route;
import com.umc.gusto.domain.route.entity.RouteList;
import com.umc.gusto.domain.route.model.request.ModifyRoueListRequest;
import com.umc.gusto.domain.route.model.request.ModifyRouteRequest;
import com.umc.gusto.domain.route.model.request.RouteRequest;
import com.umc.gusto.domain.route.model.response.RoutePagingResponse;
import com.umc.gusto.domain.route.model.response.RouteResponse;
import com.umc.gusto.domain.route.repository.RouteListRepository;
import com.umc.gusto.domain.route.repository.RouteRepository;
import com.umc.gusto.domain.store.entity.Store;
import com.umc.gusto.domain.store.repository.StoreRepository;
import com.umc.gusto.domain.user.entity.User;
import com.umc.gusto.domain.user.repository.UserRepository;
import com.umc.gusto.global.common.BaseEntity;
import com.umc.gusto.global.common.PublishStatus;
import com.umc.gusto.global.exception.Code;
import com.umc.gusto.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RouteServiceImpl implements RouteService{
    private final RouteRepository routeRepository;
    private final GroupRepository groupRepository;
    private final RouteListRepository routeListRepository;
    private final RouteListService routeListService;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;

    private static final int ROUTE_LIST_PAGE = 6;


    @Transactional
    @Override
    public void createRoute(RouteRequest request,User user) {
        // 루트명은 내 루트명 중에서 중복 불가능
        if (routeRepository.existsByRouteName(request.getRouteName(),BaseEntity.Status.ACTIVE,user)) {
            throw new GeneralException(Code.ROUTE_DUPLICATE_ROUTENAME);
        }
        // 루트 리스트 갯수 6개 제한 확인
        if(request.getRouteList().size()>=7){
            throw new GeneralException(Code.ROUTELIST_TO_MANY_REQUEST);
        }

        // 루트 생성
        Route route = Route.builder()
                .routeName(request.getRouteName())
                .user(user)
                .group(groupRepository.findGroupByGroupIdAndStatus(request.getGroupId(), BaseEntity.Status.ACTIVE).orElse(null))
                .build();
        Route savedRoute = routeRepository.save(route);

      
        if(request.getRouteList() != null){
            // 루트리스트 생성 비지니스 로직 호출
            routeListService.createRouteList(savedRoute, request.getRouteList());
        }else if(request.getGroupId() == null ){
            // 내 루트 생성시에는 최소 1개 이상의 경로가 포함되어야 함
            throw new GeneralException(Code.ROUTE_MYROUTE_BAD_REQUEST);
        }
    }

    @Transactional
    @Override
    public void deleteRoute(Long routeId, User user) {
        // 그룹X, ACTIVE 한정
        Route route = routeRepository.findRouteByRouteIdAndStatusAndGroup(routeId,BaseEntity.Status.ACTIVE)
                .orElseThrow(() -> new GeneralException(Code.ROUTE_NOT_FOUND));

        // 루트를 생성한 유저만 삭제
        if(!route.getUser().getNickname().equals(user.getNickname())){
            throw new GeneralException(Code.USER_NO_PERMISSION_FOR_ROUTE);
        }

        //루트 삭제 : soft delete // TODO:DB 최종 삭제 주기 체크
        route.updateStatus(BaseEntity.Status.INACTIVE);

    }

    @Override
    public RoutePagingResponse getRoute(User user, Long routeId) {
        userRepository.findByNicknameAndMemberStatusIs(user.getNickname(), User.MemberStatus.ACTIVE)
                .orElseThrow(()->new GeneralException(Code.USER_NOT_FOUND));

        Page<Route> routes;
        if(routeId == null) {
            routes = routeRepository.findRouteByUserFirstId(user, BaseEntity.Status.ACTIVE, Pageable.ofSize(ROUTE_LIST_PAGE));
        }else{
            routes = routeRepository.findRouteByAfterIRouted(user,routeId, BaseEntity.Status.ACTIVE,Pageable.ofSize(ROUTE_LIST_PAGE));
        }
        List<RouteResponse> list = routes.getContent().stream()
                .map(route -> RouteResponse.builder()
                        .routeId(route.getRouteId())
                        .routeName(route.getRouteName())
                        .numStore(routeListRepository.countRouteListByRoute(route))
                        .build())
                .collect(Collectors.toList());
        return RoutePagingResponse.builder()
                .hasNest(routes.hasNext())
                .result(list)
                .build();

    }

    @Override
    public  RoutePagingResponse getGroupRoute(Long groupId, Long routeId) {
        if(routeId == null) {
            routeId = 0L;
        }

        // 그룹 존재 여부 확인
        Group group = groupRepository.findGroupByGroupIdAndStatus(groupId, BaseEntity.Status.ACTIVE)
                .orElseThrow(()->new GeneralException(Code.FIND_FAIL_GROUP));

        //특정 그룹 내 루트 조회
        Page<Route> routes = routeRepository.findRoutesByGroup(group,routeId,BaseEntity.Status.ACTIVE,Pageable.ofSize(ROUTE_LIST_PAGE));
        List<RouteResponse> list = routes.stream().map(
                        Route -> RouteResponse.builder()
                                .routeId(Route.getRouteId())
                                .routeName(Route.getRouteName())
                                .numStore(routeListRepository.countRouteListByRoute(Route))
                                .groupId(Route.getRouteId())
                                .build())
                .collect(Collectors.toList());
        return RoutePagingResponse.builder()
                .hasNest(routes.hasNext())
                .result(list)
                .build();
    }

    @Transactional
    @Override
    public void modifyRouteList(Long routeId, ModifyRouteRequest request) {

        // 루트 리스트.size()가 null 일때,즉 루트명만 수정 시
        if (request.getRouteList() == null) {
            // 빈 리스트로 routeList를 초기화합니다.
            request.setRouteList(Collections.emptyList());
        }

        // 루트리스트 갯수가 6개 이하인지 확인
        if (request.getRouteList().size() >= 7) {
            throw new GeneralException(Code.ROUTELIST_TO_MANY_REQUEST);
        }

        // 루트 존재 여부 확인
        Route route = routeRepository.findRouteByRouteIdAndStatus(routeId, BaseEntity.Status.ACTIVE)
                .orElseThrow(() -> new GeneralException(Code.ROUTE_NOT_FOUND));

        // 루트 이름 변경 확인
        if (request.getRouteName() != null) {
            route.updateRouteName(request.getRouteName());
        }

        for (ModifyRoueListRequest modifyRoueListRequest : request.getRouteList()) {
            // 변경된 PK값 확인
            if (modifyRoueListRequest.getRouteListId() != null) {
                // 해당 루트리스트 조회
                RouteList routeList = routeListRepository.findById(modifyRoueListRequest.getRouteListId())
                        .orElseThrow(() -> new GeneralException(Code.ROUTELIST_NOT_FOUND));

                // 루트리스트 내 수정된 컬럼 값만 업데이트 진행
                if (modifyRoueListRequest.getOrdinal() != null) {
                    routeList.updateOrdinal(modifyRoueListRequest.getOrdinal());
                }
                if (modifyRoueListRequest.getStoreId() != null) {
                    Store store = storeRepository.findById(modifyRoueListRequest.getStoreId())
                            .orElseThrow(() -> new GeneralException(Code.STORE_NOT_FOUND));
                    routeList.updateStore(store);
                }
            }
        }
    }

    @Override
    public RoutePagingResponse getRoute(String nickname,Long routeId) {
        User user = userRepository.findByNicknameAndMemberStatusIs(nickname, User.MemberStatus.ACTIVE)
                .orElseThrow(() -> new GeneralException(Code.USER_NOT_FOUND));

        Page<Route> routes;
        if(routeId == null) {
            routes = routeRepository. findRouteByUserFirstId(user,BaseEntity.Status.ACTIVE,Pageable.ofSize(ROUTE_LIST_PAGE));
        }else{
            routes = routeRepository.findRouteByAfterIRouted(user,routeId, BaseEntity.Status.ACTIVE,Pageable.ofSize(ROUTE_LIST_PAGE));
        }

        List<RouteResponse> list = routes.stream()
                .map(route -> {
                    // 유저 활성화 설정 변경
                    if (!route.getUser().getPublishRoute().equals(PublishStatus.PUBLIC)) {
                        throw new GeneralException(Code.NO_PUBLIC_ROUTE);
                    }
                    return RouteResponse.builder()
                            .routeId(route.getRouteId())
                            .routeName(route.getRouteName())
                            .numStore(routeListRepository.countRouteListByRoute(route))
                            .build();
                })
                .collect(Collectors.toList());
        return RoutePagingResponse.builder()
                .hasNest(routes.hasNext())
                .result(list)
                .build();
    }

}
