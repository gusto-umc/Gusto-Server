package com.umc.gusto.domain.user.controller;

import com.umc.gusto.domain.user.entity.User;
import com.umc.gusto.domain.user.model.request.PublishingInfoRequest;
import com.umc.gusto.domain.user.model.request.SignInRequest;
import com.umc.gusto.domain.user.model.request.UpdateProfileRequest;
import com.umc.gusto.domain.user.model.response.*;
import com.umc.gusto.domain.user.service.UserService;
import com.umc.gusto.domain.user.model.request.SignUpRequest;
import com.umc.gusto.global.auth.model.AuthUser;
import com.umc.gusto.global.auth.model.Tokens;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    /**
     * 회원 가입 API
     * [POST] /users/sign-up
     * @param multipartFile
     * @param signUpRequest
     * @return -
     */
    @PostMapping("/sign-up")
    public ResponseEntity signUp(@RequestPart(name = "profileImg", required = false) MultipartFile multipartFile,
                         @RequestPart(name = "info") @Valid SignUpRequest signUpRequest) {
        Tokens tokens = userService.createUser(multipartFile, signUpRequest);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Auth-Token", tokens.getAccessToken());
        headers.set("refresh-token", tokens.getRefreshToken());

        return ResponseEntity.status(HttpStatus.CREATED)
                .headers(headers)
                .build();
    }

    /**
     * 기본 닉네임 생성 API
     * [GET] /users/random-nickname
     * @param -
     * @return String
     */
    @PostMapping("/random-nickname")
    public ResponseEntity generateNickname() {
        NicknameResponse nicknameResponse = userService.generateRandomNickname();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(nicknameResponse);
    }

    /**
     * 닉네임 중복 체크 API
     * [GET] /users/check-nickname/{nickname}
     * @param nickname
     * @return -
     */
    @GetMapping("/check-nickname/{nickname}")
    public ResponseEntity checkNickname(@PathVariable("nickname") @Size(max = 15, message = "닉네임은 15자를 초과할 수 없습니다.") String nickname) {
        userService.checkNickname(nickname);

        return ResponseEntity.ok()
                .build();
    }

    /**
     * 닉네임 사용 확정 API
     * [POST] /users/confirm-nickname/{nickname}
     * @param nickname
     * @return -
     */
    @PostMapping("/confirm-nickname/{nickname}")
    public ResponseEntity confirmNickname(@PathVariable("nickname") @Size(max = 15, message = "닉네임은 15자를 초과할 수 없습니다.") String nickname) {
        userService.confirmNickname(nickname);

        return ResponseEntity.ok()
                .build();
    }

    /**
     * 로그인 API
     * [POST] /users/sign-in
     * @param signInRequest
     * @return -
     */
    @PostMapping("/sign-in")
    public ResponseEntity signIn(@RequestBody @Valid SignInRequest signInRequest) {
        Tokens tokens = userService.signIn(signInRequest);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Auth-Token", tokens.getAccessToken());
        headers.set("refresh-token", tokens.getRefreshToken());

        return ResponseEntity.ok()
                .headers(headers)
                .build();
    }

    /**
     * 로그아웃 API
     * [POST] /users/sign-out
     * @param -
     * @return -
     */
    @PostMapping("/sign-out")
    public ResponseEntity signOut(@AuthenticationPrincipal AuthUser authUser,
                                  @RequestHeader("refresh-Token") @NotBlank String refreshToken) {
        userService.signOut(authUser.getUser(), refreshToken);

        return ResponseEntity.status(HttpStatus.RESET_CONTENT)
                .build();
    }

    /**
     * 먹스또 프로필 조회
     * [GET] /users/{nickname}/profile
     * @param nickname
     * @return ProfileRes
     */
    @GetMapping("/{nickname}/profile")
    public ResponseEntity<FeedProfileResponse> retrieveProfile(@AuthenticationPrincipal AuthUser authUser,
                                                               @PathVariable("nickname") @Size(max = 15, message = "닉네임은 15자를 초과할 수 없습니다.") String nickname) {
        User user = null;

        if(authUser != null) {
            user = authUser.getUser();
        }

        FeedProfileResponse profileRes = userService.getProfile(user, nickname);

        return ResponseEntity.ok()
                .body(profileRes);
    }

    /**
     * 닉네임 수정
     * [PATCH] /users/update-nickname?nickname={new_nickname}
     * @param nickname
     * @return
     */
    @PatchMapping("/update-nickname")
    public ResponseEntity updateNickname(@AuthenticationPrincipal AuthUser authUser,
                                         @RequestParam("nickname") @Size(max = 15, message = "닉네임은 15자를 초과할 수 없습니다.") String nickname) {
        userService.updateNickname(authUser.getUser(), nickname);

        return ResponseEntity.ok()
                .build();
    }

    /**
     * 프로필 정보 조회
     * [GET] /users/my-info
     * @param -
     * @return -
     */
    @GetMapping("/my-info")
    public ResponseEntity getProfile(@AuthenticationPrincipal AuthUser authUser) {
        ProfileResponse profileResponse = userService.getProfile(authUser.getUser());

        return ResponseEntity.ok()
                .body(profileResponse);
    }

    /**
     * 프로필 정보 수정
     * [PATCH] /users/my-info
     * @param -
     * @return -
     */
    @PatchMapping("/my-info")
    public ResponseEntity updateProfile(@AuthenticationPrincipal AuthUser authUser,
                                        @RequestPart(required = false, name = "profileImg") MultipartFile profileImg,
                                        @RequestPart(required = false, name = "setting") @Valid UpdateProfileRequest setting) {
        userService.updateProfile(authUser.getUser(), profileImg, setting);

        return ResponseEntity.status(HttpStatus.RESET_CONTENT)
                .build();
    }
  
    /**
     * 팔로우
     * [POST] /users/follow/{nickname}
     * @param nickname
     * @return -
     */
    @PostMapping("/follow/{nickname}")
    public ResponseEntity followUser(@AuthenticationPrincipal AuthUser authUser, @PathVariable("nickname") @Size(max = 15, message = "닉네임은 15자를 초과할 수 없습니다.") String nickname) {
        userService.followUser(authUser.getUser(), nickname);

        return ResponseEntity.status(HttpStatus.CREATED)
                .build();
    }


    /**
     * 언팔로우
     * [DELETE] /users/unfollow/{nickname}
     * @param nickname
     * @return -
     */
    @DeleteMapping("/unfollow/{nickname}")
    public ResponseEntity unfollow(@AuthenticationPrincipal AuthUser authUser, @PathVariable("nickname") @Size(max = 15, message = "닉네임은 15자를 초과할 수 없습니다.") String nickname) {
        userService.unfollowUser(authUser.getUser(), nickname);

        return ResponseEntity.status(HttpStatus.RESET_CONTENT)
                .build();
    }

    /**
     * 콘텐츠 공개 여부 조회
     * [GET] /users/my-info/publishing
     * @param -
     * @return -
     */
    @GetMapping("/my-info/publishing")
    public ResponseEntity<PublishingInfoResponse> getPublishingInfo(@AuthenticationPrincipal AuthUser authUser) {
        PublishingInfoResponse pir = userService.getPublishingInfo(authUser.getUser());

        return ResponseEntity.ok()
                .body(pir);
    }

    /**
     * 콘텐츠 공개 여부 수정
     * [PATCH] /users/my-info/publishing
     * @param -
     * @return -
     */
    @PatchMapping("/my-info/publishing")
    public ResponseEntity updatePublishingInfo(@AuthenticationPrincipal AuthUser authUser, @RequestBody @Valid PublishingInfoRequest request) {
        userService.updatePublishingInfo(authUser.getUser(), request);

        return ResponseEntity.ok()
                .build();
    }

    /**
     * 팔로우 리스트 조회
     * [GET] /users/following?followId={followId}
     * @param followId
     * @return List<>
     */
    @GetMapping("/following")
    public ResponseEntity<PagingResponse> followList(@AuthenticationPrincipal AuthUser authUser,
                                                     @RequestParam(required = false, name = "followId") Long followId) {
        PagingResponse pagingResponse = userService.getFollowList(authUser.getUser(), followId);

        return ResponseEntity.ok()
                .body(pagingResponse);
    }

    /**
     * 팔로워 리스트 조회
     * [GET] /users/follower?followId={followId}
     * @param followId
     * @return List<>
     */
    @GetMapping("/follower")
    public ResponseEntity<PagingResponse> followerList(@AuthenticationPrincipal AuthUser authUser,
                                                       @RequestParam(required = false, name = "followId") Long followId) {
        PagingResponse pagingResponse = userService.getFollwerList(authUser.getUser(), followId);

        return ResponseEntity.ok()
                .body(pagingResponse);
    }

    /**
     * 소셜 연동 해제
     * [DELETE] /users/auth/social-account
     * @param -
     * @return -
     */
    @DeleteMapping("/auth/social-account")
    public ResponseEntity disconnectSocialAccount(@AuthenticationPrincipal AuthUser authUser,
                                                  @RequestBody @Valid SignInRequest signInRequest) {

        userService.disconnectSocialAccount(authUser.getUser(), signInRequest);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .build();
    }

    /**
     * 소셜 연동 추가
     * [POST] /users/auth/social-account
     * @param -
     * @return -
     */
    @PostMapping("/auth/social-account")
    public ResponseEntity connectSocialAccount(@AuthenticationPrincipal AuthUser authUser,
                                               @RequestBody @Valid SignInRequest signInRequest) {
        userService.connectSocialAccount(authUser.getUser(), signInRequest);

        return ResponseEntity.status(HttpStatus.CREATED)
                .build();
    }

    /**
     * 연결된 소셜 계정 목록
     * [POST] /users/social-accounts
     * @param -
     * @return -
     */
    @GetMapping("/social-accounts")
    public ResponseEntity<Map> socialAccountList(@AuthenticationPrincipal AuthUser authUser) {
        Map<String, Boolean> accountList = userService.getAccountList(authUser.getUser());

        return ResponseEntity.ok()
                .body(accountList);
    }

    /**
     * 회원 탈퇴
     * [DELETE] /users/my
     * @param -
     * @return -
     */
    @DeleteMapping("/my")
    public ResponseEntity disconnectSocialAccount(@AuthenticationPrincipal AuthUser authUser) {
        userService.withdrawalUser(authUser.getUser());

        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .build();
    }
}
