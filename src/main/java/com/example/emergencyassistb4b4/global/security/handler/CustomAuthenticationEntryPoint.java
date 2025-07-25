package com.example.emergencyassistb4b4.global.security.handler;

import com.example.emergencyassistb4b4.global.exception.ApiException;
import com.example.emergencyassistb4b4.global.exception.dto.ErrorReasonDto;
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

        Exception exception = (Exception) request.getAttribute("exception");

        ErrorReasonDto errorResponse;
        int statusCode;

        if (exception instanceof ApiException apiException) {
            errorResponse = apiException.getErrorCode().getReasonHttpStatus();
            statusCode = errorResponse.getHttpStatus().value(); // ← 안전한 방식
        } else {
            errorResponse = ErrorStatus.UNAUTHORIZED.getReasonHttpStatus();
            statusCode = errorResponse.getHttpStatus().value(); // ← 마찬가지
        }

        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}