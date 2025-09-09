package com.example.emergencyassistb4b4.domain.volunteer.infra.redis.service;

import com.example.emergencyassistb4b4.domain.volunteer.repository.VolunteerParticipantRepository;
import com.example.emergencyassistb4b4.global.exception.ApiException;
import com.example.emergencyassistb4b4.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamParticipationRedisService {

    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<Long> joinTeamScript;
    private final DefaultRedisScript<Long> cancelJoinScript;
    private final TTLRedisService ttlRedisService;
    private final VolunteerParticipantRepository participantRepository;

    private static final String COUNT_KEY_FORMAT = "team:%d:%d:count";           // team:{postId}:{teamId}:count
    private static final String USERS_KEY_FORMAT = "team:%d:%d:users";           // team:{postId}:{teamId}:users
    private static final String DUPLICATE_KEY_FORMAT = "team:%d:%d:%d:duplicate"; // team:{postId}:{teamId}:{userId}:duplicate

    /**
     * 참가 : 현재 인원 증가 + 중복 신청 체크 + TTL 통합 설정
     */
    public void tryJoinTeam(Long postId, Long teamId, Long userId, int maxCapacity, LocalDateTime checkinEnd) {

        String countKey = String.format(COUNT_KEY_FORMAT, postId, teamId);
        String usersKey = String.format(USERS_KEY_FORMAT, postId, teamId);
        String duplicateKey = String.format(DUPLICATE_KEY_FORMAT, postId, teamId, userId);

        if (Boolean.TRUE.equals(redisTemplate.hasKey(duplicateKey))) {
            Boolean inUsers = redisTemplate.opsForSet().isMember(usersKey, String.valueOf(userId));
            if (Boolean.FALSE.equals(inUsers)) {
                // 레거시 잔재. 키 삭제 후 진행
                redisTemplate.delete(duplicateKey);
            } else {
                throw new ApiException(ErrorStatus.VOLUNTEER_CONFLICT);
            }
        }

        long expireAt = checkinEnd.plusMinutes(30).atZone(java.time.ZoneId.systemDefault()).toEpochSecond();

        Long result = redisTemplate.execute(
                joinTeamScript,
                List.of(countKey, usersKey),
                String.valueOf(maxCapacity),
                String.valueOf(userId),
                String.valueOf(expireAt) // ← 추가
        );
        if (result == null) throw new ApiException(ErrorStatus.VOLUNTEER_INTERNAL_SERVER_ERROR);
        switch (result.intValue()) {
            case 1 -> {
                Duration ttl = Duration.between(LocalDateTime.now(), checkinEnd.plusMinutes(30));
                ttl = ttl.isNegative() ? Duration.ofSeconds(1) : ttl;
                redisTemplate.opsForValue().set(duplicateKey, "1", ttl);
                // (선택) 아래는 백업용. Lua에서 이미 EXPIREAT 했으므로 없어도 됨.
                ttlRedisService.setTeamKeyTTL(postId, teamId, userId, ttl);
            }
            case 0, -1 -> throw new ApiException(ErrorStatus.VOLUNTEER_CONFLICT);
            default -> throw new ApiException(ErrorStatus.VOLUNTEER_BAD_REQUEST);
        }
    }

    /**
     * 참가 취소 : 현재 인원 감소 + duplicateKey 삭제
     */
    // 취소 + 필요시 키 정리만. TTL 보정 제거.
    public void cancelJoin(Long postId, Long teamId, Long userId) {

        String usersKey = String.format(USERS_KEY_FORMAT, postId, teamId);
        String countKey = String.format(COUNT_KEY_FORMAT, postId, teamId);
        String duplicateKey = String.format(DUPLICATE_KEY_FORMAT, postId, teamId, userId);

        Long result = redisTemplate.execute(cancelJoinScript, List.of(usersKey, countKey), String.valueOf(userId));
        if (result == null || result == -1) throw new ApiException(ErrorStatus.VOLUNTEER_BAD_REQUEST);

        redisTemplate.delete(duplicateKey);

        String countStr = redisTemplate.opsForValue().get(countKey);
        int currentCount = countStr != null ? Integer.parseInt(countStr) : 0;
        if (currentCount == 0) {
            ttlRedisService.deleteTeamKeys(postId, teamId, null);
            ttlRedisService.deleteAllDuplicateKeys(postId, teamId);
        }
    }

    // 취소 후 TTL 보정 전담
    public void cancelJoin(Long postId, Long teamId, Long userId, LocalDateTime checkinEnd) {

        cancelJoin(postId, teamId, userId); // 위 메서드 호출

        String countKey = String.format(COUNT_KEY_FORMAT, postId, teamId);
        Long ttlSec = redisTemplate.getExpire(countKey);
        if (ttlSec == null || ttlSec == -1) {
            Duration ttl = Duration.between(LocalDateTime.now(), checkinEnd.plusMinutes(30));
            long sec = ttl.isNegative() ? 1 : ttl.getSeconds();
            ttlRedisService.setTeamKeyTTL(postId, teamId, null, sec, java.util.concurrent.TimeUnit.SECONDS);
        }
    }

    /**
     * 현재 인원 조회
     */
    public int getCurrentCount(Long postId, Long teamId) {

        // 1) DB 카운트가 정답
        long dbCount = participantRepository.countParticipatedByTeamId(teamId);

        String countKey = String.format(COUNT_KEY_FORMAT, postId, teamId);
        String c = redisTemplate.opsForValue().get(countKey);
        int redisCount = (c != null) ? Integer.parseInt(c) : 0;

        // 2) 불일치 시 Redis를 DB로 맞춤
        if (dbCount != redisCount) {
            if (dbCount == 0) {
                ttlRedisService.deleteTeamKeys(postId, teamId, null); // 완전 정리
            } else {
                redisTemplate.opsForValue().set(countKey, String.valueOf(dbCount));
            }
        }
        return (int) dbCount;
    }
}
