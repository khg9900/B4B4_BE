package com.example.emergencyassistb4b4.domain.report.kafka.producer;

import com.example.emergencyassistb4b4.global.kafka.dto.DisasterReportedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class DisasterReportedEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    // 메시지를 보낼 Kafka 토픽 이름(하드코딩되어 있음, 운영 시에는 application.yml에서 주입받는 방식 권장)
    private static final String TOPIC = "report-reported";

    /**
     * 재난 신고 발생 이벤트를 Kafka로 발행하는 메서드
     * - KafkaTemplate.send()는 비동기로 작동하며, 반환값은 CompletableFuture 기반의 ListenableFuture
     * - thenAccept로 성공 콜백 처리
     * - exceptionally로 실패 콜백 처리
     */
    public void sendDisasterReportedEvent(DisasterReportedEvent event) {

        // 예시 키: "Seoul:Mapo" 형태 (도메인에 맞춰 결정)
        String key = event.getProvince() + ":" + event.getCity();

        kafkaTemplate.send(TOPIC, key, event)
                .thenAccept(result -> {
                    var meta = result.getRecordMetadata();
                    log.info("kafka publish OK topic={}, partition={}, offset={}, key={}, event={}",
                            meta.topic(), meta.partition(), meta.offset(), key, event);
                }) // 메시지 전송 성공 시 로그 출력
                .exceptionally(ex -> { // 메시지 전송 실패 시 예외 및 이벤트 로그 출력
                    log.error("kafka publish FAIL key={}, event={}", key, event, ex);

                    return null;
                });
    }
}
