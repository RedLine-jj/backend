package com.jj.redline.batch.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jj.redline.batch.listener.JobCompletionNotificationListener;
import com.jj.redline.batch.output.NdjsonSnapshotWriter;
import com.jj.redline.batch.processor.ModeManDetailCrawlingProcessor;
import com.jj.redline.batch.reader.MultiCategoryProductReader;
import com.jj.redline.common.config.ModeManCrawlProperties;
import com.jj.redline.domain.dto.ProductBrief;
import com.jj.redline.domain.dto.ProductSnapshot;
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
import org.springframework.beans.factory.annotation.Value;
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

    // StepScope Bean을 생성하기 위해 필요한 의존성들
    private final ObjectMapper objectMapper;
    private final ModeManCrawlProperties crawlProperties;

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
                .writer(ndjsonSnapshotWriter(null)) // Spring이 @Value 값을 주입해줄 것이므로 null 전달
                .faultTolerant() // 아래 실패 처리 로직을 활성화합니다.
                .retryLimit(3)   // 최대 3번까지 재시도합니다.
                .retry(IOException.class) // 네트워크 오류 발생 시 재시도합니다.
                .skipLimit(Integer.MAX_VALUE) // 스킵 횟수에는 제한을 두지 않습니다.
                .skip(Exception.class) // 재시도 실패를 포함한 모든 예외를 스킵 처리합니다.
                .build();
    }

    @Bean
    @StepScope
    public NdjsonSnapshotWriter ndjsonSnapshotWriter(
            @Value("#{jobExecutionContext['snapshotFileName']}") String snapshotFileName) {
        return new NdjsonSnapshotWriter(objectMapper, crawlProperties, snapshotFileName);
    }
}