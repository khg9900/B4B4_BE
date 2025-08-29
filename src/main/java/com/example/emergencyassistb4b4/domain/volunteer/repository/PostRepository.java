package com.example.emergencyassistb4b4.domain.volunteer.repository;

import com.example.emergencyassistb4b4.domain.volunteer.domain.Post;
import com.example.emergencyassistb4b4.domain.volunteer.dto.Join.CheckinPeriodDto;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long>, PostQueryRepository{
    @Query("""
        SELECT new com.example.emergencyassistb4b4.domain.volunteer.dto.Join.CheckinPeriodDto(
            a.checkinStart, a.checkinEnd
        )
        FROM Post p
        JOIN p.attendancePolicy a
        WHERE p.id = :postId
    """)
    Optional<CheckinPeriodDto> findCheckinPeriodByPostId(@Param("postId") Long postId);

}