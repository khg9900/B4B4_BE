package com.example.emergencyassistb4b4.global.exception.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorReasonDto {
    private final boolean isSuccess;
    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    // Optional: validation error인 경우만 사용
    private final List<FieldErrorDetail> fieldErrors;
}
