package com.example.emergencyassistb4b4.domain.user.domain;

import com.example.emergencyassistb4b4.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
@SequenceGenerator(
    name = "users_seq_gen",
    sequenceName = "users_seq",
    allocationSize = 50
)
public class User extends BaseEntity{

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "users_seq_gen"
    )
    private Long id;

    @Column(name = "nickname")
    private String nickname;

    @Column(length = 20)
    private String phoneNumber;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(length = 255)
    private String password;

    @Column(name = "provider", length = 255)
    private String provider;

    @Column(name = "province")
    private String province;

    @Column(name = "city")
    private String city;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role")
    private UserRole userRole;

    @Builder
    public User(String email, String password, String nickname, String province, String city, String provider, UserRole userRole) {

        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.province = province;
        this.city = city;
        this.provider = provider;
        this.userRole = userRole;
    }
}
