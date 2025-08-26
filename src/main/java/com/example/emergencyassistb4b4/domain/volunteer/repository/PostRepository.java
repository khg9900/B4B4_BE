package com.example.emergencyassistb4b4.domain.volunteer.repository;

import com.example.emergencyassistb4b4.domain.volunteer.domain.Post;
import com.example.emergencyassistb4b4.domain.volunteer.dto.Join.CheckinPeriodDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("""
        SELECT new com.example.emergencyassistb4b4.domain.volunteer.dto.Join.CheckinPeriodDto(
            a.checkinStart, a.checkinEnd
        )
        FROM Post p
        JOIN p.attendancePolicy a
        WHERE p.id = :postId
    """)
    Optional<CheckinPeriodDto> findCheckinPeriodByPostId(@Param("postId") Long postId);

    @Query("""
        SELECT COUNT(p) > 0
        FROM Post p
        JOIN p.attendancePolicy a
        JOIN p.teams t
        LEFT JOIN t.participants vp
        LEFT JOIN vp.user u
        WHERE p.id != :postId
          AND u.id = :userId
          AND (
              (a.checkinStart BETWEEN :checkinStart AND :checkinEnd)
              OR
              (a.checkinEnd BETWEEN :checkinStart AND :checkinEnd)
              OR
              (:checkinStart BETWEEN a.checkinStart AND a.checkinEnd)
              OR
              (:checkinEnd BETWEEN a.checkinStart AND a.checkinEnd)
          )
    """)
    boolean existsOverlappingCheckinPeriod(@Param("userId") Long userId,
                                           @Param("postId") Long postId,
                                           @Param("checkinStart") LocalDateTime checkinStart,
                                           @Param("checkinEnd") LocalDateTime checkinEnd);

    Optional<Post> findByIdAndUserId(Long postId, Long userId);
}
