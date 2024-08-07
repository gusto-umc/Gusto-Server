package com.umc.gusto.domain.myCategory.service;

import com.umc.gusto.domain.myCategory.entity.MyCategory;
import com.umc.gusto.domain.myCategory.entity.Pin;
import com.umc.gusto.domain.myCategory.model.request.CreateMyCategoryRequest;
import com.umc.gusto.domain.myCategory.model.request.UpdateMyCategoryRequest;
import com.umc.gusto.domain.myCategory.model.response.MyCategoryResponse;
import com.umc.gusto.domain.myCategory.model.response.PagingResponse;
import com.umc.gusto.domain.myCategory.model.response.PinByMyCategoryResponse;
import com.umc.gusto.domain.myCategory.repository.MyCategoryRepository;
import com.umc.gusto.domain.myCategory.repository.PinRepository;
import com.umc.gusto.domain.review.entity.Review;
import com.umc.gusto.domain.review.repository.ReviewRepository;
import com.umc.gusto.domain.store.entity.Store;
import com.umc.gusto.domain.user.entity.User;
import com.umc.gusto.domain.user.repository.UserRepository;
import com.umc.gusto.global.common.BaseEntity;
import com.umc.gusto.global.common.PublishStatus;
import com.umc.gusto.global.exception.Code;
import com.umc.gusto.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MyCategoryServiceImpl implements MyCategoryService {

    private final MyCategoryRepository myCategoryRepository;
    private final PinRepository pinRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    private static final int MY_CATEGORY_PAGE_SIZE = 7;
    private static final int PIN_PAGE_SIZE = 5;

    @Transactional(readOnly = true)
    public PagingResponse getAllMyCategory(User user, String nickname, String townName, Long myCategoryId) {
        Page<MyCategory> myCategoryList;
        if (nickname != null) {
            user = userRepository.findByNickname(nickname)      // 타 닉네임 조회
                    .orElseThrow(() -> new GeneralException(Code.USER_NOT_FOUND));
            if (myCategoryId != null) {
                myCategoryList = myCategoryRepository.findByUserNicknameAndPublishCategoryPublicPaging(user, myCategoryId, Pageable.ofSize(MY_CATEGORY_PAGE_SIZE));     // 받아온 nickname과 User의 nickname 값이 다른 경우(쿼리문 사용)
            } else {
                myCategoryList = myCategoryRepository.findByUserNicknameAndPublishCategoryPublic(user, Pageable.ofSize(MY_CATEGORY_PAGE_SIZE));
            }
        } else {    // 내 카테고리 조회
            if (myCategoryId != null) {
                myCategoryList = myCategoryRepository.findByUserNicknameAndPublishCategoryPaging(user, myCategoryId, Pageable.ofSize(MY_CATEGORY_PAGE_SIZE));
            } else {
                myCategoryList = myCategoryRepository.findByUserNicknameAndPublishCategory(user, Pageable.ofSize(MY_CATEGORY_PAGE_SIZE));
            }

        }

        User finalUser = user;
        List<MyCategoryResponse> result = myCategoryList.stream()
                .map(myCategory -> {
                    List<Pin> pinList;
                    if (townName != null) {
                        pinList = pinRepository.findPinsByMyCategoryAndTownNameAndPinIdDESC(myCategory, townName);     // 먼저 카테고리로 구분
                    } else {
                        pinList = pinRepository.findPinsByMyCategoryAndPinIdDESC(myCategory);     // 먼저 카테고리로 구분
                    }
                    return MyCategoryResponse.builder()
                            .myCategoryId(myCategory.getMyCategoryId())
                            .myCategoryName(myCategory.getMyCategoryName())
                            .myCategoryScript(myCategory.getMyCategoryScript())
                            .myCategoryIcon(myCategory.getMyCategoryIcon())
                            .publishCategory(myCategory.getPublishCategory() == PublishStatus.PUBLIC)           // PublishCategory가 PUBLIC이면 true 반환
                            .userPublishCategory(finalUser.getPublishCategory() == PublishStatus.PUBLIC)
                            .pinCnt(pinList.size())
                            .build();
                })
                .collect(Collectors.toList());

        return PagingResponse.builder()
                .hasNext(myCategoryList.hasNext())
                .result(result)
                .build();
    }

    @Transactional(readOnly = true)
    public PagingResponse getAllPinByMyCategory(User user, String nickname, Long myCategoryId, String townName, Long pinId, String storeName, String sort) {
        Optional<MyCategory> myCategory;

        final String finalSort = (sort == null) ? "default" : sort;

        Page<Pin> pinList;
        if (nickname != null) {
            if (nickname.equals(user.getNickname())) {
                throw new GeneralException(Code.USER_NOT_FOUND_SELF);
                }
            user = userRepository.findByNickname(nickname)
                    .orElseThrow(() -> new GeneralException(Code.USER_NOT_FOUND));
            myCategory = myCategoryRepository.findByMyCategoryPublicIdAndUserNickname(nickname, myCategoryId);          // PUBLIC 값에 따라 보이는 CATEGORY 처리, PIN에서까지 하지않아도 됨
        } else {
            myCategory = myCategoryRepository.findByMyCategoryIdAndUserNickname(user.getNickname(), myCategoryId);
        }

        if (townName != null) {
            if (pinId != null || storeName != null)  {
                pinList = myCategory.map(category -> switch (finalSort) {
                    case "oldest" ->
                            pinRepository.findPinsByMyCategoryAndTownNameAndPinIdASCPaging(category, townName, pinId, Pageable.ofSize(PIN_PAGE_SIZE));
                    case "storeName_asc" ->
                            pinRepository.findPinsByMyCategoryAndTownNameAndStoreNameASCPaging(category, townName, pinId, storeName, Pageable.ofSize(PIN_PAGE_SIZE));
                    case "storeName_desc" ->
                            pinRepository.findPinsByMyCategoryAndTownNameAndStoreNameDESCPaging(category, townName, pinId, storeName, Pageable.ofSize(PIN_PAGE_SIZE));
                    default ->
                            pinRepository.findPinsByMyCategoryAndTownNameAndPinIdDESCPaging(category, townName, pinId, Pageable.ofSize(PIN_PAGE_SIZE));
                }).orElseThrow(() -> new GeneralException(Code.MY_CATEGORY_NOT_FOUND));
            } else {
                pinList = myCategory.map(category -> switch (finalSort) {
                    case "oldest" ->
                            pinRepository.findPinsByMyCategoryAndTownNameAndPinIdASCFirstPaging(category, townName, Pageable.ofSize(PIN_PAGE_SIZE));
                    case "storeName_asc" ->
                            pinRepository.findPinsByMyCategoryAndTownNameAndStoreNameASCFirstPaging(category, townName, Pageable.ofSize(PIN_PAGE_SIZE));
                    case "storeName_desc" ->
                            pinRepository.findPinsByMyCategoryAndTownNameAndStoreNameDESCFirstPaging(category, townName, Pageable.ofSize(PIN_PAGE_SIZE));
                    default ->
                            pinRepository.findPinsByMyCategoryAndTownNameAndPinIdDESCFirstPaging(category, townName, Pageable.ofSize(PIN_PAGE_SIZE));
                }).orElseThrow(() -> new GeneralException(Code.MY_CATEGORY_NOT_FOUND));
            }
        } else {
            if (pinId != null || storeName != null) {
                pinList = myCategory.map(category -> switch (finalSort) {
                    case "oldest" ->
                            pinRepository.findPinsByMyCategoryAndPinIdASCPaging(category, pinId, Pageable.ofSize(PIN_PAGE_SIZE));
                    case "storeName_asc" ->
                            pinRepository.findPinsByMyCategoryAndStoreNameASCPaging(category, pinId, storeName, Pageable.ofSize(PIN_PAGE_SIZE));
                    case "storeName_desc" ->
                            pinRepository.findPinsByMyCategoryAndStoreNameDESCPaging(category, pinId, storeName, Pageable.ofSize(PIN_PAGE_SIZE));
                    default ->
                            pinRepository.findPinsByMyCategoryAndPinIdDESCPaging(category, pinId, Pageable.ofSize(PIN_PAGE_SIZE));
                }).orElseThrow(() -> new GeneralException(Code.MY_CATEGORY_NOT_FOUND));
            } else {
                pinList = myCategory.map(category -> switch (finalSort) {
                    case "oldest" ->
                            pinRepository.findPinsByMyCategoryAndPinIdASCFirstPaging(category, Pageable.ofSize(PIN_PAGE_SIZE));
                    case "storeName_asc" ->
                            pinRepository.findPinsByMyCategoryAndStoreNameASCFirstPaging(category, Pageable.ofSize(PIN_PAGE_SIZE));
                    case "storeName_desc" ->
                            pinRepository.findPinsByMyCategoryAndStoreNameDESCFirstPaging(category, Pageable.ofSize(PIN_PAGE_SIZE));
                    default ->
                            pinRepository.findPinsByMyCategoryAndPinIdDESCFirstPaging(category, Pageable.ofSize(PIN_PAGE_SIZE));
                }).orElseThrow(() -> new GeneralException(Code.MY_CATEGORY_NOT_FOUND));
            }
        }


        User finalUser = user;

        List<PinByMyCategoryResponse> result = pinList.stream()                                     // townName을 기준으로 보일 수 있는 store가 포함된 pin만 보이기
                .map(pin -> {
                    Store store = pin.getStore();
                    List<Review> topReviews = reviewRepository.findFirst4ByStoreOrderByLikedDesc(store);               // 가장 좋아요가 많은 review                     // 가장 좋아요가 많은 review 이미지(TO DO: 3개 출력으로 변경)
                    Integer reviewCnt = reviewRepository.countByStoreAndUserNickname(store, finalUser.getNickname());             // 내가 작성한 리뷰의 개수 == 방문 횟수

                    String img1 = !topReviews.isEmpty() ? topReviews.get(0).getImg1() : "";
                    String img2 = topReviews.size() > 1 ? topReviews.get(1).getImg1() : "";
                    String img3 = topReviews.size() > 2 ? topReviews.get(2).getImg1() : "";


                    return  PinByMyCategoryResponse.builder()
                            .pinId(pin.getPinId())
                            .storeId(store.getStoreId())
                            .storeName(store.getStoreName())
                            .address(store.getAddress())
                            .img1(img1)
                            .img2(img2)
                            .img3(img3)
                            .reviewCnt(reviewCnt)
                            .build();
                })
                .collect(Collectors.toList());

        return PagingResponse.builder()
                .hasNext(pinList.hasNext())
                .result(result)
                .build();
    }

    @Transactional
    public void createMyCategory(User user, CreateMyCategoryRequest createMyCategory) {
        // 중복 이름 체크
        myCategoryRepository.findByMyCategoryNameAndUser(createMyCategory.getMyCategoryName(), user)
                .ifPresent(existingCategory -> {
                    throw new GeneralException(Code.MY_CATEGORY_DUPLICATE_NAME);
                });

        // 중복된 이름이 없으면 새로운 MyCategory 생성
        MyCategory myCategory = MyCategory.builder()
                .myCategoryName(createMyCategory.getMyCategoryName())
                .myCategoryIcon(createMyCategory.getMyCategoryIcon())
                .myCategoryScript(createMyCategory.getMyCategoryScript())
                .publishCategory(createMyCategory.getPublishCategory())
                .user(user)
                .build();

        myCategoryRepository.save(myCategory);
    }


    @Transactional
    public void modifyMyCategory(User user, Long myCategoryId, UpdateMyCategoryRequest updateMyCategory) {
        MyCategory existingMyCategory = myCategoryRepository.findByUserAndMyCategoryId(user,myCategoryId)
                .orElseThrow(() -> new GeneralException(Code.MY_CATEGORY_NOT_FOUND));

        // 중복 이름 체크
        if (updateMyCategory.getMyCategoryName() != null && !updateMyCategory.getMyCategoryName().equals(existingMyCategory.getMyCategoryName())) {
            myCategoryRepository.findByMyCategoryNameAndUser(updateMyCategory.getMyCategoryName(), user)
                    .ifPresent(existingCategory -> {
                throw new GeneralException(Code.MY_CATEGORY_DUPLICATE_NAME);
            });
        }
        // 변경하려는 필드만 업데이트
        if (updateMyCategory.getMyCategoryName() != null) {
            existingMyCategory.updateMyCategoryName(updateMyCategory.getMyCategoryName());
        }

        if (updateMyCategory.getMyCategoryIcon() != null) {
            existingMyCategory.updateMyCategoryIcon(updateMyCategory.getMyCategoryIcon());
        }

        if (updateMyCategory.getMyCategoryScript() != null) {
            existingMyCategory.updateMyCategoryScript(updateMyCategory.getMyCategoryScript());
        }

        if (updateMyCategory.getPublishCategory() != null) {
            existingMyCategory.updatePublishCategory(updateMyCategory.getPublishCategory());
        }

        myCategoryRepository.save(existingMyCategory);


    }

    @Transactional
    public void deleteMyCategories(User user, List<Long> myCategoryIds) {
        for (Long myCategoryId : myCategoryIds) {
            MyCategory existingMyCategory = myCategoryRepository.findByUserAndMyCategoryId(user, myCategoryId)
                    .orElseThrow(() -> new GeneralException(Code.MY_CATEGORY_NOT_FOUND));

            existingMyCategory.updateStatus(BaseEntity.Status.INACTIVE);

            myCategoryRepository.save(existingMyCategory);
        }
    }

}