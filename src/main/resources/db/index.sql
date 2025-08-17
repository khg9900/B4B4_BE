-- flyway:transactional=false   -- (중요) 생산환경에서 CONCURRENTLY 쓰려면 트랜잭션 끄기

-- 전역 최신순
CREATE INDEX IF NOT EXISTS idx_report_created_id_desc
    ON tbl_report (created_at DESC, id DESC);

-- 지역/상태 + 최신순
CREATE INDEX IF NOT EXISTS idx_report_region_status_created_id_desc
    ON tbl_report (province, city, status, created_at DESC, id DESC);

-- 작성자 + 최신순
CREATE INDEX IF NOT EXISTS idx_report_reporter_created_id_desc
    ON tbl_report (reporter_id, created_at DESC, id DESC);