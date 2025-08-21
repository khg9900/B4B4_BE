package com.example.emergencyassistb4b4.domain.volunteer.infra.redis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.script.DefaultRedisScript;

@Configuration
public class TeamParticipationRedisScriptConfig {

    // 팀 참가 스크립트
    @Bean
    public DefaultRedisScript<Long> joinTeamScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText("""
            -- KEYS[1] = team:{id}:info (Hash)
            -- KEYS[2] = team:{id}:users (Set)
            -- ARGV[1] = 최대 정원
            -- ARGV[2] = 유저 ID

            local count = tonumber(redis.call("HGET", KEYS[1], "count") or "0")

            if count >= tonumber(ARGV[1]) then
                return 0
            end

            if redis.call("SISMEMBER", KEYS[2], ARGV[2]) == 1 then
                return -1
            end

            redis.call("SADD", KEYS[2], ARGV[2])
            redis.call("HINCRBY", KEYS[1], "count", 1)

            return 1
        """);
        script.setResultType(Long.class);
        return script;
    }

    // 팀 참가 취소 스크립트
    @Bean
    public DefaultRedisScript<Long> cancelJoinScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText("""
            -- KEYS[1] = team:{id}:info (Hash)
            -- KEYS[2] = team:{id}:users (Set)
            -- ARGV[1] = 유저 ID

            if redis.call("SREM", KEYS[2], ARGV[1]) == 1 then
                return redis.call("HINCRBY", KEYS[1], "count", -1)
            else
                return -1
            end
        """);
        script.setResultType(Long.class);
        return script;
    }
}
