package com.example.emergencyassistb4b4.domain.alert.kafka.consumer.listener;


import com.example.emergencyassistb4b4.domain.alert.orchestrator.VolunteerPostAlertOrchestratorService;
import com.example.emergencyassistb4b4.global.kafka.dto.VolunteerCancelEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class VolunteerCanceledEventListener {
    private final VolunteerPostAlertOrchestratorService orchestratorService;

    @KafkaListener(
            topics = "${spring.kafka.topic.volunteerCancel}",
            groupId = "${spring.kafka.group.volunteerCancel}",
            containerFactory = "volunteerUpdatedListenerFactory"
    )
    public void onVolunteerCanceled(
            VolunteerCancelEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset
    ) {

        log.info("[VOLUNTEER] consumed topic={}, partition={}, offset={}, payload={}", topic, partition, offset, event);

        try {
            orchestratorService.process(event);
        } catch (Exception e) {
            log.error("[봉사 게시글 취소 알림 처리 실패] postId={}, title={}",
                    event.getPostId(), event.getTitle(), e);
            throw e;
        }
    }
}
