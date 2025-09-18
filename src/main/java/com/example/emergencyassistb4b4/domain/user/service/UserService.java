package com.example.emergencyassistb4b4.domain.user.service;

import com.example.emergencyassistb4b4.domain.location.service.LocationService;
import com.example.emergencyassistb4b4.global.exception.ApiException;
import com.example.emergencyassistb4b4.global.status.ErrorStatus;
import com.example.emergencyassistb4b4.domain.user.domain.User;
import com.example.emergencyassistb4b4.domain.user.dto.UserResponseDto;
import com.example.emergencyassistb4b4.domain.user.repository.UserRepository;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final LocationService locationService;

    // 이메일로 사용자 정보 조회
    public UserResponseDto getMyInfo(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow( () -> new ApiException(ErrorStatus.USER_NOT_FOUND));

        return UserResponseDto.from(user);
    }

    // 특정 지역(시/구)에 속한 사용자 ID 목록 조회
    public List<Long> findUsersByRegion(String province, String city) {

        String regionKey = String.format("region:%s:%s", province, city);
        Set<Object> users = locationService.getRegion(regionKey);

        if (users == null || users.isEmpty()) {
            return Collections.emptyList();
        }

        return users.stream()
            .map(String::valueOf)
            .map(Long::valueOf)
            .toList();
    }
}