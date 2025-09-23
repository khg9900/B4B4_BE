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

        ReportImmediateAlertDto info = ReportImmediateAlertDto.fromEvent(event);

        String token = userDeviceService.findFcmTokenByUserId(info.getGovernmentId());

        FcmMessageDto message = FcmMessageDto.fromReportImmediateAlert(info);

        fcmSender.sendReportImmediateAlert(message, token);

    }
}