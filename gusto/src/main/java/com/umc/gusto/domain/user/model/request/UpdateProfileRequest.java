package com.umc.gusto.domain.user.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class UpdateProfileRequest {
    @Size(max = 15)
    String nickname;
    String age;
    String gender;
    Boolean useDefaultImg;
}
