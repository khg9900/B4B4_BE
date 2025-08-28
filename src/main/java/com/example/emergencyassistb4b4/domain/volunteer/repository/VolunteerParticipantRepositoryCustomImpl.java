package com.example.emergencyassistb4b4.domain.volunteer.repository;

import com.example.emergencyassistb4b4.domain.volunteer.domain.QAttendancePolicy;
import com.example.emergencyassistb4b4.domain.volunteer.domain.QPost;
import com.example.emergencyassistb4b4.domain.volunteer.domain.QVolunteerParticipant;
import com.example.emergencyassistb4b4.domain.volunteer.domain.VolunteerParticipant;
import com.example.emergencyassistb4b4.domain.volunteer.enums.CheckinStatus;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.emergencyassistb4b4.domain.volunteer.domain.QVolunteerTeam.volunteerTeam;

@Repository
@RequiredArgsConstructor
public class VolunteerParticipantRepositoryCustomImpl implements VolunteerParticipantRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QAttendancePolicy attendancePolicy= QAttendancePolicy.attendancePolicy;
    private final QVolunteerParticipant volunteerParticipant=QVolunteerParticipant.volunteerParticipant;
    private final QPost post=QPost.post;


    @Override
    public List<VolunteerParticipant> findAllByUserIdWithPostAndTeam(
            Long userId,
            CheckinStatus status,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        BooleanBuilder where = new BooleanBuilder()
                .and(volunteerParticipant.user.id.eq(userId));

        if (status != null) {
            where.and(volunteerParticipant.checkinStatus.eq(status));
        }

        if (startTime != null) {
            where.and(attendancePolicy.checkinStart.goe(startTime));
        }

        if (endTime != null) {
            where.and(attendancePolicy.checkinEnd.loe(endTime));
        }

        return queryFactory
                .selectFrom(volunteerParticipant)
                .leftJoin(volunteerParticipant.volunteerTeam, volunteerTeam).fetchJoin()
                .join(volunteerTeam.post, post).fetchJoin()
                .leftJoin(post.attendancePolicy, attendancePolicy).fetchJoin()
                .where(where)
                .orderBy(post.createdAt.desc())
                .fetch();
    }
}
