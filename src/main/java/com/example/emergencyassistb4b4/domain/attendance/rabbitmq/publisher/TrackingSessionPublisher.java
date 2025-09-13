package com.example.emergencyassistb4b4.domain.attendance.rabbitmq.publisher;

import com.example.emergencyassistb4b4.domain.attendance.rabbitmq.dto.MessageWrapper;
import static com.example.emergencyassistb4b4.domain.attendance.rabbitmq.util.RabbitMqUtils.isValidMessage;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrackingSessionPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${spring.rabbitmq.tracking.delayed-exchange}")
    private String delayedExchangeName;

    @Value("${spring.rabbitmq.tracking.delayed-routing-key}")
    private String delayedRoutingKey;

    /**
     * 지연 메시지 전송
     */
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public void publishDelayedTrackingSession(MessageWrapper messageWrapper, long delayMillis) {

        if (!isValidMessage(messageWrapper)) {
            log.warn("발행하려는 메시지가 유효하지 않습니다: {}", messageWrapper);
            return;
        }
        try {
            rabbitTemplate.convertAndSend(delayedExchangeName, delayedRoutingKey, messageWrapper, buildDelayedMessagePostProcessor(delayMillis));

            log.info("Published delayed tracking session: participantCount={}, delay={}ms",
                    messageWrapper.getPayload().getParticipantUserIds().size(), delayMillis);

        } catch (AmqpException e) {
            log.error("Failed to publish delayed tracking session: participantCount={}, delay={}ms",
                    messageWrapper.getPayload().getParticipantUserIds().size(), delayMillis, e);
            throw e;
        }
    }

    private MessagePostProcessor buildDelayedMessagePostProcessor(long delayMillis) {
        return message -> {
            message.getMessageProperties().setHeader("x-delay", delayMillis);
            return message;
        };
    }

}
