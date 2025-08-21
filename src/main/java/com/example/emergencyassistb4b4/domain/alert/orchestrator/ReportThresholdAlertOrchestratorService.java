package com.example.emergencyassistb4b4.domain.alert.orchestrator;

import com.example.emergencyassistb4b4.domain.alert.client.location.LocationClient;
import com.example.emergencyassistb4b4.domain.alert.client.user.UserClient;
import com.example.emergencyassistb4b4.domain.alert.client.userDevice.UserDeviceClient;
import com.example.emergencyassistb4b4.domain.alert.dto.fcm.FcmMessageDto;
import com.example.emergencyassistb4b4.domain.alert.dto.report.ReportThresholdAlertDto;
import com.example.emergencyassistb4b4.domain.alert.fcm.sender.FcmSender;
import com.example.emergencyassistb4b4.domain.alert.service.command.AlertCommandService;

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

    private final AlertCommandService alertCommandService;
    private final LocationClient locationClient;
    private final UserClient userClient;
    private final UserDeviceClient userDeviceClient;
    private final FcmSender fcmSender;

    public void process(String notifyKey) {

        // 1. notifyKey -> ReportThresholdAlertDto
        ReportThresholdAlertDto info = ReportThresholdAlertDto.fromKey(notifyKey);

        // 2. FCM 메시지 내용 작성
        FcmMessageDto message = FcmMessageDto.fromReportThresholdAlert(info);

        // 3. FCM 발송 대상 선정 - 사용자 현 위치를 기준으로 (민간단체는 FCM Topic 구독을 통해 처리)
        List<Long> userIds = locationClient.findUsersByRegion(info.getProvince(), info.getCity());
        // 3-1. 사용자 관심 지역 기준 조회
//        List<Long> userIds = userClient.findUsersByRegion(info.getProvince(), info.getCity());
//        if (userIds == null || userIds.isEmpty()) {
//            // 재난 신고는 사용자 현 위치 기준 -> 지역 내 사용자 없을 경우 시스템 오류로 간주
//            throw new ApiException(ErrorStatus.ALERT_SERVER_ERROR);
//        }

        // 4. FCM Token 조회
        List<String> tokens = userDeviceClient.findFcmTokensByUserIds(userIds);
        if (tokens == null || tokens.isEmpty()) {
           return;
        }

        // 5. FCM 발송
        try {
            fcmSender.sendReportThresholdAlert(message, tokens);
        } catch (Exception e) {
            log.error("누적 알림 발송 실패 - notifyKey={}", notifyKey, e);
            // fcm 전송 실패하더라도 DB에 알림 이력은 저장되도록 함.
        }

        // 6. 알림 저장
        try {
            alertCommandService.saveReportThresholdAlert(info, userIds);
        } catch (Exception e) {
            log.error("알림 이력 저장 실패 - notifyKey={}", notifyKey, e);
            throw e;
        }
    }
}
