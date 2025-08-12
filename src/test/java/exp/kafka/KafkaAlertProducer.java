package exp.kafka;


import exp.domain.Result;
import exp.domain.ResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaAlertProducer {
    private final KafkaTemplate<String, String> template;
    private final ResultRepository repo;

    public void sendBurst(int n) {
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            String payloadId = "ka-" + n + "-" + i + "-" + start;
            long published = System.currentTimeMillis();
            repo.save(Result.builder()
                    .test("broadcast").system("kafka").payloadId(payloadId)
                    .scheduledAtMs(start).publishedAtMs(published).build());
            template.send(BroadcastTopic.TOPIC, String.valueOf(i), payloadId);
        }
    }
}
