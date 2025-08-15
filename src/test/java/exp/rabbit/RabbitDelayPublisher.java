package exp.rabbit;

import exp.domain.Result;
import exp.domain.ResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class RabbitDelayPublisher {

    private final RabbitTemplate template;
    private final ResultRepository repo;

    public void publish(String payloadId, long delayMs, long now) {

        long scheduledAt = now + delayMs;

        repo.save(Result.builder()
                .test("delay-accuracy").system("rabbit").payloadId(payloadId)
                .scheduledAtMs(scheduledAt).publishedAtMs(System.currentTimeMillis()).build());

        String body = payloadId + "|" + scheduledAt;

        Message msg = MessageBuilder.withBody(body.getBytes(StandardCharsets.UTF_8))
                .setExpiration(String.valueOf(delayMs))
                .build();

        template.send("", RabbitDelayConfig.DELAY_QUEUE, msg);
    }
}
