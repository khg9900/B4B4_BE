package com.example.emergencyassistb4b4.global.security.handler;

import com.example.emergencyassistb4b4.global.response.ApiResponse;
import com.example.emergencyassistb4b4.global.status.ErrorStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

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
            throws IOException {

        Object ex = request.getAttribute("exception");

        // 기본 에러 상태
        ErrorStatus errorStatus = ErrorStatus.UNAUTHORIZED;

        if (ex instanceof JwtAuthenticationException jwtEx) {
            errorStatus = jwtEx.getErrorStatus();
        }

        // ErrorReasonDto → ApiResponse 변환
        ApiResponse<Object> errorResponse = new ApiResponse<>(
                false,
                errorStatus.getReasonHttpStatus().getCode(),
                errorStatus.getReasonHttpStatus().getMessage(),
                null
        );

        response.setStatus(errorStatus.getReasonHttpStatus().getHttpStatus().value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}