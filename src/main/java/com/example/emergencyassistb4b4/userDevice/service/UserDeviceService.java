package com.example.emergencyassistb4b4.userDevice.service;


import com.example.emergencyassistb4b4.global.exception.ApiException;
import com.example.emergencyassistb4b4.global.status.ErrorStatus;
import com.example.emergencyassistb4b4.user.domain.User;
import com.example.emergencyassistb4b4.userDevice.domain.UserDevice;
import com.example.emergencyassistb4b4.userDevice.dto.UserDeviceRequestDto;
import com.example.emergencyassistb4b4.userDevice.enums.DeviceOs;
import com.example.emergencyassistb4b4.userDevice.enums.DeviceType;
import com.example.emergencyassistb4b4.userDevice.repository.UserDeviceRepository;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDeviceService {

    private final UserDeviceRepository userDeviceRepository;

    // 기기 등록
    public void saveDevice(User user, UserDeviceRequestDto dto) {

        UserDevice device = userDeviceRepository
            .findByUser(user)
            // 현재(2025.07) 한 유저 당 하나의 기기만 등록 가능
            .orElseGet(() -> UserDevice.builder()
                .user(user)
                .type(DeviceType.from(dto.getType()))
                .os(DeviceOs.from(dto.getOs()))
                .osVersion(dto.getOsVersion())
                .model(dto.getModel())
                .fcmToken(dto.getFcmToken())
                .build());

        // 기기가 이미 존재할 경우 토큰만 갱신
        if (device.getId() != null) {
            device.updateToken(dto.getFcmToken());
        }
        userDeviceRepository.save(device);
    }

    public String findFcmTokenByUserId(Long userId) {
        return userDeviceRepository.findFcmTokenByUserId(userId)
                .orElseThrow(() -> new ApiException(ErrorStatus.USER_DEVICE_NOT_FOUND));
    }

    public List<String> findFcmTokensByUserIds(List<Long> userIds) {
        return userDeviceRepository.findFcmTokensByUserIds(userIds);
    }
}
