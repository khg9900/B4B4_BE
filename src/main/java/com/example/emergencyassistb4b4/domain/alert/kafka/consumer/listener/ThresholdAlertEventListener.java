package com.example.emergencyassistb4b4.domain.alert.kafka.consumer.listener;

import com.example.emergencyassistb4b4.domain.alert.service.trigger.ReportThresholdAlertTriggerService;
import com.example.emergencyassistb4b4.global.kafka.dto.ThresholdAlertEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class ThresholdAlertEventListener {

    private final ReportThresholdAlertTriggerService triggerService;

    @KafkaListener(
            topics = "${spring.kafka.topic.threshold}",
            groupId = "${spring.kafka.group.threshold}",
            containerFactory = "thresholdListenerFactory"
    )
    public void onDisasterReported(
            ThresholdAlertEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset
    ) {

        log.info("[THRESHOLD] consumed topic={}, partition={}, offset={}, payload={}", topic, partition, offset, event);

        try {
            triggerService.handleThresholdAlert(event);
        } catch (Exception e) {
            log.error("[누적 알림 처리 실패] province={}, city={}, type={}, time={}",
                event.getProvince(), event.getCity(), event.getAlertType(), event.getWindowStart(), e);
            throw e;
        }
    }
}
