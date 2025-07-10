package com.example.emergencyassistb4b4.alert.client.userDevice;

import com.example.emergencyassistb4b4.userDevice.service.UserDeviceService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDeviceClientImpl implements UserDeviceClient {

    private final UserDeviceService userDeviceService;

    @Override
    public String findFcmTokenByUserId(Long userId) {
        return userDeviceService.findFcmTokenByUserId(userId);
    }

    @Override
    public List<String> findFcmTokensByUserIds(List<Long> userIds) {
        return userDeviceService.findFcmTokensByUserIds(userIds);
    }
}
