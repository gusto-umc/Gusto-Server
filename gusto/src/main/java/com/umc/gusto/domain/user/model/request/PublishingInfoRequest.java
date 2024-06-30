package com.umc.gusto.domain.user.model.request;

public class PublishingInfoRequest {
    private boolean publishReview;
    private boolean publishCategory;
    private boolean publishRoute;

    public boolean getPublishReview() {
        return this.publishReview;
    }

    public boolean getPublishCategory() {
        return this.publishCategory;
    }

    public boolean getPublishRoute() { return this.publishRoute; }
}
