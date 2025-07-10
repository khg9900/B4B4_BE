package com.example.emergencyassistb4b4.alert.kafka.domain;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Table(name = "kafka_fail_log")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator( // 시퀀스 기반 자동 증가 ID 생성 전략 설정
    name = "kafka_fail_log_seq_gen",
    sequenceName = "kafka_fail_log_seq", // DB 시퀀스 이름
    allocationSize = 50 // 50개씩 미리 할당해 성능 최적화
)
public class KafkaDlqLog { // Kafka DLQ 메시지의 실패 내역 저장용 엔티티

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "kafka_fail_log_seq_gen" // 위에서 정의한 시퀀스 사용
    )
    private Long id;

    private String topic; // 실패 메시지가 발생한 Kafka 토픽

    private String consumerGroup; // 실패를 발생시킨 consumer group ID

    private String payload; // 원본 Kafka 메시지 (JSON 문자열 등)

    @Column(length = 1000)
    private String reason; // 실패 사유 (역직렬화 실패, 비즈니스 로직 예외 등)

    private String listener; // 실패를 발생시킨 리스너 이름 (메서드명 포함)

    private String exception; // 실제 발생한 예외 클래스 및 메시지

    private LocalDateTime failedAt; // 실패 발생 시각

}
