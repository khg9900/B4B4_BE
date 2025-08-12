package com.example.emergencyassistb4b4.domain.alert.kafka.config.listener;

import com.example.emergencyassistb4b4.domain.alert.kafka.config.base.KafkaBaseConfig;
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
public class ThresholdAlertListenerConfig { // 임계값 초과 이벤트(누적 발생 수 기준) 처리용 Kafka 리스너 설정

    private final KafkaBaseConfig kafkaBaseConfig;

    // ConsumerFactory 설정 (DisasterReportedEvent 처리)
    @Bean
    public ConsumerFactory<String, DisasterReportedEvent> thresholdConsumerFactory() {

        return new DefaultKafkaConsumerFactory<>(
            kafkaBaseConfig.baseConsumerProps("alert-threshold-group",
                DisasterReportedEvent.class.getName())
        );
    }

    // 리스너 팩토리 설정
    @Bean(name = "thresholdListenerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, DisasterReportedEvent> thresholdListenerFactory() {

        var factory = new ConcurrentKafkaListenerContainerFactory<String, DisasterReportedEvent>();

        factory.setConsumerFactory(thresholdConsumerFactory());
        factory.setConcurrency(3); // 병렬 처리용 Consumer 개서
        factory.getContainerProperties()
            .setAckMode(ContainerProperties.AckMode.RECORD); // 레코드 단위로 커밋
        factory.setCommonErrorHandler(kafkaBaseConfig.defaultErrorHandler()); // 자동설정 템플릿을 쓰는 에러핸들러

        return factory;
    }
}