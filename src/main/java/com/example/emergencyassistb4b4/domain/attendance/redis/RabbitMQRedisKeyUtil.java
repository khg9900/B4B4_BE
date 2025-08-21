package com.example.emergencyassistb4b4.domain.attendance.redis;

public class RabbitMQRedisKeyUtil {
    private static final String PREFIX_RABBITMQ_STATE = "attendance:rabbitmq:state:";
    private static final String PREFIX_TEAM_TRACKING_STATE = "attendance:team:tracking:";
    private static final String PREFIX_VOLUNTEER_USER = "attendance:volunteer:user:";
    private static final String PREFIX_GEO = "attendance:geo:team:";
    private static final String PREFIX_ATTENDANCE_SESSION = "attendance:session:";
    private static final String VOLUNTEER_TEAM_PREFIX = "attendance:volunteer:team:";

    public static String rabbitMQStateKey(Long teamId) {
        return PREFIX_RABBITMQ_STATE + teamId;
    }

    public static String teamTrackingStateKey(Long teamId) {
        return PREFIX_TEAM_TRACKING_STATE + teamId;
    }

    public static String volunteerUserKey(Long volunteerId) {
        return PREFIX_VOLUNTEER_USER + volunteerId;
    }

    public static String geoKey(Long teamId) {
        return PREFIX_GEO + teamId;
    }

    public static String attendanceSessionKey(Long volunteerId) {
        return PREFIX_ATTENDANCE_SESSION + volunteerId;
    }

    public static String volunteerTeamKey(Long volunteerId) {
        return VOLUNTEER_TEAM_PREFIX + volunteerId;
    }
}
