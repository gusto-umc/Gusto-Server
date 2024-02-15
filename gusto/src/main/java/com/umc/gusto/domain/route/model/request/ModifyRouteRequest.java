package com.umc.gusto.domain.route.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

import java.util.List;

@Getter
public class ModifyRouteRequest {
    @NotBlank(message = "루트 명은 필수 입력값입니다.")
    private String routeName;

    private List<ModifyRoueListRequest> routeList;
}
