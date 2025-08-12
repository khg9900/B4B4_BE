package com.example.emergencyassistb4b4.domain.alert.kafka.config.listener;

import com.example.emergencyassistb4b4.domain.alert.kafka.config.base.KafkaBaseConfig;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class DltListenerConfig {

    private final KafkaBaseConfig base;

    @Bean(name = "disasterReportedDltListenerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, String> disasterReportedDltListenerFactory() {

        // groupId는 @KafkaListener 쪽에서 설정
        Map<String, Object> props = new HashMap<>(base.baseConsumerProps(null, String.class.getName()));
        // baseConsumerProps(null, ...) 호출 시 GROUP_ID 미설정이 되도록 KafkaBaseConfig를 수정(아래 변경 3 참고)

        var cf = new DefaultKafkaConsumerFactory<String, String>(props,
                new StringDeserializer(), // key
                new StringDeserializer()  // Value → DLQ/DLT는 그냥 String으로 받음
        );


        var factory = new ConcurrentKafkaListenerContainerFactory<String, String>();

        factory.setConsumerFactory(cf);
        factory.setConcurrency(3);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD); // ✅ 명시
        factory.setCommonErrorHandler(base.defaultErrorHandler()); // ✅ DLT 매핑/백오프/Not-retryable 적용

        return factory;
    }
}

