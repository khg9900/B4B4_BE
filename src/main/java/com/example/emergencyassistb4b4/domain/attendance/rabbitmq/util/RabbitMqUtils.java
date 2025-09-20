package com.example.emergencyassistb4b4.domain.attendance.rabbitmq.util;

import com.example.emergencyassistb4b4.domain.attendance.rabbitmq.dto.MessageWrapper;

public class RabbitMqUtils {

    // 메시지 유효성 체크
    public static boolean isValidMessage(MessageWrapper message) {
        return message != null
                && message.getSessionState() != null
                && message.getPayload() != null
                && message.getPayload().getParticipantUserIds() != null
                && !message.getPayload().getParticipantUserIds().isEmpty();
    }
}
