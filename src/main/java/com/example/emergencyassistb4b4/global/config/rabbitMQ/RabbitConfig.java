// RabbitConfig.java
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

    // 메시지 컨버터
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        ObjectMapper mapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();
        return new Jackson2JsonMessageConverter(mapper);
    }

    // RabbitTemplate (Producer 재시도)
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setRetryTemplate(new RetryTemplateBuilder()
                .maxAttempts(3)
                .fixedBackoff(1000)
                .build());
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }

    // 지연 큐 + Dead Letter 연결
    @Bean("trackingDelayQueue")
    public Queue trackingDelayQueue() {
        return QueueBuilder.durable(RabbitMQConstant.DELAYED_QUEUE_NAME)
                .withArgument("x-dead-letter-exchange", RabbitMQConstant.DEAD_LETTER_EXCHANGE_NAME)
                .withArgument("x-dead-letter-routing-key", RabbitMQConstant.DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    // 지연 익스체인지
    @Bean("trackingDelayExchange")
    public CustomExchange trackingDelayExchange() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "direct");
        return new CustomExchange(RabbitMQConstant.DELAYED_EXCHANGE_NAME, "x-delayed-message", true, false, args);
    }

    // 바인딩
    @Bean
    public Binding trackingDelayBinding() {
        return BindingBuilder.bind(trackingDelayQueue())
                .to(trackingDelayExchange())
                .with(RabbitMQConstant.DELAYED_ROUTING_KEY)
                .noargs();
    }

    // Dead Letter 큐
    @Bean("trackingDeadLetterQueue")
    public Queue trackingDeadLetterQueue() {
        return QueueBuilder.durable(RabbitMQConstant.DEAD_LETTER_QUEUE_NAME).build();
    }

    @Bean("trackingDeadLetterExchange")
    public Exchange trackingDeadLetterExchange() {
        return ExchangeBuilder.topicExchange(RabbitMQConstant.DEAD_LETTER_EXCHANGE_NAME).durable(true).build();
    }

    @Bean
    public Binding trackingDeadLetterBinding() {
        return BindingBuilder.bind(trackingDeadLetterQueue())
                .to(trackingDeadLetterExchange())
                .with(RabbitMQConstant.DEAD_LETTER_ROUTING_KEY)
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
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter messageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL); // 수동 Ack/Nack
        factory.setPrefetchCount(10);
        factory.setAdviceChain(RetryInterceptorBuilder.stateless()
                .maxAttempts(3)
                .backOffOptions(1000, 2.0, 10000)
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build());
        return factory;
    }
}
