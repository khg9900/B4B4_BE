package com.example.emergencyassistb4b4.domain.location.util;

import org.springframework.http.HttpHeaders;

public class KakaoApiUtils {

    private static final String BASE_URL = "https://dapi.kakao.com/v2/local/search/category.json";

    public static String buildCategorySearchUrl(String categoryCode, double longitude, double latitude, double radius) {
        return BASE_URL +
                "?category_group_code=" + categoryCode +
                "&x=" + longitude +
                "&y=" + latitude +
                "&radius=" + radius;
    }

    public static HttpHeaders createAuthHeader(String apiKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + apiKey);
        return headers;
    }

}