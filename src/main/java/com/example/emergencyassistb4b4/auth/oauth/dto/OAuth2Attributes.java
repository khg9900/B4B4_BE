package com.example.emergencyassistb4b4.auth.oauth.dto;

import com.example.emergencyassistb4b4.global.exception.ApiException;
import com.example.emergencyassistb4b4.global.status.ErrorStatus;
import lombok.Getter;

import java.util.Map;
/**
 * OAuth2 공급자에서 받은 사용자 정보를 내부 공통 포맷으로 변환
 * Provider별 분기를 위해 정적 팩토리 메서드 of를 사용하고, ofGoogle, ofKakao 메서드를 통해
 * 각 Provider의 응답으로부터 필요한 사용자 정보를 추출하여 OAuth2Attributes 객체로 만듬
 */
@Getter
public class OAuth2Attributes {
    private final String name;
    private final String email;
    private final Map<String, Object> attributes;
    private final String provider;
    private final String providerId;


    public OAuth2Attributes(String providerId, String name, String email, String provider, Map<String, Object> attributes) {
        this.providerId = providerId;
        this.name = name;
        this.email = email;
        this.provider = provider;
        this.attributes = attributes;

    }

    public static OAuth2Attributes of(String provider, Map<String, Object> attributes) {
        return switch (provider.toLowerCase()) {
            case "google" -> ofGoogle(attributes);
            case "kakao" -> ofKakao(attributes);
            default -> throw new ApiException(ErrorStatus.INVALID_REQUEST);
        };

    }

    public static OAuth2Attributes ofGoogle(Map<String, Object> attributes) {
        // Google은 'sub' 필드를 고유 ID 로 사용함
        String googleId = (String) attributes.get("sub");
        return new OAuth2Attributes(
                googleId, //provider 고유 ID 설정
                (String) attributes.get("name"),
                (String) attributes.get("email"),
                "google",
                attributes
        );
    }

    public static OAuth2Attributes ofKakao(Map<String, Object> attributes) {
        // kakao 는 id
        Long kakaoId = (Long) attributes.get("id");
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        return new OAuth2Attributes(
                String.valueOf(kakaoId),
                (String) profile.get("nickname"),
                (String) kakaoAccount.get("email"),
                "kakao",
                attributes
        );
    }
}