package com.example.emergencyassistb4b4.domain.alert.kafka.config.listener;

import com.example.emergencyassistb4b4.domain.alert.kafka.config.base.KafkaBaseConfig;
import com.example.emergencyassistb4b4.global.kafka.dto.DisasterReportedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

@Configuration
@RequiredArgsConstructor
public class ImmediateAlertListenerConfig { // 즉시 알림 처리용 Kafka 리스너 설정

    private final KafkaBaseConfig kafkaBaseConfig; // 공통 Kafka 설정을 분리한 Config

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
        factory.getContainerProperties().setIdleEventInterval(30000L); // 30초간 수신 없으면 이벤트 발생
        factory.setCommonErrorHandler(kafkaBaseConfig.defaultErrorHandler()); // 여기서 DLT/재시도 정책 적용

        return factory;
    }
}