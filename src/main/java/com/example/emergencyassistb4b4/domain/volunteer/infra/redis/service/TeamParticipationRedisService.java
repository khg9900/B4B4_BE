package com.example.emergencyassistb4b4.domain.volunteer.infra.redis.service;

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
            throw new ApiException(ErrorStatus.VOLUNTEER_CONFLICT);
        }

        Long result = redisTemplate.execute(
                joinTeamScript,
                List.of(countKey, usersKey),
                String.valueOf(maxCapacity),
                String.valueOf(userId)
        );

        if (result == null) {
            throw new ApiException(ErrorStatus.VOLUNTEER_INTERNAL_SERVER_ERROR);
        }

        switch (result.intValue()) {
            case 1 -> {
                // TTL 계산: checkinEnd + 30분
                Duration ttl = Duration.between(LocalDateTime.now(), checkinEnd.plusMinutes(30));
                ttl = ttl.isNegative() ? Duration.ofSeconds(1) : ttl;

                // 중복 키와 팀 키 TTL 동시 설정
                redisTemplate.opsForValue().set(duplicateKey, "1", ttl);
                ttlRedisService.setTeamKeyTTL(postId, teamId,userId,ttl);
            }
            case 0, -1 -> throw new ApiException(ErrorStatus.VOLUNTEER_CONFLICT);
            default -> throw new ApiException(ErrorStatus.VOLUNTEER_BAD_REQUEST);
        }
    }

    /**
     * 참가 취소 : 현재 인원 감소 + duplicateKey 삭제
     */
    public void cancelJoin(Long postId, Long teamId, Long userId) {
        String usersKey = String.format(USERS_KEY_FORMAT, postId, teamId);
        String countKey = String.format(COUNT_KEY_FORMAT, postId, teamId);
        String duplicateKey = String.format(DUPLICATE_KEY_FORMAT, postId, teamId, userId);

        Long result = redisTemplate.execute(
                cancelJoinScript,
                List.of(usersKey, countKey),
                String.valueOf(userId)
        );

        if (result == null || result == -1) {
            throw new ApiException(ErrorStatus.VOLUNTEER_BAD_REQUEST);
        }

        // 취소 성공 시 중복 키 삭제
        String countStr = redisTemplate.opsForValue().get(countKey);
        int currentCount = countStr != null ? Integer.parseInt(countStr) : 0;
        if (currentCount == 0) {
            ttlRedisService.deleteTeamKeys(postId, teamId, null);
        }

        redisTemplate.delete(duplicateKey);

    }

    /**
     * 현재 인원 조회
     */
    public int getCurrentCount(Long postId, Long teamId) {
        String countKey = String.format(COUNT_KEY_FORMAT, postId, teamId);
        String count = redisTemplate.opsForValue().get(countKey);
        return count != null ? Integer.parseInt(count) : 0;
    }
}
