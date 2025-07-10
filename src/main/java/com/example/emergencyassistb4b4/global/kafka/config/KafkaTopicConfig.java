package com.example.emergencyassistb4b4.global.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig { // Kafka 토픽을 코드에서 직접 생성하는 설정 (주로 개발 초기나 테스트 환경에서 유용)

    /**
     * report-reported 토픽 생성
     * - 재난 신고 이벤트가 발행되는 메인 토픽
     * - 3개의 파티션으로 병렬 처리 가능
     * - 복제본 수는 1 (운영 환경에서는 2 이상 권장)
     */
    @Bean
    public NewTopic disasterReported() {
        return TopicBuilder.name("report-reported")
            .partitions(3) // 컨슈머 병렬성을 위한 파티션 수 (토픽이 3개의 파티션으로 분할됨)
            .replicas(1) // 장애 대응을 위한 복제본 수, 운영 환경에서는 최소 replica = 2 이상 권장 (1이면 단일 노드에만 저장됨 >> 각 파티션의 복제본이 없다는 뜻(ex.Partition 0이 브로커 A에만 존재 → A가 죽으면 해당 파티션도 사용 불가))
            .build();
    }

    /**
     * DLQ(Dead Letter Topic)용 토픽 생성
     * - 메시지 소비 중 오류 발생 시 이 토픽으로 전송됨
     * - 즉시알림/누적알림 모두 이 DLQ를 공통 사용
     */
    @Bean
    public NewTopic disasterReportedDLT() {
        return TopicBuilder.name("report-reported-dlt")
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic volunteerPostUpdated() {
        return TopicBuilder.name("volunteer-post-updated")
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic volunteerPostUpdatedDLT() {
        return TopicBuilder.name("volunteer-post-updated-dlt")
            .partitions(3)
            .replicas(1)
            .build();
    }
}