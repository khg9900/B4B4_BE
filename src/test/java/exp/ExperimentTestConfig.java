package exp;


import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootConfiguration
@EnableAutoConfiguration // DataSource, JPA, Kafka, Rabbit 자동설정
@EntityScan("exp.domain") // 테스트 전용 엔티티 스캔
@EnableJpaRepositories("exp.domain")
@ComponentScan({"exp.rabbit","exp.kafka"}) // 퍼블리셔/컨슈머 스캔
public class ExperimentTestConfig {

    // Kafka 테스트 토픽(파티션 6개 생성)
    @Bean
    NewTopic expTopic() {

        return new NewTopic("exp.disaster.alerts", 6, (short) 1);
    }

    // RabbitMQ 리스너 팩토리(동시성/프리패치 올리기)
    @Bean(name = "expRabbitFactory")
    SimpleRabbitListenerContainerFactory expRabbitFactory(ConnectionFactory cf) {

        var f = new SimpleRabbitListenerContainerFactory();

        f.setConnectionFactory(cf);
        f.setConcurrentConsumers(4);
        f.setMaxConcurrentConsumers(8);
        f.setPrefetchCount(200);

        return f;
    }
}
