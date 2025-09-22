package com.example.emergencyassistb4b4.domain.attendance.socket.utils;

public class WebSocketUtils {
    // 위도, 경도가 올바른지 판별
    public static boolean isValidCoordinates(double lat, double lon) {
        return lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180;
    }
}
