package com.example.emergencyassistb4b4.domain.alert.kafka.service;

import com.example.emergencyassistb4b4.domain.alert.kafka.domain.KafkaDltLog;
import com.example.emergencyassistb4b4.domain.alert.kafka.repository.KafkaDltLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaDltLogService {

    private final KafkaDltLogRepository kafkaDlqLogRepository;

    public void logFailure(String topic, String consumerGroup, String payload, String reason, String listener, String exception, LocalDateTime failedAt) {

        try {
            KafkaDltLog log = KafkaDltLog.builder()
                .topic(topic)
                .consumerGroup(consumerGroup)
                .payload(payload)
                .reason(reason)
                .listener(listener)
                .exception(exception)
                .failedAt(failedAt)
                .build();

            kafkaDlqLogRepository.save(log);

        } catch (Exception e) {
            log.error("[Kafka DLQ] 로그 저장 실패! reason: {}", reason, e);
        }
    }
}
