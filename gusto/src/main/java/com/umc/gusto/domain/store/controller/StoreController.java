package com.umc.gusto.domain.store.controller;


import com.umc.gusto.domain.store.model.response.*;
import com.umc.gusto.domain.store.service.StoreService;
import com.umc.gusto.domain.user.entity.User;
import com.umc.gusto.global.auth.model.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    private static final int DEFAULT_PAGE_NUMBER = 0;
    private static final int DEFAULT_PAGE_SIZE = 3;

    /**
     * 가게 1건 조회
     * [GET] /stores/{storeId}
     */
    @GetMapping("/{storeId}")
    public ResponseEntity<GetStoreResponse> getStore(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long storeId) {
        User user = authUser.getUser();
        GetStoreResponse getStore = storeService.getStore(user, storeId);
        return ResponseEntity.status(HttpStatus.OK).body(getStore);
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
        Pageable pageable = PageRequest.of(DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE);

        // 상점 세부 정보 가져오기
        GetStoreDetailResponse getStoreDetail = storeService.getStoreDetail(user, storeId, visitedAt, reviewId, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(getStoreDetail);
    }

    /**
     * 현 지역의 카테고리 별 찜한 가게 목록 조회
     * [GET] /stores/map?townName={townName}&myCategoryId={myCategoryId}&visited={visitStatus}
     */
    @GetMapping("/map")
    public ResponseEntity<List<GetStoresInMapResponse>> getStoresInMap(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(name = "townName") String townName,
            @RequestParam(name = "myCategoryId", required = false) Long myCategoryId,
            @RequestParam(name = "visited", required = false) Boolean visited
            ) {

        User user = authUser.getUser();
        List<GetStoresInMapResponse> getStoresInMaps = storeService.getStoresInMap(user, townName, myCategoryId, visited);
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
