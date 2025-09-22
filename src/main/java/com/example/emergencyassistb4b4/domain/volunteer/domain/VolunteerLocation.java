package com.example.emergencyassistb4b4.domain.volunteer.domain;

import com.example.emergencyassistb4b4.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "volunteer_location")
@SequenceGenerator(
    name = "volunteer_location_seq_gen",
    sequenceName = "volunteer_location_seq",
    allocationSize = 50
)
public class VolunteerLocation extends BaseEntity {

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "volunteer_location_seq_gen"
    )
    private Long id;

    @Column(nullable = false, length = 255)
    private String province;

    private String city;

    @Column(nullable = false)
    private String placeName;

    private Double locationLat;

    private Double locationLng;

    @Setter
    @OneToOne
    @JoinColumn(name = "post_id", nullable = false, unique = true)
    private Post post;

    public void update(String province, String city, String placeName, Double latitude, Double longitude) {
        this.province = province;
        this.city = city;
        this.placeName = placeName;
        this.locationLat = latitude;
        this.locationLng = longitude;
    }
}
