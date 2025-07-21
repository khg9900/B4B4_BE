package com.example.emergencyassistb4b4.domain.alert.kafka.service;

import com.example.emergencyassistb4b4.domain.alert.kafka.domain.KafkaDlqLog;
import com.example.emergencyassistb4b4.domain.alert.kafka.repository.KafkaDlqLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaDlqLogService { // Kafka 처리 실패 시 로그를 DB에 저장하는 서비스

    private final KafkaDlqLogRepository kafkaDlqLogRepository;

    // Kafka 메시지 처리 실패 시 DB에 로그를 저장
    public void logFailure(String topic, String consumerGroup, String payload, String reason, String listener, String exception, LocalDateTime failedAt) {

        try {
            // Kafka 실패 로그 엔티티 객체 생성
            KafkaDlqLog log = KafkaDlqLog.builder()
                .topic(topic)
                .consumerGroup(consumerGroup)
                .payload(payload)
                .reason(reason)
                .listener(listener)
                .exception(exception)
                .failedAt(failedAt)
                .build();

            // DB에 저장
            kafkaDlqLogRepository.save(log);

        } catch (Exception e) {
            // 로그 저장 자체가 실패할 경우 에러 출력
            log.error("[Kafka DLQ] 로그 저장 실패! reason: {}", reason, e);
        }
    }
}
