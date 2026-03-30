package com.jj.redline.batch.config;

import com.jj.redline.batch.listener.JobCompletionNotificationListener;
import com.jj.redline.batch.output.DbSnapshotWriter;
import com.jj.redline.batch.processor.SemiBasementDetailProcessor;
import com.jj.redline.batch.reader.SemiBasementProductReader;
import com.jj.redline.batch.scheduler.ScheduledCrawlingJob;
import com.jj.redline.domain.dto.CrawlSite;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SemiBasementBatchJobConfig {

    private final BatchJobFactory batchJobFactory;
    private final SemiBasementProductReader semiBasementProductReader;
    private final SemiBasementDetailProcessor semiBasementDetailProcessor;
    private final JobCompletionNotificationListener jobCompletionNotificationListener;
    private final DbSnapshotWriter dbSnapshotWriter;

    @Bean
    public Job semiBasementCrawlingJob() {
        return batchJobFactory.createJob(
                "semiBasementCrawlingJob",
                semiBasementCrawlingStep(),
                jobCompletionNotificationListener
        );
    }

    @Bean
    public Step semiBasementCrawlingStep() {
        return batchJobFactory.createStep(
                "semiBasementCrawlingStep",
                semiBasementProductReader,
                semiBasementDetailProcessor,
                dbSnapshotWriter
        );
    }

    @Bean
    public ScheduledCrawlingJob semiBasementScheduledCrawlingJob(@Qualifier("semiBasementCrawlingJob") Job job) {
        return new ScheduledCrawlingJob(CrawlSite.SEMI_BASEMENT.name(), job);
    }
}
