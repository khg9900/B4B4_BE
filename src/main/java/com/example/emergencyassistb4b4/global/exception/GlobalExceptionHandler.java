package com.example.emergencyassistb4b4.global.exception;

import com.example.emergencyassistb4b4.global.exception.dto.ErrorReasonDto;
import com.example.emergencyassistb4b4.global.exception.dto.FieldErrorDetail;
import com.example.emergencyassistb4b4.global.response.ApiResponse;
import com.example.emergencyassistb4b4.global.status.ErrorStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<ErrorReasonDto>> handleApiException(ApiException e) {
        return ApiResponse.onFailure(e.getErrorCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<List<FieldErrorDetail>>> handleValidationException(MethodArgumentNotValidException ex) {

        List<FieldErrorDetail> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> FieldErrorDetail.of(
                        error.getField(),
                        error.getRejectedValue(),
                        error.getDefaultMessage()
                ))
                .toList();

        // 실패 응답: ErrorStatus.INVALID_REQUEST 사용
        return ResponseEntity.status(ErrorStatus.INVALID_REQUEST.getHttpStatus())
                .body(new ApiResponse<>(
                        false,
                        ErrorStatus.INVALID_REQUEST.getCode(),
                        "유효하지 않은 요청입니다.",
                        fieldErrors
                ));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<ErrorReasonDto>> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        return ApiResponse.onFailure(ErrorStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    // 추가로, 예기치 않은 모든 예외를 처리하는 fallback 핸들러
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ErrorReasonDto>> handleUnhandledException(Exception ex) {
        ex.printStackTrace();  // 서버 로그 기록용
        return ApiResponse.onFailure(ErrorStatus.INTERNAL_SERVER_ERROR);
    }
}