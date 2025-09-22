package com.example.emergencyassistb4b4.domain.location.redis;


public class LocationRedisKeyUtil {

    private static final String REGION_PREFIX = "region:";
    private static final String USER_LOCATIONS = "user:locations";
    private static final String LOCATION_TTL_PREFIX = "location:ttl:";

    // 특정 지역 key 생성
    public static String regionKey(String province, String city) {
        return REGION_PREFIX + province + ":" + city;
    }

    // 좌표 저장 key
    public static String userLocationsKey() {
        return USER_LOCATIONS;
    }

    // 좌표 TTL key
    public static String locationTtlKey(Long userId) {
        return LOCATION_TTL_PREFIX + userId;
    }
}
