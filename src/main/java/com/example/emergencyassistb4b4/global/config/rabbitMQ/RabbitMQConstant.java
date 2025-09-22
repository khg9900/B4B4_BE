package com.example.emergencyassistb4b4.global.config.rabbitMQ;

public class RabbitMQConstant {
    // 지연 익스체인지, 큐, 라우팅키
    public static final String DELAYED_EXCHANGE_NAME = "tracking.delay.exchange";
    public static final String DELAYED_QUEUE_NAME = "tracking-delay-queue";
    public static final String DELAYED_ROUTING_KEY = "tracking.delay.routingkey";

    // 데드레터 큐
    public static final String DEAD_LETTER_QUEUE_NAME = "tracking-dead-letter-queue";
    public static final String DEAD_LETTER_EXCHANGE_NAME = "tracking-dlx";
    public static final String DEAD_LETTER_ROUTING_KEY = "tracking.session.dead";
}
