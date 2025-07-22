package com.example.emergencyassistb4b4.domain.alert.repository.report;

import com.example.emergencyassistb4b4.domain.alert.domain.report.UserReportAlert;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserReportAlertRepository extends JpaRepository<UserReportAlert, Long> {

    List<UserReportAlert> findByUserIdOrderByIdDesc(Long userId);

}
