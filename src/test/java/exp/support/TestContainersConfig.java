package exp.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

public abstract class TestContainersConfig {
    static final KafkaContainer KAFKA = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));
    static final RabbitMQContainer RABBIT = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.13-management"));

    static {
        KAFKA.start();
        RABBIT.start();
    }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
        r.add("spring.kafka.producer.acks", () -> "all");
        r.add("spring.kafka.producer.properties.enable.idempotence", () -> "true");

        r.add("spring.rabbitmq.host", RABBIT::getHost);
        r.add("spring.rabbitmq.port", () -> RABBIT.getAmqpPort());

        // 테스트 DB (H2 인메모리)
        r.add("spring.datasource.url", () -> "jdbc:h2:mem:exp;DB_CLOSE_DELAY=-1");
        r.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        r.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        r.add("spring.jpa.properties.hibernate.jdbc.time_zone", () -> "UTC");
    }
}