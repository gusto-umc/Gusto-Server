package com.umc.gusto.domain.user.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignUpRequest {
    @NotBlank
    String provider;
    @NotBlank
    String providerId;
    @NotBlank
    String accessToken;
    @NotBlank @Size(max = 15)
    String nickname;
    @NotBlank
    String age;
    @NotBlank
    String gender;
    String profileImg;
}
