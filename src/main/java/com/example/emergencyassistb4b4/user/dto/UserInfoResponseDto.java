package com.example.emergencyassistb4b4.user.dto;

import com.example.emergencyassistb4b4.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UserInfoResponseDto {

    private final String name;

    private final String email;

    private final String phoneNumber;

    public static UserInfoResponseDto from(User user) {

        return UserInfoResponseDto.builder()
                .name(user.getNickname())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }
}