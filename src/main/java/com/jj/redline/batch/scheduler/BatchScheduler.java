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
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchScheduler {

    // 크롤링할 카테고리 정보를 담는 내부 클래스
    private record CategoryToCrawl(String code, String name) {}

    private final JobLauncher jobLauncher;
    private final Job modeManCrawlingJob;

    // 크롤링할 카테고리 목록을 정의합니다.
    private final List<CategoryToCrawl> categoriesToCrawl = List.of(
            new CategoryToCrawl("263", "Denim Jackets"),
            new CategoryToCrawl("858", "Denim Pants")
    );

    /**
     * 빠른 스케줄러: 30분 주기로 실행
     */
    @Scheduled(cron = "0 0/30 * * * ?")
    public void runFastScheduledJob() {
        if (hasActiveSubscriptions()) {
            log.info("[FAST-SCHEDULER] 구독자가 있어 30분 주기로 크롤링을 실행합니다.");
            runAllCategoryJobs("FAST_SCHEDULE");
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
            runAllCategoryJobs("HOURLY_DEFAULT");
        } else {
            log.info("[SLOW-SCHEDULER] 구독자가 있으므로 빠른 스케줄러가 처리합니다. 실행을 건너뜁니다.");
        }
    }

    /**
     * 정의된 모든 카테고리에 대해 잡을 실행하는 메소드
     */
    private void runAllCategoryJobs(String trigger) {
        log.info("Trigger: [{}]. 모든 카테고리에 대한 크롤링을 시작합니다.", trigger);

        for (CategoryToCrawl category : categoriesToCrawl) {
            try {
                // JobParameter는 매번 달라야 하므로, 카테고리명과 시간을 조합하여 고유성을 보장합니다.
                JobParameters params = new JobParametersBuilder()
                        .addString("runDateTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                        .addString("trigger", trigger)
                        .addString("categoryCode", category.code())
                        .addString("categoryName", category.name())
                        .toJobParameters();

                log.info("카테고리 \"{}\"에 대한 잡을 실행합니다.", category.name());
                jobLauncher.run(modeManCrawlingJob, params);

            } catch (Exception e) {
                log.error("카테고리 \"{}\" 크롤링 잡 실행 실패", category.name(), e);
            }
        }
    }

    /**
     * TODO: 임시 구독 확인 메소드
     */
    private boolean hasActiveSubscriptions() {
        // 현재는 테스트를 위해 항상 '구독자가 있는' 상태(true)를 반환합니다.
        return true;
    }
}