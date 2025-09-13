package com.example.emergencyassistb4b4.domain.user.service;

import com.example.emergencyassistb4b4.domain.location.service.LocationService;
import com.example.emergencyassistb4b4.domain.user.dto.UserInfoResponseDto;
import com.example.emergencyassistb4b4.global.exception.ApiException;
import com.example.emergencyassistb4b4.global.status.ErrorStatus;
import com.example.emergencyassistb4b4.domain.report.domain.Report;
import com.example.emergencyassistb4b4.domain.report.repository.ReportRepository;
import com.example.emergencyassistb4b4.domain.user.domain.User;
import com.example.emergencyassistb4b4.domain.user.dto.UserResponseDto;
import com.example.emergencyassistb4b4.domain.user.repository.UserRepository;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final LocationService locationService;

    @Transactional(readOnly = true)
    public User getReporterInfo(Long reportId, User responder) {

        // 신고 조회
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ApiException(ErrorStatus.REPORT_NOT_FOUND));

        // 권한 확인 (해당 신고의 담당자인지 확인)
        if (!report.getResponder().getId().equals(responder.getId())) {
            throw new ApiException(ErrorStatus.CUSTOM_ERROR_STATUS);
        }

        // 신고자 반환
        return report.getReporter();
    }

    public UserResponseDto getMyInfo(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow( () -> new ApiException(ErrorStatus.USER_NOT_FOUND));

        return UserResponseDto.from(user);
    }

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

    // 컨트롤러를 위한 DTO 반환용 메서드
    @Transactional(readOnly = true)
    public UserInfoResponseDto getReporterInfoDto(Long reportId, User responder) {

        User reporter = getReporterInfo(reportId, responder);

        return UserInfoResponseDto.from(reporter);
    }
}