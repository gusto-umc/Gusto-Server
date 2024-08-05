package com.umc.gusto.domain.route.service;

import com.umc.gusto.domain.route.model.request.ModifyRouteRequest;
import com.umc.gusto.domain.route.model.request.RouteRequest;
import com.umc.gusto.domain.route.model.response.RoutePagingResponse;
import com.umc.gusto.domain.user.entity.User;

public interface RouteService {
    // 루트 생성
    void createRoute(RouteRequest request,User user);
    void createRouteGroup(Long groupId,RouteRequest request,User user);

    // 루트 삭제
    void deleteRoute(Long routeId,User user);

    // 내 루트 조회
    RoutePagingResponse getRoute(User user, Long routeId);

    // 그룹 내 루트 조회
    RoutePagingResponse getGroupRoute(Long groupId,Long routeId);

    // 루트 상세 수정
    void modifyRouteList(Long routeId, ModifyRouteRequest request);

    // 타인의 루트 조회
    RoutePagingResponse getOtherRoute(String nickname, Long routeId);

    // 루트 공개/비공개 수정
    void  modifyPublishingInfo(User user, Long routeId,boolean publishStatus);

    //
    void hardDeleteAllSoftDeleted();
}
