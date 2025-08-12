package exp.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ResultRepository extends JpaRepository<Result, Long> {

    List<Result> findByTestAndSystemAndConsumedAtMsIsNotNull(String test, String system);

    Optional<Result> findByPayloadId(String payloadId);

    // ✅ 컨슈머에서 O(n) 스트림 대신 바로 단건 업데이트
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("update Result r set r.consumedAtMs = :ts where r.payloadId = :pid")
    int markConsumed(@Param("pid") String payloadId, @Param("ts") long consumedAtMs);
}