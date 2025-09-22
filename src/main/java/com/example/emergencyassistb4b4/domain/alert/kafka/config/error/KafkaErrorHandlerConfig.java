package com.example.emergencyassistb4b4.domain.alert.kafka.config.error;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.util.backoff.FixedBackOff;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class KafkaErrorHandlerConfig {

    @Value("${spring.kafka.topic.immediate}")
    private String immediateTopic;

    @Value("${spring.kafka.topic.dlt.immediate}")
    private String immediateDltTopic;

    @Value("${spring.kafka.topic.threshold}")
    private String thresholdTopic;

    @Value("${spring.kafka.topic.dlt.threshold}")
    private String thresholdDltTopic;

    @Value("${spring.kafka.topic.volunteer}")
    private String volunteerTopic;

    @Value("${spring.kafka.topic.dlt.volunteer}")
    private String volunteerDltTopic;

    @Value("${spring.kafka.topic.volunteerCancel}")
    private String volunteerCancelTopic;

    @Value("${spring.kafka.topic.dlt.volunteerCancel}")
    private String volunteerCancelDltTopic;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Bean
    public CommonErrorHandler commonErrorHandler() {

        Map<String, String> bizDltMap = new java.util.HashMap<>();
        bizDltMap.put(immediateTopic, immediateDltTopic);
        bizDltMap.put(thresholdTopic, thresholdDltTopic);
        bizDltMap.put(volunteerTopic, volunteerDltTopic);
        bizDltMap.put(volunteerCancelTopic, volunteerCancelDltTopic);

        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, ex) -> {
                    // 역직렬화 실패를 별도 DLT로 보냄
                    if (ex instanceof DeserializationException) {
                        String deserDlt = bizDltMap.getOrDefault(record.topic(), record.topic() + "-dlt") + "-deser";
                        return new TopicPartition(deserDlt, record.partition());
                    }
                    // 일반 비즈니스 예외
                    String dest = bizDltMap.getOrDefault(record.topic(), record.topic() + "-dlt");
                    return new TopicPartition(dest, record.partition());
                }
        );

        FixedBackOff backoff = new FixedBackOff(1000L, 3);

        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backoff);
        handler.addNotRetryableExceptions(
                DeserializationException.class,
                com.fasterxml.jackson.databind.JsonMappingException.class,
                com.fasterxml.jackson.core.JsonParseException.class,
                IllegalArgumentException.class
        );
        handler.setRetryListeners((record, ex, attempt) -> log.error(
                "Kafka 처리 예외: attempt={}, topic={}, partition={}, offset={}, key={}, error={}",
                attempt, record.topic(), record.partition(), record.offset(), record.key(), ex.toString()
        ));

        return handler;
    }
}
