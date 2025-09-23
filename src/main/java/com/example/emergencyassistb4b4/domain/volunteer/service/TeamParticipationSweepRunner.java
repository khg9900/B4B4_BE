package com.example.emergencyassistb4b4.domain.volunteer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TeamParticipationSweepRunner {

    private final TeamParticipationSweepService sweep;

    @EventListener(org.springframework.boot.context.event.ApplicationReadyEvent.class)
    public void onReady() { sweep.sweepOnce(2000, 1000); }

    @Scheduled(fixedDelay = 300_000)
    public void periodic() { sweep.sweepOnce(5000, 1000); }
}

