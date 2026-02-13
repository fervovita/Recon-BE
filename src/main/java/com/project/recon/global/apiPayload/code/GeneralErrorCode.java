package com.project.recon.global.apiPayload.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum GeneralErrorCode implements BaseErrorCode {

    // 인증 에러
    DUPLICATE_LOGINID(HttpStatus.BAD_REQUEST, "AUTH_4001", "중복되는 아이디가 존재합니다."),
    MISSING_AUTH_INFO(HttpStatus.UNAUTHORIZED, "AUTH_4011", "인증 정보가 누락되었습니다."),
    INVALID_LOGIN(HttpStatus.UNAUTHORIZED, "AUTH_4012", "올바르지 않은 아이디, 혹은 비밀번호입니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_4013", "유효하지 않은 토큰입니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_4014", "토큰이 만료되었습니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH_4031", "접근 권한이 없습니다."),

    // 회원가입 에러
    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "SIGNUP_4001", "이미 사용 중인 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.BAD_REQUEST, "SIGNUP_4002", "이미 사용 중인 닉네임입니다."),
    UNDER_AGE(HttpStatus.BAD_REQUEST, "SIGNUP_4003", "만 14세 미만은 가입할 수 없습니다."),
    DUPLICATE_PHONE(HttpStatus.BAD_REQUEST, "SIGNUP_4004", "이미 사용중인 전화번호입니다."),

    // 카카오 에러
    KAKAO_INVALID_CODE(HttpStatus.UNAUTHORIZED, "KAKAO_4011", "유효하지 않은 카카오 인가 코드입니다."),
    KAKAO_USER_INFO_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "KAKAO_5001", "카카오 사용자 정보 조회에 실패했습니다."),

    // 회원 에러
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEM_4041", "존재하지 않는 회원입니다."),
    ALREADY_WITHDRAWN(HttpStatus.GONE, "MEM_4101", "이미 탈퇴한 회원입니다."),

    // 주문 에러
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORD_4041", "존재하지 않는 주문입니다."),
    ORDER_CANNOT_CANCEL(HttpStatus.BAD_REQUEST, "ORD_4001", "배송이 시작되어 취소가 불가능합니다."),
    ORDER_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "ORD_4002", "이미 완료된 주문입니다."),

    // 결제 에러
    PAYMENT_FAILED(HttpStatus.BAD_REQUEST, "PAY_4001", "결제 처리에 실패했습니다."),
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "PAY_4002", "결제 금액이 일치하지 않습니다."),
    REFUND_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "PAY_4003", "환불 가능한 상태가 아닙니다."),

    // 재고 에러
    OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "STOCK_4001", "상품 재고가 부족합니다."),
    SOLDOUT(HttpStatus.BAD_REQUEST, "STOCK_4002", "품절된 상품입니다."),

    // 배송 에러
    ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "DLV_4041", "등록된 배송지 정보를 찾을 수 없습니다."),
    DELIVERY_STARTED(HttpStatus.BAD_REQUEST, "DLV_4001", "배송이 이미 시작되어 정보를 수정할 수 없습니다."),

    // 요청/파라미터 에러
    MISSING_PARAMETER(HttpStatus.BAD_REQUEST, "REQ_4001", "필수 파라미터가 누락되었습니다."),
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "REQ_4002", "파라미터 형식이 잘못되었습니다."),
    UNSUPPORTED_CONTENT_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "REQ_4151", "지원하지 않는 Content-Type입니다."),

    // API/라우팅 에러
    API_NOT_FOUND(HttpStatus.NOT_FOUND, "API_4041", "존재하지 않는 API입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "API_4051", "지원하지 않는 HTTP 메서드입니다."),

    // 서버 내부 에러
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_5001", "서버 내부 오류입니다."),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "SERVER_5031", "서버가 일시적으로 불안정합니다."),
    EXTERNAL_SERVICE_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "SERVER_5041", "외부 서비스 응답 지연");


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
