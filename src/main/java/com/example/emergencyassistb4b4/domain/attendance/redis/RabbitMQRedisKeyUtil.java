package com.example.emergencyassistb4b4.domain.attendance.redis;

public class RabbitMQRedisKeyUtil {

    private static final String PREFIX_RABBITMQ_STATE = "attendance:rabbitmq:state:";
    private static final String PREFIX_TEAM_TRACKING_STATE = "attendance:team:tracking:";
    private static final String PREFIX_VOLUNTEER_USER = "attendance:volunteer:user:";
    private static final String PREFIX_GEO = "attendance:geo:team:";
    private static final String PREFIX_ATTENDANCE_SESSION = "attendance:session:";
    private static final String VOLUNTEER_TEAM_PREFIX = "attendance:volunteer:team:";

    // RabbitMQ 상태 키
    public static String rabbitMQStateKey(Long teamId) {
        return PREFIX_RABBITMQ_STATE + teamId;
    }

    // 팀 트래킹 상태 키
    public static String teamTrackingStateKey(Long teamId) {
        return PREFIX_TEAM_TRACKING_STATE + teamId;
    }

    // 자원봉사자 사용자 키
    public static String volunteerUserKey(Long volunteerId) {
        return PREFIX_VOLUNTEER_USER + volunteerId;
    }

    // 팀 위치 정보 키
    public static String geoKey(Long teamId) {
        return PREFIX_GEO + teamId;
    }

    // 출석 세션 키
    public static String attendanceSessionKey(Long volunteerId) {
        return PREFIX_ATTENDANCE_SESSION + volunteerId;
    }

    // 자원봉사자 팀 키
    public static String volunteerTeamKey(Long volunteerId) {
        return VOLUNTEER_TEAM_PREFIX + volunteerId;
    }
}
