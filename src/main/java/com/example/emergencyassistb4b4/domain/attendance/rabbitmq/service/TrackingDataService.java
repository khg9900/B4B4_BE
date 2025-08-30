package com.example.emergencyassistb4b4.domain.attendance.rabbitmq.service;
import com.example.emergencyassistb4b4.domain.attendance.redis.RabbitMQRedisService;
import com.example.emergencyassistb4b4.domain.volunteer.domain.VolunteerParticipant;
import com.example.emergencyassistb4b4.domain.volunteer.enums.CheckinStatus;
import com.example.emergencyassistb4b4.domain.volunteer.repository.VolunteerParticipantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Service
public class TrackingDataService {

    private final RabbitMQRedisService rabbitMQRedisService;
    private final VolunteerParticipantRepository participantRepository;

    private static final int ATTENDANCE_THRESHOLD = 3;

    @Transactional
    public void saveSessionAttendanceData(List<Long> volunteerIds, Long teamId) {
        List<VolunteerParticipant> updateList = new ArrayList<>();

        for (Long volunteerId : volunteerIds) {
            List<String> records = rabbitMQRedisService.fetchAttendanceRecords(volunteerId);
            if (records == null || records.isEmpty()) continue;

            long presentCount = records.stream()
                    .map(this::parseRecordToBoolean)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .filter(Boolean::booleanValue)
                    .count();

            CheckinStatus finalStatus = (presentCount > ATTENDANCE_THRESHOLD) ? CheckinStatus.PRESENT : CheckinStatus.ABSENT;

            participantRepository.findById(volunteerId).ifPresent(participant -> {
                participant.updateStatus(finalStatus);
                updateList.add(participant);
            });
        }

        if (!updateList.isEmpty()) participantRepository.saveAll(updateList);

        // DB 저장 후 Redis 삭제
        for (Long volunteerId : volunteerIds) {
            rabbitMQRedisService.clearAttendanceHistory(volunteerId);
        }

        log.info("참여자 출석 상태 {}건 저장 완료 (teamId={})", updateList.size(), teamId);
    }

    private Optional<Boolean> parseRecordToBoolean(String record) {
        if (record == null || record.isBlank()) return Optional.empty();

        // "1" → true, "0" → false
        return switch (record) {
            case "1" -> Optional.of(true);
            case "0" -> Optional.of(false);
            default -> Optional.empty();
        };
    }
}
