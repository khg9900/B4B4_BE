package com.example.emergencyassistb4b4.global.security.handler;

import com.example.emergencyassistb4b4.global.status.ErrorStatus;
import lombok.Getter;
import org.springframework.security.core.AuthenticationException;

// JWT 인증 과정에서 발생하는 예외 상황을 처리하기 위해 만든 커스텀 예외 클래스 (JWT 관련 예외를 명확하게 분리해서 처리하기 위함 >> ex. 유효하지 않은 JWT (서명 위조, 형식 오류) / 만료된 JWT / Redis에 블랙리스트로 등록된 로그아웃 토큰)
// Spring Security의 기본 예외 처리 흐름과 연동되도록 AuthenticationException을 상속받고 있음 > JWT 오류만을 의미적으로 구분하기 위해서 따로 만드는 것
// Spring Security가 처리하는 기본 AuthenticationException은 너무 포괄적이라, JWT 관련 에러를 따로 잡아 정확한 메시지나 로직을 넣고 싶을 때 유용
// 이 예외는 주로 JwtAuthenticationFilter 내부에서 사용됨
@Getter
public class JwtAuthenticationException extends AuthenticationException {

    private final ErrorStatus errorStatus;

    public JwtAuthenticationException(ErrorStatus errorStatus ) {

        super(errorStatus.getReasonHttpStatus().getMessage());
        this.errorStatus = errorStatus;
    }
}
