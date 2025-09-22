package com.example.emergencyassistb4b4.domain.alert.kafka.config.consumer;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // DTO 역직렬화용 (정상 리스너)
    public Map<String, Object> baseConsumerProps(String groupId, String dtoClassFqcn) {

        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        if (groupId != null && !groupId.isBlank()) {
            props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        }
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.example.emergencyassistb4b4.global.kafka.dto");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, dtoClassFqcn);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        return props;
    }

    // 문자열 수신용 (DLT 리스너)
    public Map<String, Object> dltConsumerProps(String groupId) {

        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        if (groupId != null && !groupId.isBlank()) {
            props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        }
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        return props;
    }
}
