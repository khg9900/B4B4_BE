package com.example.emergencyassistb4b4.domain.report.repository;

import com.example.emergencyassistb4b4.domain.report.domain.QReport;
import com.example.emergencyassistb4b4.domain.report.domain.Report;
import com.example.emergencyassistb4b4.domain.report.enums.ReportStatus;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
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
}