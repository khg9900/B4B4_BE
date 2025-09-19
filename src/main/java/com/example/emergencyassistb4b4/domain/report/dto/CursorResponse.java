package com.example.emergencyassistb4b4.domain.report.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

public record CursorResponse<T>(
    List<T> content,
    boolean hasNext,
    LocalDateTime lastCreatedAt,
    Long nextId
) {

    public static <T> CursorResponse<T> of(
            List<T> rows, int pageSize,
            Function<T, LocalDateTime> createdAtGetter,
            Function<T, Long> idGetter
    ) {

        boolean hasNext = rows.size() > pageSize;

        if (hasNext) rows = rows.subList(0, pageSize);

        LocalDateTime nextCreatedAt = null;
        Long nextId = null;

        if (!rows.isEmpty()) {
            T last = rows.get(rows.size() - 1);
            nextCreatedAt = createdAtGetter.apply(last);
            nextId = idGetter.apply(last);
        }

        return new CursorResponse<>(rows, hasNext, nextCreatedAt, nextId);
    }
}
