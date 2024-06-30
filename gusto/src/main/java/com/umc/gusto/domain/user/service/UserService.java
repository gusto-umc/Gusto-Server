package com.umc.gusto.domain.user.service;

import com.umc.gusto.domain.user.entity.User;
import com.umc.gusto.domain.user.model.request.PublishingInfoRequest;
import com.umc.gusto.domain.user.model.request.SignInRequest;
import com.umc.gusto.domain.user.model.request.SignUpRequest;
import com.umc.gusto.domain.user.model.request.UpdateProfileRequest;
import com.umc.gusto.domain.user.model.response.*;
import com.umc.gusto.global.auth.model.Tokens;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    
    // 회원 가입
    Tokens createUser(MultipartFile multipartFile, SignUpRequest signUpRequest);

    // 닉네임 중복 체크
    void checkNickname(String nickname);

    // 닉네임 사용 확정 - 회원 가입 시
    void confirmNickname(String nickname);

    // 닉네임 랜덤 생성
    NicknameResponse generateRandomNickname();

    Tokens signIn(SignInRequest signInRequest);

    void signOut(User user, String refreshToken);

    // 먹스또 프로필 조회
    FeedProfileResponse getProfile(User user, String nickname);

    // 닉네임 갱신
    void updateNickname(User user, String nickname);

    // 프로필 정보 리턴
    ProfileResponse getProfile(User user);

    // 프로필 정보 갱신
    void updateProfile(User user, MultipartFile profileImg, UpdateProfileRequest request);

    // 콘텐츠 공개 여부 검색
    PublishingInfoResponse getPublishingInfo(User user);

    // 콘텐츠 공개 여부 갱신
    void updatePublishingInfo(User user, PublishingInfoRequest request);

    // 팔로우
    void followUser(User user, String nickname);

    // 언팔로우
    void unfollowUser(User user, String nickname);

    // 팔로우 목록
    PagingResponse getFollowList(User user, Long followId);

    // 팔로워 목록
    PagingResponse getFollwerList(User user, Long followId);
}
