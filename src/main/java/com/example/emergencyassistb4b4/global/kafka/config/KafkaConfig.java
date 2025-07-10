package com.example.emergencyassistb4b4.global.kafka.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Profile("!test") // 운영 환경에서만 활성화
public class KafkaConfig { // Kafka 메시지 발행자(Producer)를 위한 설정

    /**
     * Kafka ProducerFactory를 설정함
     * - KafkaTemplate에서 내부적으로 사용되며, 실제 메시지를 Kafka broker로 전송하는 역할
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory() {

        Map<String, Object> config = new HashMap<>();

        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"); // Kafka 브로커 주소 (운영 시 외부 설정으로 빼는 것이 일반적)
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class); // 메시지 Key 직렬화 방식: 문자열
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class); // 메시지 Value 직렬화 방식: JSON (스프링 Kafka에서 제공하는 JsonSerializer)

        return new DefaultKafkaProducerFactory<>(config);
    }

    /**
     * Kafka 메시지 발행을 위한 KafkaTemplate 빈 등록
     * - 외부 서비스나 이벤트 퍼블리셔가 이를 통해 메시지를 보냄
     * - Generic type은 <String, Object>: 다양한 이벤트 클래스 직렬화 가능
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
