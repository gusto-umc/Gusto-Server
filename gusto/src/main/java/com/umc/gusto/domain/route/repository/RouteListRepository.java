package com.umc.gusto.domain.route.repository;

import com.umc.gusto.domain.route.entity.Route;
import com.umc.gusto.domain.route.entity.RouteList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RouteListRepository extends JpaRepository<RouteList,Long> {
    int countRouteListByRoute(Route route);

    List<RouteList> findByRouteOrderByOrdinalAsc(Route route); // 이동 순서 기준 정렬

    @Query("select MAX(rl.ordinal) from RouteList  rl where rl.route = :route ")
    Integer findLastRouteListOrdinal(Route route);

    @Modifying //DELETE 쿼리임을 명시 - 벌크 연산
    @Query("DELETE FROM RouteList rl WHERE rl.route.status = 'INACTIVE' ")
    void deleteByRouteINACTIVE();
}
