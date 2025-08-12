package com.example.emergencyassistb4b4.domain.alert.kafka.consumer.dlt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ImmediateAlertDltListener {

    @KafkaListener(
            topics = "report-reported-dlt",
            groupId = "alert-dlt-immediate-group",
            containerFactory = "disasterReportedDltListenerFactory", // 기존 DLT용 팩토리 이름 사용
            properties = {
                    "auto.offset.reset=earliest" // 테스트 시 과거 메시지도 읽기
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
        // TODO: rawMessage 파싱 + 위 메타데이터까지 DB 저장
        log.warn("[DLT-IMMEDIATE] topic={}, partition={}, offset={}, ex={} - {}, payload={}",
                topic, partition, offset, exClass, exMessage, rawMessage);
    }
}