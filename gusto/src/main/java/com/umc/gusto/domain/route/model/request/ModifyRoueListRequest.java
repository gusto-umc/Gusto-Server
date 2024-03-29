package com.umc.gusto.domain.route.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ModifyRoueListRequest {
    @NotNull
    private Long routeListId;

    @NotNull(message = "루트 작성 시 식당 입력은 필수입니다.")
    private Long storeId;

    @NotNull(message = "루트 작성 시 식당 순서를 기입해주세요.")
    private Integer ordinal;

}
