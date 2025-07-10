package com.example.emergencyassistb4b4.alert.kafka.consumer.dlq;

import com.example.emergencyassistb4b4.alert.kafka.service.KafkaDlqLogService;
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
public class ImmediateAlertDlqHandler { // Kafka DLT로 전송된 실패 메시지를 후처리(log 기록)하기 위한 리스너

    private final ObjectMapper objectMapper; // DLQ로부터 받은 JSON 메시지를 객체로 변환
    private final KafkaDlqLogService kafkaDlqLogService; // DLQ 로깅 서비스 (DB 또는 파일 기록 등)

    /**
     * KafkaListener로 report-reported-dlt 토픽을 구독하고, 해당 토픽은 즉시 알림 리스너에서 처리 실패한 메시지가 전송됨
     * disasterReportedDltListenerFactory를 사용하여 String 타입 메시지 처리
     */
    @KafkaListener(
        topics = "report-reported-dlt",
        containerFactory = "disasterReportedDltListenerFactory"
    )
    public void handle(String rawMessage) {

        // 어떤 리스너가 실패했는지 명시적으로 로깅하기 위한 식별자
        final String listener = "ImmediateAlertEventListener#onDisasterReported";

        DisasterReportedEvent parsedEvent = null;

        try {
            // DLQ 메시지를 원래 DTO 객체로 역직렬화
            parsedEvent = objectMapper.readValue(rawMessage, DisasterReportedEvent.class);

        } catch (Exception e) {
            // 역직렬화 실패 시 로그 기록 및 DB 등에 저장
            log.error("[DLQ:즉시알림] 역직렬화 실패 - 리스너: {}, 이유: {}", listener, e.getMessage());

            kafkaDlqLogService.logFailure(
                "report-reported", // 원래의 토픽명
                "alert-immediate-group", // 실패한 consumer group
                rawMessage, // 원본 메시지
                "역직렬화 실패로 인해 DLQ 메시지 파싱 불가", // 요약 사유
                listener, // 실패한 리스너 이름
                e.getClass().getSimpleName() + ": " + e.getMessage(), // 상세 예외
                LocalDateTime.now() // 실패 시간
            );
            return;
        }

        // 역직렬화에는 성공했지만, 비즈니스 로직상 처리중 실패했을 가능성
        log.warn("[DLQ:즉시알림] 역직렬화 성공 - 원인은 비즈니스 로직 처리 중 예외일 가능성 높음: {}", parsedEvent);
    }
}


