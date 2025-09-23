package com.example.emergencyassistb4b4.domain.volunteer.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import lombok.Setter;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "volunteer_team")
@SequenceGenerator(
    name = "volunteer_team_seq_gen",
    sequenceName = "volunteer_team_seq",
    allocationSize = 50
)
public class VolunteerTeam {

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "volunteer_team_seq_gen"
    )
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    private int teamNumber;
    private int maxCapacity;

    @Builder.Default
    @OneToMany(mappedBy = "volunteerTeam", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<VolunteerParticipant> participants = new ArrayList<>();

}