package com.example.emergencyassistb4b4.domain.alert.kafka.config.listener;

import com.example.emergencyassistb4b4.domain.alert.kafka.config.consumer.KafkaConsumerConfig;
import com.example.emergencyassistb4b4.domain.alert.kafka.config.error.KafkaErrorHandlerConfig;
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
public class ImmediateAlertListenerConfig {

    private final KafkaConsumerConfig consumerConfig;
    private final KafkaErrorHandlerConfig errorHandlerConfig;

    // DisasterReportedEvent 객체를 처리할 ConsumerFactory 설정
    @Bean
    public ConsumerFactory<String, DisasterReportedEvent> immediateConsumerFactory() {

        return new DefaultKafkaConsumerFactory<>(
                consumerConfig.baseConsumerProps(null, DisasterReportedEvent.class.getName())
        );
    }

    // Kafka 리스너 컨테이너 팩토리 정의
    @Bean(name = "immediateListenerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, DisasterReportedEvent> immediateListenerFactory() {

        var factory = new ConcurrentKafkaListenerContainerFactory<String, DisasterReportedEvent>();

        factory.setConsumerFactory(immediateConsumerFactory());
        factory.setConcurrency(3);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        factory.getContainerProperties().setIdleEventInterval(30000L);
        factory.setCommonErrorHandler(errorHandlerConfig.commonErrorHandler());

        return factory;
    }
}