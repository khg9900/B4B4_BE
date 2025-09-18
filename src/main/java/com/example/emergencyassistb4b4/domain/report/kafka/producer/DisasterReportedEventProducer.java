package com.example.emergencyassistb4b4.domain.report.kafka.producer;

import com.example.emergencyassistb4b4.global.kafka.dto.DisasterReportedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class DisasterReportedEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topic.immediate}")
    private String topic;

    // 재난 신고 이벤트를 Kafka로 비동기 발행하는 메서드 (성공 시 콜백 실행, 실패 시 예외 로그 출력)
    public void sendDisasterReportedEvent(DisasterReportedEvent event) {

        // ex. "Seoul:Mapo" 형태
        String key = event.getProvince() + ":" + event.getCity();

        kafkaTemplate.send(topic, key, event)
                .thenAccept(result -> {
                    var meta = result.getRecordMetadata();
                    log.debug("kafka publish OK topic={}, partition={}, offset={}, key={}, event={}",
                            meta.topic(), meta.partition(), meta.offset(), key, event);
                })
                .exceptionally(ex -> {
                    log.error("kafka publish FAIL key={}, event={}", key, event, ex);

                    return null;
                });
    }
}
