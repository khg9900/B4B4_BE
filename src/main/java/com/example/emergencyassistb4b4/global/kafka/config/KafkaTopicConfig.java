package com.example.emergencyassistb4b4.global.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@Profile("dev") // 운영은 IaC로 관리
public class KafkaTopicConfig {

    @Value("${spring.kafka.topic.immediate}")
    private String immediateTopic;

    @Value("${spring.kafka.topic.dlt.immediate}")
    private String immediateDltTopic;

    @Value("${spring.kafka.topic.threshold}")
    private String thresholdTopic;

    @Value("${spring.kafka.topic.dlt.threshold}")
    private String thresholdDltTopic;

    @Value("${spring.kafka.topic.volunteer}")
    private String volunteerTopic;

    @Value("${spring.kafka.topic.dlt.volunteer}")
    private String volunteerDltTopic;

    @Value("${kafka.topic.partitions:3}")
    private int partitions;

    @Value("${kafka.topic.replicas:1}")
    private int replicas;

    // 재난 신고 메인 토픽
    @Bean
    public NewTopic disasterReported() {

        return TopicBuilder.name(immediateTopic)
            .partitions(partitions) // 컨슈머 병렬성을 위한 파티션 수
            .replicas(replicas) // 운영 환경에서는 최소 replica = 2 이상 권장
            .build();
    }

    // 재난 신고 DLQ
    @Bean
    public NewTopic disasterReportedDLT() {

        return TopicBuilder.name(immediateDltTopic)
            .partitions(partitions)
            .replicas(replicas)
            .build();
    }

    // 임계치 토픽
    @Bean
    public NewTopic reportThreshold() {

        return TopicBuilder.name(thresholdTopic).partitions(partitions).replicas(replicas).build();
    }

    // 임계치 DLQ
    @Bean
    public NewTopic reportThresholdDLT() {

        return TopicBuilder.name(thresholdDltTopic).partitions(partitions).replicas(replicas).build();
    }

   // 봉사글 토픽
    @Bean
    public NewTopic volunteerPostUpdated() {

        return TopicBuilder.name(volunteerTopic).partitions(partitions).replicas(replicas).build();
    }

    // 봉사글 DLQ
    @Bean
    public NewTopic volunteerPostUpdatedDLT() {

        return TopicBuilder.name(volunteerDltTopic).partitions(partitions).replicas(replicas).build();
    }

    // 역직렬화 실패용 DLQ
    @Bean
    public NewTopic disasterReportedDLTDeser() {

        return TopicBuilder.name(immediateDltTopic + "-deser")
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }

    @Bean
    public NewTopic reportThresholdDLTDeser() {

        return TopicBuilder.name(thresholdDltTopic + "-deser").partitions(partitions).replicas(replicas).build();
    }

    @Bean
    public NewTopic volunteerPostUpdatedDLTDeser() {

        return TopicBuilder.name(volunteerDltTopic + "-deser").partitions(partitions).replicas(replicas).build();
    }
}