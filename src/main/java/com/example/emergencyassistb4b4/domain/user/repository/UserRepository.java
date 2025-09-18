package com.example.emergencyassistb4b4.domain.user.repository;

import com.example.emergencyassistb4b4.domain.user.domain.User;
import com.example.emergencyassistb4b4.domain.user.domain.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);

    boolean existsByEmail(String email);

    // city & role 기준 조회
    Optional<User> findFirstByProvinceAndCityAndUserRole(String province, String city, UserRole role);

    // province 만으로 조회
    Optional<User> findFirstByProvinceAndUserRole(String province, UserRole role);
}