package com.umc.gusto.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum Code {
    INTERNAL_SEVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 500, "서버 에러 발생, 관리자에게 문의해주세요."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST,400, "잘못된 요청입니다."),
    NO_PERMISSION(HttpStatus.FORBIDDEN,403, "해당 권한이 없습니다."),

    //User 관련 에러 +0
    USER_DUPLICATE_NICKNAME(HttpStatus.CONFLICT, 409001, "이미 사용중인 닉네임입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, 404001, "존재하지 않는 유저입니다."),
    USER_ALREADY_SIGNUP(HttpStatus.CONFLICT, 409003, "가입이 완료된 유저입니다."),
    USER_FOLLOW_NOT_FOUND(HttpStatus.NOT_FOUND, 404002, "팔로우한 유저가 아닙니다."),
    USER_FOLLOW_NO_MORE_CONTENT(HttpStatus.NOT_FOUND, 404003, "리스트가 더이상 존재하지 않습니다."),
    USER_FOLLOW_ALREADY(HttpStatus.CONFLICT, 409002, "이미 팔로우했습니다."),
    USER_FOLLOW_SELF(HttpStatus.FORBIDDEN, 403001, "자신을 팔로우할 수 없습니다."),
    USER_NOT_FOUND_SELF(HttpStatus.FORBIDDEN, 403002, "자신의 닉네임을 사용해 접근할 수 없습니다."),

    //Store 관련 에러 +1
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, 404101, "존재하지 않는 가게입니다."),
    OPENING_HOURS_NOT_FOUND(HttpStatus.NOT_FOUND, 404102,"해당 가게에 대한 운영시간이 존재하지 않습니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, 404103,"해당 가게에 대한 카테고리가 존재하지 않습니다."),

    //Review 관련 에러 +2
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, 404201, "존재하지 않는 리뷰입니다."),
    HASHTAG_NOT_FOUND(HttpStatus.NOT_FOUND, 404202, "존재하지 않는 해시태그입니다."),
    USER_NO_PERMISSION_FOR_REVIEW(HttpStatus.FORBIDDEN, 403203, "해당 유저는 해당 리뷰에 대한 권한이 없습니다."),
    NO_PUBLIC_REVIEW(HttpStatus.FORBIDDEN, 403204, "해당 리뷰는 private입니다."),
    NO_ONESELF_LIKE(HttpStatus.BAD_REQUEST, 400204, "자기자신의 리뷰는 좋아요할 수 없습니다."),
    NO_LIKE_REVIEW(HttpStatus.BAD_REQUEST, 400205, "해당 리뷰에 좋아요를 한 적이 없습니다."),

    //Route 관련 에러 +3
    ROUTE_DUPLICATE_ROUTENAME(HttpStatus.CONFLICT, 409301,"이미 사용중인 루트명입니다."),
    ROUTE_NOT_FOUND(HttpStatus.NOT_FOUND,404302,"존재하지 않는 루트입니다."),
    ROUTELIST_NOT_FOUND(HttpStatus.NOT_FOUND,404302,"루트에 존재하지 않은 상점 항목입니다"),
    USER_NO_PERMISSION_FOR_ROUTE(HttpStatus.FORBIDDEN,403303,"해당 유저는 해당 루트에 대한 권한이 없습니다"),
    ROUTE_ORDINAL_BAD_REQUEST(HttpStatus.BAD_REQUEST,403304,"루트 내 이동 경로는 1부터 6까지만 가능합니다"),
    ROUTE_MYROUTE_BAD_REQUEST(HttpStatus.BAD_REQUEST,403305,"내 루트는 최소 1개 이상의 경로를 포함해야 합니다."),
    ROUTELIST_TO_MANY_REQUEST(HttpStatus.TOO_MANY_REQUESTS,403306,"루트 내 상점 항목은 최대 6개까지 가능합니다."),
    NO_PUBLIC_ROUTE(HttpStatus.FORBIDDEN,403307,"해당 루트는 비공개 상태입니다."),

    //Group 관련 에러 +4
    FIND_FAIL_GROUP(HttpStatus.NOT_FOUND, 404401, "존재하지 않는 그룹입니다."),
    UNAUTHORIZED_DELETE_GROUP(HttpStatus.FORBIDDEN, 403402, "그룹을 삭제할 권한이 없습니다."),
    UNAUTHORIZED_MODIFY_GROUP_NAME(HttpStatus.FORBIDDEN, 403403, "그룹명을 수정할 권한이 없습니다."),
    INVITATION_CODE_NOT_FOUND(HttpStatus.NOT_FOUND, 404404,"그룹의 초대 코드를 찾을 수 없습니다."),
    INVALID_INVITATION_CODE(HttpStatus.FORBIDDEN, 403405, "그룹의 초대 코드가 올바르지 않습니다."),
    USER_NOT_IN_GROUP(HttpStatus.NOT_FOUND, 404406, "그룹에 가입되지 않은 유저입니다."),
    NO_TRANSFER_PERMISSION(HttpStatus.FORBIDDEN, 403407, "그룹 소유자만이 그룹 소유권을 이전할 수 있습니다."),
    GROUPLIST_NOT_FROUND(HttpStatus.NOT_FOUND,403409,"존재하지 않는 그룹 내 상점입니다."),
    ALREADY_JOINED_GROUP(HttpStatus.BAD_REQUEST, 400410, "이미 해당 그룹에 참여한 유저입니다."),
    ALREADY_ADD_GROUP_LIST(HttpStatus.BAD_REQUEST, 400411,"이미 해당 그룹에 존재하는 그룹리스트입니다."),
  
    //myCategory 관련 에러 +5
    MY_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, 404501, "존재하지 않는 카테고리입니다."),
    PIN_NOT_FOUND(HttpStatus.NOT_FOUND, 404502,"존재하지 않는 찜 입니다"),
    MY_CATEGORY_DUPLICATE_NAME(HttpStatus.CONFLICT, 409501, "이미 존재하는 카테고리입니다."),
    USER_NO_PERMISSION_FOR_MY_CATEGORY(HttpStatus.FORBIDDEN, 403501, "해당 유저는 해당 카테고리에 대한 권한이 없습니다."),

    // token 관련 에러 +6
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, 401601, "유효하지 않은 토큰입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, 401603, "refresh-token이 유효하지 않습니다."),
    EXPIRED_ACCESS_TOKEN(HttpStatus.FORBIDDEN, 403601, "X-AUTH-TOKEN이 만료되었습니다. 토큰 재발급을 실행해주세요."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.FORBIDDEN, 403602, "refresh-token이 만료되었습니다. 재로그인이 필요합니다"),

    FOR_TEST_ERROR(HttpStatus.BAD_REQUEST,49999, "테스트용 에러")


    ;

    private final HttpStatus httpStatus;
    private final int code;
    private final String message;
}
