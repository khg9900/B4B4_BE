package com.example.emergencyassistb4b4.domain.volunteer.domain;

import com.example.emergencyassistb4b4.global.entity.BaseEntity;
import jakarta.persistence.*;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class VolunteerLocation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 행정구역 (시/도)
    @Column(name = "province", nullable = false, length = 255)
    private String province;

    // 행정구역 (구/군)
    @Column(name = "city", length = 255)
    private String city;

    // 상세 주소
    @Column(name = "place_name", nullable = false)
    private String placeName;

    // 위도
    @Column(name = "location_lat")
    private Double locationLat;

    // 경도
    @Column(name = "location_lng")
    private Double locationLng;

    @OneToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    public void setPost(Post post) {
        this.post = post;
    }

    public void update(String province, String city, String placeName, Double latitude, Double longitude) {
        this.province = province;
        this.city = city;
        this.placeName = placeName;
        this.locationLat = latitude;
        this.locationLng = longitude;
    }
}
