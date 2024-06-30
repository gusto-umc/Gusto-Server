package com.umc.gusto.domain.myCategory.model.request;

import com.umc.gusto.global.common.PublishStatus;
import lombok.Getter;

@Getter
public class UpdateMyCategoryRequest {
    String myCategoryName;
    String myCategoryScript;
    Integer myCategoryIcon;
    PublishStatus publishCategory = PublishStatus.PUBLIC;     // 데모데이 이후 각각 카테고리에 대해 publish 처리

}