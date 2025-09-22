package com.example.emergencyassistb4b4.domain.alert.service.query;

import com.example.emergencyassistb4b4.domain.alert.dto.report.ReportAlertResponseDto;
import com.example.emergencyassistb4b4.domain.alert.dto.response.UserAlert;
import com.example.emergencyassistb4b4.domain.alert.dto.volunteer.VolunteerAlertResponseDto;
import com.example.emergencyassistb4b4.domain.alert.enums.AlertType;
import com.example.emergencyassistb4b4.domain.alert.repository.report.UserReportAlertRepository;
import com.example.emergencyassistb4b4.domain.alert.repository.volunteer.UserVolunteerAlertRepository;
import com.example.emergencyassistb4b4.domain.user.domain.User;
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

    public List<UserAlert> listAlerts(AlertType type, User user) {

        return switch (type) {
            case DISASTER -> listDisasterAlerts(user.getId());
            case VOLUNTEER -> listVolunteerAlerts(user.getId());
        };
    }

    private List<UserAlert> listDisasterAlerts(Long userId) {

        return userReportAlertRepository
            .findByUserIdOrderByIdDesc(userId)
            .stream()
            .map(alert -> (UserAlert) ReportAlertResponseDto.fromUserReportAlert(alert))
            .toList();
    }

    private List<UserAlert> listVolunteerAlerts(Long userId) {

        return userVolunteerAlertRepository
            .findByUserIdOrderByIdDesc(userId)
            .stream()
            .map(alert -> (UserAlert) VolunteerAlertResponseDto.fromUserVolunteerAlert(alert))
            .toList();
    }
}
