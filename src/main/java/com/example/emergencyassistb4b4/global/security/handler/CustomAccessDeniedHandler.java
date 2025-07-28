package com.example.emergencyassistb4b4.global.security.handler;

import com.example.emergencyassistb4b4.global.exception.ApiException;
import com.example.emergencyassistb4b4.global.response.ApiResponse;
import com.example.emergencyassistb4b4.global.status.ErrorStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public CustomAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        // 공통 실패 응답 생성 (403 Forbidden: 인증은 되었으나 권한 부족)
        ApiException exception = new ApiException(ErrorStatus.FORBIDDEN);
        ApiResponse<Object> errorResponse = ApiResponse.onFailure(exception.getErrorCode()).getBody();

        response.setStatus(exception.getErrorCode().getReasonHttpStatus().getHttpStatus().value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // JSON 직렬화 후 응답
        String json = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(json);
    }
}