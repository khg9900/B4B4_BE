package com.example.emergencyassistb4b4.global.config.schedulerConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
@EnableScheduling
public class SchedulerConfig implements SchedulingConfigurer {

    @Bean(name = "customTaskScheduler")
    @Primary
    public ThreadPoolTaskScheduler customTaskScheduler() {

        ThreadPoolTaskScheduler s = new ThreadPoolTaskScheduler();
        s.setPoolSize(4);
        s.setThreadNamePrefix("sched-");
        s.setWaitForTasksToCompleteOnShutdown(true);

        return s;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar reg) {

        reg.setTaskScheduler(customTaskScheduler());
    }

    @Bean
    public ScheduledExecutorService scheduledExecutorService() {
        return Executors.newSingleThreadScheduledExecutor();
    }
}


