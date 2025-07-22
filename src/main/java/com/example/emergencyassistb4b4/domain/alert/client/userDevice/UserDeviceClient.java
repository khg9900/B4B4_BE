package com.example.emergencyassistb4b4.domain.alert.client.userDevice;

import java.util.List;

public interface UserDeviceClient {

    String findFcmTokenByUserId(Long userId);

    List<String> findFcmTokensByUserIds(List<Long> userIds);

}
