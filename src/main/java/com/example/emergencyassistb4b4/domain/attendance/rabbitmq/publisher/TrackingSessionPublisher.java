package com.example.emergencyassistb4b4.domain.attendance.rabbitmq.publisher;

import com.example.emergencyassistb4b4.domain.attendance.rabbitmq.dto.MessageWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrackingSessionPublisher {

    private final RabbitTemplate rabbitTemplate;

    private static final String DELAYED_EXCHANGE_NAME = "tracking.delay.exchange";
    private static final String DELAYED_ROUTING_KEY = "tracking.delay.routingkey";

    /**
     * 지연 메시지 전송
     */
    public void publishDelayedTrackingSession(MessageWrapper messageWrapper, long delayMillis) {
        try {
            MessagePostProcessor messagePostProcessor = message -> {
                message.getMessageProperties().setHeader("x-delay", delayMillis);
                return message;
            };

            rabbitTemplate.convertAndSend(DELAYED_EXCHANGE_NAME, DELAYED_ROUTING_KEY, messageWrapper, messagePostProcessor);

            log.info("Published delayed tracking session: participants={}, delay={}ms",
                    messageWrapper.getPayload().getParticipantUserIds(), delayMillis);

        } catch (AmqpException e) {
            log.error("Failed to publish delayed tracking session: participants={}, delay={}ms",
                    messageWrapper.getPayload().getParticipantUserIds(),delayMillis);
        }
    }

}
