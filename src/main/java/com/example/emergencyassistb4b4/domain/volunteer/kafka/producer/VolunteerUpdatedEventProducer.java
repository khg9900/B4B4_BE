package com.example.emergencyassistb4b4.domain.volunteer.kafka.producer;

import com.example.emergencyassistb4b4.global.kafka.dto.VolunteerUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class VolunteerUpdatedEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topic.volunteer}")
    private String topic;

    public void sendVolunteerUpdatedEvent(VolunteerUpdatedEvent event) {

        kafkaTemplate.send(topic, event)
                .thenAccept(result -> log.info("kafka - volunteer-post-updated 발행 성공: {}", event))
                .exceptionally(ex -> {
                    log.error("kafka - volunteer-post-updated 발행 실패: {}", event, ex);
                    return null;
                });
    }
}
