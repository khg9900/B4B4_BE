package com.example.emergencyassistb4b4.alert.kafka.repository;

import com.example.emergencyassistb4b4.alert.kafka.domain.KafkaDlqLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KafkaDlqLogRepository extends JpaRepository<KafkaDlqLog, Long> {

}