package com.example.emergencyassistb4b4.domain.alert.fcm.sender;

import com.example.emergencyassistb4b4.domain.alert.dto.fcm.FcmMessageDto;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.WebpushConfig;
import com.google.firebase.messaging.WebpushFcmOptions;
import com.google.firebase.messaging.WebpushNotification;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmSender {

    private final FirebaseMessaging fcm;

    // FCM Multicast 최대 발송 건수
    private static final int FCM_MAX_TOKEN_BATCH_SIZE = 500;

    /* ---------- Android ----------*/
    private Notification buildNotification(FcmMessageDto dto) {
        return Notification.builder()
            .setTitle(dto.getTitle())
            .setBody(dto.getBody())
            .build();
    }

    private AndroidNotification buildAndroidNotification() {
        return AndroidNotification.builder()
            .setChannelId("default")
            .build();
    }

    private AndroidConfig buildAndroidConfig() {
        return AndroidConfig.builder()
            .setNotification(buildAndroidNotification())
            .build();
    }

    /* ---------- Web ----------*/
    private WebpushConfig buildWebpushConfig(FcmMessageDto dto) {

        return WebpushConfig.builder()
            .setNotification(
                WebpushNotification.builder()
                .setTitle(dto.getTitle())
                .setBody(dto.getBody())
                .build()
            )
            .build();
    }

    private List<List<String>> partition(List<String> tokens) {
        List<List<String>> result = new ArrayList<>();
        for (int i = 0; i < tokens.size(); i += FCM_MAX_TOKEN_BATCH_SIZE) {
            result.add(tokens.subList(i, Math.min(i + FCM_MAX_TOKEN_BATCH_SIZE, tokens.size())));
        }
        return result;
    }

    // 재난 즉시 알림
    public void sendReportImmediateAlert(FcmMessageDto dto, String token) {

        // 단일 기관
        Message message = Message.builder()
            .setToken(token)
            .setWebpushConfig(buildWebpushConfig(dto))
            .build();

        try {
            fcm.sendAsync(message);
        } catch (Exception e) {
            log.error("공공기관 즉시 알림 전송 실패", e);
        }
    }

    // 재난 누적 알림
    public void sendReportThresholdAlert(FcmMessageDto dto, List<String> tokens) {

        // 헤당 지역 일반 사용자
        List<List<String>> batches = partition(tokens);

        for (List<String> batch : batches) {
            MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(batch)
                .setNotification(buildNotification(dto))
                .setAndroidConfig(buildAndroidConfig())
                .build();

            try {
                fcm.sendEachForMulticastAsync(message);
            } catch (Exception e) {
                log.error("FCM 멀티캐스트 전송 실패 - 대상 수: {}", batch.size(), e);
            }
        }

        // 전국 민간단체 (토픽 구독)
        Message topicMessage = Message.builder()
            .setTopic("threshold-alert")
            .setWebpushConfig(buildWebpushConfig(dto))
            .build();
        try {
            fcm.sendAsync(topicMessage);
        } catch (Exception e) {
            log.error("FCM 메시지 전송 실패", e);
        }
    }

    // 봉사 게시글 수정 알림
    public void sendVolunteerUpdateAlert(FcmMessageDto dto, List<String> tokens) {

        // 봉사 참여자
        MulticastMessage message = MulticastMessage.builder()
            .addAllTokens(tokens)
            .setNotification(buildNotification(dto))
            .setAndroidConfig(buildAndroidConfig())
            .build();

        try {
            fcm.sendEachForMulticastAsync(message);
        } catch (Exception e) {
            log.error("FCM 메시지 전송 실패", e);
        }
    }
    public void sendVolunteerCancelAlert(FcmMessageDto dto, List<String> tokens) {

        // 봉사 참여자
        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(tokens)
                .setNotification(buildNotification(dto))
                .setAndroidConfig(buildAndroidConfig())
                .build();

        try {
            fcm.sendEachForMulticastAsync(message);
        } catch (Exception e) {
            log.error("FCM 메시지 전송 실패", e);
        }
    }
}