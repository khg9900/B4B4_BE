package com.example.emergencyassistb4b4.global.security.handler;

import com.example.emergencyassistb4b4.global.exception.ApiException;
import com.example.emergencyassistb4b4.global.exception.dto.ErrorReasonDto;
import com.example.emergencyassistb4b4.global.response.ApiResponse;
import com.example.emergencyassistb4b4.global.status.ErrorStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

// 인증 되지 않은 사용자가 보호된 리소스에 접근하려고 할 때 호출되는 EntryPoint(JWT 누락, 유효하지 않은 경우 등)
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public CustomAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException)
            throws IOException, ServletException {

        Object ex = request.getAttribute("exception");

        // 기본 에러 상태
        ErrorStatus errorStatus = ErrorStatus.UNAUTHORIZED;

        String message = errorStatus.getReasonHttpStatus().getMessage(); // 기본 메시지

        if (ex instanceof JwtAuthenticationException jwtEx) {
            message = jwtEx.getMessage(); // 예외에 담긴 메시지로 대체
        }

        // ErrorReasonDto → ApiResponse 변환
        ApiResponse<Object> errorResponse = new ApiResponse<>(
                false,
                errorStatus.getReasonHttpStatus().getCode(),
                message, // 커스텀 메시지 적용
                null
        );

        response.setStatus(errorStatus.getReasonHttpStatus().getHttpStatus().value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}