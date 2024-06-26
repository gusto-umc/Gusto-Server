package com.umc.gusto.domain.user.service;

import com.umc.gusto.domain.user.entity.Follow;
import com.umc.gusto.domain.user.entity.Social;
import com.umc.gusto.domain.user.entity.User;
import com.umc.gusto.domain.user.model.NicknameBucket;
import com.umc.gusto.domain.user.model.request.PublishingInfoRequest;
import com.umc.gusto.domain.user.model.request.SignInRequest;
import com.umc.gusto.domain.user.model.request.SignUpRequest;
import com.umc.gusto.domain.user.model.request.UpdateProfileRequest;
import com.umc.gusto.domain.user.model.response.*;
import com.umc.gusto.domain.user.repository.FollowRepository;
import com.umc.gusto.domain.user.repository.SocialRepository;
import com.umc.gusto.domain.user.repository.UserRepository;
import com.umc.gusto.global.auth.JwtService;
import com.umc.gusto.global.auth.model.Tokens;
import com.umc.gusto.global.common.PublishStatus;
import com.umc.gusto.global.config.secret.JwtConfig;
import com.umc.gusto.global.exception.Code;
import com.umc.gusto.global.exception.GeneralException;
import com.umc.gusto.global.exception.customException.NotFoundException;
import com.umc.gusto.global.util.RedisService;
import com.umc.gusto.global.util.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    private final SocialRepository socialRepository;
    private final JwtService jwtService;
    private final RedisService redisService;
    private final FollowRepository followRepository;
    private final S3Service s3Service;

    private static final long NICKNAME_EXPIRED_TIME = 1000L * 60 * 15;
    private static final int MAX_NICKNAME_NUMBER = 999;
    private static final int MIN_NICKNAME_NUMBER = 1;
    private static final int FOLLOW_LIST_PAGE = 30;


    @Value("${default.img.url}")
    private String DEFAULT_PROFILE_IMG;

    @Override
    @Transactional
    public Tokens createUser(MultipartFile multipartFile, SignUpRequest request) {
        // 이미 가입된 계정이 존재함
        socialRepository.findBySocialTypeAndProviderId(Social.SocialType.valueOf(request.getProvider()), request.getProviderId())
                .ifPresent( info -> { throw new GeneralException(Code.USER_ALREADY_SIGNUP); });

        // redis에 임시 저장된 닉네임 정보를 삭제 및 닉네임 중복 검사
        redisService.deleteValues(request.getNickname());
        checkNickname(request.getNickname());

        String profileImg = DEFAULT_PROFILE_IMG;

        if(multipartFile != null) {
            // profileImg를 이미지 파일로 받았다면
            profileImg = s3Service.uploadImage(multipartFile);
        } else if(request.getProfileImg() != null) {
            // profileImg를 이미지로 받지 않고, url로 받았다면
            profileImg = request.getProfileImg();
        }

        // user 생성
        User user = User.builder()
                .userId(UUID.randomUUID())
                .nickname(request.getNickname())
                .gender(User.Gender.valueOf(request.getGender()))
                .age(User.Age.valueOf(request.getAge()))
                .profileImage(profileImg)
                .build();

        user = userRepository.save(user);

        // 새로운 소셜 정보 생성
        Social socialInfo = Social.builder()
                .socialType(Social.SocialType.valueOf(request.getProvider()))
                .providerId(request.getProviderId())
                .user(user)
                .build();

        socialRepository.save(socialInfo);

        // access-token 및 refresh-token 생성
        Tokens tokens = jwtService.createToken(String.valueOf(user.getUserId()));
        redisService.setValuesWithTimeout(tokens.getRefreshToken(), String.valueOf(user.getUserId()), JwtConfig.REFRESH_TOKEN_VALID_TIME);

        return tokens;
    }

    @Override
    @Transactional(readOnly = true)
    public void checkNickname(String nickname) {
        // redis 내 검색
        redisService.getValues(nickname).ifPresent(a -> {
            throw new GeneralException(Code.USER_DUPLICATE_NICKNAME);
        });

        // DB 내 검색
        if(userRepository.countUsersByNicknameAndMemberStatusIs(nickname, User.MemberStatus.ACTIVE) > 0) {
            throw new GeneralException(Code.USER_DUPLICATE_NICKNAME);
        }
    }

    @Override
    @Transactional
    public void confirmNickname(String nickname) {
        checkNickname(nickname);
        redisService.setValuesWithTimeout(nickname, "null", NICKNAME_EXPIRED_TIME);
    }

    @Override
    public NicknameResponse generateRandomNickname() {
        String nickname;

        // 중복 없는 닉네임이 생성될 때까지 반복
        while (true) {
            try {
                // 단어 두 개 선택
                String[] nicknames = NicknameBucket.getNicknames();

                // MIN_NICKNAME_NUMBER : 1 ~ MAX_NICKNAME_NUMBER : 999 까지 수 중 랜덤 수 생성
                int random = (int) (Math.random() * (MAX_NICKNAME_NUMBER - MIN_NICKNAME_NUMBER) + MIN_NICKNAME_NUMBER);

                nickname = nicknames[0] + " " + nicknames[1] + " " + random;

                checkNickname(nickname);
            } catch (RuntimeException e) {
                continue;
            }
            break;
        }

        return new NicknameResponse(nickname);
    }

    @Override
    public Tokens signIn(SignInRequest signInRequest) {
        // social 정보 확인
        Social social = socialRepository.findBySocialTypeAndProviderId(Social.SocialType.valueOf(signInRequest.getProvider()), signInRequest.getProviderId())
                .orElseThrow(() -> new GeneralException(Code.USER_NOT_OUR_CLIENT));

        // social 정보와 연결된 유저 정보 불러옴
        User user = social.getUser();

        // access-token 및 refresh-token 생성
        Tokens tokens = jwtService.createToken(String.valueOf(user.getUserId()));
        redisService.setValuesWithTimeout(tokens.getRefreshToken(), String.valueOf(user.getUserId()), JwtConfig.REFRESH_TOKEN_VALID_TIME);

        return tokens;
    }

    @Override
    public void signOut(User user, String refreshToken) {
        jwtService.matchCheckTokens(user.getUserId(), refreshToken);
        redisService.deleteValues(refreshToken);
    }

    @Override
    public FeedProfileResponse getProfile(User user, String nickname) {
        User target;

        if(nickname.equals("my")) {
            target = user;
        } else {
            target = userRepository.findByNicknameAndMemberStatusIs(nickname, User.MemberStatus.ACTIVE)
                    .orElseThrow(() -> new GeneralException(Code.USER_NOT_FOUND));
        }

        AtomicBoolean followed = new AtomicBoolean(false);

        if(user != null) {
            followRepository.findByFollowerAndFollowing(user, target).ifPresent(a -> followed.set(true));
        }

        return FeedProfileResponse.builder()
                .nickname(target.getNickname())
                .profileImg(target.getProfileImage())
                .review(target.getReviewCnt())
                .following(target.getFollowing())
                .follower(target.getFollower())
                .followed(followed.get())
                .build();
    }

    @Override
    @Transactional
    public void updateNickname(User user, String nickname) {
        redisService.deleteValues(nickname);
        checkNickname(nickname);

        user.updateNickname(nickname);

        userRepository.save(user);
    }

    @Override
    public ProfileResponse getProfile(User user) {
        return ProfileResponse.builder()
                .profileImg(user.getProfileImage())
                .nickname(user.getNickname())
                .age(user.getAge().toString())
                .gender(user.getGender().toString())
                .build();
    }

    @Override
    @Transactional
    public void updateProfile(User user, MultipartFile profileImg, UpdateProfileRequest request) {
        if(profileImg != null) {
            s3Service.deleteImageFromUrl(user.getProfileImage());
            String newProfile = s3Service.uploadImage(profileImg);

            user.updateProfile(newProfile);
        }

        if(request != null) {
            if(request.getAge() != null) {
                user.updateAge(User.Age.valueOf(request.getAge()));
            }

            if(request.getGender() != null) {
                user.updateGender(User.Gender.valueOf(request.getGender()));
            }

            if(request.getNickname() != null) {
                updateNickname(user, request.getNickname());
            }
        }

        userRepository.save(user);
    }

    @Override
    public PublishingInfoResponse getPublishingInfo(User user) {
        return PublishingInfoResponse.builder()
                .publishReview(user.getPublishReview() == PublishStatus.PUBLIC)
                .publishPin(user.getPublishCategory() == PublishStatus.PUBLIC)
                .publishRoute(user.getPublishRoute() == PublishStatus.PUBLIC)
                .build();
    }

    @Override
    public void updatePublishingInfo(User user, PublishingInfoRequest request) {
        PublishStatus reviewStatus = (request.getPublishReview()) ?PublishStatus.PUBLIC : PublishStatus.PRIVATE;
        PublishStatus pinStatus = (request.getPublishPin()) ? PublishStatus.PUBLIC : PublishStatus.PRIVATE;
        PublishStatus routeStatus = (request.getPublishRoute()) ? PublishStatus.PUBLIC : PublishStatus.PRIVATE;

        user.updatePublishReview(reviewStatus);
        user.updatePublishPin(pinStatus);
        user.updatePublishRoute(routeStatus);

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void followUser(User user, String nickname) {
        User target = userRepository.findByNicknameAndMemberStatusIs(nickname, User.MemberStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException(Code.USER_NOT_FOUND));

        // 팔로우 대상이 자기 자신인지 check
        if(target.getUserId().equals(user.getUserId())) {
            throw new GeneralException(Code.USER_FOLLOW_SELF);
        }

        // 이미 follow한 내역이 있는지 check
        followRepository.findByFollowerAndFollowing(user, target)
                .ifPresent(follow -> {
                    throw new GeneralException(Code.USER_FOLLOW_ALREADY);
                });

        Follow newFollow = Follow.builder()
                .follower(user)
                .following(target)
                .build();

        followRepository.save(newFollow);

        // target의 팔로워 수 1 증가
        target.updateFollower(target.getFollower() + 1);
        user.updateFollowing(user.getFollowing() + 1);
        userRepository.save(target);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void unfollowUser(User user, String nickname) {
        User target = userRepository.findByNicknameAndMemberStatusIs(nickname, User.MemberStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException(Code.USER_NOT_FOUND));

        Follow followInfo = followRepository.findByFollowerAndFollowing(user, target)
                .orElseThrow(() -> new NotFoundException(Code.USER_FOLLOW_NOT_FOUND));

        followRepository.delete(followInfo);

        // target의 팔로워 수 1 감소
        target.updateFollower(target.getFollower() - 1);
        user.updateFollowing(user.getFollowing() - 1);
        userRepository.save(target);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public PagingResponse getFollowList(User user, Long followId) {
        Page<Follow> followList;

        if(followId == null) {
            followList = followRepository.findFollowList(user, Pageable.ofSize(FOLLOW_LIST_PAGE));
        } else {
            followList = followRepository.findFollowList(user, followId, Pageable.ofSize(FOLLOW_LIST_PAGE));
        }

        // page가 존재하지 않으면 throw Exception
        if(followList.isEmpty()) {
            throw new NotFoundException(Code.USER_FOLLOW_NOT_EXIST);
        }

        // res mapping
        List<FollowResponse> result = followList.stream()
                .map(follow -> {
                    FollowResponse item = FollowResponse.builder()
                            .followId(follow.getFollowId())
                            .nickname(follow.getFollowing().getNickname())
                            .profileImg(follow.getFollowing().getProfileImage())
                            .build();

                    return item;
                })
                .collect(Collectors.toList());

        return PagingResponse.builder()
                .hasNext(followList.hasNext())
                .result(result)
                .build();
    }

    @Override
    @Transactional
    public PagingResponse getFollwerList(User user, Long followId) {
        Page<Follow> followList;

        if(followId == null) {
            followList = followRepository.findFollwerList(user, Pageable.ofSize(FOLLOW_LIST_PAGE));
        } else {
            followList = followRepository.findFollwerList(user, followId, Pageable.ofSize(FOLLOW_LIST_PAGE));
        }

        // page가 존재하지 않으면 throw Exception
        if(followList.isEmpty()) {
            throw new NotFoundException(Code.USER_FOLLOWER_NOT_EXIST);
        }

        // res mapping
        List<FollowResponse> result = followList.stream()
                .map(follow -> {
                    FollowResponse item = FollowResponse.builder()
                            .followId(follow.getFollowId())
                            .nickname(follow.getFollower().getNickname())
                            .profileImg(follow.getFollower().getProfileImage())
                            .build();

                    return item;
                })
                .collect(Collectors.toList());

        return PagingResponse.builder()
                .hasNext(followList.hasNext())
                .result(result)
                .build();
    }
}
