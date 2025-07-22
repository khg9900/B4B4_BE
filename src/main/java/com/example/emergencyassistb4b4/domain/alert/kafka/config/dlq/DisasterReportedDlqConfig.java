package com.example.emergencyassistb4b4.domain.alert.kafka.config.dlq;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;


@Configuration
public class DisasterReportedDlqConfig { // DLT(Dead Letter Topic) 전용 리스너 설정

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // DLT 메시지를 소비할 전용 리스너 팩토리 빈 등록
    @Bean(name = "disasterReportedDltListenerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, String> disasterReportedDltListenerFactory() {

        Map<String, Object> props = new HashMap<>();

        // Kafka 클러스터 주소 설정
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // DLT용 고유한 Consumer group 설정
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "alert-dlt-generic-group");

        // 메시지 key/value 디시리얼라이저 설정 (DLT는 일반적으로 String 타입)
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        var factory = new ConcurrentKafkaListenerContainerFactory<String, String>();

        // DLT용 Consumer Factory 설정
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(props));

        return factory;
    }
}