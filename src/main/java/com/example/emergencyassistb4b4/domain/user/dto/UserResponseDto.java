package com.example.emergencyassistb4b4.domain.user.dto;

import com.example.emergencyassistb4b4.domain.user.domain.User;
import com.example.emergencyassistb4b4.domain.user.domain.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResponseDto {

    private Long id;

    private String email;

    private UserRole userRole;

    private String nickname;

    private String province;

    private String city;

    public static UserResponseDto from(User user) {

        return new UserResponseDto(user.getId(), user.getEmail(), user.getUserRole(), user.getNickname(), user.getProvince(), user.getCity());
    }
}