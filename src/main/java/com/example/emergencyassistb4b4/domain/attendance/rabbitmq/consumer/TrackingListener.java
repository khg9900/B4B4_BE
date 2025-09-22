package com.example.emergencyassistb4b4.domain.attendance.rabbitmq.consumer;

import com.example.emergencyassistb4b4.domain.attendance.rabbitmq.dto.MessageWrapper;
import com.example.emergencyassistb4b4.domain.attendance.rabbitmq.dto.SessionState;
import com.example.emergencyassistb4b4.domain.attendance.rabbitmq.dto.TrackingSessionDto;
import com.example.emergencyassistb4b4.domain.attendance.rabbitmq.service.TrackingDataService;
import com.example.emergencyassistb4b4.domain.attendance.redis.RabbitMQRedisService;
import com.example.emergencyassistb4b4.domain.attendance.socket.handler.TrackingSocketHandler;

import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.example.emergencyassistb4b4.domain.attendance.rabbitmq.dto.IndividualTrackingSessionDto.buildIndividualDto;
import static com.example.emergencyassistb4b4.domain.attendance.rabbitmq.util.RabbitMqUtils.isValidMessage;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrackingListener {

    private final TrackingSocketHandler socketHandler;
    private final TrackingDataService trackingService;
    private final RabbitMQRedisService rabbitMQRedisService;
    private final ScheduledExecutorService scheduler;

    // 메시지 수신 및 처리
    @RabbitListener(queues = "tracking-delay-queue", containerFactory = "rabbitListenerContainerFactory")
    public void onMessage(MessageWrapper message, Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            if (!isValidMessage(message)) {
                channel.basicAck(tag, false);
                return;
            }

            processMessage(message);
            channel.basicAck(tag, false);

        } catch (Exception e) {
            log.error("메시지 처리 실패, 재시도 예정: {}", message, e);
            channel.basicNack(tag, false, true);
        }
    }

    // 메시지 상태에 따라 WebSocket 전송 및 종료 후 처리
    private void processMessage(MessageWrapper message) {
        SessionState state = message.getSessionState();
        TrackingSessionDto dto = message.getPayload();
        List<Long> participantIds = dto.getParticipantUserIds();

        switch (state) {
            case READY, STARTED -> sendToVolunteers(participantIds, state, dto);
            case ENDED -> {
                sendToVolunteers(participantIds, state, dto);
                scheduler.schedule(() -> endSessionProcessing(participantIds, dto.getTeamId()), 1, TimeUnit.MINUTES); // 종료 후 처리 예약
            }
            default -> log.warn("알 수 없는 세션 상태 수신: {}", state);
        }
    }

    // 상태 기반 WebSocket 메시지 전송
    private void sendToVolunteers(List<Long> volunteerIds, SessionState state, TrackingSessionDto dto) {
        for (Long volunteerId : volunteerIds) {
            socketHandler.sendToUser(volunteerId, state.name(), buildIndividualDto(dto, volunteerId));
        }
    }

    // 세션 종료 후 출석 저장 및 Redis 매핑 해제
    private void endSessionProcessing(List<Long> participantIds, Long teamId) {
        trackingService.saveSessionAttendanceData(participantIds, teamId);
        participantIds.forEach(rabbitMQRedisService::unmapVolunteerFromUser);
    }
}
