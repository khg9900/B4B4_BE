package exp.kafka;

import exp.domain.ResultRepository;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PublicConsumer {

    private final ResultRepository repo;

    // ✅ 파티션 6개에 맞춰 동시성 6
    @KafkaListener(topics = BroadcastTopic.TOPIC, groupId = "exp.public", concurrency = "6")
    public void onMessage(ConsumerRecord<String, String> rec) {
        String payloadId = rec.value();
        repo.markConsumed(payloadId, System.currentTimeMillis());  // ✅ 단건 업데이트
    }
}
