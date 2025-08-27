package com.example.emergencyassistb4b4.domain.alert.kafka.config.events;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.event.ListenerContainerIdleEvent;

@Slf4j
@Configuration
@EnableKafka
public class KafkaListenerEventConfig {

    @Bean
    public ApplicationListener<ListenerContainerIdleEvent> kafkaIdleLogger() {

        return event -> {
            var containerId = event.getListenerId();
            log.warn("[KAFKA-IDLE] listenerId={}, idleTimeMs={}", containerId, event.getIdleTime());
        };
    }
}
