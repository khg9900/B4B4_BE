package com.example.emergencyassistb4b4.volunteer.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "VolunteerTeam")
public class VolunteerTeam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    private int teamNumber;
    private int maxCapacity;

    @Builder.Default
    @OneToMany(mappedBy = "volunteerTeam", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<VolunteerParticipant> participants = new ArrayList<>();


    public void setPost(Post post) {
        this.post = post;
    }
}