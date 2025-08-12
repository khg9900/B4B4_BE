//package com.example.emergencyassistb4b4;
//
//import com.example.emergencyassistb4b4.domain.alert.kafka.consumer.listener.ImmediateAlertEventListener;
//import com.example.emergencyassistb4b4.domain.alert.kafka.repository.KafkaDltLogRepository;
//import com.example.emergencyassistb4b4.global.kafka.dto.DisasterReportedEvent;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.awaitility.Awaitility;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.kafka.test.context.EmbeddedKafka;
//import org.springframework.test.annotation.DirtiesContext;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
//
//import java.time.Duration;
//import java.time.LocalDateTime;
//
//import static org.mockito.ArgumentMatchers.refEq;
//import static org.mockito.Mockito.timeout;
//import static org.mockito.Mockito.verify;
//
//@ActiveProfiles("test") // 테스트용 application-test.yml 설정 사용
//@SpringBootTest // 통합 테스트로 전체 Spring Context 로딩
//@DirtiesContext // 테스트마다 Context 초기화
//@EmbeddedKafka(partitions = 1, topics = {"disaster-alert",
//    "disaster-alert.DLT"}, brokerProperties = {"listeners=PLAINTEXT://localhost:0"})
//// 임베디드 Kafka 브로커 설정
//public class KafkaFcmIntegrationTest { // Kafka to FCM 전체 연동 테스트 (수신, FCM, 실패 처리까지)
//
//    @Autowired
//    private KafkaTemplate<String, String> kafkaTemplate; // Kafka에 메시지 발행할 producer
//
//    @Autowired
//    private ObjectMapper objectMapper; // 객체를 JSON 문자열로 변환
//
//    @MockitoSpyBean
//    private ImmediateAlertEventListener consumer; // Kafka 메시지 Consumer -> 실제 사용 + 호출 감시
//
//    @MockitoSpyBean
//    private FcmFailureService fcmFailureService; // FCM 발송 서비스 -> 실제 사용 + 호출 감시
//
//    @Autowired
//    private KafkaDltLogRepository kafkaDlqLogRepository; // DLQ 로그 저장소
//
//    /*
//    테스트 1: Kafka 메시지가 정상적으로 Consumer에게 도달하는지 검증하는 테스트
//    (메시지를 발행하고, Consumer의 consumeDisasterAlert() 메서드가 호출되는지를 확인)
//     */
//    @Test
//    void testKafkaConsumerReceivesMessage() throws Exception {
//        // given: 테스트용 메시지 준비
//        DisasterReportedEvent event = DisasterReportedEvent.builder()
//            .reportId(1L)
//            .reporterId(100L)
//            .governmentId(999L)
//            .disasterType("홍수")
//            .province("서울특별시")
//            .city("종로구")
//            .reportedAt(LocalDateTime.now())
//            .build();
//
//        String json = objectMapper.writeValueAsString(event);
//
//        // when: Kafka에 메시지 발행
//        kafkaTemplate.send("report-reported", json);
//
//        // then - consumer의 consumeDisasterAlert()가 호출되었는지 검증
//        Thread.sleep(2000); // 메시지가 소비될 시간 잠시 기다림
//
//        verify(consumer, timeout(3000)).consumeDisasterAlert(json); // 실제 호출되는 public 메서드여야 감시 가능
//    }
//
//    /*
//    테스트 2: Kafka 메시지를 수신한 수, FCM 발송 서비스가 호출되는지 검증하는 테스트
//    (Kafka 수신 -> FcmFailureService.sendAlert() 실행 흐름을 검증)
//     */
//    @Test
//    void testKafkaConsumerAndFcmServiceIntegration() throws Exception {
//        // given: 메시지 생성
//        DisasterReportedEvent message = DisasterReportedEvent.builder()
//            .reportId(2L)
//            .reporterId(200L)
//            .governmentId(9999L)
//            .disasterType("지진")
//            .province("서울특별시")
//            .city("강남구")
//            .reportedAt(LocalDateTime.now())
//            .build();
//
//        String json = objectMapper.writeValueAsString(message);
//
//        // when: Kafka에 메시지 발행
//        kafkaTemplate.send("disaster-alert", json);
//
//        // then: FCM 발송 메서드가 정확한 파라미터로 호출되었는지 검증
//        verify(fcmFailureService, timeout(3000)).sendAlert(
//            refEq(message)); // refEq()는 동일한 필드값을 가진 객체면 통과시킴
//    }
//
//    /*
//    테스트 3: FCM 발송 실패 후 DLQ에 전송되며, 실패 기록이 DB에 저장되는지 검증하는 테스트
//    (1. 강제로 발송 실패하도록 설정된 메시지 발송
//     2. 재시도 3회 후 실패 -> DLT 이동
//     3. DLT Consumer가 메시지 처리 후 DB에 AlertFailureLog 저장)
//     */
//    @Test
//    void testDLQAfterFailure() throws Exception {
//        // given: 실패 유도용 메시지 생성 (force-fail: true 환경에서만 실패)
//        DisasterReportedEvent message = DisasterReportedEvent.builder()
//            .reportId(3L)
//            .reporterId(1L)
//            .governmentId(9999L)
//            .disasterType("TSUNAMI")
//            .province("부산광역시")
//            .city("해운대구")
//            .reportedAt(LocalDateTime.now())
//            .build();
//
//        String json = objectMapper.writeValueAsString(message);
//
//        // when: Kafka에 메시지 발행
//        kafkaTemplate.send("report-reported", json);
//
//        // then: Awaitility로 DLQ 처리까지 기다리며 실패 로그 저장 여부 확인
//        Awaitility.await()
//            .atMost(Duration.ofSeconds(10)) // 최대 10초 대기
//            .pollInterval(Duration.ofMillis(500)) // 0.5초마다 확인
//            .until(() -> kafkaDlqLogRepository.existsByReportId(3L)); // DB에 저장되었는지 확인
//    }
//}
