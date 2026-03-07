package com.jj.redline.batch.config;

import com.jj.redline.batch.output.NdjsonSnapshotWriter;
import com.jj.redline.batch.processor.ModeManDetailCrawlingProcessor;
import com.jj.redline.batch.reader.ProductBriefReader;
import com.jj.redline.batch.tasklet.ModeManListCrawlingTasklet;
import com.jj.redline.domain.dto.ProductBrief;
import com.jj.redline.domain.dto.ProductSnapshot;
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

    // 기존 의존성은 유지됩니다.
    private final ModeManListCrawlingTasklet modeManListCrawlingTasklet;
    
    // 새로 추가될 의존성들입니다.
    private final ModeManDetailCrawlingProcessor modeManDetailCrawlingProcessor;
    private final NdjsonSnapshotWriter ndjsonSnapshotWriter;

    @Bean
    public Job modeManCrawlingJob() {
        return new JobBuilder("modeManCrawlingJob", jobRepository)
                .incrementer(new RunIdIncrementer()) // Job 재실행을 위한 ID 자동 증가
                .start(modeManListCrawlingStep())
                .next(modeManDetailProcessingStep()) // 다음 스텝을 연결합니다.
                .build();
    }

    @Bean
    public Step modeManListCrawlingStep() {
        return new StepBuilder("modeManListCrawlingStep", jobRepository)
                .tasklet(modeManListCrawlingTasklet, transactionManager)
                .build();
    }

    @Bean
    public Step modeManDetailProcessingStep() {
        return new StepBuilder("modeManDetailProcessingStep", jobRepository)
                .<ProductBrief, ProductSnapshot>chunk(10, transactionManager) // 10개씩 처리
                .reader(productBriefReader())
                .processor(modeManDetailCrawlingProcessor)
                .writer(ndjsonSnapshotWriter)
                .build();
    }

    @Bean
    public ProductBriefReader productBriefReader() {
        // 이 Reader는 이전 Step(modeManListCrawlingStep)의 실행 컨텍스트에서
        // ProductBrief 리스트를 가져와야 합니다.
        return new ProductBriefReader();
    }
}
