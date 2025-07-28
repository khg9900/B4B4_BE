package com.example.emergencyassistb4b4.domain.auth.dto.response;

import com.example.emergencyassistb4b4.domain.user.domain.User;
import com.example.emergencyassistb4b4.domain.user.domain.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignUpResponseDto {

    private Long id;

    private String email;

    private String nickname;

    private UserRole userRole;

    public static SignUpResponseDto from(User user) {

        return new SignUpResponseDto(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getUserRole()
        );
    }
}
