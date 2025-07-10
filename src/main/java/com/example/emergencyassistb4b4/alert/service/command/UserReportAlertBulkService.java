package com.example.emergencyassistb4b4.alert.service.command;

import com.example.emergencyassistb4b4.alert.domain.report.UserReportAlert;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserReportAlertBulkService {

    private final EntityManager entityManager;

    public void saveAllInBatches(List<UserReportAlert> alerts, int batchSize) {
        for (int i = 0; i < alerts.size(); i++) {
            entityManager.persist(alerts.get(i));

            if ((i + 1) % batchSize == 0) {
                flushAndClear(); // 캐시 비우기 + DB에 쌓인 insert 전송
            }
        }

        // 마지막 남은 데이터 처리
        flushAndClear();
    }

    private void flushAndClear() {
        entityManager.flush();  // DB로 전송
        entityManager.clear();  // 영속성 컨텍스트 메모리 비우기
    }
}

