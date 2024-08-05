package com.umc.gusto.domain.review.service;

import com.umc.gusto.domain.review.entity.HashTag;
import com.umc.gusto.domain.review.entity.Liked;
import com.umc.gusto.domain.review.entity.Review;
import com.umc.gusto.domain.review.entity.Tagging;
import com.umc.gusto.domain.review.model.request.CreateReviewRequest;
import com.umc.gusto.domain.review.model.request.UpdateReviewRequest;
import com.umc.gusto.domain.review.model.response.ReviewDetailResponse;
import com.umc.gusto.domain.review.repository.HashTagRepository;
import com.umc.gusto.domain.review.repository.LikedRepository;
import com.umc.gusto.domain.review.repository.ReviewRepository;
import com.umc.gusto.domain.store.entity.Store;
import com.umc.gusto.domain.store.repository.StoreRepository;
import com.umc.gusto.domain.user.entity.User;
import com.umc.gusto.domain.user.repository.UserRepository;
import com.umc.gusto.global.common.BaseEntity;
import com.umc.gusto.global.common.PublishStatus;
import com.umc.gusto.global.exception.Code;
import com.umc.gusto.global.exception.GeneralException;
import com.umc.gusto.global.exception.customException.NoPermission;
import com.umc.gusto.global.exception.customException.NotFoundException;
import com.umc.gusto.global.exception.customException.PrivateItemException;
import com.umc.gusto.global.util.S3Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService{
    @Value("${default.img.url}")
    private String DEFAULT_IMG;
    private final ReviewRepository reviewRepository;
    private final StoreRepository storeRepository;
    private final HashTagRepository hashTagRepository;
    private final LikedRepository likedRepository;
    private final S3Service s3Service;
    private final UserRepository userRepository;

    public void validateReviewByUser(final User user, final Long reviewId){
        if(!reviewRepository.existsByReviewIdAndUser(reviewId, user)){
            throw new NoPermission(Code.USER_NO_PERMISSION_FOR_REVIEW);
        }
    }

    @Override
    @Transactional
    public void createReview(User user, List<MultipartFile> images, CreateReviewRequest createReviewRequest) {
        Store store= storeRepository.findById(createReviewRequest.getStoreId()).orElseThrow(()-> new NotFoundException(Code.STORE_NOT_FOUND));
        LocalDate visitedAt = createReviewRequest.getVisitedAt();
        if(createReviewRequest.getVisitedAt()==null){
            visitedAt = LocalDate.now();
        }
        //리뷰 생성
        Review review = Review.builder()
                .skipCheck(createReviewRequest.isSkipCheck())
                .store(store)
                .user(user)
                .visitedAt(visitedAt)
                .menuName(createReviewRequest.getMenuName())
                .taste(createReviewRequest.getTaste())
                .comment(createReviewRequest.getComment())
                .publishReview(PublishStatus.of(createReviewRequest.isPublicCheck()))
                .build();

        //TODO: review 엔티티에서 이미지를 분리하거나 monogoDB를 쓰는게 나을 듯, 나머지 기능 개발 후 바꿀 예정
        //s3에 이미지 저장
        if(images!=null){
            updateImages(images, review);
        }else{ //이미지가 null인 경우 디폴트 이미지로 저장
            review.updateImg1(DEFAULT_IMG);
        }

        //리뷰와 해시태그 연결
        if(createReviewRequest.getHashTagId()!=null){
            createReviewRequest.getHashTagId().forEach(hashTag -> connectHashTag(review, hashTag));
        }

        reviewRepository.save(review);
        user.updateCountReview(true);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateReview(Long reviewId, List<MultipartFile> images, UpdateReviewRequest updateReviewRequest) {
        Review review = reviewRepository.findByReviewIdAndStatus(reviewId, BaseEntity.Status.ACTIVE).orElseThrow(()->new NotFoundException(Code.REVIEW_NOT_FOUND));

        //방문일자 변경
        if(updateReviewRequest.getVisitedAt()!=null){
            review.updateVisitedAt(updateReviewRequest.getVisitedAt());
        }

        //메뉴명 변경
        if(updateReviewRequest.getMenuName()!=null){
            review.updateMenu(updateReviewRequest.getMenuName());
        }
        //해시태그 변경
        if(updateReviewRequest.getHashTagId()!=null){
            //두가지 방법이 있음 => 1. 기존의 해시태그 다 지우고 다시 생성 / 2. 기존의 해시태그와 현재 해시태그 값을 비교해서 지우고 생성
            //TODO: 어떤게 더 빠른지 성능 측정 하기
//            List<HashTag> originalHashTag = review.getTaggingSet().stream().map(Tagging::getHashTag).toList();
            //기존 해시태그 지우기
            review.getTaggingSet().clear();
            //새로운 해시태그로 생성
            updateReviewRequest.getHashTagId().forEach(changeHashTag -> connectHashTag(review, changeHashTag));
        }
        if(updateReviewRequest.getTaste()!=null){
            review.updateTaste(updateReviewRequest.getTaste());
        }
        if(updateReviewRequest.getComment()!=null){
            review.updateComment(updateReviewRequest.getComment());
        }
        if(images!=null){
            //TODO: review 엔티티에서 이미지를 분리하거나 monogoDB를 쓰는게 나을 듯, 나머지 기능 개발 후 바꿀 예정
            updateImages(images, review);
        }

        if(updateReviewRequest.getPublicCheck() != null){
            review.updatePublishReview(updateReviewRequest.getPublicCheck());
        }

        reviewRepository.save(review);
    }

    @Override
    @Transactional
    public void deleteReview(User user, Long reviewId) {
        Review review = reviewRepository.findByReviewIdAndStatus(reviewId, BaseEntity.Status.ACTIVE).orElseThrow(()->new NotFoundException(Code.REVIEW_NOT_FOUND));
        review.updateStatus(BaseEntity.Status.INACTIVE);
        reviewRepository.save(review);
        user.updateCountReview(false);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewDetailResponse getReview(Long reviewId) {
        Review review = reviewRepository.findByReviewIdAndStatus(reviewId, BaseEntity.Status.ACTIVE).orElseThrow(()->new NotFoundException(Code.REVIEW_NOT_FOUND));
        //TODO: 후에 각 리뷰마다의 공개, 비공개를 확인해서 주는거로 수정하기
        //TODO: 해당 함수는 사용자의 공개를 체크하면 안 될 것 같은데?? 리뷰 하나를 조회하는 것으로 분명 앞에서 리뷰가 보였으니까 해당 함수를 사용할 것임
        if(!review.getUser().getPublishReview().equals(PublishStatus.PUBLIC)){
            throw new PrivateItemException(Code.NO_PUBLIC_REVIEW);
        }

        List<Long> hashTags = new ArrayList<>();
        review.getTaggingSet().stream().map(r-> r.getHashTag().getHasTagId()).forEach(hashTags::add);

        //리뷰에 해시태그가 없다면 response에 해시태그 없이 반환
        if(hashTags.isEmpty()) ReviewDetailResponse.of(review, null);
        return ReviewDetailResponse.of(review, hashTags);
    }

    @Override
    @Transactional
    public void likeReview(User user, Long reviewId) {
        Review review = reviewRepository.findByReviewIdAndStatus(reviewId, BaseEntity.Status.ACTIVE).orElseThrow(()->new NotFoundException(Code.REVIEW_NOT_FOUND));

        //본인 리뷰를 좋아요하는지 확인
        if(review.getUser().getUserId().equals(user.getUserId())){ //TODO: .equals로 하는 동등성 비교가 안되서 DB의 @ID를 비교하는 식으로 했으나 비즈니스 키로 equals를 구현해보자.
            throw new GeneralException(Code.NO_ONESELF_LIKE);
        }

        //이미 리뷰를 했었는지 확인
        if(likedRepository.existsByUserAndReview(user, review)){
            throw new GeneralException(Code.ALREADY_LIKED_REVIEW);
        }

        review.updateLiked("like");
        reviewRepository.save(review);

        Liked liked = Liked.builder().user(user).review(review).build();
        likedRepository.save(liked);
    }

    @Override
    @Transactional
    public void unlikeReview(User user, Long reviewId) {
        Review review = reviewRepository.findByReviewIdAndStatus(reviewId, BaseEntity.Status.ACTIVE).orElseThrow(()->new NotFoundException(Code.REVIEW_NOT_FOUND));

        //해당 리뷰를 좋아요 클릭한 적이 있는지 확인
        Liked liked = likedRepository.findByUserAndReview(user, review).orElseThrow(()->new GeneralException(Code.NO_LIKE_REVIEW));

        likedRepository.delete(liked);
        review.updateLiked("unlike");
        reviewRepository.save(review);
    }

    private void connectHashTag(Review review, Long hashTagId){
        HashTag hashTag = hashTagRepository.findById(hashTagId).orElseThrow(()-> new NotFoundException(Code.HASHTAG_NOT_FOUND));
        Tagging tagging = Tagging.builder()
                .hashTag(hashTag)
                .review(review)
                .build();
        review.connectHashTag(tagging);
    }

    private void updateImages(List<MultipartFile> images, Review review){
        List<String> imageUrls = s3Service.uploadImages(images);
        review.updateImg1(imageUrls.get(0));
        if(imageUrls.size()>1) review.updateImg2(imageUrls.get(1));
        if(imageUrls.size()>2) review.updateImg3(imageUrls.get(2));
        if(imageUrls.size()>3) review.updateImg4(imageUrls.get(3));
    }

}
