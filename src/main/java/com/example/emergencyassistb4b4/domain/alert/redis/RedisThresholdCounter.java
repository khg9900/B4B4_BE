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
                -- 카운터 증가 및 TTL 설정
                local count = redis.call('INCR', KEYS[1])
                redis.call('EXPIRE', KEYS[1], tonumber(ARGV[1]))
    
                -- 임계치 목록 순회
                for i = 2, #ARGV do
                    local threshold = tonumber(ARGV[i])
                    if count == threshold then
                        -- 알림 키 구성 및 중복 알림 방지 (SETNX)
                        local notifyKey = KEYS[2] .. ":" .. threshold
                        local set = redis.call('SETNX', notifyKey, "true")
                        if set == 1 then
                            redis.call('EXPIRE', notifyKey, tonumber(ARGV[1]))
                            return threshold
                        else
                            return -1 -- 이미 알림 처리됨
                        end
                    end
                end
    
                return -1 -- 임계치 조건 미충족
                """
        );
        LUA_SCRIPT.setResultType(Long.class);
    }

    /**
     * Redis Cluster-safe threshold check
     *
     * @param counterKeySuffix  이벤트 고유 식별자 (ex: 경기도:안산시 상록구:지진:2025-08-14)
     * @param notifyKeyPrefix   알림 키 접두사 (ex: alert:report)
     * @param ttl               카운터 TTL
     * @param thresholds        알림 임계치 목록
     * @return                  임계치 도달 시 해당 값, 아니면 -1
     */
    public Long incrementAndCheckThreshold(String counterKeySuffix,
                                           String notifyKeyPrefix,
                                           Duration ttl,
                                           List<Long> thresholds) {
        // Redis Cluster-safe 키 생성: 해시태그 { } 안에 동일 문자열 넣어 슬롯 일치 보장
        String hashTag = "{" + counterKeySuffix + "}";
        String counterKey = "report:" + hashTag + ":count";
        String notifyKey = notifyKeyPrefix + ":" + hashTag + ":notify";

        List<String> keys = List.of(counterKey, notifyKey);

        // 파라미터 준비: TTL + thresholds
        List<String> args = new ArrayList<>();
        args.add(String.valueOf(ttl.getSeconds()));
        thresholds.stream()
                .filter(t -> t != null && t > 0)
                .distinct()
                .map(String::valueOf)
                .forEach(args::add);

        try {
            return redisTemplate.execute(LUA_SCRIPT, keys, args.toArray());
        } catch (Exception e) {
            log.error("Redis Lua 실행 실패 - counterKey={}, notifyKey={}, 원인={}",
                    counterKey, notifyKey, e.getMessage(), e);
            throw new ApiException(ErrorStatus.REDIS_SERVER_ERROR);
        }
    }
}
