package com.example.emergencyassistb4b4.domain.alert.kafka.config.listener;

import com.example.emergencyassistb4b4.domain.alert.kafka.config.consumer.KafkaConsumerConfig;
import com.example.emergencyassistb4b4.domain.alert.kafka.config.error.KafkaErrorHandlerConfig;
import com.example.emergencyassistb4b4.global.kafka.dto.VolunteerUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

@Configuration
@RequiredArgsConstructor
public class VolunteerUpdatedAlertListenerConfig {

    private final KafkaConsumerConfig consumerConfig;
    private final KafkaErrorHandlerConfig errorHandlerConfig;

    @Bean
    public ConsumerFactory<String, VolunteerUpdatedEvent> VolunteerUpdateConsumerFactory() {

        return new DefaultKafkaConsumerFactory<>(
            consumerConfig.baseConsumerProps(null, VolunteerUpdatedEvent.class.getName())
        );
    }

    @Bean(name = "volunteerUpdatedListenerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, VolunteerUpdatedEvent> volunteerUpdateListenerFactory() {

        var factory = new ConcurrentKafkaListenerContainerFactory<String, VolunteerUpdatedEvent>();

        factory.setConsumerFactory(VolunteerUpdateConsumerFactory());
        factory.setConcurrency(3);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        factory.setCommonErrorHandler(errorHandlerConfig.commonErrorHandler());

        return factory;
    }
}
