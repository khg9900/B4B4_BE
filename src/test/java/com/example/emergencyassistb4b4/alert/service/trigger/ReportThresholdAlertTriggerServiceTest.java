//package com.example.emergencyassistb4b4.alert.service.trigger;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.contains;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.never;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//import com.example.emergencyassistb4b4.domain.alert.redis.RedisThresholdCounter;
//import com.example.emergencyassistb4b4.domain.alert.orchestrator.ReportThresholdAlertOrchestratorService;
//import com.example.emergencyassistb4b4.domain.alert.service.trigger.ReportThresholdAlertTriggerService;
//import com.example.emergencyassistb4b4.global.kafka.dto.DisasterReportedEvent;
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.atomic.AtomicBoolean;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.invocation.InvocationOnMock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.mockito.stubbing.Answer;
//
//@ExtendWith(MockitoExtension.class)
//class ReportThresholdAlertTriggerServiceTest {
//
//    @InjectMocks
//    private ReportThresholdAlertTriggerService triggerService;
//
//    @Mock
//    private RedisThresholdCounter redisThresholdCounter;
//
//    @Mock
//    private ReportThresholdAlertOrchestratorService reportAlertOrchestratorService;
//
//    private final DisasterReportedEvent event = DisasterReportedEvent.builder()
//        .province("서울")
//        .city("강남구")
//        .disasterType("FLOOD")
//        .reportedAt(LocalDateTime.of(2025, 7, 3, 0, 0))
//        .build();
//
//    private static final List<Long> THRESHOLDS = List.of(3L, 5L, 7L, 10L);
//
//    @Test
//    void 임계치에_도달하면_알림이_전송되어야_한다() {
//        // given
//        when(redisThresholdCounter.incrementAndCheckThreshold(
//            any(), any(), any(), eq(THRESHOLDS)
//        )).thenReturn(3L);
//
//        // when
//        triggerService.checkReportThreshold(event);
//
//        // then
//        verify(reportAlertOrchestratorService, times(1)).process(contains("alert:3:"));
//    }
//
//    @Test
//    void 임계치에_도달하지_않으면_알림이_전송되지_않아야_한다() {
//        // given
//        when(redisThresholdCounter.incrementAndCheckThreshold(
//            any(), any(), any(), eq(THRESHOLDS)
//        )).thenReturn(-1L);
//
//        // when
//        triggerService.checkReportThreshold(event);
//
//        // then
//        verify(reportAlertOrchestratorService, never()).process(any());
//    }
//
//    @Test
//    void null_반환시에도_알림이_전송되지_않아야_한다() {
//        // given
//        when(redisThresholdCounter.incrementAndCheckThreshold(
//            any(), any(), any(), eq(THRESHOLDS)
//        )).thenReturn(null);
//
//        // when
//        triggerService.checkReportThreshold(event);
//
//        // then
//        verify(reportAlertOrchestratorService, never()).process(any());
//    }
//
//    @Test
//    void 동시성_상황에서도_임계치_알림은_딱_한번만_전송되어야_한다() throws Exception {
//        // given
//        int threadCount = 20;
//        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
//        CountDownLatch latch = new CountDownLatch(threadCount);
//
//        String counterKey = "report:서울:강남구:FLOOD:2025-07-03";
//        String notifyKeyPrefix = "alert:" + counterKey;
//
//        when(redisThresholdCounter.incrementAndCheckThreshold(
//            eq(counterKey), eq(notifyKeyPrefix), any(), eq(THRESHOLDS)
//        )).thenAnswer(new Answer<>() {
//            private final AtomicBoolean once = new AtomicBoolean(true);
//            @Override
//            public Long answer(InvocationOnMock invocation) {
//                return once.getAndSet(false) ? 3L : -1L;
//            }
//        });
//
//        // when
//        for (int i = 0; i < threadCount; i++) {
//            executorService.submit(() -> {
//                try {
//                    triggerService.checkReportThreshold(buildDummyEvent());
//                } finally {
//                    latch.countDown();
//                }
//            });
//        }
//
//        latch.await();
//
//        // then
//        verify(reportAlertOrchestratorService, times(1)).process(contains("alert:3:"));
//    }
//
//    private DisasterReportedEvent buildDummyEvent() {
//        return DisasterReportedEvent.builder()
//            .province("서울")
//            .city("강남구")
//            .disasterType("FLOOD")
//            .reportedAt(LocalDateTime.of(2025, 7, 3, 0, 0))
//            .build();
//    }
//}
