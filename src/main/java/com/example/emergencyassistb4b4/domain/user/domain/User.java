package com.example.emergencyassistb4b4.domain.user.domain;

import com.example.emergencyassistb4b4.domain.auth.oauth.dto.SocialUserUpdateDto;
import com.example.emergencyassistb4b4.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;


@Getter
@Builder
@Entity
@AllArgsConstructor
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nickname")
    private String nickname;

    @Column(length = 20)
    private String phoneNumber;

    @Column(unique = true, nullable = false, length = 100)
    private String email; //필수

    @Column(length = 255)
    private String password; //필수

    @Enumerated(EnumType.STRING)
    @Column(name = "login_type")
    private LoginType loginType; //필수

    @Column(name = "provider", length = 255)
    private String provider;

    // 시,도
    @Column(name = "province")
    private String province;

    // 구,군
    @Column(name = "city")
    private String city;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role")
    private UserRole userRole;

    @Builder
    public User(String email, String password, String nickname, String province, String city, LoginType loginType, String provider, UserRole userRole) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.province = province;
        this.city = city;
        this.loginType = loginType;
        this.provider = provider;
        this.userRole = userRole;

    }


    public User updateNickname(String nickname) {
        this.nickname = nickname;
        return this;
    }

    public User updateSocialInfo(SocialUserUpdateDto dto) {
        this.nickname = dto.getNickname();
        return this;
    }
}
