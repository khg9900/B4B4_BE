package com.example.emergencyassistb4b4.domain.alert.service.command;

import com.example.emergencyassistb4b4.domain.alert.domain.report.ReportAlert;
import com.example.emergencyassistb4b4.domain.alert.domain.report.UserReportAlert;
import com.example.emergencyassistb4b4.domain.alert.domain.volunteer.UserVolunteerAlert;
import com.example.emergencyassistb4b4.domain.alert.domain.volunteer.VolunteerAlert;
import com.example.emergencyassistb4b4.domain.alert.dto.report.ReportThresholdAlertDto;
import com.example.emergencyassistb4b4.domain.alert.dto.volunteer.VolunteerPostAlertDto;
import com.example.emergencyassistb4b4.domain.alert.repository.report.ReportAlertRepository;
import com.example.emergencyassistb4b4.domain.alert.repository.volunteer.UserVolunteerAlertRepository;
import com.example.emergencyassistb4b4.domain.alert.repository.volunteer.VolunteerAlertRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AlertCommandService {

    private final ReportAlertRepository reportAlertRepository;
    private final UserReportAlertBulkService userReportAlertBulkService;
    private final VolunteerAlertRepository volunteerAlertRepository;
    private final UserVolunteerAlertRepository userVolunteerAlertRepository;

    public void saveReportThresholdAlert(ReportThresholdAlertDto dto, List<Long> userIds) {

        ReportAlert alert = reportAlertRepository.save(dto.toEntity());

        List<UserReportAlert> userReportAlerts = UserReportAlert.fromUsers(alert, userIds);

        userReportAlertBulkService.saveAllInBatches(userReportAlerts, 1000);
    }

    public void saveVolunteerPostAlert(VolunteerPostAlertDto dto, List<Long> participants) {

        VolunteerAlert alert = volunteerAlertRepository.save(dto.toEntity());

        userVolunteerAlertRepository.saveAll(UserVolunteerAlert.fromUsers(alert, participants));
    }
}