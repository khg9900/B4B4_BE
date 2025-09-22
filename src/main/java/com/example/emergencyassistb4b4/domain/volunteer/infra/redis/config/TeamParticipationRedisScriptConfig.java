package com.example.emergencyassistb4b4.domain.volunteer.infra.redis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.script.DefaultRedisScript;

@Configuration
public class TeamParticipationRedisScriptConfig {

    @Bean
    public DefaultRedisScript<Long> joinTeamScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText("""
            if redis.call("SCARD", KEYS[2]) >= tonumber(ARGV[1]) then return 0 end

            if redis.call("SISMEMBER", KEYS[2], ARGV[2]) == 1 then return -1 end

            redis.call("SADD", KEYS[2], ARGV[2])
            redis.call("INCR", KEYS[1])
            return 1
    """);
        script.setResultType(Long.class);
        return script;
    }

    @Bean
    public DefaultRedisScript<Long> cancelJoinScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText("""
        if redis.call("SREM", KEYS[1], ARGV[1]) == 1 then
          return redis.call("DECR", KEYS[2])
        else
          return -1
        end
        """);
        script.setResultType(Long.class);
        return script;
    }
}