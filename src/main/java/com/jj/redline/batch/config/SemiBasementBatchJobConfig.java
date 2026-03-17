package com.jj.redline.batch.config;

import com.jj.redline.batch.listener.JobCompletionNotificationListener;
import com.jj.redline.batch.output.DbSnapshotWriter;
import com.jj.redline.batch.processor.SemiBasementDetailProcessor;
import com.jj.redline.batch.reader.SemiBasementProductReader;
import com.jj.redline.domain.dto.ProductBrief;
import com.jj.redline.domain.dto.ProductSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.IOException;

@Configuration
@RequiredArgsConstructor
public class SemiBasementBatchJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final SemiBasementProductReader semiBasementProductReader;
    private final SemiBasementDetailProcessor semiBasementDetailProcessor;
    private final JobCompletionNotificationListener jobCompletionNotificationListener;
    private final DbSnapshotWriter dbSnapshotWriter;

    @Bean
    public Job semiBasementCrawlingJob() {
        return new JobBuilder("semiBasementCrawlingJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobCompletionNotificationListener)
                .start(semiBasementCrawlingStep())
                .build();
    }

    @Bean
    public Step semiBasementCrawlingStep() {
        return new StepBuilder("semiBasementCrawlingStep", jobRepository)
                .<ProductBrief, ProductSnapshot>chunk(10, transactionManager)
                .reader(semiBasementProductReader)
                .processor(semiBasementDetailProcessor)
                .writer(dbSnapshotWriter)
                .faultTolerant()
                .retryLimit(3)
                .retry(IOException.class)
                .skipLimit(Integer.MAX_VALUE)
                .skip(Exception.class)
                .build();
    }
}
