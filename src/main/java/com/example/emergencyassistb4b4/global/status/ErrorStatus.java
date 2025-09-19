package com.example.emergencyassistb4b4.global.status;

import com.example.emergencyassistb4b4.global.exception.dto.ErrorReasonDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorStatus implements BaseErrorCode {

    //공통
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C001", "서버 오류가 발생했습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "C002", "잘못된 요청입니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "C003", "권한이 없습니다."),

    // 인증 및 인가
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AU001","인증이 필요합니다."),
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "AU002","유효하지 않은 액세스 토큰입니다."),
    EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "AU003","액세스 토큰이 만료되었습니다."),
    INVAlID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AU004","유효하지 않은 리프레시 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AU005","리프레시 토큰을 찾을 수 없습니다."),
    TOKEN_USER_MISMATCH(HttpStatus.UNAUTHORIZED, "AU006","토큰의 사용자 정보가 일치하지 않습니다."),
    INVALID_SIGNATURE(HttpStatus.UNAUTHORIZED, "AU006","토큰의 사용자 정보가 일치하지 않습니다."),
    UNSUPPORTED_JWT(HttpStatus.UNAUTHORIZED, "AU006","토큰의 사용자 정보가 일치하지 않습니다."),

    //회원가입 및 로그인
    DUPLICATED_EMAIL(HttpStatus.CONFLICT, "AU007", "이미 존재하는 이메일입니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "AU008", "비밀번호가 일치하지 않습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "AU009", "해당 사용자를 찾을 수 없습니다."),
    OAUTH_PROVIDER_MISMATCH(HttpStatus.BAD_REQUEST, "AU010", "다른 OAuth 제공자로 가입된 계정입니다."),
    OAUTH_LOGIN_ONLY(HttpStatus.BAD_REQUEST, "AU015", "소셜 로그인으로 가입된 계정입니다. 자체 로그인 불가합니다."),
    SELF_LOGIN_ONLY(HttpStatus.BAD_REQUEST, "AU016", "자체 로그인으로 가입된 계정입니다. 소셜 로그인 불가합니다."),
    SIGNUP_STRATEGY_NOT_FOUND(HttpStatus.BAD_REQUEST, "AU017", "지원하지 않는 회원가입 방식입니다."),
    LOGIN_STRATEGY_NOT_FOUND(HttpStatus.BAD_REQUEST, "AU018", " 지원하지 않는 로그인 방식입니다."),
    BUSINESS_NUMBER_REQUIRED(HttpStatus.BAD_REQUEST, "AU019" ,"이 필드는 필수값입니다."),
    INVALID_SIGNUP_REQUEST(HttpStatus.BAD_REQUEST, "AU020", "유효하지 않은 회원가입 요청입니다."),
    COOKIE_NOT_FOUND(HttpStatus.BAD_REQUEST, "AU021", "쿠키가 존재하지 않습니다."),
    INVALID_OBJECT_TYPE(HttpStatus.BAD_REQUEST, "AU022", "역직렬화된 객체 타입이 일치하지 않음."),
    DESERIALIZATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "AU023", "역직렬화 중 오류 발생"),
    LOGOUT_TOKEN(HttpStatus.INTERNAL_SERVER_ERROR, "AU023", "로그아웃 토큰입니다."),

    // 로그아웃
    LOGOUT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AU001", "로그아웃 처리에 실패했습니다."),
    CUSTOM_ERROR_STATUS(HttpStatus.INTERNAL_SERVER_ERROR, "C001", "Custom Error"),

    // Redis
    REDIS_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "RE010", "Redis 서버 오류 발생"),
    INVALID_TTL(HttpStatus.BAD_REQUEST, "AU024", "TTL 값은 0보다 커야 합니다."),

    // 자원봉사
    VOLUNTEER_INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "VO010", "서버 오류로 인해 봉사 참여 처리에 실패했습니다."),
    VOLUNTEER_ALREADY_PARTICIPATED(HttpStatus.BAD_REQUEST, "VO011", "이미 참여 중인 봉사입니다."),
    VOLUNTEER_POST_CLOSED(HttpStatus.CONFLICT, "VO012", "모집이 마감된 게시글입니다."),
    VOLUNTEER_CHECKIN_TOO_LATE(HttpStatus.BAD_REQUEST, "VO013", "체크인 시작 5분 전 이후에는 참여할 수 없습니다."),
    VOLUNTEER_CHECKIN_CONFLICT(HttpStatus.CONFLICT, "VO014", "다른 봉사 활동과 체크인 시간이 겹칩니다."),
    VOLUNTEER_CAPACITY_EXCEEDED(HttpStatus.CONFLICT, "VO015", "팀 정원이 초과되어 참여할 수 없습니다."),
    VOLUNTEER_NOT_FOUND(HttpStatus.NOT_FOUND, "VO004", "존재하지 않는 봉사 게시글/참여자입니다."),
    VOLUNTEER_FORBIDDEN(HttpStatus.FORBIDDEN, "VO003", "봉사 참여 권한이 없습니다."),
    VOLUNTEER_PARTICIPANT_BLACKLISTED(HttpStatus.FORBIDDEN, "VO016", "블랙리스트 처리된 참여자입니다."),
    TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "VO005", "존재하지 않는 팀입니다."),
    VOLUNTEER_CANCEL_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "VO017", "체크인 시작 이후에는 참여 취소가 불가능합니다."),
    VOLUNTEER_INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "VO018", "봉사 시작일이 종료일보다 늦습니다."),

    // 신고
    REPORT_BAD_REQUEST(HttpStatus.BAD_REQUEST, "RP004", "유효하지 않은 값입니다"),
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "RP007", "신고 정보를 찾을 수 없습니다."),
    GOV_NOT_FOUND(HttpStatus.NOT_FOUND, "RP007", "해당 지역 공공기관을 찾을 수 없습니다."),
    S3_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "RP010", "S3 파일 업로드 중 오류가 발생했습니다."),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "RP011", "지원하지 않는 미디어 타입입니다."),
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "RP004", "시 정보가 누락되었습니다."),

    // Alert
    ALERT_BAD_REQUEST(HttpStatus.BAD_REQUEST, "AL004", "유효하지 않은 값입니다"),
    ALERT_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "AL010", "알림 전송 실패"),

    // UserDevice
    USER_DEVICE_NOT_FOUND(HttpStatus.NOT_FOUND, "UD007", "유저 디바이스가 존재하지 않습니다."),

    // 자원봉사
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "VO004", "존재하지 않는 게시글입니다."),
    ATTENDANCE_RECORD_PARSE_FAILED(HttpStatus.BAD_REQUEST, "VO004", "출석 기록 파싱 실패"),
    WEBSOCKET_MESSAGE_SERIALIZATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "VO010", "WebSocket 메시지 직렬화 실패"),
    WEBSOCKET_MESSAGE_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "VO010", "WebSocket 메시지 전송 실패"),
    ATTENDANCE_LOCATION_OR_POLICY_MISSING(HttpStatus.BAD_REQUEST, "VO004","위치 정보나 출석 정책이 설정되지 않았습니다."),

    // 카카오
    KAKAO_API_FAILED(HttpStatus.BAD_GATEWAY, "KAKAO001", "카카오 API 호출에 실패했습니다."),
    KAKAO_DATA_INVALID(HttpStatus.BAD_REQUEST, "KAKAO002", "카카오 사용자 정보가 올바르지 않습니다."),
    KAKAO_API_REQUEST_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "LC010", "카카오 API 요청 실패"),
    KAKAO_API_RESPONSE_PARSE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "LC010", "카카오 API 응답 파싱 실패"),
    KAKAO_API_RESPONSE_STATUS_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "LC010", "카카오 API 비정상 응답"),

    KAFKA_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "RE010", "카프카 서버 오류 발생");

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