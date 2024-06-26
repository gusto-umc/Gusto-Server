package com.umc.gusto.domain.myCategory.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PinByMyCategoryResponse{
    Long pinId;
    Long storeId;
    String storeName;
    String address;
    List<String> reviewImg3;
    String img1;
    String img2;
    String img3;
    Integer reviewCnt;
}