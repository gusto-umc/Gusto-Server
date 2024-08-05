package com.umc.gusto.domain.user.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class PublishingInfoRequest {
    @NotNull
    private boolean publishReview;
    @NotNull
    private boolean publishPin;
    @NotNull
    private boolean publishRoute;

    public boolean getPublishReview() {
        return this.publishReview;
    }

    public boolean getPublishPin() {
        return this.publishPin;
    }

    public boolean getPublishRoute() { return this.publishRoute; }
}
