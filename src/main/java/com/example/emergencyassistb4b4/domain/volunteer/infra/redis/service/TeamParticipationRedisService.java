package com.example.emergencyassistb4b4.domain.volunteer.infra.redis.service;

import com.example.emergencyassistb4b4.global.exception.ApiException;
import com.example.emergencyassistb4b4.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamParticipationRedisService {

    private final RedisTemplate<String, String> redisTemplate;
    private final DefaultRedisScript<Long> joinTeamScript;
    private final DefaultRedisScript<Long> cancelJoinScript;
    private final TTLRedisService ttlRedisService;

    // hash tag 적용: {teamId} → 같은 slot에 배치
    private static final String TEAM_KEY_FORMAT = "team:{%d}:info";
    private static final String USERS_KEY_FORMAT = "team:{%d}:users";

    // 참가
    public void tryJoinTeam(Long teamId, Long userId, int maxCapacity, LocalDateTime checkinEnd) {
        String teamKey = String.format(TEAM_KEY_FORMAT, teamId);
        String usersKey = String.format(USERS_KEY_FORMAT, teamId);

        Long result = redisTemplate.execute(
                joinTeamScript,
                List.of(teamKey, usersKey),
                String.valueOf(maxCapacity),
                String.valueOf(userId)
        );

        if (result == null) {
            throw new ApiException(ErrorStatus.VOLUNTEER_INTERNAL_SERVER_ERROR);
        }

        switch (result.intValue()) {
            case 1 -> {
                Duration ttl = Duration.between(LocalDateTime.now(), checkinEnd.plusHours(1));
                ttlRedisService.setTeamKeyTTL(teamId, ttl);
            }
            case 0, -1 -> throw new ApiException(ErrorStatus.VOLUNTEER_CONFLICT);
            default -> throw new ApiException(ErrorStatus.VOLUNTEER_BAD_REQUEST);
        }
    }

    // 취소
    public void cancelJoin(Long teamId, Long userId) {
        String teamKey = String.format(TEAM_KEY_FORMAT, teamId);
        String usersKey = String.format(USERS_KEY_FORMAT, teamId);

        Long result = redisTemplate.execute(
                cancelJoinScript,
                List.of(teamKey, usersKey),
                String.valueOf(userId)
        );

        if (result == null || result == -1) {
            throw new ApiException(ErrorStatus.VOLUNTEER_BAD_REQUEST);
        }
    }

    // 현재 인원 조회
    public int getCurrentCount(Long teamId) {
        String teamKey = String.format(TEAM_KEY_FORMAT, teamId);
        Object count = redisTemplate.opsForHash().get(teamKey, "count");
        return count != null ? Integer.parseInt(count.toString()) : 0;
    }
}
