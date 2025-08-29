package com.example.emergencyassistb4b4.domain.alert.orchestrator;

import com.example.emergencyassistb4b4.domain.alert.dto.report.ReportImmediateAlertDto;
import com.example.emergencyassistb4b4.domain.alert.dto.fcm.FcmMessageDto;
import com.example.emergencyassistb4b4.domain.alert.fcm.sender.FcmSender;
import com.example.emergencyassistb4b4.domain.userDevice.service.UserDeviceService;
import com.example.emergencyassistb4b4.global.exception.ApiException;
import com.example.emergencyassistb4b4.global.kafka.dto.DisasterReportedEvent;
import com.example.emergencyassistb4b4.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportImmediateAlertOrchestratorService {

    private final FcmSender fcmSender;
    private final UserDeviceService userDeviceService;

    public void process(DisasterReportedEvent event) {

        // 1. KafkaEvent -> ReportImmediateAlertDto
        ReportImmediateAlertDto info = ReportImmediateAlertDto.fromEvent(event);

        // 2. FCM 메시지 내용 작성
        FcmMessageDto message = FcmMessageDto.fromReportImmediateAlert(info);

        // 3. FCM Token 조회
        String token = userDeviceService.findFcmTokenByUserId(info.getGovernmentId());

        if (token == null || token.isBlank()) {
            // 즉시 알림은 발송 대상(관할 공공기관)이 반드시 있어야 함.
            throw new ApiException(ErrorStatus.ALERT_SERVER_ERROR);
        }

        try {
            fcmSender.sendReportImmediateAlert(message, token);
        } catch (Exception e) {
            log.error("즉시 알림 발송 실패 - governmentId()={}, token={}", info.getGovernmentId(), token, e);
            throw e;
        }
    }
}