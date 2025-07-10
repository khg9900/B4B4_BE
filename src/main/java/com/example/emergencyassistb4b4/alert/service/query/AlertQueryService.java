package com.example.emergencyassistb4b4.alert.service.query;

import com.example.emergencyassistb4b4.alert.dto.report.ReportAlertResponseDto;
import com.example.emergencyassistb4b4.alert.dto.response.UserAlert;
import com.example.emergencyassistb4b4.alert.dto.volunteer.VolunteerAlertResponseDto;
import com.example.emergencyassistb4b4.alert.enums.AlertType;
import com.example.emergencyassistb4b4.alert.repository.report.UserReportAlertRepository;
import com.example.emergencyassistb4b4.alert.repository.volunteer.UserVolunteerAlertRepository;
import com.example.emergencyassistb4b4.user.domain.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlertQueryService {

    private final UserReportAlertRepository userReportAlertRepository;
    private final UserVolunteerAlertRepository userVolunteerAlertRepository;

    // 알림 조회
    public List<UserAlert> listAlerts(AlertType type, User user) {

        return switch (type) {
            // 재난 알림
            case DISASTER -> listDisasterAlerts(user.getId());
            // 봉사 알림
            case VOLUNTEER -> listVolunteerAlerts(user.getId());
        };
    }

    // 재난 알림 조회
    private List<UserAlert> listDisasterAlerts(Long userId) {

        return userReportAlertRepository
            .findByUserIdOrderByIdDesc(userId)
            .stream()
            .map(alert -> (UserAlert) ReportAlertResponseDto.fromUserReportAlert(alert))
            .toList();
    }

    // 봉사 알림 조회
    private List<UserAlert> listVolunteerAlerts(Long userId) {

        return userVolunteerAlertRepository
            .findByUserIdOrderByIdDesc(userId)
            .stream()
            .map(alert -> (UserAlert) VolunteerAlertResponseDto.fromUserVolunteerAlert(alert))
            .toList();
    }
}
