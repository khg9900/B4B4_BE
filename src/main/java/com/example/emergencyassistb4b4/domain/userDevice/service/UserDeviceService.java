package com.example.emergencyassistb4b4.domain.userDevice.service;

import static com.example.emergencyassistb4b4.domain.user.domain.UserRole.NGO;

import com.example.emergencyassistb4b4.domain.alert.fcm.subscribe.FcmSubService;
import com.example.emergencyassistb4b4.global.exception.ApiException;
import com.example.emergencyassistb4b4.global.status.ErrorStatus;
import com.example.emergencyassistb4b4.domain.user.domain.User;
import com.example.emergencyassistb4b4.domain.userDevice.domain.UserDevice;
import com.example.emergencyassistb4b4.domain.userDevice.dto.UserDeviceRequestDto;
import com.example.emergencyassistb4b4.domain.userDevice.repository.UserDeviceRepository;
import com.google.firebase.messaging.FirebaseMessagingException;
import java.util.List;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDeviceService {

    private final UserDeviceRepository userDeviceRepository;
    private final FcmSubService fcmSubService;

    public void saveDevice(User user, UserDeviceRequestDto dto) {

        UserDevice device = userDeviceRepository
            .findByUser(user)
            .orElseGet(() -> UserDevice.builder()
                .user(user)
                .build());

        String oldToken = blankToNull(device.getFcmToken());
        String newToken = blankToNull(dto.getFcmToken());

        device.update(dto);
        userDeviceRepository.save(device);

        if (user.getUserRole() == NGO) {

            if (oldToken != null && !Objects.equals(oldToken, newToken)) {
                try {
                    fcmSubService.unsubscribeNgoTokens(List.of(oldToken));
                } catch (FirebaseMessagingException e) {
                    log.warn("FCM 토픽 구독 해제 실패");
                }
            }

            try {
                fcmSubService.subscribeNgoTokens(List.of(newToken));
            } catch (FirebaseMessagingException e) {
                log.warn("FCM 토픽 구독 실패");
            }
        }

    }

    public String findFcmTokenByUserId(Long userId) {
        return userDeviceRepository.findFcmTokenByUserId(userId)
                .orElseThrow(() -> new ApiException(ErrorStatus.USER_DEVICE_NOT_FOUND));
    }

    public List<String> findFcmTokensByUserIds(List<Long> userIds) {
        return userDeviceRepository.findFcmTokensByUserIds(userIds);
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

}
