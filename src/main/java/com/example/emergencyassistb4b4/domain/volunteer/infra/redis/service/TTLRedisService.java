package com.example.emergencyassistb4b4.domain.volunteer.infra.redis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.Cursor;

import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TTLRedisService {

    private final StringRedisTemplate redisTemplate;

    private static final String COUNT_KEY_FORMAT = "team:%d:%d:count";
    private static final String USERS_KEY_FORMAT = "team:%d:%d:users";
    private static final String DUPLICATE_KEY_FORMAT = "team:%d:%d:%d:duplicate";

    /**
     * TTL 설정 - Duration 기반
     */
    public void setTeamKeyTTL(Long postId, Long teamId, Long userId, Duration duration) {

        long seconds = duration.isNegative() ? 1 : duration.getSeconds(); // 최소 1초
        setTeamKeyTTL(postId, teamId, userId, seconds, TimeUnit.SECONDS);
    }

    /**
     * TTL 설정 - 직접 시간 단위
     */
    public void setTeamKeyTTL(Long postId, Long teamId, Long userId, long duration, TimeUnit unit) {

        String countKey = String.format(COUNT_KEY_FORMAT, postId, teamId);
        String usersKey = String.format(USERS_KEY_FORMAT, postId, teamId);

        redisTemplate.expire(countKey, duration, unit);
        redisTemplate.expire(usersKey, duration, unit);

        if (userId != null) {
            String duplicateKey = String.format(DUPLICATE_KEY_FORMAT, postId, teamId, userId);
            redisTemplate.expire(duplicateKey, duration, unit);
        }
    }

    /**
     * 팀 키 삭제
     */
    public void deleteTeamKeys(Long postId, Long teamId, Long userId) {

        String countKey = String.format(COUNT_KEY_FORMAT, postId, teamId);
        String usersKey = String.format(USERS_KEY_FORMAT, postId, teamId);
        redisTemplate.delete(countKey);
        redisTemplate.delete(usersKey);

        if (userId != null) {
            String duplicateKey = String.format(DUPLICATE_KEY_FORMAT, postId, teamId, userId);
            redisTemplate.delete(duplicateKey);
        }
    }

    // 팀 전체 duplicate 삭제
    public long deleteAllDuplicateKeys(Long postId, Long teamId) {

        String pattern = String.format("team:%d:%d:*:duplicate", postId, teamId);
        ScanOptions opt = ScanOptions.scanOptions().match(pattern).count(5000).build();

        List<byte[]> keys = new java.util.ArrayList<>();
        redisTemplate.execute((RedisCallback<Void>) (RedisConnection connection) -> {
            try (Cursor<byte[]> c = connection.keyCommands().scan(opt)) {
                while (c.hasNext()) keys.add(c.next());
            } catch (Exception ignore) {}
            return null;
        });

        if (keys.isEmpty()) return 0;
        redisTemplate.executePipelined((org.springframework.data.redis.core.RedisCallback<Object>) c -> {
            for (byte[] k : keys) c.keyCommands().unlink(k);
            return null;
        });

        return keys.size();
    }

    // 팀 전체 키 삭제(duplicate 전체 + count/users)
    public long deleteWholeTeamKeys(Long postId, Long teamId) {

        String countKey = String.format("team:%d:%d:count", postId, teamId);
        String usersKey = String.format("team:%d:%d:users", postId, teamId);

        long dup = deleteAllDuplicateKeys(postId, teamId);
        redisTemplate.delete(List.of(countKey, usersKey));

        return dup + 2;
    }


    public void deleteAllKeysByPostId(Long postId) {
        String pattern = "team:" + postId + ":*";
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(100).build();

        var connection = Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection();
        try (Cursor<byte[]> cursor = connection.keyCommands().scan(options)) {
            while (cursor.hasNext()) {
                byte[] keyBytes = cursor.next();
                redisTemplate.delete(new String(keyBytes));
            }
        }
    }
}
