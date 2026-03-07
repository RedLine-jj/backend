package com.jj.redline.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job modeManCrawlingJob;

    /**
     * 빠른 스케줄러: 2분 주기로 실행 (테스트용)
     */
    @Scheduled(cron = "*/20 * * * * ?")
    public void runFastScheduledJob() {
        if (hasActiveSubscriptions()) {
            log.info("[FAST-SCHEDULER] 구독자가 있어 2분 주기로 크롤링을 실행합니다.");
            runCrawlingJob("FAST_SCHEDULE");
        } else {
            log.info("[FAST-SCHEDULER] 구독자가 없어 실행을 건너뜁니다.");
        }
    }

    /**
     * 느린 스케줄러: 1시간 주기로 실행
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void runSlowScheduledJob() {
        if (!hasActiveSubscriptions()) {
            log.info("[SLOW-SCHEDULER] 구독자가 없어 1시간 주기로 크롤링을 실행합니다.");
            runCrawlingJob("HOURLY_DEFAULT");
        } else {
            log.info("[SLOW-SCHEDULER] 구독자가 있으므로 빠른 스케줄러가 처리합니다. 실행을 건너뜁니다.");
        }
    }

    /**
     * 통합된 크롤링 잡을 한번만 실행하는 메소드
     */
    private void runCrawlingJob(String trigger) {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addString("runDateTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .addString("trigger", trigger)
                    .toJobParameters();

            log.info("Trigger: [{}]. 통합 크롤링 잡을 실행합니다.", trigger);
            jobLauncher.run(modeManCrawlingJob, params);

        } catch (Exception e) {
            log.error("스케줄된 크롤링 잡 실행 실패 (Trigger: {})", trigger, e);
        }
    }

    private boolean hasActiveSubscriptions() {
        // TODO: 실제 구독자 확인 로직 구현 필요
        return true;
    }
}
