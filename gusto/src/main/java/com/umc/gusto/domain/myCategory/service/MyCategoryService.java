package com.umc.gusto.domain.myCategory.service;

import com.umc.gusto.domain.myCategory.model.request.CreateMyCategoryRequest;
import com.umc.gusto.domain.myCategory.model.request.UpdateMyCategoryRequest;
import com.umc.gusto.domain.myCategory.model.response.MyCategoryResponse;
import com.umc.gusto.domain.myCategory.model.response.PagingResponse;
import com.umc.gusto.domain.myCategory.model.response.PinByMyCategoryResponse;
import com.umc.gusto.domain.user.entity.User;
import com.umc.gusto.global.common.PublishStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MyCategoryService {

    PagingResponse getAllMyCategory(User user, String nickname, String townName, Long myCategoryId);
//    List<MyCategoryResponse> getAllMyCategoryWithLocation(User user, String townName);

    PagingResponse getAllPinByMyCategory(User user, String nickname, Long myCategoryId, String townName, Long pinId, String storeName, String sort);

//    List<PinByMyCategoryResponse> getAllPinByMyCategoryWithLocation(User user, Long myCategoryId, String townName);

    void createMyCategory(User user, CreateMyCategoryRequest request);

    void modifyMyCategory(User user,Long myCategoryId, UpdateMyCategoryRequest request);

    void deleteMyCategories(User user, List<Long> myCategoryId);
}