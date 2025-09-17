package com.example.emergencyassistb4b4.domain.volunteer.repository;

import com.example.emergencyassistb4b4.domain.volunteer.domain.QAttendancePolicy;
import com.example.emergencyassistb4b4.domain.volunteer.domain.QPost;
import com.example.emergencyassistb4b4.domain.volunteer.domain.QVolunteerLocation;
import com.example.emergencyassistb4b4.domain.volunteer.domain.QVolunteerParticipant;
import com.example.emergencyassistb4b4.domain.volunteer.domain.VolunteerParticipant;
import com.example.emergencyassistb4b4.domain.volunteer.dto.Join.VolunteerParticipationFilter;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.example.emergencyassistb4b4.domain.volunteer.domain.QVolunteerTeam.volunteerTeam;


@Repository
@RequiredArgsConstructor
public class VolunteerParticipantRepositoryCustomImpl implements VolunteerParticipantRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QAttendancePolicy attendancePolicy = QAttendancePolicy.attendancePolicy;
    private final QVolunteerLocation volunteerLocation = QVolunteerLocation.volunteerLocation;
    private final QVolunteerParticipant volunteerParticipant = QVolunteerParticipant.volunteerParticipant;
    private final QPost post = QPost.post;

    @Override
    public List<VolunteerParticipant> getMyParticipation(
            Long userId,
            VolunteerParticipationFilter f
    ) {
        BooleanBuilder where = new BooleanBuilder()
                .and(volunteerParticipant.user.id.eq(userId));

        if (hasText(f.getProvince())) {
            where.and(volunteerLocation.province.eq(f.getProvince()));
        }
        if (hasText(f.getCity())) {
            where.and(volunteerLocation.city.eq(f.getCity()));
        }
        if (f.getPostStatus() != null) {
            where.and(post.status.eq(f.getPostStatus()));
        }
        if (f.getCategory() != null) {
            where.and(post.category.eq(f.getCategory()));
        }
        if (f.getCheckinStatus() != null) {
            where.and(volunteerParticipant.checkinStatus.eq(f.getCheckinStatus()));
        }
        if (f.getVolunteerStartDate() != null) {
            where.and(post.volunteerDate.goe(f.getVolunteerStartDate()));
        }
        if (f.getVolunteerEndDate() != null) {
            where.and(post.volunteerDate.loe(f.getVolunteerEndDate()));
        }

        return queryFactory
                .selectFrom(volunteerParticipant)
                .leftJoin(volunteerParticipant.volunteerTeam, volunteerTeam).fetchJoin()
                .join(volunteerTeam.post, post).fetchJoin()
                .leftJoin(post.attendancePolicy, attendancePolicy).fetchJoin()
                .leftJoin(post.location, volunteerLocation).fetchJoin()
                .where(where)
                .orderBy(volunteerParticipant.joinedAt.desc())
                .fetch();
    }

    private boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
