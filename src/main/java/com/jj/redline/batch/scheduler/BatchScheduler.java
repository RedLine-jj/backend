package com.jj.redline.batch.scheduler;

import com.jj.redline.domain.repository.SubscriptionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("scheduler")
@RequiredArgsConstructor
public class BatchScheduler {

    private final CrawlingJobExecutor crawlingJobExecutor;
    private final List<ScheduledCrawlingJob> scheduledCrawlingJobs;
    private final SubscriptionRepository subscriptionRepository;

    @Scheduled(cron = "0 */20 * * * ?")
    public void runFastScheduledJob() {
        if (hasActiveSubscriptions()) {
            log.info("[FAST-SCHEDULER] 구독자가 있어 20분 주기로 크롤링을 실행합니다.");
            runCrawlingJobs("FAST_SCHEDULE");
        } else {
            log.info("[FAST-SCHEDULER] 구독자가 없어 실행을 건너뜁니다.");
        }
    }

    @Scheduled(cron = "0 0 * * * ?")
    public void runSlowScheduledJob() {
        if (!hasActiveSubscriptions()) {
            log.info("[SLOW-SCHEDULER] 구독자가 없어 1시간 주기로 크롤링을 실행합니다.");
            runCrawlingJobs("HOURLY_DEFAULT");
        } else {
            log.info("[SLOW-SCHEDULER] 구독자가 있으므로 빠른 스케줄러가 처리합니다. 실행을 건너뜁니다.");
        }
    }

    private void runCrawlingJobs(String trigger) {
        scheduledCrawlingJobs.forEach(job -> crawlingJobExecutor.execute(job, trigger));
    }

    private boolean hasActiveSubscriptions() {
        return subscriptionRepository.count() > 0;
    }
}
