package com.example.emergencyassistb4b4.global.status;

import com.example.emergencyassistb4b4.global.response.ErrorReasonDto;

public interface BaseErrorCode {
    ErrorReasonDto getReasonHttpStatus();
}