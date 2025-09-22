package com.example.emergencyassistb4b4.domain.volunteer.kafka.producer;

import com.example.emergencyassistb4b4.global.kafka.dto.VolunteerUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class VolunteerUpdatedEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topic.volunteer}")
    private String topic;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendVolunteerUpdatedEvent(VolunteerUpdatedEvent event) {

        kafkaTemplate.send(topic, event)
                .exceptionally(ex -> {
                    log.error("kafka - volunteer-post-updated 발행 실패: {}", event, ex);
                    return null;
                });
    }
}
