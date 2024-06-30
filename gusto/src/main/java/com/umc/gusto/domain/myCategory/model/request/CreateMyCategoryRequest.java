package com.umc.gusto.domain.myCategory.model.request;

import com.umc.gusto.global.common.BaseEntity;
import com.umc.gusto.global.common.PublishStatus;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class CreateMyCategoryRequest {
    @Size(max=10, message = "카테고리 이름은 10자를 초과할 수 없습니다.")
    String myCategoryName;
    @Size(max=20, message = "카테고리 설명은 20자를 초과할 수 없습니다.")
    String myCategoryScript;
    Integer myCategoryIcon;
    PublishStatus publishCategory;            // 데모데이 이후
    BaseEntity.Status status = BaseEntity.Status.ACTIVE;

}
