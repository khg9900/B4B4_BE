package com.example.emergencyassistb4b4.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenReissueRequestDto {

    @NotBlank(message = "리프레시 토큰은 필수입니다.")
    private String refreshToken;

}
