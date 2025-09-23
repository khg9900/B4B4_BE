package com.example.emergencyassistb4b4.global.status;

import com.example.emergencyassistb4b4.global.exception.dto.ErrorReasonDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorStatus implements BaseErrorCode {

    // Common
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "CO010", "서버 오류가 발생했습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "CO004", "잘못된 요청입니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "CO006", "권한이 없습니다."),

    // Auth
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AU005","인증이 필요합니다."),
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "AU005","유효하지 않은 액세스 토큰입니다."),
    EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "AU005","액세스 토큰이 만료되었습니다."),
    INVAlID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AU005","유효하지 않은 리프레시 토큰입니다."),
    DUPLICATED_EMAIL(HttpStatus.CONFLICT, "AU008", "이미 존재하는 이메일입니다."),
    INVALID_CREDENTIALS(HttpStatus.BAD_REQUEST, "AU004", "아이디 또는 비밀번호가 잘못되었습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "AU007", "해당 사용자를 찾을 수 없습니다."),
    LOGOUT_TOKEN(HttpStatus.INTERNAL_SERVER_ERROR, "AU010", "로그아웃 된 토큰입니다."),

    // Redis
    REDIS_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "RE010", "Redis 서버에 오류가 발생했습니다."),
    INVALID_TTL(HttpStatus.BAD_REQUEST, "RE004", "TTL 값은 0보다 커야 합니다."),

    // Volunteer
    VOLUNTEER_INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "VO010", "서버 오류로 인해 봉사 참여 처리에 실패했습니다."),
    VOLUNTEER_ALREADY_PARTICIPATED(HttpStatus.BAD_REQUEST, "VO004", "이미 참여 중인 봉사입니다."),
    VOLUNTEER_POST_CLOSED(HttpStatus.CONFLICT, "VO008", "모집이 마감된 게시글입니다."),
    VOLUNTEER_CHECKIN_TOO_LATE(HttpStatus.BAD_REQUEST, "VO004", "체크인 시작 5분 전 이후에는 참여할 수 없습니다."),
    VOLUNTEER_CHECKIN_CONFLICT(HttpStatus.CONFLICT, "VO008", "다른 봉사 활동과 체크인 시간이 겹칩니다."),
    VOLUNTEER_CAPACITY_EXCEEDED(HttpStatus.CONFLICT, "VO008", "팀 정원이 초과되어 참여할 수 없습니다."),
    VOLUNTEER_NOT_FOUND(HttpStatus.NOT_FOUND, "VO007", "존재하지 않는 봉사 게시글 또는 참여자입니다."),
    VOLUNTEER_FORBIDDEN(HttpStatus.FORBIDDEN, "VO006", "봉사 참여 권한이 없습니다."),
    VOLUNTEER_PARTICIPANT_BLACKLISTED(HttpStatus.FORBIDDEN, "VO006", "블랙리스트 처리된 참여자입니다."),
    TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "VO003", "존재하지 않는 팀입니다."),
    VOLUNTEER_CANCEL_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "VO004", "체크인 시작 이후에는 참여 취소가 불가능합니다."),
    VOLUNTEER_INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "VO004", "봉사 시작일이 종료일보다 늦습니다."),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "VO007", "존재하지 않는 게시글입니다."),
    ATTENDANCE_LOCATION_OR_POLICY_MISSING(HttpStatus.BAD_REQUEST, "VO004","위치 정보나 출석 정책이 설정되지 않았습니다."),

    // Report
    REPORT_BAD_REQUEST(HttpStatus.BAD_REQUEST, "RP004", "유효하지 않은 값입니다"),
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "RP007", "신고 정보를 찾을 수 없습니다."),
    GOV_NOT_FOUND(HttpStatus.NOT_FOUND, "RP007", "해당 지역 공공기관을 찾을 수 없습니다."),
    S3_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "RP010", "S3 파일 업로드 중 오류가 발생했습니다."),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "RP011", "지원하지 않는 미디어 타입입니다."),

    // Alert
    ALERT_INVALID_KEY_PREFIX(HttpStatus.BAD_REQUEST, "AL004", "알림 키 접두사가 올바르지 않습니다"),
    ALERT_INVALID_KEY_FORMAT(HttpStatus.BAD_REQUEST, "AL004", "알림 키 형식이 올바르지 않습니다"),
    ALERT_NO_TARGET_USER(HttpStatus.BAD_REQUEST, "AL004", "재난 신고 수신 대상자가 존재하지 않습니다"),

    // UserDevice
    USER_DEVICE_NOT_FOUND(HttpStatus.NOT_FOUND, "UD007", "유저 디바이스가 존재하지 않습니다."),

    // Kakao
    KAKAO_API_REQUEST_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "KA010", "카카오 API 요청을 실패했습니다."),
    KAKAO_API_RESPONSE_PARSE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "KA010", "카카오 API 응답 파싱을 실패했습니다."),
    KAKAO_API_RESPONSE_STATUS_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "KA010", "카카오 API 비정상 응답이 발생했습니다."),

    // Kafka
    KAFKA_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "KF010", "카프카 서버에 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
    private final ErrorReasonDto cachedErrorReasonDto;

    ErrorStatus(HttpStatus httpStatus, String code, String message) {

        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
        this.cachedErrorReasonDto = ErrorReasonDto.builder()
            .isSuccess(false)
            .httpStatus(httpStatus)
            .code(code)
            .message(message)
            .build();
    }

    @Override
    public ErrorReasonDto getReasonHttpStatus() {

        return cachedErrorReasonDto;
    }
}