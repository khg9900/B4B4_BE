package com.example.emergencyassistb4b4.domain.volunteer.repository;

import com.example.emergencyassistb4b4.domain.volunteer.domain.VolunteerTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface VolunteerTeamRepository extends JpaRepository<VolunteerTeam, Long> {
    Optional<VolunteerTeam> findByPost_IdAndTeamNumber(Long postId, int teamNumber);

    @Query("""
    SELECT DISTINCT VT FROM VolunteerTeam VT
    JOIN FETCH VT.post p
    JOIN FETCH p.location
    JOIN FETCH p.attendancePolicy
    LEFT JOIN FETCH VT.participants
    WHERE VT.id = :teamId
""")
    Optional<VolunteerTeam> findWithPostAndDetailsById(@Param("teamId") Long teamId);
}