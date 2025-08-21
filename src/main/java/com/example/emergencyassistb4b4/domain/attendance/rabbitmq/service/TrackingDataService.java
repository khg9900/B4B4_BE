package com.example.emergencyassistb4b4.domain.attendance.rabbitmq.service;
import com.example.emergencyassistb4b4.domain.attendance.redis.RabbitMQRedisService;
import com.example.emergencyassistb4b4.global.exception.ApiException;
import com.example.emergencyassistb4b4.domain.volunteer.domain.VolunteerParticipant;
import com.example.emergencyassistb4b4.domain.volunteer.enums.CheckinStatus;
import com.example.emergencyassistb4b4.domain.volunteer.repository.VolunteerParticipantRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.example.emergencyassistb4b4.global.status.ErrorStatus.ATTENDANCE_RECORD_PARSE_FAILED;

@RequiredArgsConstructor
@Slf4j
@Service
public class TrackingDataService {

    private final RabbitMQRedisService rabbitMQRedisService;
    private final VolunteerParticipantRepository participantRepository;
    private final ObjectMapper objectMapper;

    // ✅ 상수화: 출석 기준, JSON 필드
    private static final int ATTENDANCE_THRESHOLD = 27;
    private static final String FIELD_PRESENT = "present";

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

    // JSON 기반 출석 기록 파싱
    private Optional<Boolean> parseRecordToBoolean(String recordJson) {
        try {
            Map<?, ?> map = objectMapper.readValue(recordJson, Map.class);
            Object present = map.get(FIELD_PRESENT);
            if (present instanceof Boolean) {
                return Optional.of((Boolean) present);
            } else if (present instanceof String) {
                return Optional.of("1".equals(present));
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            throw new ApiException(ATTENDANCE_RECORD_PARSE_FAILED);
        }
    }
}
