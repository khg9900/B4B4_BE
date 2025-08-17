package com.example.emergencyassistb4b4.domain.alert.kafka.consumer.dlt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ThresholdAlertDltListener {

    @KafkaListener(
            topics = "${spring.kafka.topic.dlt.threshold}",
            groupId = "${spring.kafka.group.dlt.threshold}",
            containerFactory = "dltListenerFactory",
            properties = { "auto.offset.reset=earliest" }
    )
    public void onDlt(
            String rawMessage,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(KafkaHeaders.EXCEPTION_FQCN) String exClass,
            @Header(KafkaHeaders.EXCEPTION_MESSAGE) String exMessage
    ) {

        log.warn("[DLT-THRESHOLD] topic={}, partition={}, offset={}, ex={} - {}, payload={}",
                topic, partition, offset, exClass, exMessage, rawMessage);
    }
}
