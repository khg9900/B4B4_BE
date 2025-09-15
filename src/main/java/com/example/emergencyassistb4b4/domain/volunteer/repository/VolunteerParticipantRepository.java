package com.example.emergencyassistb4b4.domain.volunteer.repository;

import com.example.emergencyassistb4b4.domain.volunteer.domain.VolunteerParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VolunteerParticipantRepository extends JpaRepository<VolunteerParticipant, Long> {

    Optional<VolunteerParticipant> findByIdAndUserId(Long participantId, Long userId);

    @Query("""
        SELECT vp.user.id
        FROM VolunteerParticipant vp
        JOIN vp.volunteerTeam t
        WHERE t.post.id = :postId
    """)
    List<Long> findUserIdsByPostId(@Param("postId") Long postId);

    @Query("""
        SELECT t.post.id
        FROM VolunteerParticipant vp
        JOIN vp.volunteerTeam t
        WHERE vp.id = :participantId
    """)
    Optional<Long> findPostIdByParticipantId(@Param("participantId") Long participantId);

    @Query("""
        SELECT vp
        FROM VolunteerParticipant vp
        JOIN FETCH vp.volunteerTeam t
        JOIN FETCH t.post p
        JOIN FETCH p.location l
        JOIN FETCH p.attendancePolicy ap
        WHERE vp.id = :volunteerId
    """)
    Optional<VolunteerParticipant> findWithTeamAndPolicyById(@Param("volunteerId") Long volunteerId);

    @Query("""
    SELECT COUNT(vp) > 0
    FROM VolunteerParticipant vp
    WHERE vp.user.id = :userId
      AND vp.volunteerTeam.post.id = :postId
      AND vp.checkinStatus = 'PARTICIPATED'
""")
    boolean existsActiveParticipation(@Param("userId") Long userId, @Param("postId") Long postId);

    @Query("""
    select count(vp) 
    from VolunteerParticipant vp
    where vp.volunteerTeam.id = :teamId
      and vp.checkinStatus = 'PARTICIPATED'
""")
    long countParticipatedByTeamId(@Param("teamId") Long teamId);
}
