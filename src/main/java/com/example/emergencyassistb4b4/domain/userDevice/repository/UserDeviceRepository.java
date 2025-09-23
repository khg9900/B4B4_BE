package com.example.emergencyassistb4b4.domain.userDevice.repository;

import com.example.emergencyassistb4b4.domain.user.domain.User;
import com.example.emergencyassistb4b4.domain.userDevice.domain.UserDevice;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {

    Optional<UserDevice> findByUser(User user);

    @Query("""
        SELECT ud.fcmToken
        FROM UserDevice ud
        WHERE ud.user.id = :userId
        """)
    Optional<String> findFcmTokenByUserId(@Param("userId") Long userId);

    @Query("""
            SELECT ud.fcmToken
            FROM UserDevice ud
            WHERE ud.user.id IN :userIds
        """)
    List<String> findFcmTokensByUserIds(@Param("userIds") List<Long> userIds);

}