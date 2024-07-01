package com.umc.gusto.domain.store.service;

import com.umc.gusto.domain.store.model.response.*;
import com.umc.gusto.domain.user.entity.User;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;


public interface StoreService {

    List<GetStoreResponse> getStores(User user, List<Long> storeIds);
    GetStoreDetailResponse getStoreDetail(User user, Long storeId, LocalDate visitedAt, Long reviewId);
    List<GetStoresInMapResponse> getStoresInMap(User user, String townName, List<Long> myCategoryIds, Boolean visited);
    List<GetPinStoreResponse> getPinStoresByCategoryAndLocation(User user, Long myCategoryId, String townName);
    List<GetStoreInfoResponse> searchStore(String keyword);
}
