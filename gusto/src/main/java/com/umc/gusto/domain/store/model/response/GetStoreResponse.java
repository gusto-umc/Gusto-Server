package com.umc.gusto.domain.store.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.umc.gusto.domain.store.entity.OpeningHours;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetStoreResponse{
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Long pinId;
    Long storeId;
    String storeName;
    String address;
    Double longitude;
    Double latitude;
    Map<OpeningHours.BusinessDay, Timing> businessDay;
    String contact;
    String img1;
    String img2;
    String img3;
    Boolean pin;        // 찜 여부

    // 하루 영업 시간을 나타내는 내부 클래스
    @Getter
    @AllArgsConstructor
    public static class Timing {
        LocalTime openedAt; // 오픈 시간
        LocalTime closedAt; // 마감 시간
    }

}