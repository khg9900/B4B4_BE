package com.example.emergencyassistb4b4.domain.alert.kafka.repository;

import com.example.emergencyassistb4b4.domain.alert.kafka.domain.KafkaDlqLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KafkaDlqLogRepository extends JpaRepository<KafkaDlqLog, Long> {

}