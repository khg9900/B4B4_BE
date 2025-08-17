package com.example.emergencyassistb4b4.domain.alert.kafka.consumer.dlt;

import com.example.emergencyassistb4b4.domain.alert.kafka.service.KafkaDltLogService;
import com.example.emergencyassistb4b4.global.kafka.dto.VolunteerUpdatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class VolunteerUpdateAlertDltHandler {

    private final ObjectMapper objectMapper;
    private final KafkaDltLogService kafkaDltLogService;

    // 원본(정상) 토픽/그룹 – 실패 로그 기록용
    @Value("${spring.kafka.topic.volunteer}")
    private String volunteerTopic;

    @Value("${spring.kafka.group.volunteer}")
    private String volunteerGroup;

    // DLT 토픽 – 리스너 구독용(주석용으로도 사용 가능)
    @Value("${spring.kafka.topic.dlt.volunteer}")
    private String volunteerDltTopic;

    @KafkaListener(
            topics = "${spring.kafka.topic.dlt.volunteer}",
            groupId = "${spring.kafka.group.dlt.volunteer}",
            containerFactory = "dltListenerFactory"
    )
    public void handle(String rawMessage) {

        final String listener = "volunteerUpdatedEventListener";

        VolunteerUpdatedEvent parsedEvent = null;
        try {
            parsedEvent = objectMapper.readValue(rawMessage, VolunteerUpdatedEvent.class);
        } catch (Exception e) {
            log.error("[DLT:봉사글] 역직렬화 실패 - 리스너: {}, 이유: {}", listener, e.getMessage());

            kafkaDltLogService.logFailure(
                    volunteerTopic,
                    volunteerGroup,
                    rawMessage,
                    "역직렬화 실패로 인해 DLQ 메시지 파싱 불가",
                    listener,
                    e.getClass().getSimpleName() + ": " + e.getMessage(),
                    LocalDateTime.now()
            );

            return;
        }

        log.warn("[DLT:봉사글] 역직렬화 성공 - 비즈니스 로직 처리 중 예외 가능성: {}", parsedEvent);
    }
}
