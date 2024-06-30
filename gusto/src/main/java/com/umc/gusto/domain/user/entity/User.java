package com.umc.gusto.domain.user.entity;

import com.umc.gusto.global.common.BaseTime;
import com.umc.gusto.global.common.PublishStatus;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@Getter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@DynamicInsert
@DynamicUpdate
public class User extends BaseTime {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name="uuid2", strategy = "uuid2")
    @Column(columnDefinition = "BINARY(16)")
    private UUID userId;

    @Column(nullable = false, length = 15)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Age age;

    @Column(nullable = false)
    private String profileImage;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "publishReview", nullable = false, length = 10)
    private PublishStatus publishReview = PublishStatus.PUBLIC;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "publishCategory", nullable = false, length = 10)
    private PublishStatus publishCategory = PublishStatus.PUBLIC;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "publishRoute", nullable = false, length = 10)
    private PublishStatus publishRoute = PublishStatus.PUBLIC;

    @Column(nullable = false, columnDefinition = "bigint default 0")
    private Long follower;

    @Column(nullable = false, columnDefinition = "bigint default 0")
    private Long following;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer pinCnt;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer reviewCnt;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private MemberStatus memberStatus = MemberStatus.ACTIVE;

    public enum Gender {
        FEMALE, MALE, NONE
    }

    public enum Age {
        TEEN, TWENTIES, THIRTIES, FOURTIES, FIFTIES, OLDER, NONE
    }

    public enum MemberStatus {
        ACTIVE, INACTIVE, PAUSE
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateProfile(String profileImage) {
        this.profileImage = profileImage;
    }

    public void updateAge(User.Age age) {
        this.age = age;
    }

    public void updateGender(User.Gender gender) {
        this.gender = gender;
    }

    public void updatePublishReview(PublishStatus status) {
        this.publishReview = status;
    }

    public void updatePublishCategory(PublishStatus status) {
        this.publishCategory = status;
    }

    public void updatePublishRoute(PublishStatus status) {
        this.publishRoute = status;
    }

    public void updateFollower(long follower) {
        this.follower = follower;
    }

    public void updateFollowing(long following) {
        this.following = following;
    }
    public void updatePinCnt(Integer pinCnt) {
        this.pinCnt = pinCnt;
    }
    public void updateCountReview() {
        this.reviewCnt++;
    }
    public void updateCountReview(boolean plus){
        if(plus) this.reviewCnt++;
        else this.reviewCnt--;
    }
}
