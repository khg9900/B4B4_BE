package com.example.emergencyassistb4b4.global.config.schedulerConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class SchedulerConfig {

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {

        ThreadPoolTaskScheduler t = new ThreadPoolTaskScheduler();
        t.setPoolSize(2);
        t.setThreadNamePrefix("cleanup-");

        return t;
    }
}

