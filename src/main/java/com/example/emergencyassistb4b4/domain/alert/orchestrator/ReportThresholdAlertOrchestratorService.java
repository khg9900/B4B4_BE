package com.example.emergencyassistb4b4.domain.alert.orchestrator;

import com.example.emergencyassistb4b4.domain.alert.dto.fcm.FcmMessageDto;
import com.example.emergencyassistb4b4.domain.alert.dto.report.ReportThresholdAlertDto;
import com.example.emergencyassistb4b4.domain.alert.fcm.sender.FcmSender;
import com.example.emergencyassistb4b4.domain.alert.service.command.AlertCommandService;

import com.example.emergencyassistb4b4.domain.user.service.UserService;
import com.example.emergencyassistb4b4.domain.userDevice.service.UserDeviceService;
import com.example.emergencyassistb4b4.global.exception.ApiException;
import com.example.emergencyassistb4b4.global.status.ErrorStatus;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ReportThresholdAlertOrchestratorService {

    private final UserService userService;
    private final AlertCommandService alertCommandService;
    private final UserDeviceService userDeviceService;
    private final FcmSender fcmSender;

    public void process(String notifyKey) {

        ReportThresholdAlertDto info = ReportThresholdAlertDto.fromKey(notifyKey);

        List<Long> userIds = userService.findUsersByRegion(info.getProvince(), info.getCity());

        if (userIds == null || userIds.isEmpty()) {
            throw new ApiException(ErrorStatus.ALERT_NO_TARGET_USER);
        }

        List<String> tokens = userDeviceService.findFcmTokensByUserIds(userIds);

        if (tokens == null || tokens.isEmpty()) {
           return;
        }

        FcmMessageDto message = FcmMessageDto.fromReportThresholdAlert(info);

        fcmSender.sendReportThresholdAlert(message, tokens);

        try {
            alertCommandService.saveReportThresholdAlert(info, userIds);
        } catch (Exception e) {
            log.error("알림 이력 저장 실패 - notifyKey={}", notifyKey, e);
            throw e;
        }
    }
}
