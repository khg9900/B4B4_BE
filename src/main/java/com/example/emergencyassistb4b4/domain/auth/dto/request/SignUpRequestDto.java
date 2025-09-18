package com.example.emergencyassistb4b4.domain.auth.dto.request;

import com.example.emergencyassistb4b4.domain.user.domain.UserRole;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequestDto {

    @NotBlank(message = "이메일은 필수 입력값입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    @Size(min = 8, message = "비밀번호는 최소 8자리 이상이어야 합니다.")
    @Pattern(regexp = "(?=.*[A-Za-z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,}", message = "비밀번호는 영문자, 숫자, 특수문자를 포함해야 합니다.")
    private String password; // local만 필수

    private String name;

    @Pattern(regexp = "^01[016789]-?\\d{3,4}-?\\d{4}$", message = "전화번호는 올바른 한국 휴대폰 번호 형식이어야 합니다.")
    private String phoneNumber; // IND

    @NotNull(message = "사용자 역할은 필수 선택 값입니다.")
    private UserRole userRole;

    private String province;

    private String city;
}
