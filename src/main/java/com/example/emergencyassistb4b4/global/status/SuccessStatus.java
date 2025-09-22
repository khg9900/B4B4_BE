package com.example.emergencyassistb4b4.global.status;

import com.example.emergencyassistb4b4.global.response.ReasonDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SuccessStatus implements BaseCode {

    // Auth
    SIGNUP_SUCCESS(HttpStatus.CREATED, "AU002" ,"회원가입이 완료되었습니다."),
    LOGIN_SUCCESS(HttpStatus.OK, "AU001", "로그인이 완료되었습니다."),
    LOGOUT_SUCCESS(HttpStatus.OK,"AU001", "로그아웃이 완료되었습니다."),
    TOKEN_REISSUE_SUCCESS(HttpStatus.OK, "AU001", "액세스 토큰 재발급이 완료되었습니다."),

    // User
    USER_INFO_GET_SUCCESS(HttpStatus.OK, "US001", "사용자 정보 조회가 완료되었습니다."),

    // Report
    REPORT_CREATE_SUCCESS(HttpStatus.CREATED, "RP002", "재난 신고가 완료되었습니다."),
    IND_REPORT_GET_SUCCESS(HttpStatus.OK, "RP001", "재난 신고 이력 조회가 완료되었습니다."),
    GOV_REPORT_GET_SUCCESS(HttpStatus.OK, "RP001", "재난 신고 목록 조회가 완료되었습니다."),
    REPORT_SUMMARY_GET_SUCCESS(HttpStatus.OK, "RP001", "재난 신고 현황 조회가 완료되었습니다."),
    REPORT_STATUS_UPDATE_SUCCESS(HttpStatus.OK, "RP001", "재난 신고 접수 상태 변경이 완료되었습니다."),

    // Alert
    ALERTS_GET_SUCCESS(HttpStatus.OK, "AL001", "알림 조회가 완료되었습니다."),

    // UserDevice
    DEVICE_CREATE_SUCCESS(HttpStatus.CREATED, "UD002", "기기 정보 저장이 완료되었습니다."),

    // Volunteer
    VOLUNTEER_CREATE_POST_SUCCESS(HttpStatus.CREATED, "VO002", "봉사활동 게시글 작성이 완료되었습니다."),
    VOLUNTEER_UPDATE_POST_SUCCESS(HttpStatus.OK, "VO001", "봉사활동 게시글 수정이 완료되었습니다."),
    VOLUNTEER_DELETE_POST_SUCCESS(HttpStatus.OK, "VO001", "봉사활동 게시글 삭제가 완료되었습니다."),
    VOLUNTEER_GET_POSTS_SUCCESS(HttpStatus.OK, "VO001", "(전체) 봉사활동 게시글 조회가 완료되었습니다."),
    VOLUNTEER_GET_MY_POSTS_SUCCESS(HttpStatus.OK, "VO001", "(본인 작성) 봉사활동 게시글 조회가 완료되었습니다."),
    VOLUNTEER_GET_POST_DETAIL_SUCCESS(HttpStatus.OK, "VO001", "봉사활동 게시글 상세 조회가 완료되었습니다."),
    VOLUNTEER_GET_TEAM_SUCCESS(HttpStatus.OK, "VO001", "봉사활동 게시글 팀별 모집 현황 조회가 완료되었습니다."),
    VOLUNTEER_APPLY_SUCCESS(HttpStatus.OK, "VO001", "봉사활동 참가 신청이 완료되었습니다."),
    VOLUNTEER_CANCEL_SUCCESS(HttpStatus.OK, "VO001", "봉사활동 참가 취소가 완료되었습니다."),
    VOLUNTEER_GET_PARTICIPATION_SUCCESS(HttpStatus.OK, "VO001", "봉사활동 참여 이력 조회가 완료되었습니다."),
    VOLUNTEER_GET_PARTICIPANT_SUCCESS(HttpStatus.OK, "VO001", "봉사활동 참여자 정보 조회가 완료되었습니다."),
    VOLUNTEER_UPDATE_PARTICIPATION_STATUS_SUCCESS(HttpStatus.OK, "VO001", "봉사활동 참여자 출결 상태 변경이 완료되었습니다."),

    // Location
    LOCATION_SAVE_SUCCESS(HttpStatus.CREATED, "LC002", "실시간 위치 정보 전송이 완료되었습니다."),
    SHELTER_SEARCH_SUCCESS(HttpStatus.OK, "LC001", "대피소 위치 조회가 완료되었습니다."),
    DISASTER_SEARCH_SUCCESS(HttpStatus.OK, "LC001", "재난 발생 위치 조회가 완료되었습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
    private final ReasonDto cachedReasonDto;

    SuccessStatus(HttpStatus httpStatus, String code, String message) {

        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
        this.cachedReasonDto = ReasonDto.builder()
            .isSuccess(true)
            .httpStatus(httpStatus)
            .code(code)
            .message(message)
            .build();
    }

    @Override
    public ReasonDto getReasonHttpStatus() {

        return cachedReasonDto;
    }
}
