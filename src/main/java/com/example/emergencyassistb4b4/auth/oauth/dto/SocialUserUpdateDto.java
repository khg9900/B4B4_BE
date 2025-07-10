package com.example.emergencyassistb4b4.auth.oauth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SocialUserUpdateDto {
    private final String nickname;
    private final String profileImage; // 추후 확장 가능
    // 추가로 소셜에서 받아올 수 있는 필드들을 확장 가능
}
