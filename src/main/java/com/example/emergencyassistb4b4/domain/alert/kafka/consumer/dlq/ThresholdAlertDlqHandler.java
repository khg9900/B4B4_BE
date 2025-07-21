package com.example.emergencyassistb4b4.domain.alert.kafka.consumer.dlq;

import com.example.emergencyassistb4b4.domain.alert.kafka.service.KafkaDlqLogService;
import com.example.emergencyassistb4b4.global.kafka.dto.DisasterReportedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class ThresholdAlertDlqHandler { // Kafka DLT로 전송된 실패 메시지를 후처리(log 기록)하기 위한 리스너

    private final ObjectMapper objectMapper;
    private final KafkaDlqLogService kafkaDlqLogService;

    /**
     * 즉시 알림과 마찬가지로 동일한 DLQ 토픽(report-reported-dlt)을 구독하되,
     * 이 리스너는 임계값 기반 알림 리스너(alert-threshold-group)에서 실패한 메시지를 처리
     */
    @KafkaListener(
        topics = "report-reported-dlt",
        containerFactory = "disasterReportedDltListenerFactory"
    )
    public void handle(String rawMessage) {
        final String listener = "ThresholdAlertEventListener#onDisasterReported";

        DisasterReportedEvent parsedEvent = null;
        try {
            parsedEvent = objectMapper.readValue(rawMessage, DisasterReportedEvent.class);
        } catch (Exception e) {
            log.error("[DLQ:누적알림] 역직렬화 실패 - 리스너: {}, 이유: {}", listener, e.getMessage());

            kafkaDlqLogService.logFailure(
                "report-reported",
                "alert-threshold-group",
                rawMessage,
                "역직렬화 실패로 인해 DLQ 메시지 파싱 불가",
                listener,
                e.getClass().getSimpleName() + ": " + e.getMessage(),
                LocalDateTime.now()
            );
            return;
        }

        log.warn("[DLQ:누적알림] 역직렬화 성공 - 원인은 비즈니스 로직 처리 중 예외일 가능성 있음: {}", parsedEvent);
    }
}