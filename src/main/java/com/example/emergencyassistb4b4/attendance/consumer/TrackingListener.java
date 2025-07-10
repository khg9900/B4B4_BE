package com.example.emergencyassistb4b4.attendance.consumer;

import com.example.emergencyassistb4b4.attendance.dto.MessageWrapper;
import com.example.emergencyassistb4b4.attendance.dto.SessionState;
import com.example.emergencyassistb4b4.attendance.dto.TrackingSessionDto;
import com.example.emergencyassistb4b4.attendance.dto.IndividualTrackingSessionDto;
import com.example.emergencyassistb4b4.attendance.service.TrackingDataService;
import com.example.emergencyassistb4b4.attendance.socket.handler.TrackingSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrackingListener {

    private final TrackingSocketHandler socketHandler;
    private final TrackingDataService trackingService;

    @RabbitListener(queues = "tracking-delay-queue")
    public void onMessage(MessageWrapper message) {
        if (!isValidMessage(message)) {
            log.warn("잘못된 메시지를 수신했습니다: {}", message);
            return;
        }

        SessionState state = message.getSessionState();
        TrackingSessionDto dto = message.getPayload();
        List<Long> participantIds = dto.getParticipantUserIds();

        switch (state) {
            case READY -> {
                log.info("[READY] 세션 시작 알림 - 시작 시각: {}", dto.getStartTime());
                sendTypedMessageToVolunteers(participantIds, "READY", dto);
            }

            case STARTED -> {
                log.info("[STARTED] 위치 요청");
                sendTypedMessageToVolunteers(participantIds, "STARTED", dto);
            }

            case ENDED -> {
                log.info("[ENDED] 세션 종료 알림");
                sendTypedMessageToVolunteers(participantIds, "ENDED", dto);

                trackingService.saveSessionAttendanceData(participantIds, dto.getTeamId());
                participantIds.forEach(socketHandler::removeVolunteerUserMapping);
            }

            default -> log.warn("알 수 없는 세션 상태 수신: {}", state);
        }
    }

    private boolean isValidMessage(MessageWrapper message) {
        return message != null
                && message.getSessionState() != null
                && message.getPayload() != null
                && message.getPayload().getParticipantUserIds() != null
                && !message.getPayload().getParticipantUserIds().isEmpty();
    }

    /**
     * 각 volunteerId 별로 개별 메시지를 만들고 전송
     */
    private void sendTypedMessageToVolunteers(List<Long> volunteerIds, String type, TrackingSessionDto dto) {
        for (Long volunteerId : volunteerIds) {
            log.info("📨 WebSocket 메시지 전송 - volunteerId={}, type={}", volunteerId, type);

            IndividualTrackingSessionDto individualDto = IndividualTrackingSessionDto.builder()
                    .teamId(dto.getTeamId())
                    .startTime(dto.getStartTime())
                    .endTime(dto.getEndTime())
                    .targetLat(dto.getTargetLat())
                    .targetLng(dto.getTargetLng())
                    .meter(dto.getMeter())
                    .intervalSeconds(dto.getIntervalSeconds())
                    .participantUserId(volunteerId) // 개별 아이디 설정
                    .build();

            socketHandler.sendToUser(volunteerId, type, individualDto);
        }
    }
}