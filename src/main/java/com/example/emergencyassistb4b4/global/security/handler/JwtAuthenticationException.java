package com.example.emergencyassistb4b4.global.security.handler;

import com.example.emergencyassistb4b4.global.status.ErrorStatus;
import lombok.Getter;
import org.springframework.security.core.AuthenticationException;

@Getter
public class JwtAuthenticationException extends AuthenticationException {

    private final ErrorStatus errorStatus;

    public JwtAuthenticationException(ErrorStatus errorStatus ) {

        super(errorStatus.getReasonHttpStatus().getMessage());
        this.errorStatus = errorStatus;
    }
}
