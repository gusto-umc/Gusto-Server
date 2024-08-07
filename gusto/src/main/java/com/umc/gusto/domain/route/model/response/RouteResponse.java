package com.umc.gusto.domain.route.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class RouteResponse {
    private Long routeId;
    private String routeName;
    private int numStore;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private boolean publishRoute;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long groupId;
}
