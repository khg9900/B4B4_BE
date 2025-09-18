package com.example.emergencyassistb4b4.global.exception;

import com.example.emergencyassistb4b4.global.status.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ApiException extends RuntimeException {

    private final BaseErrorCode errorCode;
}
