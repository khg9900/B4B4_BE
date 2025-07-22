package com.example.emergencyassistb4b4.domain.alert.repository.volunteer;

import com.example.emergencyassistb4b4.domain.alert.domain.volunteer.UserVolunteerAlert;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserVolunteerAlertRepository extends JpaRepository<UserVolunteerAlert, Long> {

    List<UserVolunteerAlert> findByUserIdOrderByIdDesc(Long userId);

}
