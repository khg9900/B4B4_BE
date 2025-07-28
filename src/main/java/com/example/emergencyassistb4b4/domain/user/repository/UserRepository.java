package com.example.emergencyassistb4b4.domain.user.repository;

import com.example.emergencyassistb4b4.domain.user.domain.User;
import com.example.emergencyassistb4b4.domain.user.domain.UserRole;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findById(Long id);

    // 예: city 와 role 기준 조회
    Optional<User> findFirstByProvinceAndCityAndUserRole(String province, String city, UserRole role);

    // 또는 province 만으로 조회
    Optional<User> findFirstByProvinceAndUserRole(String province, UserRole role);

    @Query("SELECT u.id FROM User u WHERE u.province LIKE CONCAT(:province, '%') AND u.city LIKE CONCAT(:city, '%') AND u.userRole = :userRole")
    List<Long> findUsersByRegion(
        @Param("province") String province,
        @Param("city") String city,
        @Param("userRole") UserRole userRole
    );

}