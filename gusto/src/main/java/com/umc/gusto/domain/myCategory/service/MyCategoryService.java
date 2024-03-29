package com.umc.gusto.domain.myCategory.service;

import com.umc.gusto.domain.myCategory.model.request.CreateMyCategoryRequest;
import com.umc.gusto.domain.myCategory.model.request.UpdateMyCategoryRequest;
import com.umc.gusto.domain.myCategory.model.response.MyCategoryResponse;
import com.umc.gusto.domain.myCategory.model.response.PinByMyCategoryResponse;
import com.umc.gusto.domain.user.entity.User;

import java.util.List;

public interface MyCategoryService {

    List<MyCategoryResponse> getAllMyCategory(User user, String nickname, String townName);
//    List<MyCategoryResponse> getAllMyCategoryWithLocation(User user, String townName);

    List<PinByMyCategoryResponse> getAllPinByMyCategory(User user, String nickname, Long myCategoryId, String townName);

//    List<PinByMyCategoryResponse> getAllPinByMyCategoryWithLocation(User user, Long myCategoryId, String townName);

    void createMyCategory(User user, CreateMyCategoryRequest request);

    void modifyMyCategory(User user,Long myCategoryId, UpdateMyCategoryRequest request);

    void deleteMyCategories(User user, List<Long> myCategoryId);
}