package com.umc.gusto.domain.store.controller;


import com.umc.gusto.domain.store.model.response.*;
import com.umc.gusto.domain.store.service.StoreService;
import com.umc.gusto.domain.user.entity.User;
import com.umc.gusto.global.auth.model.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stores")
public class StoreController {
    private final StoreService storeService;

    /**
     * 가게 조회
     * [GET] /stores?storeId={storeId}&storeId={storeId}
     */
    @GetMapping
    public ResponseEntity<List<GetStoreResponse>> getStores(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(name = "storeId") List<Long> storeIds) {
        User user = authUser.getUser();
        List<GetStoreResponse> getStores = storeService.getStores(user, storeIds);
        return ResponseEntity.status(HttpStatus.OK).body(getStores);
    }

    /**
     * 가게 1건 상세 조회
     * [GET] /stores/{storeId}/detail?reviewId={reviewId}
     */
    @GetMapping("/{storeId}/detail")
    public ResponseEntity<GetStoreDetailResponse> getStoreDetail(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long storeId,
            @RequestParam(name = "visitedAt", required = false) LocalDate visitedAt,
            @RequestParam(name = "reviewId", required = false) Long reviewId){
        User user = authUser.getUser();
        // 상점 세부 정보 가져오기
        GetStoreDetailResponse getStoreDetail = storeService.getStoreDetail(user, storeId, visitedAt, reviewId);
        return ResponseEntity.status(HttpStatus.OK).body(getStoreDetail);
    }

    /**
     * 현 지역의 카테고리 별 찜한 가게 목록 조회(카테고리 다중 선택 가능)
     * [GET] /stores/map?townName={townName}&visited={visitStatus}&myCategoryId={myCategoryId}&myCategoryId={myCategoryId}...
     */
    @GetMapping("/map")
    public ResponseEntity<List<GetStoresInMapResponse>> getStoresInMap(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(name = "townName") String townName,
            @RequestParam(name = "myCategoryId", required = false) List<Long> myCategoryIds,
            @RequestParam(name = "visited", required = false) Boolean visited
            ) {

        User user = authUser.getUser();
        List<GetStoresInMapResponse> getStoresInMaps = storeService.getStoresInMap(user, townName, myCategoryIds, visited);
        return  ResponseEntity.status(HttpStatus.OK).body(getStoresInMaps);
    }

    /**
     * 현재 지역의 찜한 식당 방문 여부 조회
     * [GET] /stores/pins?myCategoryId={categoryId}&townName={townName}
     */
    @GetMapping("/pins")
    public ResponseEntity<List<GetPinStoreResponse>> getPinStoresByCategoryAndLocation(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(name = "myCategoryId", required = false) Long myCategoryId,
            @RequestParam(name = "townName") String townName){
        User user = authUser.getUser();
        List<GetPinStoreResponse> storeList = storeService.getPinStoresByCategoryAndLocation(user, myCategoryId, townName);
        return ResponseEntity.status(HttpStatus.OK).body(storeList);
    }

    /**
     * 맛집 검색 엔진
     */
    @GetMapping("/search")
    public ResponseEntity<List<GetStoreInfoResponse>> searchStore(@RequestParam(name = "keyword") String keyword){
        return ResponseEntity.status(HttpStatus.OK).body(storeService.searchStore(keyword));
    }
}
