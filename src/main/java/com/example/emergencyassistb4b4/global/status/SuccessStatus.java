package com.example.emergencyassistb4b4.global.status;

import com.example.emergencyassistb4b4.global.response.ReasonDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SuccessStatus implements BaseCode {

    // 인증
    LOGIN_SUCCESS(HttpStatus.OK, "S1000", "로그인에 성공했습니다."),
    LOGOUT_SUCCESS(HttpStatus.OK,"S1002", "로그아웃에 성공했습니다."),
    SIGNUP_SUCCESS(HttpStatus.OK, "S1009" ,"회원가입에 성공했습니다"),
    TOKEN_REISSUE_SUCCESS(HttpStatus.CREATED, "S1003", "액세스 토큰 재발급에 성공했습니다."),

    // Report
    REPORT_GET_SUCCESS(HttpStatus.OK, "RP001", "페이지 조회가 완료되었습니다."),
    REPORT_REPORTER_GET_SUCCESS(HttpStatus.OK, "RP001", "신고자 조회가 완료되었습니다."),
    REPORT_CREATE_SUCCESS(HttpStatus.CREATED, "RP002", "재난 신고가 접수되었습니다."),

    // Volunteer
    VOLUNTEER_CREATE_SUCCESS(HttpStatus.CREATED, "VO001", "자원봉사 모집글이 성공적으로 생성되었습니다."),
    VOLUNTEER_SUCCESS(HttpStatus.OK, "VO002", "자원봉사 모집글 상세 내역이 정상적으로 조회되었습니다."),
    LOCATION_SAVE_SUCCESS(HttpStatus.CREATED, "LC002", "Location information save is success"),
    VOLUNTEER_STATUS_SUCCESS(HttpStatus.OK, "VO002", "자원봉사 봉사 변경 되었습니다.."),
    VOLUNTEER_INFORMATION_SUCCESS(HttpStatus.OK, "VO002", "자원봉사 정보조회가 완료되었습니다."),




    // Location
    SHELTER_SEARCH_SUCCESS(HttpStatus.OK, "LC001", "Shelter search completed successfully"),
    DISASTER_SEARCH_SUCCESS(HttpStatus.OK, "LC001", "Disaster summary search completed successfully"),

    // Alert
    ALERTS_GET_SUCCESS(HttpStatus.OK, "AL001", "알림 조회 성공"),

    // UserDevice
    DEVICE_CREATE_SUCCESS(HttpStatus.CREATED, "UD002", "디바이스 저장 성공"),

    CUSTOM_SUCCESS_STATUS(HttpStatus.OK, "S1001", "Custom Success");

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
