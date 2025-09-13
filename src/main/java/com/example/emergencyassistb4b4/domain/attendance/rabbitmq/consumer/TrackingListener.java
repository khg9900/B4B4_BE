package com.example.emergencyassistb4b4.domain.attendance.rabbitmq.consumer;

import com.example.emergencyassistb4b4.domain.attendance.rabbitmq.dto.MessageWrapper;
import com.example.emergencyassistb4b4.domain.attendance.rabbitmq.dto.SessionState;
import com.example.emergencyassistb4b4.domain.attendance.rabbitmq.dto.TrackingSessionDto;
import com.example.emergencyassistb4b4.domain.attendance.rabbitmq.dto.IndividualTrackingSessionDto;
import com.example.emergencyassistb4b4.domain.attendance.rabbitmq.service.TrackingDataService;
import com.example.emergencyassistb4b4.domain.attendance.socket.handler.TrackingSocketHandler;

import static com.example.emergencyassistb4b4.domain.attendance.rabbitmq.dto.IndividualTrackingSessionDto.buildIndividualDto;
import static com.example.emergencyassistb4b4.domain.attendance.rabbitmq.util.RabbitMqUtils.isValidMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import com.rabbitmq.client.Channel;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrackingListener {

    private final TrackingSocketHandler socketHandler;
    private final TrackingDataService trackingService;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "tracking-delay-queue",
            containerFactory = "rabbitListenerContainerFactory")
    public void onMessage(MessageWrapper message, Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {

        try {
            String json = objectMapper.writeValueAsString(message);
            log.debug("Received raw message from queue: {}", json);

            if (!isValidMessage(message)) {
                log.warn("잘못된 메시지를 수신했습니다: {}", message);
                channel.basicAck(tag, false); // 잘못된 메시지라도 Ack 처리
                return;
            }

            SessionState state = message.getSessionState();
            TrackingSessionDto dto = message.getPayload();
            List<Long> participantIds = dto.getParticipantUserIds();

            switch (state) {
                case READY, STARTED -> sendTypedMessageToVolunteers(participantIds, state, dto);
                case ENDED -> {
                    sendTypedMessageToVolunteers(participantIds, state, dto);
                    trackingService.saveSessionAttendanceData(participantIds, dto.getTeamId());
                    participantIds.forEach(socketHandler::removeVolunteerUserMapping);
                }
                default -> log.warn("알 수 없는 세션 상태 수신: {}", state);
            }

            // 성공 처리 시 Ack
            channel.basicAck(tag, false);

        } catch (Exception e) {
            log.error("메시지 처리 실패, 재시도 예정: {}", message, e);
            // 실패 시 Nack + requeue=true → 재시도
            channel.basicNack(tag, false, true);
        }
    }

    private void sendTypedMessageToVolunteers(List<Long> volunteerIds, SessionState state, TrackingSessionDto dto) {
        for (Long volunteerId : volunteerIds) {
            socketHandler.sendToUser(volunteerId, state.name(), buildIndividualDto(dto, volunteerId));
        }
    }

}
