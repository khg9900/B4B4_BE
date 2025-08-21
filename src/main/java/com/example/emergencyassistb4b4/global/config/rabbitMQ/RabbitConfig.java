package com.example.emergencyassistb4b4.global.config.rabbitMQ;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.retry.support.RetryTemplateBuilder;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitConfig {

    // 지연 익스체인지, 큐, 라우팅키
    public static final String DELAYED_EXCHANGE_NAME = "tracking.delay.exchange";
    public static final String DELAYED_QUEUE_NAME = "tracking-delay-queue";
    public static final String DELAYED_ROUTING_KEY = "tracking.delay.routingkey";

    // 메시지 컨버터
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        ObjectMapper mapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();
        mapper.activateDefaultTyping(
                mapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL
        );
        return new Jackson2JsonMessageConverter(mapper);
    }

    // RabbitTemplate (Producer 재시도)
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setRetryTemplate(new RetryTemplateBuilder()
                .maxAttempts(3)
                .fixedBackoff(1000)
                .build());
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }

    // 지연 큐
    @Bean
    public Queue trackingDelayQueue() {
        return new Queue(DELAYED_QUEUE_NAME, true);
    }

    // 지연 익스체인지
    @Bean
    public CustomExchange trackingDelayExchange() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "direct");
        return new CustomExchange(DELAYED_EXCHANGE_NAME, "x-delayed-message", true, false, args);
    }

    // 바인딩
    @Bean
    public Binding trackingDelayBinding() {
        return BindingBuilder.bind(trackingDelayQueue())
                .to(trackingDelayExchange())
                .with(DELAYED_ROUTING_KEY)
                .noargs();
    }

    // 데드레터 큐
    @Bean
    public Queue trackingDeadLetterQueue() {
        return QueueBuilder.durable("tracking-dead-letter-queue").build();
    }

    @Bean
    public Exchange trackingDeadLetterExchange() {
        return ExchangeBuilder.topicExchange("tracking-dlx").durable(true).build();
    }

    @Bean
    public Binding trackingDeadLetterBinding() {
        return BindingBuilder.bind(trackingDeadLetterQueue())
                .to(trackingDeadLetterExchange())
                .with("tracking.session.dead")
                .noargs();
    }

    // RabbitAdmin
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public ApplicationRunner rabbitAdminInitializer(RabbitAdmin rabbitAdmin) {
        return args -> rabbitAdmin.initialize();
    }

    // Listener Container Factory (Consumer Ack/Nack + 재시도)
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL); // 수동 Ack/Nack
        factory.setPrefetchCount(10); // 한 번에 처리할 메시지 수
        factory.setAdviceChain(RetryInterceptorBuilder.stateless()
                .maxAttempts(3)
                .backOffOptions(1000, 2.0, 10000) // 초기 1초, multiplier 2, max 10초
                .recoverer(new RejectAndDontRequeueRecoverer()) // 재시도 후 실패 시 처리
                .build());
        return factory;
    }
}
