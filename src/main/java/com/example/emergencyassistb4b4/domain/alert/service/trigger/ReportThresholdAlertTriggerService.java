package com.example.emergencyassistb4b4.domain.alert.service.trigger;

import com.example.emergencyassistb4b4.domain.alert.redis.RedisThresholdCounter;
import com.example.emergencyassistb4b4.domain.alert.orchestrator.ReportThresholdAlertOrchestratorService;
import com.example.emergencyassistb4b4.global.kafka.dto.DisasterReportedEvent;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportThresholdAlertTriggerService {

    private final RedisThresholdCounter redisThresholdCounter;
    private final ReportThresholdAlertOrchestratorService reportAlertOrchestratorService;

    private static final Duration KEY_TTL = Duration.ofDays(1);

    public void checkReportThreshold(DisasterReportedEvent event) {
        String counterKey = generateReportCounterKey(event);
        String notifyKeyPrefix = "alert:" + counterKey;
        List<Long> thresholds = List.of(3L, 5L, 7L, 10L);

        Long matchedThreshold = redisThresholdCounter.incrementAndCheckThreshold(counterKey,
            notifyKeyPrefix, KEY_TTL, thresholds);

        if (matchedThreshold != null && matchedThreshold > 0) {
            String notifyKey = String.format("alert:%d:%s", matchedThreshold, counterKey);
            log.info("임계치 도달 - key={}, count={}", notifyKey, matchedThreshold);
            reportAlertOrchestratorService.process(notifyKey);
        }
    }

    private String generateReportCounterKey(DisasterReportedEvent event) {
        return String.format("report:%s:%s:%s:%s",
            event.getProvince(),
            event.getCity(),
            event.getDisasterType(),
            event.getReportedAt().toLocalDate());
    }
}