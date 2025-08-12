package exp.rabbit;


import exp.domain.ResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class RabbitWorkConsumer {

    private final ResultRepository repo;

    // ✅ 동시성/프리패치 적용: containerFactory 지정
    @RabbitListener(
            queues = RabbitDelayConfig.WORK_QUEUE,
            ackMode = "AUTO",
            containerFactory = "expRabbitFactory"
    )
    public void onMessage(byte[] body) {
        String s = new String(body, StandardCharsets.UTF_8);
        String payloadId = s.substring(0, s.indexOf('|'));

        // ✅ 단건 업데이트로 성능/지연 개선
        repo.markConsumed(payloadId, System.currentTimeMillis());
    }
}
