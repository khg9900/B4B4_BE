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
public class ImmediateAlertEventListener {

    private final ReportImmediateAlertOrchestratorService orchestratorService;

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
            orchestratorService.process(event);
        } catch (Exception e) {
            log.error("[즉시 알림 처리 실패] province={}, city={}, disaster={}, time={}",
                event.getProvince(), event.getCity(), event.getDisasterType(), event.getReportedAt(), e);
            throw e;
        }
    }
}
