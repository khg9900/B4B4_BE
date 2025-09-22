package com.example.emergencyassistb4b4.domain.volunteer.repository;

import static com.example.emergencyassistb4b4.domain.volunteer.domain.QPost.post;
import static com.example.emergencyassistb4b4.domain.volunteer.domain.QVolunteerLocation.volunteerLocation;

import com.example.emergencyassistb4b4.domain.volunteer.domain.Post;
import com.example.emergencyassistb4b4.domain.volunteer.dto.Post.PostFilterRequest;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PostQueryRepositoryImpl implements PostQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<Post> findPosts(Long userId, PostFilterRequest f, Pageable pageable) {

        BooleanBuilder where = new BooleanBuilder();

        if (userId != null) {
            where.and(post.user.id.eq(userId));
        }

        if (f.getStatus() != null) {
            where.and(post.status.eq(f.getStatus()));
        }
        if (f.getCategory() != null) {
            where.and(post.category.eq(f.getCategory()));
        }
        if (f.getVolunteerStartDate() != null) {
            where.and(post.volunteerDate.goe(f.getVolunteerStartDate()));
        }
        if (f.getVolunteerEndDate() != null) {
            where.and(post.volunteerDate.loe(f.getVolunteerEndDate()));
        }

        if (hasText(f.getProvince())) {
            where.and(volunteerLocation.province.eq(f.getProvince()));
        }
        if (hasText(f.getCity())) {
            where.and(volunteerLocation.city.eq(f.getCity()));
        }

        int size = pageable.getPageSize();

        List<Post> rows = queryFactory
            .selectFrom(post)
            .leftJoin(post.location, volunteerLocation).fetchJoin()
            .where(where)
            .orderBy(post.id.desc())
            .offset(pageable.getOffset())
            .limit(size + 1L)
            .fetch();

        boolean hasNext = rows.size() > size;
        if (hasNext) rows = rows.subList(0, size);

        return new SliceImpl<>(rows, pageable, hasNext);
    }

    private boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}


