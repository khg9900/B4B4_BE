package com.example.emergencyassistb4b4.alert.kafka.config.base;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@EnableKafka
public class KafkaBaseConfig { // Kafka 공통 설정 클래스 (Consumer 설정 및 ErrorHandler 공용 정의)

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // ProducerFactory로부터 KafkaTemplate을 생성 (DLT 전송용)
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(
        ProducerFactory<String, Object> producerFactory
    ) {
        return new KafkaTemplate<>(producerFactory);
    }

    // 각 리스너 설정 클래스에서 사용하는 공통 Consumer 속성 정의
    public Map<String, Object> baseConsumerProps(String groupId, String dtoClassFqcn) {

        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        // 예외 발생 시 wrapping 해주는 ErrorHandlingDeserializer 사용
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class); // 실제 JSON 역직렬화 담당
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.example.emergencyassistb4b4.global.kafka.dto");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, dtoClassFqcn); // DTO 클래스 정보 설정

        return props;
    }

    // Kafka 메시지 처리 중 오류 발생 시 DLT로 전송하는 DefaultErrorHandler 정의
    public DefaultErrorHandler defaultErrorHandler(KafkaTemplate<String, Object> kafkaTemplate) {

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
            // 실패 메시지를 DLT로 보내기 위한 Recoverer
            new DeadLetterPublishingRecoverer(kafkaTemplate),
            new FixedBackOff(1000L, 3) // 1초 간격으로 최대 3번 재시도 후 DLT 전송
        );
        errorHandler.setRetryListeners((record, ex, attempt) -> {
            // 재시도 중 예외 발생 로그 기록
            log.error("Kafka 처리 중 예외 발생: attempt={}, key={}, value={}, error={}",
                attempt,
                record.key(),
                record.value(),
                ex.getMessage(),
                ex);
        });

        return errorHandler;
    }
}