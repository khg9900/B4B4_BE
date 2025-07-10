package com.example.emergencyassistb4b4.global.response;

import com.example.emergencyassistb4b4.global.exception.dto.FieldErrorDetail;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorReasonDto {

    private HttpStatus httpStatus;
    private final boolean isSuccess;
    private final String code;
    private final String message;

    private final List<FieldErrorDetail> fieldErrors;

}