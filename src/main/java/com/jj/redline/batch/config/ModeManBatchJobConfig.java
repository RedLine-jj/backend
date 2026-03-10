package com.jj.redline.batch.config;

import com.jj.redline.batch.listener.JobCompletionNotificationListener;
import com.jj.redline.batch.output.DbSnapshotWriter;
import com.jj.redline.batch.processor.ModeManDetailCrawlingProcessor;
import com.jj.redline.batch.reader.MultiCategoryProductReader;
import com.jj.redline.domain.dto.ProductBrief;
import com.jj.redline.domain.dto.ProductSnapshot;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class ModeManBatchJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    // Step에서 사용할 의존성들
    private final MultiCategoryProductReader multiCategoryProductReader;
    private final ModeManDetailCrawlingProcessor modeManDetailCrawlingProcessor;
    private final JobCompletionNotificationListener jobCompletionNotificationListener;
    private final DbSnapshotWriter dbSnapshotWriter;

    @Bean
    public Job modeManCrawlingJob() {
        return new JobBuilder("modeManCrawlingJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobCompletionNotificationListener)
                .start(crawlingStep())
                .build();
    }

    @Bean
    public Step crawlingStep() {
        return new StepBuilder("crawlingStep", jobRepository)
                .<ProductBrief, ProductSnapshot>chunk(10, transactionManager)
                .reader(multiCategoryProductReader)
                .processor(modeManDetailCrawlingProcessor)
                .writer(dbSnapshotWriter)
                .faultTolerant()
                .retryLimit(3)
                .retry(IOException.class)
                .skipLimit(Integer.MAX_VALUE)
                .skip(Exception.class)
                .build();
    }
}