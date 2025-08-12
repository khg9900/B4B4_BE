package com.example.emergencyassistb4b4.domain.alert.kafka.consumer.dlt;

import com.example.emergencyassistb4b4.domain.alert.kafka.service.KafkaDltLogService;
import com.example.emergencyassistb4b4.global.kafka.dto.VolunteerUpdatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class VolunteerUpdateAlertDltHandler {

    private final ObjectMapper objectMapper;
    private final KafkaDltLogService kafkaDltLogService;

    @KafkaListener(
        topics = "volunteer-post-updated-dlt",
        containerFactory = "volunteerUpdatedDltListenerFactory"
    )
    public void handle(String rawMessage) {
        final String listener = "volunteerUpdatedEventListener";

        VolunteerUpdatedEvent parsedEvent = null;
        try {
            parsedEvent = objectMapper.readValue(rawMessage, VolunteerUpdatedEvent.class);
        } catch (Exception e) {
            log.error("[DLQ:누적알림] 역직렬화 실패 - 리스너: {}, 이유: {}", listener, e.getMessage());

            kafkaDltLogService.logFailure(
                "volunteer-post-updated",
                "alert-volunteer-update-group",
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
