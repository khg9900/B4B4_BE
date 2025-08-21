package com.example.emergencyassistb4b4.global.config.rabbitMQ;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
        // JavaTimeModule 등록 → LocalDateTime 직렬화/역직렬화 지원
        ObjectMapper mapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();
        mapper.activateDefaultTyping(
                mapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL
        );
        return new Jackson2JsonMessageConverter(mapper);
    }

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

    // 메인 큐

    // 지연 큐 (delayed message 용)
    @Bean
    public Queue trackingDelayQueue() {
        return new Queue(DELAYED_QUEUE_NAME, true);
    }


    // 지연 익스체인지 (x-delayed-message)
    @Bean
    public CustomExchange trackingDelayExchange() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "direct");
        return new CustomExchange(DELAYED_EXCHANGE_NAME, "x-delayed-message", true, false, args);
    }


    // 바인딩 - 지연 큐 <-> 지연 익스체인지
    @Bean
    public Binding trackingDelayBinding() {
        return BindingBuilder.bind(trackingDelayQueue())
                .to(trackingDelayExchange())
                .with(DELAYED_ROUTING_KEY)
                .noargs();
    }

    // 데드레터 큐 및 익스체인지 (선택적, 기존 선언 유지)
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

    // RabbitAdmin 초기화
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public ApplicationRunner rabbitAdminInitializer(RabbitAdmin rabbitAdmin) {
        return args -> rabbitAdmin.initialize();
    }
}
