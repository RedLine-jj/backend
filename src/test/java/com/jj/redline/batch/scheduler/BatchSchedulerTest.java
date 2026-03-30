package com.jj.redline.batch.scheduler;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jj.redline.domain.repository.SubscriptionRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BatchSchedulerTest {

    @Mock
    private CrawlingJobExecutor crawlingJobExecutor;
    @Mock
    private SubscriptionRepository subscriptionRepository;

    private BatchScheduler batchScheduler;
    private List<ScheduledCrawlingJob> jobs;

    @BeforeEach
    void setUp() {
        jobs = List.of(
                new ScheduledCrawlingJob("MODEMAN", null),
                new ScheduledCrawlingJob("SEMI_BASEMENT", null)
        );
        batchScheduler = new BatchScheduler(crawlingJobExecutor, jobs, subscriptionRepository);
    }

    @Test
    void 구독자가있으면빠른스케줄러가모든잡을실행한다() {
        when(subscriptionRepository.count()).thenReturn(2L);

        batchScheduler.runFastScheduledJob();

        verify(crawlingJobExecutor, times(2)).execute(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq("FAST_SCHEDULE"));
    }

    @Test
    void 구독자가있으면느린스케줄러는실행하지않는다() {
        when(subscriptionRepository.count()).thenReturn(1L);

        batchScheduler.runSlowScheduledJob();

        verify(crawlingJobExecutor, never()).execute(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void 구독자가없으면느린스케줄러가모든잡을실행한다() {
        when(subscriptionRepository.count()).thenReturn(0L);

        batchScheduler.runSlowScheduledJob();

        verify(crawlingJobExecutor, times(2)).execute(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq("HOURLY_DEFAULT"));
    }
}
