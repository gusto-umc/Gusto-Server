package com.umc.gusto.domain.user.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignInRequest {
    @NotBlank
    String provider;
    @NotBlank
    String providerId;
    @NotBlank
    String accessToken;
}
