package com.example.emergencyassistb4b4.volunteer.repository;

import com.example.emergencyassistb4b4.volunteer.domain.Post;
import com.example.emergencyassistb4b4.volunteer.dto.Join.CheckinPeriodDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    @Query("""
        SELECT new com.example.emergencyassistb4b4.volunteer.dto.Join.CheckinPeriodDto(
            a.checkinStart, a.checkinEnd
        )
        FROM Post p
        JOIN p.attendancePolicy a
        WHERE p.id = :postId
    """)
    Optional<CheckinPeriodDto> findCheckinPeriodByPostId(@Param("postId") Long postId);

    Optional<Post> findByIdAndUserId(Long postId, Long userId);
}