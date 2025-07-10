package com.example.emergencyassistb4b4.volunteer.domain;

import com.example.emergencyassistb4b4.global.entity.BaseEntity;
import com.example.emergencyassistb4b4.volunteer.dto.Post.common.PostLocationDto;
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

    public void update(String placeName, Double latitude, Double longitude) {
        this.placeName = placeName;
        this.locationLat = latitude;
        this.locationLng = longitude;
    }
}
