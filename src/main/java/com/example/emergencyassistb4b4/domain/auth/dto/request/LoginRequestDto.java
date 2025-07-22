package com.example.emergencyassistb4b4.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class LoginRequestDto {

    @Email(message = "올바른 이메일 형식이어야 합니다.")
    @NotBlank(message = "이메일은 필수 입력값입니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    private String password;

    private String accessToken; // OAuth2 로그인 (카카오, 구글 등에서 받은 액세스 토큰)
    private String refreshToken; // 카카오 리프레시 토큰

}
