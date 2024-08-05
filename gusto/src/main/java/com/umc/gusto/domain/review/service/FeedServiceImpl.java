package com.umc.gusto.domain.review.service;

import com.umc.gusto.domain.review.entity.Review;
import com.umc.gusto.domain.review.model.response.*;
import com.umc.gusto.domain.review.repository.LikedRepository;
import com.umc.gusto.domain.review.repository.ReviewRepository;
import com.umc.gusto.domain.user.entity.User;
import com.umc.gusto.global.common.BaseEntity;
import com.umc.gusto.global.common.PublishStatus;
import com.umc.gusto.global.exception.Code;
import com.umc.gusto.global.exception.customException.NotFoundException;
import com.umc.gusto.global.exception.customException.PrivateItemException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService{
    private final ReviewRepository reviewRepository;
    private final LikedRepository likedRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RandomFeedResponse> getRandomFeed(User user) {
        List<Review> feedList = reviewRepository.findRandomFeedByUser(user.getUserId());
        return feedList.stream().map(RandomFeedResponse::of).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SearchFeedResponse searchFeed(String keyword, List<Long> hashTags, Long cursor) { //TODO: 검색 결과가 랜덤으로 다시 정렬되야 할듯 & 자기 리뷰가 아니여야함
        Page<Review> searchResult = null;
        PageRequest pageRequest = PageRequest.of(0,33);

        if(cursor == null){
            cursor = Long.MAX_VALUE;
        }

        //맛집과 해시태그를 함께 검색하는 경우
        if(keyword!=null && hashTags!=null){
            for(Long hashTagId: hashTags){
//                searchResult.addAll(reviewRepository.searchByStoreAndHashTagContains(keyword, hashTagId, pageRequest));
                searchResult = reviewRepository.searchByStoreAndHashTagContains(keyword, hashTagId, cursor, pageRequest);
            }
        } else if(keyword!=null){ //맛집을 검색하는 경우
            searchResult = reviewRepository.searchByStoreContains(keyword, cursor, pageRequest);
        } else if(hashTags!=null){
            for(Long hashTagId: hashTags){
                searchResult = reviewRepository.searchByHashTagContains(hashTagId, cursor, pageRequest);
            }
        }

        if(searchResult == null){
            return SearchFeedResponse.builder().build();
        }

        boolean checkNext = searchResult.hasNext();
        List<BasicViewResponse> basicViewResponse = searchResult.stream().map(BasicViewResponse::of).toList();
        Long cursorId = basicViewResponse.isEmpty() ? null : basicViewResponse.get(basicViewResponse.size()-1).getReviewId();

        return SearchFeedResponse.of(basicViewResponse, checkNext, cursorId);
    }

    //TODO: 해당 함수도 이미 피드에서 보인 리뷰임. 그래서 디테일이 안보이면 안됨 사용자 공개 체크를 할 필요가 없음
    @Override
    @Transactional(readOnly = true)
    public FeedDetailResponse getFeedDetail(User user, Long reviewId) {
        //TOOD: reviewServiceImpl와 중복되는 코드 분리하기
        Review review = reviewRepository.findByReviewIdAndStatus(reviewId, BaseEntity.Status.ACTIVE).orElseThrow(()->new NotFoundException(Code.REVIEW_NOT_FOUND));
        //TODO: 후에 각 리뷰마다의 공개, 비공개를 확인해서 주는거로 수정하기
        if(!review.getUser().getPublishReview().equals(PublishStatus.PUBLIC)){
            throw new PrivateItemException(Code.NO_PUBLIC_REVIEW);
        }

        List<Long> hashTags = new ArrayList<>();
        review.getTaggingSet().stream().map(r-> r.getHashTag().getHasTagId()).forEach(hashTags::add);

        //이 리뷰를 보는 유저가 해당 리뷰를 좋아요했는지 체크
        boolean likeCheck = likedRepository.existsByUserAndReview(user, review);

        //리뷰에 해시태그가 없다면 response에 해시태그 없이 반환
        if(hashTags.isEmpty()) return FeedDetailResponse.of(review, null, likeCheck);
        return FeedDetailResponse.of(review, hashTags, likeCheck);
    }
}
