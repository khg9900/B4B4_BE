package com.example.emergencyassistb4b4.domain.alert.kafka.config.listener;

import com.example.emergencyassistb4b4.domain.alert.kafka.config.consumer.KafkaConsumerConfig;
import com.example.emergencyassistb4b4.domain.alert.kafka.config.error.KafkaErrorHandlerConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

@Configuration
@RequiredArgsConstructor
public class ThresholdAlertListenerConfig {

    private final KafkaConsumerConfig consumerConfig;
    private final KafkaErrorHandlerConfig errorHandlerConfig;

    // ConsumerFactory 설정 (DisasterReportedEvent 처리)
    @Bean
    public ConsumerFactory<String, com.example.emergencyassistb4b4.global.kafka.dto.ThresholdAlertEvent> thresholdConsumerFactory() {

        return new DefaultKafkaConsumerFactory<>(
                consumerConfig.baseConsumerProps(null, com.example.emergencyassistb4b4.global.kafka.dto.ThresholdAlertEvent.class.getName())
                        );
        }

    // 리스너 팩토리 설정
    @Bean(name = "thresholdListenerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, com.example.emergencyassistb4b4.global.kafka.dto.ThresholdAlertEvent> thresholdListenerFactory() {


        var factory = new ConcurrentKafkaListenerContainerFactory<String, com.example.emergencyassistb4b4.global.kafka.dto.ThresholdAlertEvent>();

        factory.setConsumerFactory(thresholdConsumerFactory());
        factory.setConcurrency(3);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        factory.setCommonErrorHandler(errorHandlerConfig.commonErrorHandler());

        return factory;
    }
}
