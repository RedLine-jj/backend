package com.jj.redline.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@Profile("scheduler") // "scheduler" 프로필이 활성화될 때만 이 클래스가 동작하도록 제한
@RequiredArgsConstructor
public class BatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job modeManCrawlingJob;
    private final Job nestStoreCrawlingJob;
    private final Job semiBasementCrawlingJob;

    /**
     * 빠른 스케줄러: 20분 주기로 실행
     */
    @Scheduled(cron = "0 */20 * * * ?") // 20초 -> 20분으로 변경
    public void runFastScheduledJob() {
        if (hasActiveSubscriptions()) {
            log.info("[FAST-SCHEDULER] 구독자가 있어 20분 주기로 크롤링을 실행합니다.");
            runCrawlingJobs("FAST_SCHEDULE");
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
            runCrawlingJobs("HOURLY_DEFAULT");
        } else {
            log.info("[SLOW-SCHEDULER] 구독자가 있으므로 빠른 스케줄러가 처리합니다. 실행을 건너뜁니다.");
        }
    }

    /**
     * 통합된 크롤링 잡을 한번만 실행하는 메소드
     */
    private void runCrawlingJobs(String trigger) {
        runModeManCrawlingJob(trigger);
        runNestStoreCrawlingJob(trigger);
        runSemiBasementCrawlingJob(trigger);
    }

    private void runModeManCrawlingJob(String trigger) {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addString("runDateTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .addString("trigger", trigger)
                    .addString("site", "MODEMAN")
                    .toJobParameters();

            log.info("Trigger: [{}]. MODEMAN 크롤링 잡을 실행합니다.", trigger);
            jobLauncher.run(modeManCrawlingJob, params);

        } catch (Exception e) {
            log.error("MODEMAN 크롤링 잡 실행 실패 (Trigger: {})", trigger, e);
        }
    }

    private void runSemiBasementCrawlingJob(String trigger) {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addString("runDateTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .addString("trigger", trigger)
                    .addString("site", "SEMI_BASEMENT")
                    .toJobParameters();

            log.info("Trigger: [{}]. SEMI_BASEMENT 크롤링 잡을 실행합니다.", trigger);
            jobLauncher.run(semiBasementCrawlingJob, params);

        } catch (Exception e) {
            log.error("SEMI_BASEMENT 크롤링 잡 실행 실패 (Trigger: {})", trigger, e);
        }
    }

    private void runNestStoreCrawlingJob(String trigger) {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addString("runDateTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .addString("trigger", trigger)
                    .addString("site", "NEST_STORE")
                    .toJobParameters();

            log.info("Trigger: [{}]. NEST_STORE 크롤링 잡을 실행합니다.", trigger);
            jobLauncher.run(nestStoreCrawlingJob, params);

        } catch (Exception e) {
            log.error("NEST_STORE 크롤링 잡 실행 실패 (Trigger: {})", trigger, e);
        }
    }

    private boolean hasActiveSubscriptions() {
        // TODO: 실제 구독자 확인 로직 구현 필요
        return true;
    }
}
