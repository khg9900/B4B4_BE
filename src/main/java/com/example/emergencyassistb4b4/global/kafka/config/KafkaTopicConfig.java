package com.example.emergencyassistb4b4.global.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@Profile("dev") // 운영은 IaC로 관리
public class KafkaTopicConfig { // Kafka 토픽을 코드에서 직접 생성하는 설정 (주로 개발 초기나 테스트 환경에서 유용)

    @Value("${spring.kafka.topic.immediate}")
    private String immediateTopic;

    @Value("${spring.kafka.topic.dlt.immediate}")
    private String immediateDltTopic;

    @Value("${spring.kafka.topic.threshold}")
    private String thresholdTopic;

    @Value("${spring.kafka.topic.dlt.threshold}")
    private String thresholdDltTopic;

    @Value("${kafka.topic.partitions:3}")
    private int partitions;

    @Value("${kafka.topic.replicas:1}")
    private int replicas;

    /**
     * report-reported 토픽 생성
     * - 재난 신고 이벤트가 발행되는 메인 토픽
     * - 3개의 파티션으로 병렬 처리 가능
     * - 복제본 수는 1 (운영 환경에서는 2 이상 권장)
     */
    @Bean
    public NewTopic disasterReported() {

        return TopicBuilder.name(immediateTopic)
            .partitions(partitions) // 컨슈머 병렬성을 위한 파티션 수 (토픽이 3개의 파티션으로 분할됨)
            .replicas(replicas) // 장애 대응을 위한 복제본 수, 운영 환경에서는 최소 replica = 2 이상 권장 (1이면 단일 노드에만 저장됨 >> 각 파티션의 복제본이 없다는 뜻(ex.Partition 0이 브로커 A에만 존재 → A가 죽으면 해당 파티션도 사용 불가))
            .build();
    }

    /**
     * DLQ(Dead Letter Topic)용 토픽 생성
     * - 메시지 소비 중 오류 발생 시 이 토픽으로 전송됨
     * - 즉시알림/누적알림 모두 이 DLQ를 공통 사용
     */
    @Bean
    public NewTopic disasterReportedDLT() {

        return TopicBuilder.name(immediateDltTopic)
            .partitions(partitions)
            .replicas(replicas)
            .build();
    }

    @Bean
    public NewTopic volunteerPostUpdated() {

        return TopicBuilder.name(thresholdTopic)
            .partitions(partitions)
            .replicas(replicas)
            .build();
    }

    @Bean
    public NewTopic volunteerPostUpdatedDLT() {

        return TopicBuilder.name(thresholdDltTopic)
            .partitions(partitions)
            .replicas(replicas)
            .build();
    }

    // 역직렬화 실패용 DLT(-deser)
    @Bean
    public NewTopic disasterReportedDLTDeser() {

        return TopicBuilder.name(immediateDltTopic + "-deser")
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }

    // 역직렬화 실패용 DLT(-deser)
    @Bean
    public NewTopic volunteerPostUpdatedDLTDeser() {

        return TopicBuilder.name(thresholdDltTopic + "-deser")
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }
}