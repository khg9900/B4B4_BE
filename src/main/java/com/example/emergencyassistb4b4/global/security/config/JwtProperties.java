package com.example.emergencyassistb4b4.global.security.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

// JWT 설정 파일에서 관련 값 로딩하는 클래스
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String issuer; //jwt 발급자 식별자
    private String secret; // 서명용 비밀키 Base64 인코딩 된 문자열

    private long accessTokenValidity; // 액세스 토큰 유효시간
    private long refreshTokenValidity; // 리프레시 토큰 유효시간
}
