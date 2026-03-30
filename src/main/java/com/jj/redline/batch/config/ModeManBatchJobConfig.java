package com.jj.redline.batch.config;

import com.jj.redline.batch.listener.JobCompletionNotificationListener;
import com.jj.redline.batch.output.DbSnapshotWriter;
import com.jj.redline.batch.processor.ModeManDetailCrawlingProcessor;
import com.jj.redline.batch.reader.MultiCategoryProductReader;
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
public class ModeManBatchJobConfig {

    private final BatchJobFactory batchJobFactory;
    private final MultiCategoryProductReader multiCategoryProductReader;
    private final ModeManDetailCrawlingProcessor modeManDetailCrawlingProcessor;
    private final JobCompletionNotificationListener jobCompletionNotificationListener;
    private final DbSnapshotWriter dbSnapshotWriter;

    @Bean
    public Job modeManCrawlingJob() {
        return batchJobFactory.createJob("modeManCrawlingJob", crawlingStep(), jobCompletionNotificationListener);
    }

    @Bean
    public Step crawlingStep() {
        return batchJobFactory.createStep(
                "modeManCrawlingStep",
                multiCategoryProductReader,
                modeManDetailCrawlingProcessor,
                dbSnapshotWriter
        );
    }

    @Bean
    public ScheduledCrawlingJob modeManScheduledCrawlingJob(@Qualifier("modeManCrawlingJob") Job job) {
        return new ScheduledCrawlingJob(CrawlSite.MODEMAN.name(), job);
    }
}
