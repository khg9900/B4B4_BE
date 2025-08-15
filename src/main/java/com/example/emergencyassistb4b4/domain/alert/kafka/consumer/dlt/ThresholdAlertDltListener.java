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
            topics = "report-reported-dlt",
            groupId = "alert-dlt-threshold-group",
            containerFactory = "disasterReportedDltListenerFactory",
            properties = {
                    "auto.offset.reset=earliest"
            }
    )
    public void onDlt(
            String rawMessage,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(KafkaHeaders.EXCEPTION_FQCN) String exClass,
            @Header(KafkaHeaders.EXCEPTION_MESSAGE) String exMessage
    ) {

        // TODO: 누적/임계 로직용 저장 또는 별도 후처리
        log.warn("[DLT-THRESHOLD] topic={}, partition={}, offset={}, ex={} - {}, payload={}",
                topic, partition, offset, exClass, exMessage, rawMessage);
    }
}
