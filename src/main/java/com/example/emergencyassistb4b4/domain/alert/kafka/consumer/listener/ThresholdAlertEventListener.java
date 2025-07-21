package com.example.emergencyassistb4b4.domain.alert.kafka.consumer.listener;

import com.example.emergencyassistb4b4.domain.alert.service.trigger.ReportThresholdAlertTriggerService;
import com.example.emergencyassistb4b4.global.kafka.dto.DisasterReportedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class ThresholdAlertEventListener { // 누적 기준(예: 같은 장소에서 일정 횟수 이상 신고 시) 경보 발생을 처리하는 Kafka 리스너

    // 실제 임계치(누적 기준) 기반 알림 로직을 담당하는 서비스
    private final ReportThresholdAlertTriggerService triggerService;

    /**
     * Kafka 토픽 "report-reported" 구독
     * - 리스너 컨테이너 팩토리는 thresholdListenerFactory 사용
     * - 메시지는 DisasterReportedEvent 객체로 역직렬화됨
     */
    @KafkaListener(
        topics = "report-reported",
        containerFactory = "thresholdListenerFactory"
    )
    public void onDisasterReported(DisasterReportedEvent event) {

        try {
            // 수신된 메시지 기반으로 누적 기준 검사 및 알림 트리거
            triggerService.checkReportThreshold(event);
        } catch (Exception e) {
            // 처리 중 예외 발생 시 로그 출력 및 예외 재전파 -> DefaultErrorHandler에 의해 retry 또는 DLQ 전송됨
            log.error("[누적 알림 처리 실패] province={}, city={}, type={}, time={}",
                event.getProvince(), event.getCity(), event.getDisasterType(), event.getReportedAt(), e);
            throw e; // DLQ로 이동하기 위해 예외를 반드시 throw 해야 함
        }
    }
}
