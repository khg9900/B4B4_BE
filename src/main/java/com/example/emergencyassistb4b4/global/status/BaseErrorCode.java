package com.example.emergencyassistb4b4.global.status;

import com.example.emergencyassistb4b4.global.exception.dto.ErrorReasonDto;

public interface BaseErrorCode {
    ErrorReasonDto getReasonHttpStatus();
}