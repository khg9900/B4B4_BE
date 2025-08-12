//package com.example.emergencyassistb4b4;
//
//import com.example.emergencyassistb4b4.global.kafka.dto.DisasterReportedEvent;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.kafka.test.context.EmbeddedKafka;
//import org.springframework.test.context.ActiveProfiles;
//
//import java.time.LocalDateTime;
//
//@ActiveProfiles("test")
//@SpringBootTest(properties = "spring.profiles.active=test")
//@EmbeddedKafka(partitions = 1, topics = {"disaster-alert"}, brokerProperties = {"listeners=PLAINTEXT://localhost:0"}) // 이걸 톨해 임시 Kafka Broker에 실제 메시지 전송됨
//class DisasterAlertKafkaTest { // Kafka Producer -> Kafka Broker (Kafka Producer 단위 테스트)
//
//    @Autowired
//    private KafkaTemplate<String, String> kafkaTemplate;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    /*
//    KafkaTemplate이 Kafka 토픽(disaster-alert)으로 메시지를 잘 발행하는지만 확인하는 테스트
//     */
//    @Test
//    void testKafkaSend() throws Exception {
//
//        // 테스트용 메시지 생성 (DisasterAlertMessage 객체 수동 생성)
//        DisasterReportedEvent message = DisasterReportedEvent.builder()
//                .reportId(123L)
//                .disasterType("EARTHQUAKE")
//                .province("서울특별시")
//                .city("강남구")
//                .reporterId(1L)
//                .reportedAt(LocalDateTime.now())
//                .build();
//
//        // JSON 직렬화 (ObjectMapper로 Kafka 메시지용 JSON 문자열로 변환)
//        String json = objectMapper.writeValueAsString(message);
//
//        // Kafka 메시지 발행 (KafkaTemplate.send() 호출로 Kafka 토픽(disaster-alert)에 메시지 발행)
//        kafkaTemplate.send("disaster-alert", json);
//
//        // 결과 확인용 (일단은 수동 확인 or Consumer에서 로그 확인 가능)
//        System.out.println("✅ Kafka 테스트 메시지 발행 완료: " + json);
//
//        // Optional → Consumer 쪽 테스트는 ConsumerRecordListener 활용 가능 (고급 예시 가능)
//    }
//}
//
//
