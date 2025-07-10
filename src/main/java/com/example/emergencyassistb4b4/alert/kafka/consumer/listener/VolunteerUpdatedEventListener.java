package com.example.emergencyassistb4b4.alert.kafka.consumer.listener;

import com.example.emergencyassistb4b4.alert.orchestrator.VolunteerUpdateAlertOrchestratorService;
import com.example.emergencyassistb4b4.global.kafka.dto.VolunteerUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class VolunteerUpdatedEventListener {

    private final VolunteerUpdateAlertOrchestratorService orchestratorService;

    @KafkaListener(
        topics = "volunteer-post-updated",
        containerFactory = "volunteerUpdatedListenerFactory"
    )
    public void onVolunteerUpdated(VolunteerUpdatedEvent event) {
        try {
            orchestratorService.process(event);
        } catch (Exception e) {
            log.error("[봉사 게시글 수정 알림 처리 실패] postId={}, title={}",
                event.getPostId(), event.getTitle(), e);
            throw e;
        }
    }
}
