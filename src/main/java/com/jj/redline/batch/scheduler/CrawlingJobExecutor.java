package com.jj.redline.batch.scheduler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrawlingJobExecutor {

    private final JobLauncher jobLauncher;

    public void execute(ScheduledCrawlingJob scheduledJob, String trigger) {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addString("runDateTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .addString("trigger", trigger)
                    .addString("site", scheduledJob.siteName())
                    .toJobParameters();

            log.info("Trigger: [{}]. {} 크롤링 잡을 실행합니다.", trigger, scheduledJob.siteName());
            jobLauncher.run(scheduledJob.job(), params);
        } catch (Exception e) {
            log.error("{} 크롤링 잡 실행 실패 (Trigger: {})", scheduledJob.siteName(), trigger, e);
        }
    }
}
