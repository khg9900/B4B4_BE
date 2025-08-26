package com.example.emergencyassistb4b4.domain.alert.kafka.streams;

import com.example.emergencyassistb4b4.global.kafka.dto.DisasterReportedEvent;
import com.example.emergencyassistb4b4.global.kafka.dto.ThresholdAlertEvent;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.support.serializer.JsonSerde;


@Configuration
@EnableKafkaStreams
public class ThresholdDetectionTopology {

    @Value("${spring.kafka.topic.immediate}")
    private String immediateTopic;

    @Value("${spring.kafka.topic.threshold}")
    private String thresholdTopic;

    @Bean
    public KStream<String, DisasterReportedEvent> thresholdStream(StreamsBuilder builder) {

        Serde<String> stringSerde = Serdes.String();
        JsonSerde<DisasterReportedEvent> drSerde  = new JsonSerde<>(DisasterReportedEvent.class);
        JsonSerde<ThresholdAlertEvent> alertSerde = new JsonSerde<>(ThresholdAlertEvent.class);

        drSerde.deserializer().addTrustedPackages("com.example.emergencyassistb4b4.global.kafka.dto");
        alertSerde.deserializer().addTrustedPackages("com.example.emergencyassistb4b4.global.kafka.dto");

        KStream<String, DisasterReportedEvent> source =
                builder.stream(immediateTopic, Consumed.with(stringSerde, drSerde));

        source.map((k, v) -> {
            String groupKey = v.getProvince() + ":" + v.getCity() + ":" + v.getDisasterType();
            long reportedAtEpoch =
                    v.getReportedAt().atZone(ZoneId.of("Asia/Seoul")).toInstant().toEpochMilli();

            ThresholdAlertEvent evt = new ThresholdAlertEvent(
                    v.getProvince(),
                    v.getCity(),
                    v.getDisasterType(),   // alertType 슬롯에 재난타입 유지
                    reportedAtEpoch       // windowStart=신고 시각 → 트리거에서 '날짜'로 사용
            );

            return KeyValue.pair(groupKey, evt);
        })
                .to(thresholdTopic, Produced.with(stringSerde, alertSerde));

        return source;
    }
}
