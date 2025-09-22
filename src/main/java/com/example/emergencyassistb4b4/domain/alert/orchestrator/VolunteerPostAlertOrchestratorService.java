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

        VolunteerPostAlertDto info = VolunteerPostAlertDto.from(event);

        List<Long> participants = volunteerParticipantService.findParticipants(info.getPostId());
        if (participants == null || participants.isEmpty()) {
            return;
        }

        List<String> tokens = userDeviceService.findFcmTokensByUserIds(participants);

        FcmMessageDto message = null;

        switch (info.getSubtype()) {
            case UPDATED -> message = FcmMessageDto.fromVolunteerUpdateAlert(info);
            case CANCELED -> message = FcmMessageDto.fromVolunteerCancelAlert(info);
        }

        fcmSender.sendVolunteerPostAlert(message, tokens);

        try {
            alertCommandService.saveVolunteerPostAlert(info, participants);
        } catch (Exception e) {
            log.error("알림 이력 저장 실패 - postId={}, participantCount={}", info.getPostId(), participants.size(), e);
            throw e;
        }
    }
}
