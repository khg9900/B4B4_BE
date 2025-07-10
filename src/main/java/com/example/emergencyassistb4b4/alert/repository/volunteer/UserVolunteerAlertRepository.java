package com.example.emergencyassistb4b4.alert.repository.volunteer;

import com.example.emergencyassistb4b4.alert.domain.volunteer.UserVolunteerAlert;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserVolunteerAlertRepository extends JpaRepository<UserVolunteerAlert, Long> {

    List<UserVolunteerAlert> findByUserIdOrderByIdDesc(Long userId);

}
