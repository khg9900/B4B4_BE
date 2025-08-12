package com.example.emergencyassistb4b4.domain.alert.kafka.config.base;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.event.ListenerContainerIdleEvent;
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

    // 자동 설정 된 KafkaTemplate을 주입만 받음 (Bean 생성 X)
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaBaseConfig(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // 각 리스너 설정 클래스에서 사용하는 공통 Consumer 속성 정의
    public Map<String, Object> baseConsumerProps(String groupId, String dtoClassFqcn) {

        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        if (groupId != null && !groupId.isBlank()) {
            props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId); // ← null이면 넣지 않음
        }
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class); // 예외 발생 시 wrapping 해주는 ErrorHandlingDeserializer 사용
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class); // 실제 JSON 역직렬화 담당
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.example.emergencyassistb4b4.global.kafka.dto");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, dtoClassFqcn); // DTO 클래스 정보 설정
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // 우리 코드가 커밋을 통제하게 끔하는 코드(AckMode.RECORD와 궁합)

        return props;
    }

    // Kafka 메시지 처리 중 오류 발생 시 DLT로 전송하는 DefaultErrorHandler 정의
    public DefaultErrorHandler defaultErrorHandler() {

        // 1) 원토픽 -> "<원토픽>-dlt" 로 목적지 매핑
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, ex) -> new TopicPartition(
                        record.topic() + "-dlt", record.partition()
                )
        );

        // 2) 백오프 (1초 간격, 3회)
        FixedBackOff backOff = new FixedBackOff(1000L, 3);

        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);

        // 3) 재시도 무의미 예외는 즉시 DLT
        handler.addNotRetryableExceptions(
                org.springframework.kafka.support.serializer.DeserializationException.class,
                com.fasterxml.jackson.databind.JsonMappingException.class,
                com.fasterxml.jackson.core.JsonParseException.class,
                IllegalArgumentException.class
        );

        // 재시도 중 예외 발생 로그 기록
        handler.setRetryListeners((record, ex, attempt) -> log.error(
                "Kafka 처리 예외: attempt={}, topic={}, partition={}, offset={}, key={}, error={}",
                attempt, record.topic(), record.partition(), record.offset(), record.key(), ex.toString()
        ));

        return handler;
    }

    // 메시지가 안들어오는건지 컨슈머가 죽은 건지 구분하기 위함
    @Bean
    public ApplicationListener<ListenerContainerIdleEvent> kafkaIdleLogger() {
        return event -> {
            var container = event.getListenerId(); // 컨테이너 id
            log.warn("[KAFKA-IDLE] listenerId={}, idleTimeMs={}", container, event.getIdleTime());
        };
    }
}