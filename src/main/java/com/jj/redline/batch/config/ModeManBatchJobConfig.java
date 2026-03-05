package com.jj.redline.batch.config;

import com.jj.redline.batch.tasklet.ModeManListCrawlingTasklet;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
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
    private final ModeManListCrawlingTasklet modeManListCrawlingTasklet;

    @Bean
    public Step modeManListCrawlingStep() {
        return new StepBuilder("modeManListCrawlingStep", jobRepository)
                .tasklet(modeManListCrawlingTasklet, transactionManager)
                .build();
    }

    @Bean
    public Job modeManCrawlingJob() {
        return new JobBuilder("modeManCrawlingJob", jobRepository)
                .start(modeManListCrawlingStep())
                .build();
    }
}