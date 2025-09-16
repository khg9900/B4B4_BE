package com.example.emergencyassistb4b4.domain.report.repository;

import com.example.emergencyassistb4b4.domain.report.domain.QReport;
import com.example.emergencyassistb4b4.domain.report.domain.Report;
import com.example.emergencyassistb4b4.domain.report.enums.ReportStatus;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.emergencyassistb4b4.domain.report.domain.QReport.report;

@Repository
@RequiredArgsConstructor
public class ReportRepositoryImpl implements ReportRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QReport r = report;

    //offset
    @Override
    public Slice<Report> findNearby(String province, String city,ReportStatus status, Pageable pageable) {
        BooleanBuilder where = new BooleanBuilder();
        if (province != null && !province.isEmpty()) {where.and(report.province.eq(province));}
        if (city != null && !city.isEmpty()) {where.and(report.city.eq(city));}
        if (status != null) {where.and(report.status.eq(status));}

        // 쿼리 생성 및 페이징 (최신순)
        List<Report> content = queryFactory
                .selectFrom(report)
                .where(where)
                .orderBy(report.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = content.size() > pageable.getPageSize();
        if (hasNext) {content.remove(pageable.getPageSize());}
        return new SliceImpl<>(content, pageable, hasNext);
    }

    @Override
    public Slice<Report> findByReporter(Long userId, ReportStatus status, LocalDateTime start, LocalDateTime end, Pageable pageable) {
        BooleanBuilder where = new BooleanBuilder().and(r.reporter.id.eq(userId));

        if (status != null) where.and(r.status.eq(status));
        if (start != null) where.and(r.createdAt.goe(start));
        if (end != null) where.and(r.createdAt.loe(end));

        List<Report> content = queryFactory.selectFrom(r)
                .where(where)
                .orderBy(r.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        //true면 다음페이지 보여주기
        boolean hasNext = content.size() > pageable.getPageSize();
        //넘치는거 다시 제거
        if (hasNext) content.remove(pageable.getPageSize());
        return new SliceImpl<>(content, pageable, hasNext);
    }

    //cursor 페이지
    @Override
    public List<Report> findNearbyByCursor(String province, String city, ReportStatus status,
                                           LocalDateTime lastCreatedAt, Long lastId,
                                           int limitPlusOne) {
        BooleanBuilder where = new BooleanBuilder();
        if(province != null && !province.isEmpty()) where.and(r.province.eq(province));
        if (city != null && !city.isEmpty()) where.and(r.city.eq(city));
        if (status != null) where.and(r.status.eq(status));

        BooleanBuilder cursor = buildCursorWhere(lastCreatedAt, lastId);

        return queryFactory
                .selectFrom(r)
                .where(where.and(cursor))
                .orderBy(r.createdAt.desc(), r.id.desc())
                .limit(limitPlusOne)
                .fetch();
    }

    @Override
    public List<Report> findByReporterByCursor(Long userId, ReportStatus status,
                                               LocalDate start, LocalDate end,
                                               LocalDateTime lastCreatedAt, Long lastId,
                                               int limitPlusOne, boolean desc) {
        BooleanBuilder where = new BooleanBuilder().and(r.reporter.id.eq(userId));
        if (status != null) where.and(r.status.eq(status));
        if (start != null) where.and(r.createdAt.goe(start.atStartOfDay()));
        if (end != null) where.and(r.createdAt.loe(end.atTime(23, 59, 59)));

        BooleanBuilder cursor = new BooleanBuilder();
        if (lastCreatedAt != null && lastId != null) {
            if (desc) {
                cursor.and(r.createdAt.lt(lastCreatedAt)
                    .or(r.createdAt.eq(lastCreatedAt).and(r.id.lt(lastId))));
            } else {
                cursor.and(r.createdAt.gt(lastCreatedAt)
                    .or(r.createdAt.eq(lastCreatedAt).and(r.id.gt(lastId))));
            }
        }

        return queryFactory
                .selectFrom(r)
                .where(where.and(cursor))
                .orderBy(desc ? r.createdAt.desc() : r.createdAt.asc(),
                         desc ? r.id.desc() : r.id.asc())
                .limit(limitPlusOne)
                .fetch();
    }

    /*
    * (createdAt DESC, id DESC) 커서 다음 구간:
    *  createdAt < lastCreatedAt OR (createdAt = lastCreatedAt AND id < lastId)
    */
    private BooleanBuilder buildCursorWhere(LocalDateTime lastCreatedAt, Long lastId){
    BooleanBuilder b = new BooleanBuilder();
        if(lastCreatedAt == null || lastId ==null) return b;
        b.and(
            r.createdAt.lt(lastCreatedAt)
                    .or(r.createdAt.eq(lastCreatedAt).and(r.id.lt(lastId)))
        );
        return b;
    }
}
