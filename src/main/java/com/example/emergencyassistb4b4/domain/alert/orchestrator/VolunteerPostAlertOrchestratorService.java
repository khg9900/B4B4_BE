package com.example.emergencyassistb4b4.domain.alert.orchestrator;

import com.example.emergencyassistb4b4.domain.alert.dto.fcm.FcmMessageDto;
import com.example.emergencyassistb4b4.domain.alert.dto.volunteer.VolunteerPostAlertDto;
import com.example.emergencyassistb4b4.domain.alert.enums.VolunteerAlertSubtype;
import com.example.emergencyassistb4b4.domain.alert.fcm.sender.FcmSender;
import com.example.emergencyassistb4b4.domain.alert.service.command.AlertCommandService;
import com.example.emergencyassistb4b4.domain.userDevice.service.UserDeviceService;
import com.example.emergencyassistb4b4.domain.volunteer.service.VolunteerParticipantService;
import com.example.emergencyassistb4b4.global.kafka.dto.VolunteerEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Slf4j
@Service
@RequiredArgsConstructor
public class VolunteerPostAlertOrchestratorService {

    private final VolunteerParticipantService volunteerParticipantService;
    private final AlertCommandService alertCommandService;
    private final UserDeviceService userDeviceService;
    private final FcmSender fcmSender;

    @Transactional
    public void process(VolunteerEvent event) {

        // 1. VolunteerEvent -> VolunteerAlertDto
        VolunteerPostAlertDto info = VolunteerPostAlertDto.from(event);

        // 2. FCM 메시지 생성
        FcmMessageDto message = null;

        if (info.getSubtype() == VolunteerAlertSubtype.UPDATED) {
            message = FcmMessageDto.fromVolunteerUpdateAlert(info);
        }

        if (info.getSubtype() == VolunteerAlertSubtype.CANCEL) {
            message = FcmMessageDto.fromVolunteerCancelAlert(info);
        }

        // 3. 봉사활동 참여자 조회
        List<Long> participants = volunteerParticipantService.findParticipants(info.getPostId());
        if (participants == null || participants.isEmpty()) {
            // 참여자 없을 경우 메시지 발송 x
            return;
        }

        // 4. FCM token 조회
        List<String> tokens = userDeviceService.findFcmTokensByUserIds(participants);

        // 5. FCM 발송
        try {
            fcmSender.sendVolunteerPostAlert(message, tokens);
        } catch (Exception e) {
            log.error("봉사 게시글 수정/삭제 알림 발송 실패 - postId={}, title={}", info.getPostId(), info.getTitle(), e);
            // fcm 전송 실패하더라도 DB에 알림 이력은 저장되도록 함.
        }

        // 6. 알림 저장
        try {
            alertCommandService.saveVolunteerPostAlert(info, participants);
        } catch (Exception e) {
            log.error("알림 이력 저장 실패 - postId={}, participantCount={}", info.getPostId(), participants.size(), e);
            throw e;
        }
    }
}
