package com.example.emergencyassistb4b4.domain.volunteer.infra.redis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class TeamParticipationCleanupScheduler {

    private final TaskScheduler scheduler;
    private final TTLRedisService ttlRedisService;

    public void scheduleCleanup(Long postId, Long teamId, LocalDateTime checkinEnd) {

        LocalDateTime runAt = checkinEnd.plusMinutes(30);
        scheduler.schedule(
                () -> ttlRedisService.deleteWholeTeamKeys(postId, teamId),
                Date.from(runAt.atZone(ZoneId.systemDefault()).toInstant())
        );
    }
}
