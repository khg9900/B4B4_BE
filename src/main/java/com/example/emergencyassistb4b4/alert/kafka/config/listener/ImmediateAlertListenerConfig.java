package com.example.emergencyassistb4b4.alert.kafka.config.listener;

import com.example.emergencyassistb4b4.alert.kafka.config.base.KafkaBaseConfig;
import com.example.emergencyassistb4b4.global.kafka.dto.DisasterReportedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;

@Configuration
@RequiredArgsConstructor
public class ImmediateAlertListenerConfig { // 즉시 알림 처리용 Kafka 리스너 설정

    private final KafkaBaseConfig kafkaBaseConfig; // 공통 Kafka 설정을 분리한 Config
    private final KafkaTemplate<String, Object> kafkaTemplate; // 오류 발생 시 DLT로 메시지 전송에 사용

    // DisasterReportedEvent 객체를 처리할 ConsumerFactory 설정
    @Bean
    public ConsumerFactory<String, DisasterReportedEvent> immediateConsumerFactory() {

        return new DefaultKafkaConsumerFactory<>(
            // 그룹 ID와 DTO 클래스 전달
            kafkaBaseConfig.baseConsumerProps("alert-immediate-group",
                DisasterReportedEvent.class.getName())
        );
    }

    // Kafka 리스너 컨테이너 팩토리 정의
    @Bean(name = "immediateListenerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, DisasterReportedEvent> immediateListenerFactory() {

        var factory = new ConcurrentKafkaListenerContainerFactory<String, DisasterReportedEvent>();

        factory.setConsumerFactory(immediateConsumerFactory());
        factory.setConcurrency(3); // 동시에 최대 3개의 consumer 스레드로 처리
        factory.getContainerProperties()
            .setAckMode(ContainerProperties.AckMode.RECORD); // 메시지 단위 수동 커밋
        factory.setCommonErrorHandler(
            kafkaBaseConfig.defaultErrorHandler(kafkaTemplate)); // 공통 에러 핸들러 등록

        return factory;
    }
}