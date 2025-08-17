package com.example.emergencyassistb4b4.domain.alert.kafka.consumer.listener;

import com.example.emergencyassistb4b4.domain.alert.orchestrator.ReportImmediateAlertOrchestratorService;
import com.example.emergencyassistb4b4.global.kafka.dto.DisasterReportedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class ImmediateAlertEventListener { // 즉시 알림(예: 재난 발생 시 FCM 전송 등)을 처리하는 Kafka 리스너

    // 실제 즉시 알림 처리 로직을 담당하는 orchestrator 서비스
    private final ReportImmediateAlertOrchestratorService orchestratorService;

    /**
     * Kafka 토픽 "report-reported" 구독
     * - 리스너 컨테이너 팩토리는 immediateListenerFactory 사용 (AckMode.RECORD + ErrorHandler 포함)
     * - 메시지는 DisasterReportedEvent 객체로 자동 역직렬화됨
     */
    @KafkaListener(
            topics = "${spring.kafka.topic.immediate}",
            groupId = "${spring.kafka.group.immediate}",
            containerFactory = "immediateListenerFactory"
    )
    public void onDisasterReported(
            DisasterReportedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset
    ) {

        log.info("[IMMEDIATE] consumed topic={}, partition={}, offset={}, payload={}", topic, partition, offset, event);

        try {
            // Kafka 메시지 수신 시 orchestratorService를 통해 즉시 알림 처리
            orchestratorService.process(event);
        } catch (Exception e) {
            // 처리 중 예외 발생 시 로그 출력 및 예외 재전파 -> DefaultErrorHandler에 의해 retry 또는 DLQ 전송됨
            log.error("[즉시 알림 처리 실패] province={}, city={}, disaster={}, time={}",
                event.getProvince(), event.getCity(), event.getDisasterType(), event.getReportedAt(), e);
            throw e; // 반드시 예외를 다시 던져야 DLQ로 이동
        }
    }
}
