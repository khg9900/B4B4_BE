package com.example.emergencyassistb4b4.domain.attendance.rabbitmq.service;

import com.example.emergencyassistb4b4.domain.attendance.redis.RabbitMQRedisService;
import com.example.emergencyassistb4b4.domain.volunteer.domain.VolunteerParticipant;
import com.example.emergencyassistb4b4.domain.volunteer.enums.CheckinStatus;
import com.example.emergencyassistb4b4.domain.volunteer.repository.VolunteerParticipantRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

    // 세션 참여자 출석 데이터 저장
    @Transactional
    public void saveSessionAttendanceData(List<Long> volunteerIds, Long teamId) {
        List<VolunteerParticipant> updateList = new ArrayList<>();

        for (Long volunteerId : volunteerIds) {
            List<String> records = rabbitMQRedisService.fetchAttendanceRecords(volunteerId);
            if (records == null || records.isEmpty()) continue;

            long presentCount = records.stream()
                    .map(this::parseRecordToBoolean)
                    .flatMap(Optional::stream)
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
        volunteerIds.forEach(rabbitMQRedisService::clearAttendanceHistory);
    }

    // 개별 출석 기록 문자열을 Boolean으로 변환
    private Optional<Boolean> parseRecordToBoolean(String record) {
        if (record == null || record.isBlank()) return Optional.empty();

        return switch (record) {
            case "1" -> Optional.of(true);
            case "0" -> Optional.of(false);
            default -> {
                log.warn("알 수 없는 출석 기록 값: {}", record);
                yield Optional.empty();
            }
        };
    }
}
