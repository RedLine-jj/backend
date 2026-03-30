package com.jj.redline.batch.config;

import com.jj.redline.batch.listener.JobCompletionNotificationListener;
import com.jj.redline.batch.output.DbSnapshotWriter;
import com.jj.redline.batch.processor.Cafe24DetailProcessor;
import com.jj.redline.batch.reader.Cafe24ProductReader;
import com.jj.redline.crawling.cafe24.Cafe24HttpClient;
import com.jj.redline.crawling.cafe24.Cafe24JsonLdParser;
import com.jj.redline.crawling.cafe24.Cafe24ListParser;
import com.jj.redline.crawling.config.NestStoreSiteConfig;
import com.jj.redline.domain.dto.crawl.ProductBrief;
import com.jj.redline.domain.dto.crawl.ProductSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
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
public class NestStoreBatchJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final NestStoreSiteConfig nestStoreSiteConfig;
    private final Cafe24HttpClient cafe24HttpClient;
    private final Cafe24JsonLdParser cafe24JsonLdParser;
    private final JobCompletionNotificationListener jobCompletionNotificationListener;
    private final DbSnapshotWriter dbSnapshotWriter;

    @Bean
    public Job nestStoreCrawlingJob() {
        return new JobBuilder("nestStoreCrawlingJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobCompletionNotificationListener)
                .start(nestStoreCrawlingStep())
                .build();
    }

    @Bean
    public Step nestStoreCrawlingStep() {
        return new StepBuilder("nestStoreCrawlingStep", jobRepository)
                .<ProductBrief, ProductSnapshot>chunk(10, transactionManager)
                .reader(nestStoreReader())
                .processor(nestStoreProcessor())
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
    public Cafe24ProductReader nestStoreReader() {
        return new Cafe24ProductReader(
                nestStoreSiteConfig,
                cafe24HttpClient,
                new Cafe24ListParser(nestStoreSiteConfig)
        );
    }

    @Bean
    @StepScope
    public Cafe24DetailProcessor nestStoreProcessor() {
        return new Cafe24DetailProcessor(cafe24HttpClient, cafe24JsonLdParser, nestStoreSiteConfig);
    }
}
