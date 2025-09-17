package com.example.emergencyassistb4b4.domain.alert.kafka.config.listener;

import com.example.emergencyassistb4b4.domain.alert.kafka.config.consumer.KafkaConsumerConfig;
import com.example.emergencyassistb4b4.domain.alert.kafka.config.error.KafkaErrorHandlerConfig;
import com.example.emergencyassistb4b4.global.kafka.dto.VolunteerCancelEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

@Configuration
@RequiredArgsConstructor
public class VolunteerCanceledAlertListenerConfig {

    private final KafkaConsumerConfig consumerConfig;
    private final KafkaErrorHandlerConfig errorHandlerConfig;

    @Bean
    public ConsumerFactory<String, VolunteerCancelEvent> VolunteerCancelConsumerFactory() {

        return new DefaultKafkaConsumerFactory<>(
                consumerConfig.baseConsumerProps(null, VolunteerCancelEvent.class.getName())
        );
    }

    @Bean(name = "volunteerCanceledListenerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, VolunteerCancelEvent> volunteerUpdateListenerFactory() {

        var factory = new ConcurrentKafkaListenerContainerFactory<String, VolunteerCancelEvent>();

        factory.setConsumerFactory(VolunteerCancelConsumerFactory());
        factory.setConcurrency(3);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        factory.setCommonErrorHandler(errorHandlerConfig.commonErrorHandler());

        return factory;
    }
}
