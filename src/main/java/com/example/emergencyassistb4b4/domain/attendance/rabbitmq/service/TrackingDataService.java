package com.example.emergencyassistb4b4.domain.attendance.rabbitmq.service;
import com.example.emergencyassistb4b4.domain.attendance.redis.RabbitMQRedisService;
import com.example.emergencyassistb4b4.global.exception.ApiException;
import com.example.emergencyassistb4b4.domain.volunteer.domain.VolunteerParticipant;
import com.example.emergencyassistb4b4.domain.volunteer.enums.CheckinStatus;
import com.example.emergencyassistb4b4.domain.volunteer.repository.VolunteerParticipantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.example.emergencyassistb4b4.global.status.ErrorStatus.ATTENDANCE_RECORD_PARSE_FAILED;

@RequiredArgsConstructor
@Slf4j
@Service
public class TrackingDataService {

    private final RabbitMQRedisService rabbitMQRedisService;
    private final VolunteerParticipantRepository participantRepository;

    @Transactional
    public void saveSessionAttendanceData(List<Long> volunteerIds, Long teamId) {
        List<VolunteerParticipant> updateList = new ArrayList<>();

        for (Long volunteerId : volunteerIds) {
            List<String> records = rabbitMQRedisService.fetchAttendanceRecords(volunteerId);

            if (records == null || records.isEmpty()) {

                continue;
            }

            long presentCount = records.stream()
                    .map(this::parseRecordToBoolean)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .filter(present -> present)
                    .count();

            CheckinStatus finalStatus = (presentCount > 27) ? CheckinStatus.PRESENT : CheckinStatus.ABSENT;

            participantRepository.findById(volunteerId).ifPresent(participant -> {
                participant.updateStatus(finalStatus);
                updateList.add(participant);
            });
        }

        if (!updateList.isEmpty()) {
            participantRepository.saveAll(updateList);  // 💡 DB 트랜잭션 적용 대상
        }

        // ⚠️ Redis는 트랜잭션 대상이 아니므로 DB 저장 성공 후 삭제
        for (Long volunteerId : volunteerIds) {

            rabbitMQRedisService.clearAttendanceHistory(volunteerId);

        log.info("참여자 출석 상태 {}건 저장 완료 (teamId={})", updateList.size(), teamId);
    }

    private Optional<Boolean> parseRecordToBoolean(String record) {
        try {
            String[] parts = record.split(":");
            if (parts.length != 2) return Optional.empty();
            return Optional.of("1".equals(parts[1]));
        } catch (Exception e) {
            throw new ApiException(ATTENDANCE_RECORD_PARSE_FAILED);
        }
    }
}

