package exp.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="exp_results", indexes = {
        @Index(name="idx_test_system", columnList = "test,system"),
        @Index(name="idx_payload", columnList = "payloadId", unique = true)
})
public class Result {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
    String test;        // "delay-accuracy" or "broadcast"

    String system;      // "rabbit" or "kafka"

    Long scheduledAtMs; // 목표 시각

    Long publishedAtMs; // 보낸 시각

    Long consumedAtMs;  // 소비 시각

    String payloadId;
}
