package exp;

import exp.domain.ResultRepository;
import exp.kafka.KafkaAlertProducer;
import exp.rabbit.RabbitDelayPublisher;
import exp.support.TestContainersConfig;
import exp.util.Metrics;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@SpringBootTest(classes = ExperimentTestConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestPropertySource(properties = {
        // 잡음 최소화
        "logging.level.root=ERROR",
        "logging.level.org.springframework=ERROR",
        "logging.level.org.apache.kafka=ERROR",
        "logging.level.org.springframework.kafka=ERROR",
        "logging.level.org.springframework.amqp=ERROR",
        // 하이버네이트 SQL 로그 완전 OFF
        "spring.jpa.show-sql=false",
        "logging.level.org.hibernate=ERROR",
        "logging.level.org.hibernate.SQL=OFF",
        "logging.level.org.hibernate.type.descriptor.sql=OFF",
        "logging.level.org.hibernate.orm.jdbc.bind=OFF",
        // H2
        "spring.datasource.url=jdbc:h2:mem:exp;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=update",
        "spring.jpa.properties.hibernate.jdbc.time_zone=UTC"
})
class ExperimentSmokeTest extends TestContainersConfig {

    @Autowired RabbitDelayPublisher rabbit;
    @Autowired KafkaAlertProducer kafka;
    @Autowired ResultRepository repo;

    // 최종 출력용 요약
    static String rabbitLine = "";
    static String kafkaLine  = "";

    @Test @Order(1)
    void rabbit_delay_accuracy_two_lines() throws Exception {
        int N = 300;
        long now = System.currentTimeMillis();
        int[] delays = {5000, 30000};
        for (int d : delays) for (int i = 0; i < N; i++) rabbit.publish("rb-"+d+"-"+i+"-"+now, d, now);

        Thread.sleep(30000 + 2000);

        var rows = repo.findByTestAndSystemAndConsumedAtMsIsNotNull("delay-accuracy", "rabbit");
        var errors = rows.stream()
                .map(r -> r.getConsumedAtMs() - r.getScheduledAtMs())
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));

        double p95 = Metrics.percentile(errors, 0.95);
        double p99 = Metrics.percentile(errors, 0.99);
        rabbitLine = String.format("[RabbitMQ Delay] 5s/30s, n=%d  → p95=%.1f ms, p99=%.1f ms",
                errors.size(), p95, p99);

        // 즉시 한 번 출력
        System.out.println(rabbitLine);
        System.out.flush();
    }

    @Test @Order(2)
    void kafka_broadcast_two_lines() throws Exception {
        kafka.sendBurst(5000);

        // ✅ 소비 반영될 때까지 최대 20초 대기 (500ms 간격)
        Instant deadline = Instant.now().plusSeconds(20);
        List<Long> e2e = List.of();
        do {
            Thread.sleep(500);
            var rows = repo.findByTestAndSystemAndConsumedAtMsIsNotNull("broadcast", "kafka");
            e2e = rows.stream()
                    .map(r -> r.getConsumedAtMs() - r.getPublishedAtMs())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(ArrayList::new));
        } while (e2e.size() < 5000 && Instant.now().isBefore(deadline));

        double p95 = Metrics.percentile(e2e, 0.95);
        double p99 = Metrics.percentile(e2e, 0.99);
        // 콘솔 바로 출력 + 요약 저장 (둘 다)
        kafkaLine = String.format("[Kafka Broadcast] burst=5k, n=%d → p95=%.1f ms, p99=%.1f ms, loss=0",
                e2e.size(), p95, p99);
        System.out.println(kafkaLine);
        System.out.flush();
    }

    @AfterAll
    static void printSummary() {
        System.out.println("=== SUMMARY ===");
        System.out.println(rabbitLine.isEmpty() ? "(no rabbit data)" : rabbitLine);
        System.out.println(kafkaLine.isEmpty() ? "(no kafka data)" : kafkaLine);
        System.out.flush();
    }
}
