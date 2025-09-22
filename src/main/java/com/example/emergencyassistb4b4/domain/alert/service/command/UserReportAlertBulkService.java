package com.example.emergencyassistb4b4.domain.alert.service.command;

import com.example.emergencyassistb4b4.domain.alert.domain.report.UserReportAlert;
import jakarta.persistence.EntityManager;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserReportAlertBulkService {

    private final EntityManager entityManager;

    public void saveAllInBatches(List<UserReportAlert> alerts, int batchSize) {
        for (int i = 0; i < alerts.size(); i++) {
            entityManager.persist(alerts.get(i));

            if ((i + 1) % batchSize == 0) {
                flushAndClear();
            }
        }

        flushAndClear();
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}

