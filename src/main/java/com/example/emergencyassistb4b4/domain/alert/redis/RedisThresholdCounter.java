package com.example.emergencyassistb4b4.domain.alert.redis;

import com.example.emergencyassistb4b4.global.exception.ApiException;
import com.example.emergencyassistb4b4.global.status.ErrorStatus;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisThresholdCounter {

    private final StringRedisTemplate redisTemplate;

    public static final long NO_THRESHOLD_MATCHED = -1L;

    private static final DefaultRedisScript<Long> LUA_SCRIPT;

    static {
        LUA_SCRIPT = new DefaultRedisScript<>();
        LUA_SCRIPT.setScriptText(
                """
                local count = redis.call('INCR', KEYS[1])
                redis.call('EXPIRE', KEYS[1], tonumber(ARGV[1]))
    
                for i = 2, #ARGV do
                    local threshold = tonumber(ARGV[i])
                    if count == threshold then
                        local notifyKey = KEYS[2] .. ":" .. threshold
                        local set = redis.call('SETNX', notifyKey, "true")
                        if set == 1 then
                            redis.call('EXPIRE', notifyKey, tonumber(ARGV[1]))
                            return threshold
                        else
                            return -1
                        end
                    end
                end
    
                return -1
                """
        );
        LUA_SCRIPT.setResultType(Long.class);
    }

    public Long incrementAndCheckThreshold(
        String counterKey,
        String notifyKeyPrefix,
        Duration ttl,
        List<Long> thresholds
    ) {
        List<String> keys = List.of(counterKey, notifyKeyPrefix);

        List<String> args = new ArrayList<>();

        args.add(String.valueOf(ttl.getSeconds()));

        thresholds.stream()
            .filter(t -> t != null && t > 0)
            .distinct()
            .map(String::valueOf)
            .forEach(args::add);

        try {
            return redisTemplate.execute(
                LUA_SCRIPT,
                keys,
                args.toArray()
            );
        } catch (Exception e) {
            log.error("Redis Lua 실행 중 예외 발생 - counterKey={}, prefix={}", counterKey, notifyKeyPrefix, e);
            throw new ApiException(ErrorStatus.REDIS_SERVER_ERROR);
        }
    }
}