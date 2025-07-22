package com.example.emergencyassistb4b4.domain.auth.oauth.dto;

import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
public class KakaoUserDetailsDto {
    private Long id; // 카카오 고유 ID
    private String nickname;
    private String email;


}
