package com.jj.redline.batch.config;

import com.jj.redline.batch.listener.JobCompletionNotificationListener;
import com.jj.redline.batch.output.DbSnapshotWriter;
import com.jj.redline.batch.processor.Cafe24DetailProcessor;
import com.jj.redline.batch.reader.Cafe24ProductReader;
import com.jj.redline.crawling.cafe24.Cafe24HttpClient;
import com.jj.redline.crawling.cafe24.Cafe24JsonLdParser;
import com.jj.redline.crawling.cafe24.Cafe24ListParser;
import com.jj.redline.crawling.config.ModeManSiteConfig;
import com.jj.redline.domain.dto.crawl.ProductBrief;
import com.jj.redline.domain.dto.crawl.ProductSnapshot;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
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

    private final ModeManSiteConfig modeManSiteConfig;
    private final Cafe24HttpClient cafe24HttpClient;
    private final Cafe24JsonLdParser cafe24JsonLdParser;
    private final JobCompletionNotificationListener jobCompletionNotificationListener;
    private final DbSnapshotWriter dbSnapshotWriter;

    @Bean
    public Job modeManCrawlingJob() {
        return new JobBuilder("modeManCrawlingJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobCompletionNotificationListener)
                .start(modeManCrawlingStep())
                .build();
    }

    @Bean
    public Step modeManCrawlingStep() {
        return new StepBuilder("modeManCrawlingStep", jobRepository)
                .<ProductBrief, ProductSnapshot>chunk(10, transactionManager)
                .reader(modeManReader())
                .processor(modeManProcessor())
                .writer(dbSnapshotWriter)
                .faultTolerant()
                .retryLimit(3)
                .retry(IOException.class)
                .skipLimit(Integer.MAX_VALUE)
                .skip(Exception.class)
                .build();
    }

    @Bean
    @StepScope
    public Cafe24ProductReader modeManReader() {
        return new Cafe24ProductReader(
                modeManSiteConfig,
                cafe24HttpClient,
                new Cafe24ListParser(modeManSiteConfig)
        );
    }

    @Bean
    @StepScope
    public Cafe24DetailProcessor modeManProcessor() {
        return new Cafe24DetailProcessor(cafe24HttpClient, cafe24JsonLdParser, modeManSiteConfig);
    }
}
