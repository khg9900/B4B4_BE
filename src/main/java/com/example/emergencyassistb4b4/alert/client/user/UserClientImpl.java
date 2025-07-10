package com.example.emergencyassistb4b4.alert.client.user;

import com.example.emergencyassistb4b4.user.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserClientImpl implements UserClient {

    private final UserService userService;

    @Override
    public List<Long> findUsersByRegion(String province, String city) {

        return userService.findUsersByRegion(province, city);
    }
}
