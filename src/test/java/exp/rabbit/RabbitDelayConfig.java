package exp.rabbit;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitDelayConfig {

    public static final String DLX = "exp.dlx";
    public static final String DELAY_QUEUE = "exp.delay.queue";
    public static final String WORK_QUEUE  = "exp.work.queue";
    public static final String WORK_KEY    = "exp.work";

    @Bean DirectExchange dlx() {

        return new DirectExchange(DLX);
    }

    @Bean Queue delayQueue() {

        return QueueBuilder.durable(DELAY_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", WORK_KEY)
                .build(); // 메시지별 expiration 사용
    }

    @Bean Queue workQueue() {

        return QueueBuilder.durable(WORK_QUEUE).build();
    }

    @Bean Binding bindWork() {

        return BindingBuilder.bind(workQueue()).to(dlx()).with(WORK_KEY);
    }
}
